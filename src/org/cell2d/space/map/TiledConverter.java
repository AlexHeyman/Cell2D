package org.cell2d.space.map;

import java.util.ArrayList;
import java.util.Collections;
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
import org.tiledreader.FileSystemTiledReader;
import org.tiledreader.TiledImage;
import org.tiledreader.TiledMap;
import org.tiledreader.TiledResource;
import org.tiledreader.TiledTile;
import org.tiledreader.TiledTileset;

/**
 * <p>The TiledConverter class, along with the TiledArea class, allow Cell2D
 * games to incorporate information from files created with the map editor
 * <a href="https://www.mapeditor.org/">Tiled</a>. They accomplish this by
 * interfacing with the <a href="http://www.alexheyman.org/tiledreader/">
 * TiledReader</a> library, which is a dependency of Cell2D. Instances of
 * TiledConverter have methods that convert data structures from the TiledReader
 * library into data structures native to Cell2D.</p>
 * 
 * <p>A TiledConverter has an associated instance of the TiledReader class
 * (specifically of the FileSystemTiledReader subclass), which is specified upon
 * the TiledConverter's construction. A TiledConverter can only convert
 * TiledResources that were read by its associated TiledReader; the TiledReader
 * and TiledConverter thus form a pipeline that eventually converts Tiled
 * resource file data into Cell2D data structures.</p>
 * 
 * <p>A TiledConverter stores a cache of pointers to the Sprites and Animations
 * corresponding to the TiledTilesets it has converted, as well as to the
 * TiledAreas associated with it, corresponding to the TiledMaps they have been
 * constructed from. A TiledConverter uses this cache to return the very same
 * Sprites and Animations if asked to convert the same TiledTileset multiple
 * times. This is mainly to ensure that, if multiple TiledMaps reference the
 * same TiledTileset, the TiledTileset will not be wastefully converted and
 * stored in memory multiple times. However, a TiledConverter also contains
 * methods that can be called manually to remove pointers from the cache.
 * Removing the pointers to no-longer-needed Sprites, Animations, and TiledAreas
 * is necessary to make those objects vulnerable to the Java garbage collector.
 * (Keep in mind, however, that unloading the Sprites is also necessary to fully
 * free the memory they take up.)</p>
 * 
 * <p>Tiled stores the durations of tile animation frames in milliseconds, but
 * Cell2D stores the durations of Animation frames in fracunits. A
 * TiledConverter converts milliseconds to fracunits using a consistent rate,
 * which by default is one fracunit for every 16 milliseconds. A TiledConverter
 * contains methods for getting and setting this conversion rate.</p>
 * @see TiledArea
 * @author Alex Heyman
 */
public class TiledConverter {
    
    private static class AssetData {
        
        private Object asset = null;
        private final Set<TiledResource> referToThis = new HashSet<>();
        private final Set<TiledResource> referencedByThis = new HashSet<>();
        
    }
    
    private final Map<TiledResource,AssetData> assets = new HashMap<>();
    
    private void ensureReference(TiledResource referer, TiledResource referent) {
        assets.get(referer).referencedByThis.add(referent);
        assets.get(referent).referToThis.add(referer);
    }
    
    private final FileSystemTiledReader reader;
    private final Map<TiledTile,Animatable> tilesToAnimatables = new HashMap<>();
    private long fracunitsPerMS = Frac.UNIT/16;
    
    /**
     * Constructs a new TiledConverter with the specified associated
     * TiledReader.
     * @param reader This TiledConverter's associated TiledReader
     */
    public TiledConverter(FileSystemTiledReader reader) {
        this.reader = reader;
    }
    
    /**
     * Returns this TiledConverter's associated TiledReader.
     * @return This TiledConverter's associated TiledReader
     */
    public final FileSystemTiledReader getReader() {
        return reader;
    }
    
    /**
     * Returns this TiledConverter's milliseconds-to-fracunits conversion rate,
     * in fracunits per millisecond.
     * @return this TiledConverter's milliseconds-to-fracunits conversion rate
     */
    public final long getFracunitsPerMS() {
        return fracunitsPerMS;
    }
    
    /**
     * Sets this TiledConverter's milliseconds-to-fracunits conversion rate to
     * the specified value.
     * @param fracunitsPerMS The new conversion rate, in fracunits per
     * millisecond
     */
    public final void setFracunitsPerMS(long fracunitsPerMS) {
        if (fracunitsPerMS <= 0) {
            throw new RuntimeException("Attempted to set a TiledConverter's fracunits-per-millisecond rate"
                    + " to a non-positive value (about " + Frac.toDouble(fracunitsPerMS) + " fracunits)");
        }
        this.fracunitsPerMS = fracunitsPerMS;
    }
    
    final <T extends CellGame, U extends SpaceState<T,U,?>> void addArea(TiledArea<T,U> area) {
        TiledMap map = area.getMap();
        if (assets.containsKey(map)) {
            removeAssets(map, false, false);
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
     * Converts the specified TiledTileset, if it has not already been
     * converted, and returns an Iterable of the Sprites corresponding to all of
     * the TiledTileset's TiledTiles. If the TiledTileset is a single-image
     * tileset, the Iterable will be a SpriteSheet with the same grid layout as
     * the TiledTileset. If the TiledTileset is instead an image collection
     * tileset, the Iterable will be an unmodifiable List of Sprites, given in
     * order from lowest to highest local ID of the TiledTiles to which they
     * correspond.
     * @param tileset The TiledTileset whose corresponding Sprites are to be
     * returned
     * @param load If this is true, all of the TiledTileset's Sprites (that are
     * not already loaded) will be loaded before this method returns.
     * @param filters If the TiledTileset has not yet been converted, this
     * parameter determines the Filters that should have an effect on the
     * TiledTileset's Sprites when applied to them with draw().
     * @return An Iterable of the TiledTileset's Sprites
     */
    public final Iterable<Sprite> getSprites(TiledTileset tileset, boolean load, Filter... filters) {
        if (tileset.getReader() != reader) {
            throw new RuntimeException("Attempted to use a TiledConverter to convert a tileset with a"
                    + " different TiledReader than the TiledConverter itself");
        }
        AssetData data = assets.get(tileset);
        if (data == null) {
            data = new AssetData();
            assets.put(tileset, data);
            Map<TiledTile,Sprite> tilesToSprites = new HashMap<>();
            TiledImage image = tileset.getImage();
            if (image == null) {
                //Tileset is an image collection tileset
                List<Sprite> spriteList = new ArrayList<>(tileset.getTiles().size());
                data.asset = Collections.unmodifiableList(spriteList);
                for (TiledTile tile : tileset.getTiles()) {
                    image = tile.getImage();
                    java.awt.Color transColor = image.getTransColor();
                    Sprite sprite = new Sprite(image.getSource(),
                            -tileset.getTileOffsetX(), -tileset.getTileOffsetY(),
                            (transColor == null ? null : new Color(transColor)), load, filters);
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
                        (transColor == null ? null : new Color(transColor)), load, filters);
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
                        frameDurations[i] = tile.getAnimationFrameDuration(i)*fracunitsPerMS;
                    }
                    tilesToAnimatables.put(tile, new Animation(frames, frameDurations));
                }
            }
        } else if (load) {
            if (data.asset instanceof SpriteSheet) {
                ((SpriteSheet)(data.asset)).load();
            } else {
                for (Sprite sprite : (Iterable<Sprite>)(data.asset)) {
                    sprite.load();
                }
            }
        }
        return (Iterable<Sprite>)(data.asset);
    }
    
    /**
     * Converts the TiledTileset to which the specified TiledTile belongs, if it
     * has not already been converted, and returns the Animatable representation
     * of the specified TiledTile. If the TiledTile is an animated tile, the
     * returned object will be an Animation whose frames are the Sprites
     * corresponding to the TiledTile frames of the original tile animation. If
     * the specified TiledTile is not animated, the returned object will simply
     * be the static Sprite corresponding to the TiledTile.
     * @param tile The TiledTile for which the representative Animatable is to
     * be returned
     * @param load If this is true, all of the TiledTileset's Sprites (that are
     * not already loaded) will be loaded before this method returns.
     * @param filters If the TiledTile's TiledTileset has not yet been
     * converted, this parameter determines the Filters that should have an
     * effect on the TiledTileset's Sprites when applied to them with draw().
     * @return The Animatable representation of the specified TiledTile
     */
    public final Animatable getAnimatable(TiledTile tile, boolean load, Filter... filters) {
        TiledTileset tileset = tile.getTileset();
        getSprites(tileset, load, filters);
        return tilesToAnimatables.get(tile);
    }
    
    /**
     * Removes this TiledConverter's cached pointer to the assets converted from
     * the specified resource, if it has converted that resource before.
     * @param resource The resource to forget about
     * @param cleanUp If true, also remove the cached pointers to all of the
     * assets referenced by the assets from the specified resource, and not
     * referenced by any of the other assets that this TiledConverter still
     * remembers. This parameter applies recursively, so if the removal of any
     * of these "orphaned" assets causes more assets to be orphaned, those will
     * be removed as well.
     * @param removeResources If true, also remove this TiledConverter's
     * TiledReader's cached pointer to the specified resource, if that pointer
     * exists. Note that the removal is based on the resource's source path
     * rather than its identity as an Object, and so if the resource from that
     * same path has already been removed from and re-read by the TiledReader,
     * the TiledReader <i>will</i> remove its cached pointer to the new
     * resource. The cleanUp parameter applies here as well; if it is true, the
     * TiledReader will also clean up its orphaned resources.
     * @return Whether the specified resource had been converted before this
     * method was called, and hence whether the removal occurred
     */
    public final boolean removeAssets(TiledResource resource, boolean cleanUp, boolean removeResources) {
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
                removeAssets(orphanedResource, true, removeResources);
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
            reader.removeResource(resource.getPath(), cleanUp);
        }
        return true;
    }
    
    /**
     * Removes all of this TiledConverter's cached pointers to the assets that
     * it has converted from Tiled resources.
     */
    public final void clearAssets() {
        assets.clear();
        tilesToAnimatables.clear();
    }
    
}
