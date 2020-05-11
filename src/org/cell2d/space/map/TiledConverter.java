package org.cell2d.space.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cell2d.Animatable;
import org.cell2d.Animation;
import org.cell2d.CellGame;
import org.cell2d.Color;
import org.cell2d.Filter;
import org.cell2d.Frac;
import org.cell2d.Sprite;
import org.cell2d.SpriteSheet;
import org.cell2d.space.SpaceState;
import org.tiledreader.TiledImage;
import org.tiledreader.TiledMap;
import org.tiledreader.TiledReader;
import org.tiledreader.TiledResource;
import org.tiledreader.TiledTile;
import org.tiledreader.TiledTileset;

/**
 *
 * @author Alex Heyman
 */
public final class TiledConverter {
    
    private TiledConverter() {}
    
    private static class AssetData {
        
        private Object asset = null;
        private final Set<TiledResource> referToThis = new HashSet<>();
        private final Set<TiledResource> referencedByThis = new HashSet<>();
        
    }
    
    private static final Map<TiledResource,AssetData> assets = new HashMap<>();
    
    private static void ensureReference(TiledResource referer, TiledResource referent) {
        assets.get(referer).referencedByThis.add(referent);
        assets.get(referent).referToThis.add(referer);
    }
    
    private static final Map<TiledTile,Animatable> tilesToAnimatables = new HashMap<>();
    private static long framesPerMS = 10*Frac.UNIT;
    
    public static long getFramesPerMS() {
        return framesPerMS;
    }
    
    public static void setFramesPerMS(long framesPerMS) {
        if (framesPerMS <= 0) {
            throw new RuntimeException("Attempted to set TiledConverter's frames-per-millisecond rate to a"
                    + " non-positive value (about " + Frac.toDouble(framesPerMS) + " fracunits)");
        }
        TiledConverter.framesPerMS = framesPerMS;
    }
    
    static <T extends CellGame, U extends SpaceState<T,U,?>> void addArea(TiledArea<T,U> area) {
        TiledMap map = area.getMap();
        if (assets.containsKey(map)) {
            removeAsset(map, false, false);
        }
        AssetData data = new AssetData();
        assets.put(map, data);
        data.asset = area;
        //The TiledArea constructor ensures that by now, all of the map's
        //tilesets have been converted and are present in the assets map
        for (TiledTileset tileset : map.getTilesets()) {
            ensureReference(map, tileset);
        }
    }
    
    /**
     * 
     * @param tileset
     * @param filters Note that this only determines the filters of the returned
     * Sprites if the tileset had not been converted before (improve this
     * description later)
     * @param load
     * @return 
     */
    public static Iterable<Sprite> getSprites(TiledTileset tileset, Set<Filter> filters, boolean load) {
        AssetData data = assets.get(tileset);
        if (data == null) {
            data = new AssetData();
            assets.put(tileset, data);
            Map<TiledTile,Sprite> tilesToSprites = new HashMap<>();
            TiledImage image = tileset.getImage();
            if (image == null) {
                //Tileset is an image collection tileset
                List<Sprite> spriteList = new ArrayList<>(tileset.getTiles().size());
                data.asset = spriteList;
                for (TiledTile tile : tileset.getTiles()) {
                    image = tile.getImage();
                    java.awt.Color transColor = image.getTransColor();
                    Sprite sprite = new Sprite(image.getSource(),
                            -tileset.getTileOffsetX(), -tileset.getTileOffsetY(),
                            (transColor == null ? null : new Color(transColor)), filters, load);
                    spriteList.add(sprite);
                    tilesToSprites.put(tile, sprite);
                }
            } else {
                //Tileset is a single-image tileset
                java.awt.Color transColor = image.getTransColor();
                SpriteSheet spriteSheet = new SpriteSheet(image.getSource(), tileset.getWidth(),
                        tileset.getHeight(), tileset.getTileWidth(), tileset.getTileHeight(),
                        tileset.getSpacing(), tileset.getMargin(),
                        -tileset.getTileOffsetX(), -tileset.getTileOffsetY(),
                        (transColor == null ? null : new Color(transColor)), filters, load);
                data.asset = spriteSheet;
                for (int x = 0; x < tileset.getWidth(); x++) {
                    for (int y = 0; y < tileset.getHeight(); y++) {
                        tilesToSprites.put(tileset.getTile(x, y), spriteSheet.getSprite(x, y));
                    }
                }
            }
            for (Map.Entry<TiledTile,Sprite> entry : tilesToSprites.entrySet()) {
                TiledTile tile = entry.getKey();
                int numFrames = tile.getNumAnimationFrames();
                if (numFrames == 0) {
                    tilesToAnimatables.put(tile, entry.getValue());
                } else {
                    Animatable[] frames = new Animatable[numFrames];
                    long[] frameDurations = new long[numFrames];
                    for (int i = 0; i < numFrames; i++) {
                        frames[i] = tilesToSprites.get(tile.getAnimationFrame(i));
                        frameDurations[i] = tile.getAnimationFrameDuration(i)*framesPerMS;
                    }
                    tilesToAnimatables.put(tile, new Animation(frames, frameDurations));
                }
            }
        }
        return (Iterable<Sprite>)(data.asset);
    }
    
    public static Animatable getAnimatable(TiledTile tile, Set<Filter> filters, boolean load) {
        TiledTileset tileset = tile.getTileset();
        getSprites(tileset, filters, load);
        return tilesToAnimatables.get(tile);
    }
    
    public static boolean removeAsset(TiledResource resource, boolean cleanUp, boolean removeResources) {
        AssetData data = assets.get(resource);
        if (data == null) {
            return false;
        }
        for (TiledResource refererResource : data.referToThis) {
            assets.get(refererResource).referencedByThis.remove(resource);
        }
        if (cleanUp) {
            List<TiledResource> orphanedResources = new ArrayList<>();
            for (TiledResource referencedResource : data.referencedByThis) {
                AssetData referencedData = assets.get(referencedResource);
                referencedData.referToThis.remove(resource);
                if (referencedData.referToThis.isEmpty()) {
                    orphanedResources.add(referencedResource);
                }
            }
            for (TiledResource orphanedResource : orphanedResources) {
                removeAsset(orphanedResource, true, removeResources);
            }
        } else {
            for (TiledResource referencedResource : data.referencedByThis) {
                assets.get(referencedResource).referToThis.remove(resource);
            }
        }
        assets.remove(resource);
        if (resource instanceof TiledTileset) {
            tilesToAnimatables.keySet().removeAll(((TiledTileset)resource).getTiles());
        }
        if (removeResources && resource.getPath() != null) {
            TiledReader.removeResource(resource.getPath(), cleanUp);
        }
        return true;
    }
    
    public static void clearAssets() {
        assets.clear();
        tilesToAnimatables.clear();
    }
    
}
