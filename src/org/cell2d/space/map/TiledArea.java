package org.cell2d.space.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.cell2d.Animatable;
import org.cell2d.AnimationInstance;
import org.cell2d.CellGame;
import org.cell2d.Color;
import org.cell2d.ColorMultiplyFilter;
import org.cell2d.Drawable;
import org.cell2d.Filter;
import org.cell2d.Frac;
import org.cell2d.Loadable;
import org.cell2d.Sprite;
import org.cell2d.space.Area;
import org.cell2d.space.ColorSpaceLayer;
import org.cell2d.space.RectangleHitbox;
import org.cell2d.space.SpaceObject;
import org.cell2d.space.SpaceState;
import org.tiledreader.TiledImage;
import org.tiledreader.TiledImageLayer;
import org.tiledreader.TiledLayer;
import org.tiledreader.TiledMap;
import org.tiledreader.TiledObjectLayer;
import org.tiledreader.TiledTile;
import org.tiledreader.TiledTileLayer;
import org.tiledreader.TiledTileset;

/**
 * <p>The TiledArea class, along with the TiledConverter class, allow Cell2D
 * games to incorporate information from files created with the map editor
 * <a href="https://www.mapeditor.org/">Tiled</a>. They accomplish this by
 * interfacing with the <a href="http://www.alexheyman.org/tiledreader/">
 * TiledReader</a> library, which is a dependency of Cell2D. An instance of the
 * TiledArea class is a type of Area that represents the contents of a TiledMap.
 * </p>
 * 
 * <p>A TiledArea has an associated TiledConverter, specified upon its
 * construction. This TiledConverter's associated TiledReader must be the same
 * one that read the TiledArea's TiledMap. Constructing a TiledArea constitutes
 * "converting" its TiledMap in the view of its TiledConverter, and the
 * TiledConverter thus stores a pointer to the newly constructed TiledArea. The
 * new TiledArea will replace the TiledConverter's pointer to any previous
 * TiledArea constructed from the same TiledMap.</p>
 * 
 * <p>When a TiledArea is constructed, it will cause all of the TiledTilesets
 * used in its TiledMap to be converted by its TiledCoverter if they have not
 * been already. The TiledArea will also construct Sprites that display the
 * images of the TiledMap's TiledImageLayers. (TiledImageLayers with identical
 * images will share a single Sprite.) Any new Sprites converted or constructed
 * upon a TiledArea's creation will not be affected by any Filters applied to
 * them with draw().</p>
 * 
 * <p>The default implementation of a TiledArea's load() method translates its
 * TiledMap's non-group layers one by one into sets of SpaceObjects. As part of
 * this process, it calls three other methods: loadTileLayer(),
 * loadObjectLayer(), and loadImageLayer(), for loading each TiledTileLayer,
 * TiledObjectLayer, and TiledImageLayer, respectively. loadTileLayer() and
 * loadImageLayer() have default implementations, but can be overridden. <b>
 * Currently, these default implementations assume that the TiledMap's
 * orientation is orthogonal and ignore its within-tile-layer rendering order.
 * </b> loadObjectLayer() has no default implementation, because no such
 * implementation could account for the diversity of custom SpaceObject
 * subclasses that Cell2D games may use. Each non-group layer is translated with
 * a particular draw priority, which is intended to be (and by default is) the
 * draw priority of the SpaceObjects generated from it.</p>
 * 
 * <p>A TiledArea stores a list of Loadables used by its content, including
 * the Sprites corresponding to TiledTiles and TiledImageLayers. These Loadables
 * can be manually loaded and unloaded in bulk, and any unloaded ones will be
 * automatically loaded by the TiledArea's load() method.</p>
 * @param <T> The type of CellGame that uses the SpaceStates that can load this
 * TiledArea
 * @param <U> The type of SpaceState that can load this TiledArea
 * @see TiledConverter
 * @author Alex Heyman
 */
public abstract class TiledArea<T extends CellGame, U extends SpaceState<T,U,?>> implements Area<T,U> {
    
    private final TiledMap map;
    private final TiledConverter converter;
    private final int[] drawPriorities;
    private TiledTileLayer solidLayer;
    private final int backgroundColorLayerID;
    private List<Loadable> loadables;
    
    private static class TileImageDef {
        
        private final TiledTile tile;
        private final Color tintColor;
        
        private TileImageDef(TiledTile tile, Color tintColor) {
            this.tile = tile;
            this.tintColor = tintColor;
        }
        
        @Override
        public final int hashCode() {
            return Objects.hash(tile, tintColor);
        }
        
        @Override
        public final boolean equals(Object o) {
            if (o instanceof TileImageDef) {
                TileImageDef def = (TileImageDef)o;
                return (tile.equals(def.tile) && tintColor.equals(def.tintColor));
            }
            return false;
        }
        
    }
    
    private final Map<TileImageDef,Animatable> tileAnimatables = new HashMap<>();
    
    private static class ImageLayerImageDef {
        
        private final String imageSource;
        private final Color transColor, tintColor;
        
        private ImageLayerImageDef(String imageSource, Color transColor, Color tintColor) {
            this.imageSource = imageSource;
            this.transColor = transColor;
            this.tintColor = tintColor;
        }
        
        @Override
        public final int hashCode() {
            return Objects.hash(imageSource, transColor, tintColor);
        }
        
        @Override
        public final boolean equals(Object o) {
            if (o instanceof ImageLayerImageDef) {
                ImageLayerImageDef def = (ImageLayerImageDef)o;
                return (imageSource.equals(def.imageSource)
                        && transColor.equals(def.transColor) && tintColor.equals(def.tintColor));
            }
            return false;
        }
        
    }
    
    private final Map<ImageLayerImageDef,Sprite> imageLayerSprites = new HashMap<>();
    
    private static float byteToFloat(int n) {
        return ((float)n)/255;
    }
    
    /**
     * Returns the transparent color of the specified TiledImage as a Cell2D
     * Color, or null if the TiledImage does not have a transparent color.
     * @param image The TiledImage
     * @return The TiledImage's transparent color
     */
    public static Color getTransColor(TiledImage image) {
        java.awt.Color transColor = image.getTransColor();
        return (transColor == null ? null : new Color(transColor));
    }
    
    /**
     * Returns a Cell2D Color that represents the tint color of the specified
     * TiledLayer. The Cell2D Color will not necessarily have the same RGBA
     * values as the TiledLayer's actual tint color. However, when used in a
     * ColorMultiplyFilter, the Cell2D Color will have the same effect as the
     * TiledLayer's tint color does in the Tiled editor's visuals. (The
     * discrepancy is because the alpha value of a ColorMultiplyFilter's color
     * controls transparency, while the alpha value of a TiledLayer's tint color
     * controls brightness. Thus, different RGBA values may be necessary to
     * achieve the same visual effect in both cases.)
     * @param layer The TiledLayer
     * @return A representation of the TiledLayer's tint color
     */
    public static Color getTintColor(TiledLayer layer) {
        java.awt.Color color = layer.getAbsTintColor();
        float r = byteToFloat(color.getRed());
        float g = byteToFloat(color.getGreen());
        float b = byteToFloat(color.getBlue());
        float a = byteToFloat(color.getAlpha());
        return new Color(r*a, g*a, b*a, 1f);
    }
    
    private static int[] makeDrawPriorities(TiledMap map, int spacing, String zeroPriorityLayerName) {
        List<TiledLayer> layers = map.getNonGroupLayers();
        int zeroPriorityLayerIndex = 0;
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).getName().equals(zeroPriorityLayerName)) {
                zeroPriorityLayerIndex = i;
                break;
            }
        }
        int[] drawPriorities = new int[layers.size()];
        for (int i = 0; i < drawPriorities.length; i++) {
            drawPriorities[i] = (i - zeroPriorityLayerIndex)*spacing;
        }
        return drawPriorities;
    }
    
    /**
     * Constructs a TiledArea that represents the contents of the specified
     * TiledMap.
     * @param map The TiledMap to construct the TiledArea from
     * @param converter The TiledArea's associated TiledConverter
     * @param drawPrioritySpacing The difference in the draw priorities of
     * successive non-group layers. For instance, if this is set to 100, then if
     * one non-group layer has draw priority 0, the next frontmost non-group
     * layer will have draw priority 100, and the next frontmost non-group layer
     * after that will have draw priority 200, etc.
     * @param zeroPriorityLayerName The name (accessed via getName()) of the
     * non-group layer with draw priority 0. This parameter may be null. If no
     * non-group layer has this parameter as a name, the first non-group layer
     * will have draw priority 0. If multiple non-group layers have this
     * parameter as a name, the hindmost one will have draw priority 0.
     * @param solidLayerName
     * <p>The name (accessed via getName()) of the TiledTileLayer whose tiles
     * will be overlaid with solid SpaceObjects by the default implementation of
     * loadTileLayer(). This layer can be a visible layer that depicts a part of
     * the game world, or it can be an invisible "solidity layer" whose only
     * purpose is to specify collision information. loadTileLayer() will use the
     * output of TileGridObject.cover() to generate a small set of solid,
     * invisible, rectangular SpaceObjects that overlap all and only the grid
     * locations in the TiledTileLayer that are occupied by tiles.</p>
     * 
     * <p>This parameter may be null. If no TiledTileLayer has this parameter as
     * a name, none of the TiledTileLayers will be made solid. If multiple
     * TiledTileLayers have this parameter as a name, only the hindmost one will
     * be made solid.</p>
     * @param backgroundColorLayerID If the TiledMap has a background color, the
     * default implementation of load() will assign a ColorSpaceLayer displaying
     * the background color to the loading SpaceState. This parameter is the
     * integer ID with which the ColorSpaceLayer is assigned to the SpaceState.
     * This parameter can be any number except 0, although if it is positive,
     * the "background" color will appear in the foreground.
     * @param load If this is true, all of this TiledArea's Loadables (that are
     * not already loaded) will be loaded upon the TiledArea's creation.
     */
    public TiledArea(TiledMap map, TiledConverter converter, int drawPrioritySpacing,
            String zeroPriorityLayerName, String solidLayerName, int backgroundColorLayerID, boolean load) {
        this(map, converter, makeDrawPriorities(map, drawPrioritySpacing, zeroPriorityLayerName),
                solidLayerName, backgroundColorLayerID, load);
    }
    
    /**
     * Constructs a TiledArea that represents the contents of the specified
     * TiledMap.
     * @param map The TiledMap to construct the TiledArea from
     * @param converter The TiledArea's associated TiledConverter
     * @param drawPriorities An array whose length is the same as the number of
     * non-group layers in the TiledMap. The array element at each index
     * <i>i</i> will be the draw priority of the non-group layer at index <i>i
     * </i> in the TiledMap's list of non-group layers (accessed via
     * getNonGroupLayers()).
     * @param solidLayerName
     * <p>The name (accessed via getName()) of the TiledTileLayer whose tiles
     * will be overlaid with solid SpaceObjects by the default implementation of
     * loadTileLayer(). This layer can be a visible layer that depicts a part of
     * the game world, or it can be an invisible "solidity layer" whose only
     * purpose is to specify collision information. loadTileLayer() will use the
     * output of TileGridObject.cover() to generate a small set of solid,
     * invisible, rectangular SpaceObjects that overlap all and only the grid
     * locations in the TiledTileLayer that are occupied by tiles.</p>
     * 
     * <p>This parameter may be null. If no TiledTileLayer has this parameter as
     * a name, none of the TiledTileLayers will be made solid. If multiple
     * TiledTileLayers have this parameter as a name, only the hindmost one will
     * be made solid.</p>
     * @param backgroundColorLayerID If the TiledMap has a background color, the
     * default implementation of load() will assign a ColorSpaceLayer displaying
     * the background color to the loading SpaceState. This parameter is the
     * integer ID with which the ColorSpaceLayer is assigned to the SpaceState.
     * This parameter can be any number except 0, although if it is positive,
     * the "background" color will appear in the foreground.
     * @param load If this is true, all of this TiledArea's Loadables (that are
     * not already loaded) will be loaded upon the TiledArea's creation.
     */
    public TiledArea(TiledMap map, TiledConverter converter, int[] drawPriorities,
            String solidLayerName, int backgroundColorLayerID, boolean load) {
        if (map.getReader() != converter.getReader()) {
            throw new RuntimeException("Attempted to construct a TiledArea using a TiledMap and a"
                    + " TiledConverter with two different TiledReaders");
        }
        List<TiledLayer> layers = map.getNonGroupLayers();
        if (drawPriorities.length != layers.size()) {
            throw new RuntimeException("Attempted to construct a TiledArea with an array of layer draw"
                    + " priorities whose length (" + drawPriorities.length
                    + ") is different from the number of non-group layers in the specified TiledMap ("
                    + layers.size() + ")");
        }
        if (backgroundColorLayerID == 0) {
            throw new RuntimeException("Attempted to construct a TiledArea with a background color layer ID"
                    + " of 0");
        }
        this.map = map;
        this.converter = converter;
        this.drawPriorities = drawPriorities;
        this.backgroundColorLayerID = backgroundColorLayerID;
        loadables = new ArrayList<>();
        solidLayer = null;
        
        //Make super-sure all of the map's tilesets get converted, as promised
        for (TiledTileset tileset : map.getTilesets()) {
            converter.getSprites(tileset, load);
        }
        
        Set<Sprite> tileSprites = new HashSet<>();
        for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
            TiledLayer layer = layers.get(layerIndex);
            if (layer instanceof TiledTileLayer) {
                TiledTileLayer tileLayer = (TiledTileLayer)layer;
                Color tintColor = getTintColor(layer);
                Filter tintFilter = new ColorMultiplyFilter(tintColor);
                for (Point point : tileLayer.getTileLocations()) {
                    TiledTile tile = tileLayer.getTile(point.x, point.y);
                    TileImageDef def = new TileImageDef(tile, tintColor);
                    Animatable animatable = tileAnimatables.get(def);
                    if (animatable == null) {
                        animatable = converter.getAnimatable(tile, load);
                        tileSprites.addAll(animatable.getSprites());
                        if (!tintColor.equals(Color.WHITE)) {
                            animatable = animatable.getFilteredCopy(tintFilter, load);
                            tileSprites.addAll(animatable.getSprites());
                        }
                        tileAnimatables.put(def, animatable);
                    }
                }
                if (solidLayer == null && layer.getName().equals(solidLayerName)) {
                    solidLayer = (TiledTileLayer)layer;
                }
            } else if (layer instanceof TiledImageLayer) {
                TiledImageLayer imageLayer = (TiledImageLayer)layer;
                TiledImage image = imageLayer.getImage();
                Color transColor = getTransColor(image);
                Color tintColor = getTintColor(layer);
                ImageLayerImageDef def = new ImageLayerImageDef(image.getSource(), transColor, tintColor);
                Sprite sprite = imageLayerSprites.get(def);
                if (sprite == null) {
                    sprite = new Sprite(image.getSource(), 0, 0, transColor, load);
                    loadables.add(sprite);
                    if (!tintColor.equals(Color.WHITE)) {
                        sprite = sprite.getFilteredCopy(new ColorMultiplyFilter(tintColor), load);
                        loadables.add(sprite);
                    }
                    imageLayerSprites.put(def, sprite);
                }
            }
        }
        loadables.addAll(tileSprites);
        loadables = Collections.unmodifiableList(loadables);
        converter.addArea(this);
    }
    
    @Override
    public Iterable<SpaceObject> load(T game, U state) {
        loadLoadables();
        List<SpaceObject> objects = new ArrayList<>();
        java.awt.Color backgroundColor = map.getBackgroundColor();
        if (backgroundColor != null) {
            state.setLayer(backgroundColorLayerID, new ColorSpaceLayer(new Color(backgroundColor)));
        }
        List<TiledLayer> layers = map.getNonGroupLayers();
        for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
            TiledLayer layer = layers.get(layerIndex);
            int drawPriority = drawPriorities[layerIndex];
            Iterable<SpaceObject> layerObjects = null;
            if (layer instanceof TiledTileLayer) {
                layerObjects = loadTileLayer(game, state, (TiledTileLayer)layer, drawPriority);
            } else if (layer instanceof TiledObjectLayer) {
                layerObjects = loadObjectLayer(game, state, (TiledObjectLayer)layer, drawPriority);
            } else if (layer instanceof TiledImageLayer) {
                layerObjects = loadImageLayer(game, state, (TiledImageLayer)layer, drawPriority);
            }
            if (layerObjects != null) {
                for (SpaceObject object : layerObjects) {
                    objects.add(object);
                }
            }
        }
        return objects;
    }
    
    private static class SolidTilesObject extends SpaceObject {
        
        private SolidTilesObject(RectangleHitbox hitbox) {
            setLocatorHitbox(hitbox);
            setSolidHitbox(hitbox);
            setSolid(true);
        }
        
    }
    
    /**
     * Generates a set of SpaceObjects to represent the specified
     * TiledTileLayer. This method is called as part of a TiledArea's default
     * implementation of load().
     * @param game The CellGame of the SpaceState that is loading this TiledArea
     * @param state The SpaceState that is loading this TiledArea
     * @param layer The TiledTileLayer to represent
     * @param drawPriority The draw priority that the generated SpaceObjects
     * should have
     * @return An Iterable of the generated SpaceObjects
     */
    public Iterable<SpaceObject> loadTileLayer(T game, U state, TiledTileLayer layer, int drawPriority) {
        List<SpaceObject> objects = new ArrayList<>();
        long offsetX = Frac.units(layer.getAbsOffsetX());
        long offsetY = Frac.units(layer.getAbsOffsetY());
        Color tintColor = getTintColor(layer);
        int layerArea = (layer.getX2() - layer.getX1() + 1)*(layer.getY2() - layer.getY1() + 1);
        TileGrid tileGrid;
        if (layer.getTileLocations().size() < ((double)layerArea)/4) {
            //Layer's tiles are sparse; use HashTileGrid
            tileGrid = new HashTileGrid(layer.getX1(), layer.getX2(),
                    layer.getY1(), layer.getY2(), map.getTileWidth(), map.getTileHeight());
        } else {
            //Layer's tiles are dense; use ArrayTileGrid
            tileGrid = new ArrayTileGrid(layer.getX1(), layer.getX2(),
                    layer.getY1(), layer.getY2(), map.getTileWidth(), map.getTileHeight());
        }
        Map<TiledTile,Drawable> tilesToDrawables = new HashMap<>();
        for (Point point : layer.getTileLocations()) {
            TiledTile tile = layer.getTile(point.x, point.y);
            Drawable drawable = tilesToDrawables.get(tile);
            if (drawable == null) {
                drawable = tileAnimatables.get(new TileImageDef(tile, tintColor)).getInstance();
                if (drawable instanceof AnimationInstance) {
                    ((AnimationInstance)drawable).setSpeed(Frac.UNIT);
                }
                tilesToDrawables.put(tile, drawable);
            }
            tileGrid.setTile(point.x, point.y, drawable);
            //Tiled order of operations: diagonal flip -> x flip/y flip
            boolean xFlip = layer.getTileHorizontalFlip(point.x, point.y);
            boolean yFlip = layer.getTileVerticalFlip(point.x, point.y);
            boolean dFlip = layer.getTileDiagonalFlip(point.x, point.y);
            //Cell2D order of operations: x flip/y flip -> rotate
            double angle = 0;
            if (dFlip) {
                if (xFlip ^ yFlip) {
                    angle += 270;
                } else {
                    angle += 90;
                }
                xFlip = !xFlip;
            }
            tileGrid.setTileXFlip(point.x, point.y, xFlip);
            tileGrid.setTileYFlip(point.x, point.y, yFlip);
            tileGrid.setTileAngle(point.x, point.y, angle);
        }
        TileGridObject gridObject = new TileGridObject(offsetX, offsetY, tileGrid, drawPriority, true);
        objects.add(gridObject);
        if (layer == solidLayer) {
            for (RectangleHitbox hitbox : gridObject.cover()) {
                objects.add(new SolidTilesObject(hitbox));
            }
        }
        return objects;
    }
    
    /**
     * Generates a set of SpaceObjects to represent the specified
     * TiledObjectLayer. This method is called as part of a TiledArea's default
     * implementation of load().
     * @param game The CellGame of the SpaceState that is loading this TiledArea
     * @param state The SpaceState that is loading this TiledArea
     * @param layer The TiledObjectLayer to represent
     * @param drawPriority The draw priority that the generated SpaceObjects
     * should have
     * @return An Iterable of the generated SpaceObjects
     */
    public abstract Iterable<SpaceObject> loadObjectLayer(
            T game, U state, TiledObjectLayer layer, int drawPriority);
    
    private static class ImageLayerObject extends SpaceObject {
        
        private ImageLayerObject(long x, long y, Sprite sprite, double alpha, int drawPriority) {
            setLocatorHitbox(new RectangleHitbox(x, y,
                    0, sprite.getWidth()*Frac.UNIT, 0, sprite.getHeight()*Frac.UNIT));
            setAppearance(sprite);
            setAlpha(alpha);
            setDrawPriority(drawPriority);
        }
        
    }
    
    /**
     * Generates a set of SpaceObjects to represent the specified
     * TiledImageLayer. This method is called as part of a TiledArea's default
     * implementation of load().
     * @param game The CellGame of the SpaceState that is loading this TiledArea
     * @param state The SpaceState that is loading this TiledArea
     * @param layer The TiledImageLayer to represent
     * @param drawPriority The draw priority that the generated SpaceObjects
     * should have
     * @return An Iterable of the generated SpaceObjects
     */
    public Iterable<SpaceObject> loadImageLayer(T game, U state, TiledImageLayer layer, int drawPriority) {
        List<SpaceObject> objects = new ArrayList<>();
        long offsetX = Frac.units(layer.getAbsOffsetX());
        long offsetY = Frac.units(layer.getAbsOffsetY());
        double alpha = (layer.getAbsVisible() ? layer.getAbsOpacity() : 0);
        objects.add(new ImageLayerObject(offsetX, offsetY, getSprite(layer), alpha, drawPriority));
        return objects;
    }
    
    /**
     * Returns the TiledMap whose contents this TiledArea represents.
     * @return This TiledArea's TiledMap
     */
    public final TiledMap getMap() {
        return map;
    }
    
    /**
     * Returns this TiledArea's associated TiledConverter.
     * @return This TiledArea's associated TiledConverter
     */
    public final TiledConverter getConverter() {
        return converter;
    }
    
    /**
     * Returns the draw priority of the non-group layer at the specified index
     * in this TiledArea's TiledMap's list of non-group layers (accessed via
     * getNonGroupLayers()).
     * @param index The index of the non-group layer whose draw priority is to
     * be returned
     * @return The draw priority of the non-group layer at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public final int getLayerDrawPriority(int index) {
        if (index < 0 || index >= drawPriorities.length) {
            throw new IndexOutOfBoundsException("Attempted to get a TiledArea's layer draw priority at"
                    + " invalid index " + index);
        }
        return drawPriorities[index];
    }
    
    /**
     * Returns the TiledTileLayer that this TiledArea makes solid via the
     * default implementation of loadTileLayer(), or null if there is no such
     * TiledTileLayer.
     * @return The TiledTileLayer that this TiledArea makes solid
     */
    public final TiledTileLayer getSolidLayer() {
        return solidLayer;
    }
    
    /**
     * Returns the integer ID with which this TiledArea's background color
     * SpaceLayer is assigned to the SpaceStates that load it. If this
     * TiledArea's TiledMap has no background color, this value is meaningless.
     * @return The ID of this TiledArea's background color SpaceLayer
     */
    public final int getBackgroundColorLayerID() {
        return backgroundColorLayerID;
    }
    
    /**
     * Returns an unmodifiable List view of this TiledArea's stored Loadables.
     * @return This TiledArea's stored Loadables
     */
    public final List<Loadable> getLoadables() {
        return loadables;
    }
    
    /**
     * Loads all of this TiledArea's stored Loadables (that are not already
     * loaded).
     */
    public final void loadLoadables() {
        for (Loadable loadable : loadables) {
            loadable.load();
        }
    }
    
    /**
     * Unloads all of this TiledArea's stored Loadables that are currently
     * loaded.
     */
    public final void unloadLoadables() {
        for (Loadable loadable : loadables) {
            loadable.unload();
        }
    }
    
    /**
     * Returns the Sprite that this TiledArea constructed to represent the
     * specified TiledImageLayer, or null if the TiledImageLayer is not part of
     * this TiledArea's TiledMap.
     * @param layer The TiledImageLayer whose corresponding Sprite is to be
     * returned
     * @return The Sprite representing the specified TiledImageLayer
     */
    public final Sprite getSprite(TiledImageLayer layer) {
        TiledImage image = layer.getImage();
        return imageLayerSprites.get(new ImageLayerImageDef(
                image.getSource(), getTransColor(image), getTintColor(layer)));
    }
    
}
