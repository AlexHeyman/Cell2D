package cell2D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.util.Pair;
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
    
    SpriteSheet(SpriteSheet recolorOf, Filter recolorFilter, String path,
            Color transColor, Set<Filter> filters, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX, int originY) {
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
                spriteList[i] = new Sprite(false, null, null, this, null, null, null, originX, originY);
                sprites[x][y] = spriteList[i];
                i++;
            }
        }
    }
    
    final SpriteSheet getRecolor(Filter recolorFilter) {
        return new SpriteSheet(this, recolorFilter, null, null, filters, width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY);
    }
    
    public final boolean isLoaded() {
        return loaded;
    }
    
    public final boolean load() throws SlickException {
        if (!loaded) {
            loaded = true;
            Pair<Image,BufferedImage> pair;
            if (recolorOf == null) {
                pair = CellGame.getTransparentImage(path, transColor);
            } else {
                recolorOf.load();
                pair = recolorFilter.getFilteredImage(recolorOf.bufferedImage);
            }
            bufferedImage = pair.getValue();
            for (Sprite sprite : spriteList) {
                sprite.loaded = true;
            }
            loadFilter("", pair.getKey());
            for (Filter filter : filters) {
                Pair<Image,BufferedImage> filteredPair = filter.getFilteredImage(bufferedImage);
                loadFilter(filter.getName(), filteredPair.getKey());
            }
            return true;
        }
        return false;
    }
    
    private void loadFilter(String filterName, Image image) {
        org.newdawn.slick.SpriteSheet spriteSheet = new org.newdawn.slick.SpriteSheet(image, spriteWidth, spriteHeight, spriteSpacing);
        for (int x = 0; x < sprites.length; x++) {
            for (int y = 0; y < sprites[x].length; y++) {
                sprites[x][y].loadFilter(filterName, spriteSheet.getSubImage(x, y), null);
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
    
    public final List<String> getFilters() {
        ArrayList<String> list = new ArrayList<>(filters.size());
        for (Filter filter : filters) {
            list.add(filter.getName());
        }
        return list;
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
