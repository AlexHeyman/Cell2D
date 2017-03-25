package cell2D;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * <p>A SpriteSheet is a rectangular grid of Sprites. Each Sprite has an
 * x-coordinate in the grid that starts at 0 for the leftmost column and
 * increases to the right, as well as a y-coordinate that starts at 0 for the
 * topmost row and increases below. Like Sprites, Sounds, and Music tracks,
 * SpriteSheets can be manually loaded and unloaded into and out of memory.
 * Loading may take a moment, but a SpriteSheet's Sprites cannot be loaded and
 * drawn if the SpriteSheet itself is not loaded. Thus, loading a Sprite that is
 * part of a SpriteSheet will also load its SpriteSheet.</p>
 * @author Andrew Heyman
 */
public class SpriteSheet {
    
    private boolean loaded = false;
    private final SpriteSheet basedOn;
    private final Filter basedFilter;
    private final String path;
    private final Color transColor;
    private final Set<Filter> filters;
    private BufferedImage bufferedImage = null;
    private final int spriteWidth, spriteHeight, spriteSpacing, originX, originY;
    private int width = 0;
    private int height = 0;
    private final Sprite[][] sprites;
    private final Sprite[] spriteList;
    
    /**
     * Creates a new SpriteSheet from an image file.
     * @param path The relative path to the image file
     * @param width The width in Sprites of this SpriteSheet
     * @param height The height in Sprites of this SpriteSheet
     * @param spriteWidth The width in pixels of each Sprite
     * @param spriteHeight The height in pixels of each Sprite
     * @param spriteSpacing The horizontal and vertical spacing in pixels
     * between Sprites
     * @param originX The x-coordinate in pixels on each Sprite of its origin
     * @param originY The y-coordinate in pixels on each Sprite of its origin
     * @param filters The Set of Filters that will have an effect on this
     * SpriteSheet's Sprites when applied to them with draw()
     * @param load Whether this SpriteSheet should load upon creation
     * @throws SlickException If the SpriteSheet could not be properly loaded
     * from the specified path
     */
    public SpriteSheet(String path, int width, int height, int spriteWidth,
            int spriteHeight, int spriteSpacing, int originX, int originY, Set<Filter> filters, boolean load) throws SlickException {
        this(path, width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, null, (filters == null ? null : new HashSet<>(filters)), load);
    }
    
    /**
     * Creates a new SpriteSheet from an image file.
     * @param path The relative path to the image file
     * @param width The width in Sprites of this SpriteSheet
     * @param height The height in Sprites of this SpriteSheet
     * @param spriteWidth The width in pixels of each Sprite
     * @param spriteHeight The height in pixels of each Sprite
     * @param spriteSpacing The horizontal and vertical spacing in pixels
     * between Sprites
     * @param originX The x-coordinate in pixels on each Sprite of its origin
     * @param originY The y-coordinate in pixels on each Sprite of its origin
     * @param transColor The transparent color of this SpriteSheet's Sprites, or
     * null if there should be none
     * @param filters The Set of Filters that will have an effect on this
     * SpriteSheet's Sprites when applied to them with draw()
     * @param load Whether this SpriteSheet should load upon creation
     * @throws SlickException If the SpriteSheet could not be properly loaded
     * from the specified path
     */
    public SpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX,
            int originY, Color transColor, Set<Filter> filters, boolean load) throws SlickException {
        this(null, null, path, transColor, (filters == null ? null : new HashSet<>(filters)), width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, load);
    }
    
    /**
     * Creates a new SpriteSheet from an image file.
     * @param path The relative path to the image file
     * @param width The width in Sprites of this SpriteSheet
     * @param height The height in Sprites of this SpriteSheet
     * @param spriteWidth The width in pixels of each Sprite
     * @param spriteHeight The height in pixels of each Sprite
     * @param spriteSpacing The horizontal and vertical spacing in pixels
     * between Sprites
     * @param originX The x-coordinate in pixels on each Sprite of its origin
     * @param originY The y-coordinate in pixels on each Sprite of its origin
     * @param transR The R value (0-255) of this SpriteSheet's Sprites'
     * transparent color
     * @param transG The G value (0-255) of this SpriteSheet's Sprites'
     * transparent color
     * @param transB The B value (0-255) of this SpriteSheet's Sprites'
     * transparent color
     * @param filters The Set of Filters that will have an effect on this
     * SpriteSheet's Sprites when applied to them with draw()
     * @param load Whether this SpriteSheet should load upon creation
     * @throws SlickException If the SpriteSheet could not be properly loaded
     * from the specified path
     */
    public SpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX,
            int originY, int transR, int transG, int transB, Set<Filter> filters, boolean load) throws SlickException {
        this(path, width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, new Color(transR, transG, transB), (filters == null ? null : new HashSet<>(filters)), load);
    }
    
    /**
     * Creates a new SpriteSheet from an existing SpriteSheet with a Filter
     * applied to it. The new SpriteSheet will have the same Set of Filters that
     * are usable with its Sprites' draw() methods as the existing Sprite.
     * @param spriteSheet The SpriteSheet to create this SpriteSheet from
     * @param filter The Filter to apply to the existing SpriteSheet
     * @param load Whether this SpriteSheet should load upon creation
     * @throws SlickException If the SpriteSheet could not be properly loaded
     */
    public SpriteSheet(SpriteSheet spriteSheet, Filter filter, boolean load) throws SlickException {
        this(spriteSheet, filter, null, null, spriteSheet.filters, spriteSheet.width, spriteSheet.height, spriteSheet.spriteWidth, spriteSheet.spriteHeight, spriteSheet.spriteSpacing, spriteSheet.originX, spriteSheet.originY, load);
    }
    
    private SpriteSheet(SpriteSheet recolorOf, Filter recolorFilter, String path,
            Color transColor, Set<Filter> filters, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX, int originY, boolean load) throws SlickException {
        this.basedOn = recolorOf;
        this.basedFilter = recolorFilter;
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
        spriteList = new Sprite[width*height];
        int i = 0;
        for (int x = 0; x < sprites.length; x++) {
            for (int y = 0; y < sprites[x].length; y++) {
                spriteList[i] = new Sprite(this);
                sprites[x][y] = spriteList[i];
                i++;
            }
        }
        if (load) {
            load();
        }
    }
    
    /**
     * Returns whether this SpriteSheet is loaded.
     * @return Whether this SpriteSheet is loaded
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Loads this SpriteSheet, along with all of its Sprites, if it is not
     * already loaded.
     * @return Whether the loading occurred
     * @throws SlickException If the SpriteSheet could not be properly loaded
     */
    public final boolean load() throws SlickException {
        if (!loaded) {
            loaded = true;
            GameImage gameImage;
            if (basedOn == null) {
                gameImage = Assets.getTransparentImage(path, transColor);
            } else {
                basedOn.load();
                gameImage = basedFilter.getFilteredImage(basedOn.bufferedImage);
            }
            bufferedImage = gameImage.getBufferedImage();
            for (Sprite sprite : spriteList) {
                sprite.loaded = true;
            }
            loadFilter(null, gameImage.getImage());
            if (filters != null) {
                for (Filter filter : filters) {
                    GameImage filteredImage = filter.getFilteredImage(bufferedImage);
                    loadFilter(filter, filteredImage.getImage());
                }
            }
            return true;
        }
        return false;
    }
    
    private void loadFilter(Filter filter, Image image) {
        org.newdawn.slick.SpriteSheet spriteSheet = new org.newdawn.slick.SpriteSheet(image, spriteWidth, spriteHeight, spriteSpacing);
        for (int x = 0; x < sprites.length; x++) {
            for (int y = 0; y < sprites[x].length; y++) {
                sprites[x][y].loadFilter(filter, spriteSheet.getSubImage(x, y), null);
            }
        }
    }
    
    /**
     * Unloads this SpriteSheet, along with all of its Sprites, if it is
     * currently loaded.
     * @return Whether the unloading occurred
     */
    public final boolean unload() {
        if (loaded) {
            loaded = false;
            bufferedImage = null;
            for (Sprite sprite : spriteList) {
                sprite.loaded = false;
                sprite.clear();
            }
            return true;
        }
        return false;
    }
    
    final void tryUnload() {
        for (Sprite sprite : spriteList) {
            if (sprite.loaded) {
                return;
            }
        }
        loaded = false;
        bufferedImage = null;
        for (Sprite sprite : spriteList) {
            sprite.loaded = false;
            sprite.clear();
        }
    }
    
    /**
     * Returns the Set of Filters that will have an effect on this SpriteSheet's
     * Sprites when applied to them with draw(). Changes to the returned Set
     * will not be reflected in the SpriteSheet.
     * @return The Set of Filters that will have an effect on this SpriteSheet's
     * Sprites when applied to them with draw()
     */
    public final Set<Filter> getFilters() {
        return new HashSet<>(filters);
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
     * Returns the x-coordinate in pixels on each of this SpriteSheet's Sprites
     * of its origin.
     * @return The x-coordinate in pixels on each of this SpriteSheet's Sprites
     * of its origin
     */
    public final int getOriginX() {
        return originX;
    }
    
    /**
     * Returns the y-coordinate in pixels on each of this SpriteSheet's Sprites
     * of its origin.
     * @return The y-coordinate in pixels on each of this SpriteSheet's Sprites
     * of its origin
     */
    public final int getOriginY() {
        return originY;
    }
    
    /**
     * Returns the Sprite at the specified coordinates.
     * @param x The x-coordinate in Sprites of the Sprite
     * @param y The y-coordinate in Sprites of the Sprite
     * @return The Sprite at the specified coordinates
     */
    public Sprite getSprite(int x, int y) {
        if (x < 0 || x >= sprites.length || y < 0 || y >= sprites[x].length) {
            throw new RuntimeException("Attempted to retrieve a Sprite from a SpriteSheet at invalid coordinates");
        }
        return sprites[x][y];
    }
    
}
