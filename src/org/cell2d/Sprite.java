package org.cell2d;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.Image;
import org.cell2d.celick.SlickException;

/**
 * <p>A Sprite is a static image that can be drawn to a Graphics context. Like
 * SpriteSheets, Sounds, and Music tracks, Sprites can be manually loaded and
 * unloaded into and out of memory. Loading may take a moment, but while a
 * Sprite is not loaded, it cannot be drawn. A Sprite may also be treated as an
 * Animatable with exactly one frame, namely itself, that has a duration of 0.
 * </p>
 * @see Filter
 * @see SpriteSheet
 * @see Animation
 * @author Alex Heyman
 */
public class Sprite implements Animatable, Drawable {
    
    /**
     * A blank Sprite with no appearance. It is considered to always be loaded
     * and cannot be unloaded.
     */
    public static final Sprite BLANK = new Sprite();
    
    private final boolean blank;
    boolean loaded;
    private final Sprite basedOn;
    private final Filter basedFilter;
    private final SpriteSheet spriteSheet;
    private final String path;
    private final Color transColor;
    private final Set<Filter> filters;
    private final HashMap<Filter,Image[]> images = new HashMap<>();
    private Image[] defaultImages = null;
    private final int originX, originY;
    private int width = 0;
    private int height = 0;
    private int right = 0;
    private int bottom = 0;
    
    private Sprite() {
        blank = true;
        loaded = true;
        basedOn = null;
        basedFilter = null;
        spriteSheet = null;
        path = null;
        transColor = null;
        filters = null;
        originX = 0;
        originY = 0;
    }
    
    Sprite(SpriteSheet spriteSheet) {
        blank = false;
        loaded = false;
        basedOn = null;
        basedFilter = null;
        this.spriteSheet = spriteSheet;
        path = null;
        transColor = null;
        filters = null;
        originX = spriteSheet.getOriginX();
        originY = spriteSheet.getOriginY();
    }
    
    /**
     * Constructs a Sprite from an image file.
     * @param path The relative path to the image file
     * @param originX The x-coordinate in pixels of the origin on the image
     * @param originY The y-coordinate in pixels of the origin on the image
     * @param filters The Set of Filters that should have an effect on this
     * Sprite when applied to it with draw(), or null if no Filters should have
     * an effect
     * @param load Whether this Sprite should load upon creation
     */
    public Sprite(String path, int originX, int originY, Set<Filter> filters, boolean load) {
        this(path, originX, originY, null, filters, load);
    }
    
    /**
     * Constructs a Sprite from an image file.
     * @param path The relative path to the image file
     * @param originX The x-coordinate in pixels of the origin on the image
     * @param originY The y-coordinate in pixels of the origin on the image
     * @param transColor This Sprite's transparent color, or null if there
     * should be none
     * @param filters The Set of Filters that should have an effect on this
     * Sprite when applied to it with draw(), or null if no Filters should have
     * an effect
     * @param load Whether this Sprite should load upon creation
     */
    public Sprite(String path, int originX, int originY,
            Color transColor, Set<Filter> filters, boolean load) {
        blank = false;
        loaded = false;
        basedOn = null;
        basedFilter = null;
        spriteSheet = null;
        this.path = path;
        this.transColor = transColor;
        this.filters = (filters == null ? null : new HashSet<>(filters));
        this.originX = originX;
        this.originY = originY;
        if (load) {
            load();
        }
    }
    
    /**
     * Constructs a Sprite from an image file.
     * @param path The relative path to the image file
     * @param originX The x-coordinate in pixels of the origin on the image
     * @param originY The y-coordinate in pixels of the origin on the image
     * @param transR The red value (0-255) of this Sprite's transparent color
     * @param transG The green value (0-255) of this Sprite's transparent color
     * @param transB The blue value (0-255) of this Sprite's transparent color
     * @param filters The Set of Filters that should have an effect on this
     * Sprite when applied to it with draw(), or null if no Filters should have
     * an effect
     * @param load Whether this Sprite should load upon creation
     */
    public Sprite(String path, int originX, int originY,
            int transR, int transG, int transB, Set<Filter> filters, boolean load) {
        this(path, originX, originY,  new Color(transR, transG, transB), filters, load);
    }
    
    /**
     * Constructs a Sprite from a Celick Image. This Sprite's origin will be the
     * Image's center of rotation with its coordinates rounded to the nearest
     * integer. Once created, this Sprite will be independent of the Image from
     * which it is created. This Sprite will be considered to be loaded from the
     * start, and once unloaded, it cannot be reloaded.
     * @param image The Image to create this Sprite from
     * @param filters The Set of Filters that should have an effect on this
     * Sprite when applied to it with draw(), or null if no Filters should have
     * an effect
     */
    public Sprite(Image image, Set<Filter> filters) {
        blank = false;
        loaded = true;
        basedOn = null;
        basedFilter = null;
        spriteSheet = null;
        path = null;
        transColor = null;
        this.filters = (filters == null ? null : new HashSet<>(filters));
        originX = (int)Math.round(image.getCenterOfRotationX());
        originY = (int)Math.round(image.getCenterOfRotationY());
        Image spriteImage;
        try {
            spriteImage = new Image(image.getWidth(), image.getHeight());
            spriteImage.getGraphics().drawImage(image, 0, 0);
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        loadFilter(null, spriteImage);
    }
    
    /**
     * Constructs a Sprite from an existing Sprite with a Filter applied to it.
     * The existing Sprite must not have been created as part of a SpriteSheet.
     * The new Sprite will have the same Set of Filters that are usable with
     * draw() as the existing Sprite.
     * @param sprite The Sprite to create this Sprite from
     * @param filter The Filter to apply to the existing Sprite
     * @param load Whether this Sprite should load upon creation
     */
    public Sprite(Sprite sprite, Filter filter, boolean load) {
        if (sprite.spriteSheet != null) {
            throw new RuntimeException("Attempted to create a Sprite from part of a SpriteSheet");
        }
        blank = false;
        loaded = false;
        basedOn = sprite;
        basedFilter = filter;
        spriteSheet = null;
        path = null;
        transColor = null;
        filters = sprite.filters;
        originX = sprite.originX;
        originY = sprite.originY;
        if (load) {
            load();
        }
    }
    
    /**
     * Returns whether this Sprite is loaded.
     * @return Whether this Sprite is loaded
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Loads this Sprite if it is not already loaded. If this Sprite was created
     * from another Sprite or is part of a SpriteSheet, that other Sprite or
     * SpriteSheet will be loaded as well.
     * @return Whether the loading occurred
     */
    public final boolean load() {
        if (loaded) {
            return false;
        }
        loaded = true;
        if (spriteSheet == null) {
            Image image;
            if (path != null) {
                try {
                    image = new Image(path, false, Image.FILTER_NEAREST, transColor);
                } catch (SlickException e) {
                    throw new RuntimeException(e);
                }
            } else if (basedOn != null) {
                basedOn.load();
                image = basedFilter.getFilteredImage(basedOn.defaultImages[0]);
            } else {
                throw new RuntimeException("Attempted to reload a Sprite that cannot be reloaded");
            }
            loadFilter(null, image);
            if (filters != null) {
                for (Filter filter : filters) {
                    loadFilter(filter, filter.getFilteredImage(image));   
                }
            }
        } else {
            spriteSheet.load();
        }
        return true;
    }
    
    final void loadFilter(Filter filter, Image image) {
        image.getWidth(); //Prompt the image to initialize itself if it hasn't already
        Image[] imageArray = new Image[4];
        imageArray[0] = image;
        imageArray[0].setCenterOfRotation(originX, originY);
        imageArray[1] = image.getFlippedCopy(true, false);
        imageArray[1].setCenterOfRotation(right, originY);
        imageArray[2] = image.getFlippedCopy(false, true);
        imageArray[2].setCenterOfRotation(originX, bottom);
        imageArray[3] = image.getFlippedCopy(true, true);
        imageArray[3].setCenterOfRotation(right, bottom);
        if (filter == null) {
            defaultImages = imageArray;
            width = image.getWidth();
            height = image.getHeight();
            right = width - originX;
            bottom = height - originY;
        }
        images.put(filter, imageArray);
    }
    
    /**
     * Unloads this Sprite if it is currently loaded. If this Sprite is part of
     * a SpriteSheet that had no other Sprites loaded, that SpriteSheet will be
     * unloaded as well.
     * @return Whether the unloading occurred
     */
    public final boolean unload() {
        if (blank || !loaded) {
            return false;
        } else if (spriteSheet != null) {
            spriteSheet.unloadSprite();
        } else {
            try {
                for (Image[] imageArray : images.values()) {
                    imageArray[0].destroy();
                }
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }
            clear();
        }
        loaded = false;
        return true;
    }
    
    final void clear() {
        images.clear();
        defaultImages = null;
        width = 0;
        height = 0;
        right = 0;
        bottom = 0;
    }
    
    @Override
    public final int getLevel() {
        return 0;
    }
    
    @Override
    public final int getNumFrames() {
        return 1;
    }
    
    @Override
    public final Animatable getFrame(int index) {
        return (index == 0 ? this : BLANK);
    }
    
    @Override
    public final long getFrameDuration(int index) {
        return 0;
    }
    
    @Override
    public final boolean framesAreCompatible(int index1, int index2) {
        return index1 == 0 && index2 == 0;
    }
    
    /**
     * Returns the SpriteSheet that this Sprite is part of, or null if it is not
     * part of one.
     * @return The SpriteSheet that this Sprite is part of
     */
    public final SpriteSheet getSpriteSheet() {
        return spriteSheet;
    }
    
    /**
     * Returns an unmodifiable Set view of the Filters that will have an effect
     * on this Sprite when applied to it with draw().
     * @return The Set of Filters that will have an effect on this Sprite when
     * applied to it with draw()
     */
    public final Set<Filter> getFilters() {
        return (filters == null ? Collections.emptySet() : Collections.unmodifiableSet(filters));
    }
    
    /**
     * Returns the x-coordinate in pixels of this Sprite's origin.
     * @return The x-coordinate in pixels of this Sprite's origin
     */
    public final int getOriginX() {
        return originX;
    }
    
    /**
     * Returns the y-coordinate in pixels of this Sprite's origin.
     * @return The y-coordinate in pixels of this Sprite's origin
     */
    public final int getOriginY() {
        return originY;
    }
    
    /**
     * Returns this Sprite's width in pixels. If the Sprite is not loaded, this
     * method will return 0.
     * @return This Sprite's width in pixels
     */
    public final int getWidth() {
        return width;
    }
    
    /**
     * Returns this Sprite's height in pixels. If the Sprite is not loaded, this
     * method will return 0.
     * @return This Sprite's height in pixels
     */
    public final int getHeight() {
        return height;
    }
    
    @Override
    public final void draw(Graphics g, int x, int y) {
        if (!blank && loaded) {
            draw(g, x, y, 0, width, 0, height, 1, false, false, 0, 1, null);
        }
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, boolean xFlip,
            boolean yFlip, double angle, double alpha, Filter filter) {
        if (!blank && loaded && alpha > 0) {
            draw(g, x, y, 0, width, 0, height, 1, xFlip, yFlip, (float)angle, (float)alpha, filter);
        }
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, double scale,
            boolean xFlip, boolean yFlip, double alpha, Filter filter) {
        if (!blank && loaded && scale > 0 && alpha > 0) {
            draw(g, x, y, 0, width, 0, height, (float)scale, xFlip, yFlip, 0, (float)alpha, filter);
        }
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom) {
        if (!blank && loaded && right > left && bottom > top) {
            left += originX;
            top += originY;
            draw(g, x + left, y + top, left, right + originX, top, bottom + originY, 1, false, false, 0, 1, null);
        }
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter) {
        if (!blank && loaded && right > left && bottom > top && alpha > 0) {
            if (xFlip) {
                int temp = left;
                left = -right;
                right = -temp;
            }
            if (yFlip) {
                int temp = bottom;
                bottom = -top;
                top = -temp;
            }
            left += originX;
            right += originX;
            top += originY;
            bottom += originY;
            CellVector vector = new CellVector((long)left << Frac.BITS, (long)top << Frac.BITS).changeAngle(angle);
            draw(g, x + (float)Frac.toDouble(vector.getX()), y + (float)Frac.toDouble(vector.getY()),
                    left, right, top, bottom, 1, xFlip, yFlip, (float)angle, (float)alpha, filter);
        }
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter) {
        if (!blank && loaded && right > left && bottom > top && scale > 0 && alpha > 0) {
            if (xFlip) {
                int temp = left;
                left = -right;
                right = -temp;
            }
            if (yFlip) {
                int temp = bottom;
                bottom = -top;
                top = -temp;
            }
            left += originX;
            right += originX;
            top += originY;
            bottom += originY;
            draw(g, x + (float)(left*scale), y + (float)(top*scale),
                    left, right, top, bottom, (float)scale, xFlip, yFlip, 0, (float)alpha, filter);
        }
    }
    
    private void draw(Graphics g, float x, float y, int left, int right, int top, int bottom,
            float scale, boolean xFlip, boolean yFlip, float angle, float alpha, Filter filter) {
        int index = 0;
        float xOffset, yOffset;
        if (xFlip) {
            index += 1;
            xOffset = -this.right;
        } else {
            xOffset = -originX;
        }
        if (yFlip) {
            index += 2;
            yOffset = -this.bottom;
        } else {
            yOffset = -originY;
        }
        x += xOffset*scale;
        y += yOffset*scale;
        Image image;
        if (filter == null) {
            image = defaultImages[index];
        } else {
            Image[] imageArray = images.get(filter);
            image = (imageArray == null ? defaultImages[index] : imageArray[index]);
        }
        image.setRotation(-angle);
        image.setAlpha(alpha);
        g.drawImage(image, x, y, x + (right - left)*scale, y + (bottom - top)*scale, left, top, right, bottom);
    }
    
}
