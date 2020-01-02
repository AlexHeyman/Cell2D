package org.cell2d;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.cell2d.celick.Image;
import org.cell2d.celick.SlickException;

/**
 * <p>A SpriteSheet is a rectangular grid of Sprites. Each Sprite has an
 * x-coordinate in the grid that starts at 0 for the leftmost column and
 * increases to the right, as well as a y-coordinate that starts at 0 for the
 * topmost row and increases below. Like other Loadables, SpriteSheets can be
 * manually loaded and unloaded into and out of memory. Loading may take a
 * moment, but a SpriteSheet's Sprites cannot be loaded and drawn if the
 * SpriteSheet itself is not loaded. Loading a SpriteSheet will also load all of
 * its Sprites, and loading a Sprite that is part of a SpriteSheet will also
 * load that SpriteSheet.</p>
 * @see Sprite
 * @see Filter
 * @author Alex Heyman
 */
public class SpriteSheet implements Loadable {
    
    private boolean loaded = false;
    private final SpriteSheet basedOn;
    private final Filter basedFilter;
    private final String path;
    private final Color transColor;
    private final Set<Filter> filters;
    private final HashMap<Filter,Image> images = new HashMap<>();
    private final int spriteWidth, spriteHeight, spriteSpacing, originX, originY;
    private int width = 0;
    private int height = 0;
    private final Sprite[][] sprites;
    private int numSpritesLoaded = 0;
    
    /**
     * Constructs a SpriteSheet from an image file.
     * @param path The relative path to the image file
     * @param width The width in Sprites of this SpriteSheet
     * @param height The height in Sprites of this SpriteSheet
     * @param spriteWidth The width in pixels of each Sprite
     * @param spriteHeight The height in pixels of each Sprite
     * @param spriteSpacing The horizontal and vertical spacing in pixels
     * between Sprites
     * @param originX The x-coordinate in pixels on each Sprite of that Sprite's
     * origin
     * @param originY The y-coordinate in pixels on each Sprite of that Sprite's
     * origin
     * @param filters The Set of Filters that will have an effect on this
     * SpriteSheet's Sprites when applied to them with draw(), or null if no
     * Filters should have an effect
     * @param load Whether this SpriteSheet should load upon creation
     */
    public SpriteSheet(String path, int width, int height, int spriteWidth, int spriteHeight,
            int spriteSpacing, int originX, int originY, Set<Filter> filters, boolean load) {
        this(path, width, height, spriteWidth, spriteHeight, spriteSpacing,
                originX, originY, null, (filters == null ? null : new HashSet<>(filters)), load);
    }
    
    /**
     * Constructs a SpriteSheet from an image file.
     * @param path The relative path to the image file
     * @param width The width in Sprites of this SpriteSheet
     * @param height The height in Sprites of this SpriteSheet
     * @param spriteWidth The width in pixels of each Sprite
     * @param spriteHeight The height in pixels of each Sprite
     * @param spriteSpacing The horizontal and vertical spacing in pixels
     * between Sprites
     * @param originX The x-coordinate in pixels on each Sprite of that Sprite's
     * origin
     * @param originY The y-coordinate in pixels on each Sprite of that Sprite's
     * origin
     * @param transColor The transparent color of this SpriteSheet's Sprites, or
     * null if there should be none
     * @param filters The Set of Filters that will have an effect on this
     * SpriteSheet's Sprites when applied to them with draw(), or null if no
     * Filters should have an effect
     * @param load Whether this SpriteSheet should load upon creation
     */
    public SpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX,
            int originY, Color transColor, Set<Filter> filters, boolean load) {
        this(null, null, path, transColor, (filters == null ? null : new HashSet<>(filters)),
                width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, load);
    }
    
    /**
     * Constructs a SpriteSheet from an image file.
     * @param path The relative path to the image file
     * @param width The width in Sprites of this SpriteSheet
     * @param height The height in Sprites of this SpriteSheet
     * @param spriteWidth The width in pixels of each Sprite
     * @param spriteHeight The height in pixels of each Sprite
     * @param spriteSpacing The horizontal and vertical spacing in pixels
     * between Sprites
     * @param originX The x-coordinate in pixels on each Sprite of that Sprite's
     * origin
     * @param originY The y-coordinate in pixels on each Sprite of that Sprite's
     * origin
     * @param transR The red value (0-255) of this SpriteSheet's Sprites'
     * transparent color
     * @param transG The green value (0-255) of this SpriteSheet's Sprites'
     * transparent color
     * @param transB The blue value (0-255) of this SpriteSheet's Sprites'
     * transparent color
     * @param filters The Set of Filters that will have an effect on this
     * SpriteSheet's Sprites when applied to them with draw(), or null if no
     * Filters should have an effect
     * @param load Whether this SpriteSheet should load upon creation
     */
    public SpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX,
            int originY, int transR, int transG, int transB, Set<Filter> filters, boolean load) {
        this(path, width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY,
                new Color(transR, transG, transB), (filters == null ? null : new HashSet<>(filters)), load);
    }
    
    /**
     * Constructs a SpriteSheet from an existing SpriteSheet with a Filter
     * applied to it. The new SpriteSheet will have the same Set of Filters that
     * are usable with its Sprites' draw() methods as the existing Sprite.
     * @param spriteSheet The SpriteSheet to create this SpriteSheet from
     * @param filter The Filter to apply to the existing SpriteSheet
     * @param load Whether this SpriteSheet should load upon creation
     */
    public SpriteSheet(SpriteSheet spriteSheet, Filter filter, boolean load) {
        this(spriteSheet, filter, null, null, spriteSheet.filters, spriteSheet.width, spriteSheet.height,
                spriteSheet.spriteWidth, spriteSheet.spriteHeight, spriteSheet.spriteSpacing,
                spriteSheet.originX, spriteSheet.originY, load);
    }
    
    private SpriteSheet(SpriteSheet basedOn, Filter basedFilter, String path,
            Color transColor, Set<Filter> filters, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX, int originY, boolean load) {
        this.basedOn = basedOn;
        this.basedFilter = basedFilter;
        this.path = path;
        this.transColor = transColor;
        this.filters = filters;
        this.width = width;
        this.height = height;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.spriteSpacing = spriteSpacing;
        this.originX = originX;
        this.originY = originY;
        sprites = new Sprite[width][height];
        for (int x = 0; x < sprites.length; x++) {
            Sprite[] column = sprites[x];
            for (int y = 0; y < column.length; y++) {
                column[y] = new Sprite(this);
            }
        }
        if (load) {
            load();
        }
    }
    
    @Override
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Loads this SpriteSheet, along with all of its Sprites, if it is not
     * already loaded.
     * @return Whether the loading occurred
     */
    @Override
    public final boolean load() {
        if (!loaded) {
            loaded = true;
            Image image;
            if (path != null) {
                try {
                    image = new Image(path, false, Image.FILTER_NEAREST, transColor);
                } catch (SlickException e) {
                    throw new RuntimeException(e);
                }
            } else {
                basedOn.load();
                image = basedFilter.getFilteredImage(basedOn.images.get(null));
            }
            for (Sprite[] column : sprites) {
                for (Sprite sprite : column) {
                    sprite.loaded = true;
                }
            }
            numSpritesLoaded = width*height;
            loadFilter(null, image);
            if (filters != null) {
                for (Filter filter : filters) {
                    loadFilter(filter, filter.getFilteredImage(image));
                }
            }
            return true;
        }
        return false;
    }
    
    private void loadFilter(Filter filter, Image image) {
        org.cell2d.celick.SpriteSheet spriteSheet = new org.cell2d.celick.SpriteSheet(
                image, spriteWidth, spriteHeight, spriteSpacing);
        for (int x = 0; x < sprites.length; x++) {
            Sprite[] column = sprites[x];
            for (int y = 0; y < column.length; y++) {
                column[y].loadFilter(filter, spriteSheet.getSubImage(x, y));
            }
        }
        images.put(filter, image);
    }
    
    /**
     * Unloads this SpriteSheet, along with all of its Sprites, if it is
     * currently loaded.
     * @return Whether the unloading occurred
     */
    @Override
    public final boolean unload() {
        if (loaded) {
            loaded = false;
            destroyAndClear();
            for (Sprite[] column : sprites) {
                for (Sprite sprite : column) {
                    sprite.loaded = false;
                    sprite.clear();
                }
            }
            numSpritesLoaded = 0;
            return true;
        }
        return false;
    }
    
    final void unloadSprite() {
        numSpritesLoaded--;
        if (numSpritesLoaded == 0) {
            loaded = false;
            destroyAndClear();
            for (Sprite[] column : sprites) {
                for (Sprite sprite : column) {
                    sprite.clear();
                }
            }
        }
    }
    
    private void destroyAndClear() {
        try {
            for (Image image : images.values()) {
                image.destroy();
            }
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        images.clear();
        width = 0;
        height = 0;
    }
    
    /**
     * Returns an unmodifiable Set view of the Filters that will have an effect
     * on this SpriteSheet's Sprites when applied to them with draw().
     * @return The Set of Filters that will have an effect on this SpriteSheet's
     * Sprites when applied to them with draw()
     */
    public final Set<Filter> getFilters() {
        return (filters == null ? Collections.emptySet() : Collections.unmodifiableSet(filters));
    }
    
    /**
     * Returns the x-coordinate in pixels on each of this SpriteSheet's Sprites
     * of that Sprite's origin.
     * @return The x-coordinate in pixels on each of this SpriteSheet's Sprites
     * of that Sprite's origin
     */
    public final int getOriginX() {
        return originX;
    }
    
    /**
     * Returns the y-coordinate in pixels on each of this SpriteSheet's Sprites
     * of that Sprite's origin.
     * @return The y-coordinate in pixels on each of this SpriteSheet's Sprites
     * of that Sprite's origin
     */
    public final int getOriginY() {
        return originY;
    }
    
    /**
     * Returns the width in Sprites of this SpriteSheet.
     * @return The width in Sprites of this SpriteSheet
     */
    public final int getWidth() {
        return width;
    }
    
    /**
     * Returns the height in Sprites of this SpriteSheet.
     * @return The height in Sprites of this SpriteSheet
     */
    public final int getHeight() {
        return height;
    }
    
    /**
     * Returns this SpriteSheet's Sprite at the specified coordinates.
     * @param x The x-coordinate in Sprites of the Sprite
     * @param y The y-coordinate in Sprites of the Sprite
     * @return The Sprite at the specified coordinates
     */
    public Sprite getSprite(int x, int y) {
        if (x < 0 || x >= sprites.length || y < 0 || y >= sprites[x].length) {
            throw new RuntimeException("Attempted to retrieve a Sprite from a SpriteSheet at invalid"
                    + " coordinates (" + x + ", " + y + ")");
        }
        return sprites[x][y];
    }
    
}
