package org.cell2d.space.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * 
 * @param <T>
 * @param <U>
 * @author Alex Heyman
 */
public abstract class TiledArea<T extends CellGame, U extends SpaceState<T,U,?>> implements Area<T,U> {
    
    private final TiledMap map;
    private final int[] drawPriorities;
    private TiledTileLayer solidLayer;
    private final int backgroundColorLayerID;
    private List<Loadable> loadables;
    private final Map<TiledImageLayer,Sprite> imageLayerSprites = new HashMap<>();
    
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
    
    public TiledArea(TiledMap map, int drawPrioritySpacing, String zeroPriorityLayerName,
            String solidLayerName, int backgroundColorLayerID, boolean load) {
        this(map, makeDrawPriorities(map, drawPrioritySpacing, zeroPriorityLayerName),
                solidLayerName, backgroundColorLayerID, load);
    }
    
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
                java.awt.Color transColor = image.getTransColor();
                Sprite sprite = new Sprite(image.getSource(), 0, 0,
                            (transColor == null ? null : new Color(transColor)), null, load);
                imageLayerSprites.put(imageLayer, sprite);
                loadables.add(sprite);
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
    
    public Iterable<SpaceObject> loadImageLayer(T game, U state, TiledImageLayer layer, int drawPriority) {
        List<SpaceObject> objects = new ArrayList<>();
        long offsetX = Frac.units(layer.getAbsOffsetX());
        long offsetY = Frac.units(layer.getAbsOffsetY());
        double alpha = (layer.getAbsVisible() ? layer.getAbsOpacity() : 0);
        Sprite sprite = imageLayerSprites.get(layer);
        objects.add(new ImageLayerObject(offsetX, offsetY, sprite, alpha, drawPriority));
        return objects;
    }
    
    public final TiledMap getMap() {
        return map;
    }
    
    public final int getLayerDrawPriority(int index) {
        if (index < 0 || index >= drawPriorities.length) {
            throw new IndexOutOfBoundsException("Attempted to get a TiledArea's layer draw priority at"
                    + " invalid index " + index);
        }
        return drawPriorities[index];
    }
    
    /**
     * Returns the TiledTileLayer that this TiledArea treats as solid, or null
     * if there is none.
     * @return The TiledTileLayer that this TiledArea treats as solid
     */
    public final TiledTileLayer getSolidLayer() {
        return solidLayer;
    }
    
    public final int getBackgroundColorLayerID() {
        return backgroundColorLayerID;
    }
    
    public final List<Loadable> getLoadables() {
        return loadables;
    }
    
    public final void loadLoadables() {
        for (Loadable loadable : loadables) {
            loadable.load();
        }
    }
    
    public final void unloadLoadables() {
        for (Loadable loadable : loadables) {
            loadable.unload();
        }
    }
    
    public final Sprite getSprite(TiledImageLayer layer) {
        return imageLayerSprites.get(layer);
    }
    
}
