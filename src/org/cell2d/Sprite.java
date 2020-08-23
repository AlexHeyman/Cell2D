package org.cell2d;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.Image;
import org.cell2d.celick.SlickException;

/**
 * <p>A Sprite is a static image that can be drawn to a Graphics context. Like
 * other Loadables, Sprites can be manually loaded and unloaded into and out of
 * memory. Loading may take a moment, but while a Sprite is not loaded, it
 * cannot be drawn. A Sprite may also be treated as an Animatable with exactly
 * one frame, namely itself, that has a duration of 0.</p>
 * 
 * <p>A Sprite has a fixed set of Filters, specified upon its creation, that can
 * have an effect on it when applied to it with its draw() method. The Sprite
 * makes this possible by making a copy of its image data with each Filter
 * applied upon being loaded. Thus, the amount of memory that a loaded Sprite
 * occupies is proportional to its number of applicable Filters plus 1.</p>
 * @see Filter
 * @see SpriteSheet
 * @see Animation
 * @author Alex Heyman
 */
public class Sprite implements Animatable, Drawable, Loadable {
    
    /**
     * A blank Sprite with no appearance. It is considered to always be loaded
     * and cannot be unloaded.
     */
    public static final Sprite BLANK = new Sprite();
    
    private final boolean blank;
    boolean loaded;
    private final Sprite basedOn;
    private final Filter basedFilter;
    private Map<Filter,Sprite> filteredCopies = null;
    private final SpriteSheet spriteSheet;
    private final String path;
    private final Color transColor;
    private Image[] defaultImages;
    private Map<Filter,Image[]> filterImages;
    private final int originX, originY;
    private int width = 0;
    private int height = 0;
    private int right = 0;
    private int bottom = 0;
    
    private void initImageStorage(Collection<Filter> filters) {
        defaultImages = new Image[4];
        if (filters.isEmpty()) {
            filterImages = Collections.emptyMap();
        } else {
            filterImages = new HashMap<>();
            for (Filter filter : filters) {
                filterImages.put(filter, new Image[4]);
            }
        }
    }
    
    private Sprite() {
        blank = true;
        loaded = true;
        basedOn = null;
        basedFilter = null;
        spriteSheet = null;
        path = null;
        transColor = null;
        defaultImages = new Image[4];
        filterImages = Collections.emptyMap();
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
        initImageStorage(spriteSheet.getFilters());
        originX = spriteSheet.getOriginX();
        originY = spriteSheet.getOriginY();
    }
    
    /**
     * Constructs a Sprite from an image file.
     * @param path The relative path to the image file
     * @param originX The x-coordinate in pixels of the origin on the image
     * @param originY The y-coordinate in pixels of the origin on the image
     * @param load Whether this Sprite should load upon creation
     * @param filters The Filters that should have an effect on this
     * Sprite when applied to it with draw()
     */
    public Sprite(String path, int originX, int originY, boolean load, Filter... filters) {
        this(path, originX, originY, null, load, filters);
    }
    
    /**
     * Constructs a Sprite from an image file.
     * @param path The relative path to the image file
     * @param originX The x-coordinate in pixels of the origin on the image
     * @param originY The y-coordinate in pixels of the origin on the image
     * @param transColor This Sprite's transparent color, or null if there
     * should be none
     * @param load Whether this Sprite should load upon creation
     * @param filters The Filters that should have an effect on this
     * Sprite when applied to it with draw()
     */
    public Sprite(String path, int originX, int originY,
            Color transColor, boolean load, Filter... filters) {
        blank = false;
        loaded = false;
        basedOn = null;
        basedFilter = null;
        spriteSheet = null;
        this.path = path;
        this.transColor = transColor;
        initImageStorage(Arrays.asList(filters));
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
     * @param load Whether this Sprite should load upon creation
     * @param filters The Filters that should have an effect on this
     * Sprite when applied to it with draw()
     */
    public Sprite(String path, int originX, int originY,
            int transR, int transG, int transB, boolean load, Filter... filters) {
        this(path, originX, originY,  new Color(transR, transG, transB), load, filters);
    }
    
    /**
     * Constructs a Sprite from a
     * <a href="https://cell2d.gitbook.io/cell2d-documentation/general/celick">Celick</a>
     * Image. This Sprite's origin will be the Image's center of rotation with
     * its coordinates rounded to the nearest integer. Once created, this Sprite
     * will be independent of the Image from which it is created. This Sprite
     * will be considered to be loaded from the start, and once unloaded, it
     * cannot be reloaded.
     * @param image The Image to create this Sprite from
     * @param filters The Filters that should have an effect on this Sprite when
     * applied to it with draw()
     */
    public Sprite(Image image, Filter... filters) {
        blank = false;
        loaded = true;
        basedOn = null;
        basedFilter = null;
        spriteSheet = null;
        path = null;
        transColor = null;
        initImageStorage(Arrays.asList(filters));
        originX = (int)Math.round(image.getCenterOfRotationX());
        originY = (int)Math.round(image.getCenterOfRotationY());
        Image spriteImage;
        try {
            spriteImage = image.getBlankCopy();
            Graphics graphics = spriteImage.getGraphics();
            graphics.drawImage(image, 0, 0);
            graphics.flush();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        loadFilter(null, spriteImage);
        for (Filter filter : filterImages.keySet()) {
            loadFilter(filter, filter.getFilteredImage(spriteImage));   
        }
    }
    
    private Sprite(Sprite sprite, Filter filter, boolean load) {
        blank = false;
        loaded = false;
        basedOn = sprite;
        basedFilter = filter;
        spriteSheet = null;
        path = null;
        transColor = null;
        initImageStorage(sprite.filterImages.keySet());
        originX = sprite.originX;
        originY = sprite.originY;
        if (load) {
            load();
        }
    }
    
    @Override
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Loads this Sprite if it is not already loaded. If this Sprite was created
     * from another Sprite or is part of a SpriteSheet, that other Sprite or
     * SpriteSheet will be loaded as well.
     * @return Whether the loading occurred
     */
    @Override
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
            for (Filter filter : filterImages.keySet()) {
                loadFilter(filter, filter.getFilteredImage(image));   
            }
        } else {
            spriteSheet.load();
        }
        return true;
    }
    
    final void loadFilter(Filter filter, Image image) {
        image.getWidth(); //Prompt the image to initialize itself if it hasn't already
        Image[] imageArray;
        if (filter == null) {
            imageArray = defaultImages;
            width = image.getWidth();
            height = image.getHeight();
            right = width - originX;
            bottom = height - originY;
        } else {
            imageArray = filterImages.get(filter);
        }
        imageArray[0] = image;
        imageArray[0].setCenterOfRotation(originX, originY);
        imageArray[1] = image.getFlippedCopy(true, false);
        imageArray[1].setCenterOfRotation(right, originY);
        imageArray[2] = image.getFlippedCopy(false, true);
        imageArray[2].setCenterOfRotation(originX, bottom);
        imageArray[3] = image.getFlippedCopy(true, true);
        imageArray[3].setCenterOfRotation(right, bottom);
    }
    
    /**
     * Unloads this Sprite if it is currently loaded. If this Sprite is part of
     * a SpriteSheet that had no other Sprites loaded, that SpriteSheet will be
     * unloaded as well.
     * @return Whether the unloading occurred
     */
    @Override
    public final boolean unload() {
        if (blank || !loaded) {
            return false;
        } else if (spriteSheet != null) {
            spriteSheet.unloadSprite();
        } else {
            try {
                for (Image[] imageArray : filterImages.values()) {
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
        defaultImages = null;
        for (Image[] images : filterImages.values()) {
            for (int i = 0; i < images.length; i++) {
                images[i] = null;
            }
        }
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
        if (index != 0) {
            throw new IndexOutOfBoundsException("Attempted to get an Animatable's frame at invalid index "
                    + index);
        }
        return this;
    }
    
    @Override
    public final long getFrameDuration(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("Attempted to get an Animatable's frame duration at invalid"
                    + " index " + index);
        }
        return 0;
    }
    
    @Override
    public final boolean framesAreCompatible(int index1, int index2) {
        if (index1 != 0 || index2 != 0) {
            throw new IndexOutOfBoundsException("Attempted to get an Animatable's frame compatibility at"
                    + " invalid pair of indices (" + index1 + ", " + index2 + ")");
        }
        return true;
    }
    
    @Override
    public final Set<Sprite> getSprites() {
        return Collections.singleton(this);
    }
    
    @Override
    public final Sprite getFilteredCopy(Filter filter, boolean load) {
        Sprite copy = null;
        if (filteredCopies != null) {
            copy = filteredCopies.get(filter);
        }
        if (copy == null) {
            copy = new Sprite(this, filter, load);
            if (filteredCopies == null) {
                filteredCopies = new HashMap<>();
            }
            filteredCopies.put(filter, copy);
        }
        return copy;
    }
    
    /**
     * Returns a Drawable instantiation of this Sprite - which is simply this
     * Sprite itself, since Sprites are already Drawables.
     * @return A Drawable instantiation of this Sprite
     */
    @Override
    public final Drawable getInstance() {
        return this;
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
        return Collections.unmodifiableSet(filterImages.keySet());
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
            Image[] imageArray = filterImages.get(filter);
            image = (imageArray == null ? defaultImages[index] : imageArray[index]);
        }
        image.setRotation(-angle);
        image.setAlpha(alpha);
        g.drawImage(image, x, y, x + (right - left)*scale, y + (bottom - top)*scale,
                left, top, right, bottom);
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
            left = Math.max(left + originX, 0);
            right = Math.min(right + originX, width);
            top = Math.max(top + originY, 0);
            bottom = Math.min(bottom + originY, height);
            draw(g, x + left, y + top, left, right, top, bottom, 1, false, false, 0, 1, null);
        }
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter) {
        if (!blank && loaded && right > left && bottom > top && alpha > 0) {
            if (xFlip) {
                int temp = left;
                left = -right + this.right;
                right = -temp + this.right;
            } else {
                left += originX;
                right += originX;
            }
            if (yFlip) {
                int temp = bottom;
                bottom = -top + this.bottom;
                top = -temp + this.bottom;
            } else {
                top += originY;
                bottom += originY;
            }
            left = Math.max(left, 0);
            right = Math.min(right, width);
            top = Math.max(top, 0);
            bottom = Math.min(bottom, height);
            CellVector vector = new CellVector((long)left << Frac.BITS,
                    (long)top << Frac.BITS).changeAngle(angle);
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
                left = -right + this.right;
                right = -temp + this.right;
            } else {
                left += originX;
                right += originX;
            }
            if (yFlip) {
                int temp = bottom;
                bottom = -top + this.bottom;
                top = -temp + this.bottom;
            } else {
                top += originY;
                bottom += originY;
            }
            left = Math.max(left, 0);
            right = Math.min(right, width);
            top = Math.max(top, 0);
            bottom = Math.min(bottom, height);
            draw(g, x + (float)(left*scale), y + (float)(top*scale),
                    left, right, top, bottom, (float)scale, xFlip, yFlip, 0, (float)alpha, filter);
        }
    }
    
}
