package cell2D;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

public class Sprite implements Animatable, Drawable {
    
    public static final Sprite BLANK = new Sprite();
    
    private final boolean blank;
    boolean loaded;
    private final Sprite recolorOf;
    private final Filter recolorFilter;
    private final SpriteSheet spriteSheet;
    private final String path;
    private final Color transColor;
    private final Set<Filter> filters;
    private final HashMap<Filter,Image[]> images = new HashMap<>();
    private Image[] defaultImages = null;
    private BufferedImage bufferedImage = null;
    private final int originX, originY;
    private int width = 0;
    private int height = 0;
    private int right = 0;
    private int bottom = 0;
    
    private Sprite() {
        blank = true;
        loaded = true;
        recolorOf = null;
        recolorFilter = null;
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
        recolorOf = null;
        recolorFilter = null;
        this.spriteSheet = spriteSheet;
        path = null;
        transColor = null;
        filters = null;
        this.originX = spriteSheet.getOriginX();
        this.originY = spriteSheet.getOriginY();
    }
    
    public Sprite(String path, int originX, int originY, Set<Filter> filters, boolean load) throws SlickException {
        this(path, originX, originY, null, filters, load);
    }
    
    public Sprite(String path, int originX, int originY,
            Color transColor, Set<Filter> filters, boolean load) throws SlickException {
        blank = false;
        loaded = false;
        recolorOf = null;
        recolorFilter = null;
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
    
    public Sprite(String path, int originX, int originY,
            int transR, int transG, int transB, Set<Filter> filters, boolean load) throws SlickException {
        this(path, originX, originY,  new Color(transR, transG, transB), filters, load);
    }
    
    public Sprite(Sprite sprite, Filter filter) throws SlickException {
        blank = false;
        loaded = false;
        recolorOf = sprite;
        recolorFilter = filter;
        spriteSheet = null;
        this.path = null;
        this.transColor = null;
        this.filters = sprite.filters;
        this.originX = sprite.originX;
        this.originY = sprite.originY;
        if (sprite.isLoaded()) {
            load();
        }
    }
    
    public Sprite(Sprite sprite, Map<Color,Color> colorMap) throws SlickException {
        this(sprite, new ColorMapFilter(colorMap));
    }
    
    public Sprite(Sprite sprite, Color color) throws SlickException {
        this(sprite, new ColorFilter(color));
    }
    
    public Sprite(Sprite sprite, int colorR, int colorG, int colorB) throws SlickException {
        this(sprite, new ColorFilter(colorR, colorG, colorB));
    }
    
    public final boolean isLoaded() {
        return loaded;
    }
    
    public final boolean load() throws SlickException {
        if (blank) {
            return true;
        } else if (loaded) {
            return false;
        }
        loaded = true;
        if (spriteSheet == null) {
            GameImage gameImage;
            if (recolorOf == null) {
                gameImage = CellGame.getTransparentImage(path, transColor);
            } else {
                recolorOf.load();
                gameImage = recolorFilter.getFilteredImage(recolorOf.bufferedImage);
            }
            bufferedImage = gameImage.getBufferedImage();
            loadFilter(null, gameImage.getImage(), bufferedImage);
            if (filters != null) {
                for (Filter filter : filters) {
                    GameImage filteredImage = filter.getFilteredImage(bufferedImage);
                    loadFilter(filter, filteredImage.getImage(), filteredImage.getBufferedImage());   
                }
            }
        } else {
            spriteSheet.load();
        }
        return true;
    }
    
    final void loadFilter(Filter filter, Image image, BufferedImage bufferedImage) {
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
    
    public final boolean unload() {
        if (blank) {
            return true;
        } else if (loaded) {
            return false;
        }
        loaded = false;
        if (spriteSheet == null) {
            clear();
        } else {
            spriteSheet.tryUnload();
        }
        return true;
    }
    
    final void clear() {
        images.clear();
        defaultImages = null;
        bufferedImage = null;
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
    public final int getLength() {
        return 1;
    }
    
    @Override
    public final Animatable getFrame(int index) {
        return (index == 0 ? this : BLANK);
    }
    
    @Override
    public final boolean framesAreCompatible(int index1, int index2) {
        return (index1 == 0 && index2 == 0);
    }
    
    @Override
    public final double getFrameDuration(int index) {
        return 0;
    }
    
    public final SpriteSheet getSpriteSheet() {
        return spriteSheet;
    }
    
    public final Set<Filter> getFilters() {
        return (filters == null ? new HashSet<>() : new HashSet<>(filters));
    }
    
    public final int getWidth() {
        return width;
    }
    
    public final int getHeight() {
        return height;
    }
    
    public final int getOriginX() {
        return originX;
    }
    
    public final int getOriginY() {
        return originY;
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
            right += originX;
            top += originY;
            bottom += originY;
            draw(g, x + left, y + top, left, right, top, bottom, 1, false, false, 0, 1, null);
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
            Vector2f vector = new Vector2f(left, top).sub(angle);
            draw(g, x + Math.round(vector.getX()), y + Math.round(vector.getY()),
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
            draw(g, (int)(x + Math.round(left*scale)), (int)(y + Math.round(top*scale)),
                    left, right, top, bottom, (float)scale, xFlip, yFlip, 0, (float)alpha, filter);
        }
    }
    
    private void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
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
        x += Math.round(xOffset*scale);
        y += Math.round(yOffset*scale);
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
