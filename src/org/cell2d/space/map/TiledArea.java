package org.cell2d.space.map;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import org.cell2d.CellGame;
import org.cell2d.Color;
import org.cell2d.Drawable;
import org.cell2d.Frac;
import org.cell2d.Loadable;
import org.cell2d.Sprite;
import org.cell2d.SpriteSheet;
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
 * interfacing with the <a href="https://github.com/AlexHeyman/TiledReader">
 * TiledReader</a> library, which is a dependency of Cell2D. An instance of the
 * TiledArea class is a type of Area that represents the contents of a TiledMap.
 * </p>
 * 
 * <p>Constructing a TiledArea from a TiledMap constitutes "converting" the
 * TiledMap in the view of the TiledConverter class, and that class thus stores
 * a pointer to each new TiledArea that is constructed. A newly constructed
 * TiledArea will replace the TiledConverter class' pointer to any existing
 * TiledArea constructed from the same TiledMap.</p>
 * 
 * <p>When a TiledArea is constructed from a TiledMap, the TiledArea will cause
 * all of the TiledTilesets used in the TiledMap to be converted if they have
 * not been already. The TiledArea will also construct Sprites that display the
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
 * the Sprites corresponding to TiledImageLayers and the Sprites and
 * SpriteSheets corresponding to TiledTilesets. These Loadables can be manually
 * loaded and unloaded in bulk, and any unloaded ones will be automatically
 * loaded by the TiledArea's load() method.</p>
 * @param <T> The type of CellGame that uses the SpaceStates that can load this
 * TiledArea
 * @param <U> The type of SpaceState that can load this TiledArea
 * @see TiledConverter
 * @author Alex Heyman
 */
public abstract class TiledArea<T extends CellGame, U extends SpaceState<T,U,?>> implements Area<T,U> {
    
    private final TiledMap map;
    private final int[] drawPriorities;
    private TiledTileLayer solidLayer;
    private final int backgroundColorLayerID;
    private List<Loadable> loadables;
    private final Map<Pair<File,Color>,Sprite> imageLayerSprites = new HashMap<>();
    
    private static File getCanonicalFile(String path) {
        try {
            return new File(path).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     * <p>The name (accessed via getName()) of the
     * TiledTileLayer whose tiles will be overlaid with solid SpaceObjects by
     * the default implementation of loadTileLayer(). This layer can be a
     * visible layer that depicts a part of the game world, or it can be an
     * invisible "solidity layer" whose only purpose is to specify collision
     * information. loadTileLayer() will use the output of
     * TileGridObject.cover() to generate a small set of solid, invisible,
     * rectangular SpaceObjects that overlap all and only the grid locations in
     * the TiledTileLayer that are occupied by tiles.</p>
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
    public TiledArea(TiledMap map, int drawPrioritySpacing, String zeroPriorityLayerName,
            String solidLayerName, int backgroundColorLayerID, boolean load) {
        this(map, makeDrawPriorities(map, drawPrioritySpacing, zeroPriorityLayerName),
                solidLayerName, backgroundColorLayerID, load);
    }
    
    /**
     * Constructs a TiledArea that represents the contents of the specified
     * TiledMap.
     * @param map The TiledMap to construct the TiledArea from
     * @param drawPriorities An array whose length is the same as the number of
     * non-group layers in the TiledMap. The array element at each index
     * <i>i</i> will be the draw priority of the non-group layer at index <i>i
     * </i> in the TiledMap's list of non-group layers (accessed via
     * getNonGroupLayers()).
     * @param solidLayerName
     * <p>The name (accessed via getName()) of the
     * TiledTileLayer whose tiles will be overlaid with solid SpaceObjects by
     * the default implementation of loadTileLayer(). This layer can be a
     * visible layer that depicts a part of the game world, or it can be an
     * invisible "solidity layer" whose only purpose is to specify collision
     * information. loadTileLayer() will use the output of
     * TileGridObject.cover() to generate a small set of solid, invisible,
     * rectangular SpaceObjects that overlap all and only the grid locations in
     * the TiledTileLayer that are occupied by tiles.</p>
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
    public TiledArea(TiledMap map, int[] drawPriorities, String solidLayerName,
            int backgroundColorLayerID, boolean load) {
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
        this.drawPriorities = drawPriorities;
        this.backgroundColorLayerID = backgroundColorLayerID;
        loadables = new ArrayList<>();
        for (TiledTileset tileset : map.getTilesets()) {
            Iterable<Sprite> sprites = TiledConverter.getSprites(tileset, null, load);
            if (sprites instanceof SpriteSheet) {
                loadables.add((SpriteSheet)sprites);
            } else {
                for (Sprite sprite : sprites) {
                    loadables.add(sprite);
                }
            }
        }
        solidLayer = null;
        for (int layerIndex = 0; layerIndex < layers.size(); layerIndex++) {
            TiledLayer layer = layers.get(layerIndex);
            if (layer instanceof TiledTileLayer) {
                if (solidLayer == null && layer.getName().equals(solidLayerName)) {
                    solidLayer = (TiledTileLayer)layer;
                }
            } else if (layer instanceof TiledImageLayer) {
                TiledImageLayer imageLayer = (TiledImageLayer)layer;
                TiledImage image = imageLayer.getImage();
                File file = getCanonicalFile(image.getSource());
                java.awt.Color awtTransColor = image.getTransColor();
                Color transColor = (awtTransColor == null ? null : new Color(awtTransColor));
                Pair<File,Color> pair = new Pair<>(file, transColor);
                Sprite sprite = imageLayerSprites.get(pair);
                if (sprite == null) {
                    sprite = new Sprite(image.getSource(), 0, 0, transColor, null, load);
                    imageLayerSprites.put(pair, sprite);
                    loadables.add(sprite);
                }
            }
        }
        loadables = Collections.unmodifiableList(loadables);
        TiledConverter.addArea(this);
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
                drawable = TiledConverter.getAnimatable(tile, null, false).getInstance();
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
        File file = getCanonicalFile(image.getSource());
        java.awt.Color awtTransColor = image.getTransColor();
        Color transColor = (awtTransColor == null ? null : new Color(awtTransColor));
        return imageLayerSprites.get(new Pair<>(file, transColor));
    }
    
}
