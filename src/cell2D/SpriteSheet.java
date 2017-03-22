package cell2D;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class SpriteSheet {
    
    private boolean loaded = false;
    private final SpriteSheet recolorOf;
    private final Filter recolorFilter;
    private final String path;
    private final Color transColor;
    private final Set<Filter> filters;
    private BufferedImage bufferedImage = null;
    private final int spriteWidth, spriteHeight, spriteSpacing, originX, originY;
    private int width = 0;
    private int height = 0;
    private final Sprite[][] sprites;
    private final Sprite[] spriteList;
    
    public SpriteSheet(String path, int width, int height, int spriteWidth,
            int spriteHeight, int spriteSpacing, int originX, int originY, Set<Filter> filters, boolean load) throws SlickException {
        this(path, width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, null, (filters == null ? null : new HashSet<>(filters)), load);
    }
    
    public SpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX,
            int originY, Color transColor, Set<Filter> filters, boolean load) throws SlickException {
        this(null, null, path, transColor, (filters == null ? null : new HashSet<>(filters)), width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, load);
    }
    
    public SpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX,
            int originY, int transR, int transG, int transB, Set<Filter> filters, boolean load) throws SlickException {
        this(path, width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, new Color(transR, transG, transB), (filters == null ? null : new HashSet<>(filters)), load);
    }
    
    public SpriteSheet(SpriteSheet spriteSheet, Filter filter) throws SlickException {
        this(spriteSheet, filter, null, null, spriteSheet.filters, spriteSheet.width, spriteSheet.height, spriteSheet.spriteWidth, spriteSheet.spriteHeight, spriteSheet.spriteSpacing, spriteSheet.originX, spriteSheet.originY, spriteSheet.isLoaded());
    }
    
    public SpriteSheet(SpriteSheet spriteSheet, Map<Color,Color> colorMap) throws SlickException {
        this(spriteSheet, new ColorMapFilter(colorMap));
    }
    
    public SpriteSheet(SpriteSheet spriteSheet, Color color) throws SlickException {
        this(spriteSheet, new ColorFilter(color));
    }
    
    public SpriteSheet(SpriteSheet spriteSheet, int colorR, int colorG, int colorB) throws SlickException {
        this(spriteSheet, new ColorFilter(colorR, colorG, colorB));
    }
    
    private SpriteSheet(SpriteSheet recolorOf, Filter recolorFilter, String path,
            Color transColor, Set<Filter> filters, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX, int originY, boolean load) throws SlickException {
        this.recolorOf = recolorOf;
        this.recolorFilter = recolorFilter;
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
    
    public final boolean isLoaded() {
        return loaded;
    }
    
    public final boolean load() throws SlickException {
        if (!loaded) {
            loaded = true;
            GameImage gameImage;
            if (recolorOf == null) {
                gameImage = Assets.getTransparentImage(path, transColor);
            } else {
                recolorOf.load();
                gameImage = recolorFilter.getFilteredImage(recolorOf.bufferedImage);
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
            if (sprite.isLoaded()) {
                return;
            }
        }
        loaded = false;
        for (Sprite sprite : spriteList) {
            sprite.loaded = false;
            sprite.clear();
        }
    }
    
    public final Set<Filter> getFilters() {
        return new HashSet<>(filters);
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
    
    public Sprite getSprite(int x, int y) {
        if (x < 0 || x >= sprites.length || y < 0 || y >= sprites[x].length) {
            throw new RuntimeException("Attempted to retrieve a Sprite from a SpriteSheet at invalid coordinates");
        }
        return sprites[x][y];
    }
    
}
