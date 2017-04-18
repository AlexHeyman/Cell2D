package blah;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * <p>The Assets class stores references to Filters, Sprites, SpriteSheets,
 * Animations, Sounds, and Music tracks under String names that are unique among
 * each of those types of assets. Assets must be added to the Assets class
 * manually, such as in a CellGame's initActions(), after which they can be
 * accessed at any time.</p>
 * @author Andrew Heyman
 */
public class Assets {
    
    private static final Color transparent = new Color(0, 0, 0, 0);
    private static final int transparentInt = colorToInt(transparent);
    private static final Map<String,Filter> filters = new HashMap<>();
    private static final Map<String,Sprite> sprites = new HashMap<>();
    private static final Map<String,SpriteSheet> spriteSheets = new HashMap<>();
    private static final Map<String,Animation> animations = new HashMap<>();
    private static final Map<String,Sound> sounds = new HashMap<>();
    private static final Map<String,Music> musics = new HashMap<>();
    
    private Assets() {}
    
    private static int colorToInt(Color color) {
        return (color.getAlphaByte() << 24) | (color.getRedByte() << 16) | (color.getGreenByte() << 8) | color.getBlueByte();
    }
    
    private static Color intToColor(int color) {
        return new Color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF);
    }
    
    static final GameImage getTransparentImage(String path, Color transColor) throws SlickException {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (bufferedImage.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            java.awt.Graphics bufferedGraphics = newImage.getGraphics();
            bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
            bufferedGraphics.dispose();
            bufferedImage = newImage;
        }
        Image image;
        if (transColor == null) {
            image = new Image(path);
        } else {
            image = new Image(width, height);
            Graphics graphics = image.getGraphics();
            Color color;
            int transR = transColor.getRedByte();
            int transG = transColor.getGreenByte();
            int transB = transColor.getBlueByte();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    color = intToColor(bufferedImage.getRGB(x, y));
                    if (color.getRedByte() == transR
                            && color.getGreenByte() == transG
                            && color.getBlueByte() == transB) {
                        color = transparent;
                        bufferedImage.setRGB(x, y, transparentInt);
                    }
                    graphics.setColor(color);
                    graphics.fillRect(x, y, 1, 1);
                }
            }
            graphics.flush();
        }
        image.setFilter(Image.FILTER_NEAREST);
        return new GameImage(image, bufferedImage);
    }
    
    static final GameImage getRecoloredImage(BufferedImage bufferedImage, Map<Color,Color> colorMap) throws SlickException {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        java.awt.Graphics bufferedGraphics = newImage.getGraphics();
        bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
        bufferedGraphics.dispose();
        Image image = new Image(width, height);
        image.setFilter(Image.FILTER_NEAREST);
        Graphics graphics = image.getGraphics();
        int size = colorMap.size();
        int[] oldR = new int[size];
        int[] oldG = new int[size];
        int[] oldB = new int[size];
        int[] newR = new int[size];
        int[] newG = new int[size];
        int[] newB = new int[size];
        int i = 0;
        Color key, value;
        for (Map.Entry<Color,Color> entry : colorMap.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            oldR[i] = key.getRedByte();
            oldG[i] = key.getGreenByte();
            oldB[i] = key.getBlueByte();
            newR[i] = value.getRedByte();
            newG[i] = value.getGreenByte();
            newB[i] = value.getBlueByte();
            i++;
        }
        Color color;
        int colorR, colorG, colorB;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                color = intToColor(newImage.getRGB(x, y));
                colorR = color.getRedByte();
                colorG = color.getGreenByte();
                colorB = color.getBlueByte();
                for (int j = 0; j < size; j++) {
                    if (oldR[j] == colorR && oldG[j] == colorG && oldB[j] == colorB) {
                        color = new Color(newR[j], newG[j], newB[j], color.getAlphaByte());
                        newImage.setRGB(x, y, colorToInt(color));
                        break;
                    }
                }
                graphics.setColor(color);
                graphics.fillRect(x, y, 1, 1);
            }
        }
        graphics.flush();
        return new GameImage(image, newImage);
    }
    
    static final GameImage getRecoloredImage(BufferedImage bufferedImage, Color newColor) throws SlickException {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        java.awt.Graphics bufferedGraphics = newImage.getGraphics();
        bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
        bufferedGraphics.dispose();
        Image image = new Image(width, height);
        image.setFilter(Image.FILTER_NEAREST);
        Graphics graphics = image.getGraphics();
        int newColorR = newColor.getRedByte();
        int newColorG = newColor.getGreenByte();
        int newColorB = newColor.getBlueByte();
        Color color;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                color = new Color(newColorR, newColorG, newColorB, (newImage.getRGB(x, y) >> 24) & 0xFF);
                newImage.setRGB(x, y, colorToInt(color));
                graphics.setColor(color);
                graphics.fillRect(x, y, 1, 1);
            }
        }
        graphics.flush();
        return new GameImage(image, newImage);
    }
    
    /**
     * Adds the specified Filter to the list of Filters under the specified
     * name. This method will do nothing if a Filter has already been added
     * under that name.
     * @param name The name under which the specified Filter is to be added
     * @param filter The Filter to be added to the list of Filters
     * @return Whether the addition occurred
     */
    public static final boolean add(String name, Filter filter) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a Filter to Assets under a null name");
        }
        if (filters.get(name) == null) {
            filters.put(name, filter);
            return true;
        }
        return false;
    }
    
    /**
     * Adds the specified Sprite to the list of Sprites under the specified
     * name. This method will do nothing if a Sprite has already been added
     * under that name.
     * @param name The name under which the specified Sprite is to be added
     * @param sprite The Sprite to be added to the list of Sprites
     * @return Whether the addition occurred
     */
    public static final boolean add(String name, Sprite sprite) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a Sprite to Assets under a null name");
        }
        if (sprites.get(name) == null) {
            sprites.put(name, sprite);
            return true;
        }
        return false;
    }
    
    /**
     * Adds the specified SpriteSheet to the list of SpriteSheets under the
     * specified name. This method will do nothing if a SpriteSheet has already
     * been added under that name.
     * @param name The name under which the specified SpriteSheet is to be added
     * @param spriteSheet The SpriteSheet to be added to the list of
     * SpriteSheets
     * @return Whether the addition occurred
     */
    public static final boolean add(String name, SpriteSheet spriteSheet) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a SpriteSheet to Assets under a null name");
        }
        if (spriteSheets.get(name) == null) {
            spriteSheets.put(name, spriteSheet);
            return true;
        }
        return false;
    }
    
    /**
     * Adds the specified Animation to the list of Animations under the
     * specified name. This method will do nothing if an Animation has already
     * been added under that name.
     * @param name The name under which the specified Animation is to be added
     * @param animation The Animation to be added to the list of Animations
     * @return Whether the addition occurred
     */
    public static final boolean add(String name, Animation animation) {
        if (name == null) {
            throw new RuntimeException("Attempted to add an Animation to Assets under a null name");
        }
        if (animations.get(name) == null) {
            animations.put(name, animation);
            return true;
        }
        return false;
    }
    
    /**
     * Adds the specified Sound to the list of Sounds under the specified name.
     * This method will do nothing if a Sound has already been added under that
     * name.
     * @param name The name under which the specified Sound is to be added
     * @param sound The Sound to be added to the list of Sounds
     * @return Whether the addition occurred
     */
    public static final boolean add(String name, Sound sound) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a Sound to Assets under a null name");
        }
        if (sounds.get(name) == null) {
            sounds.put(name, sound);
            return true;
        }
        return false;
    }
    
    /**
     * Adds the specified Music track to the list of Music tracks under the
     * specified name. This method will do nothing if a Music track has already
     * been added under that name.
     * @param name The name under which the specified Music track is to be added
     * @param music The Music track to be added to the list of Music tracks
     * @return Whether the addition occurred
     */
    public static final boolean add(String name, Music music) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a music track to Assets under a null name");
        }
        if (musics.get(name) == null) {
            musics.put(name, music);
            return true;
        }
        return false;
    }
    
    /**
     * Returns the Filter in the list of Filters under the specified name, or
     * null if there is none.
     * @param name The name of the Filter to return
     * @return The Filter in the list of Filters under the specified name
     */
    public static final Filter getFilter(String name) {
        return filters.get(name);
    }
    
    /**
     * Returns the Sprite in the list of Sprites under the specified name, or
     * null if there is none.
     * @param name The name of the Sprite to return
     * @return The Sprite in the list of Sprites under the specified name
     */
    public static final Sprite getSprite(String name) {
        return sprites.get(name);
    }
    
    /**
     * Returns the SpriteSheet in the list of SpriteSheets under the specified
     * name, or null if there is none.
     * @param name The name of the SpriteSheet to return
     * @return The SpriteSheet in the list of SpriteSheets under the specified
     * name
     */
    public static final SpriteSheet getSpriteSheet(String name) {
        return spriteSheets.get(name);
    }
    
    /**
     * Returns the Animation in the list of Animations under the specified name,
     * or null if there is none.
     * @param name The name of the Animation to return
     * @return The Animation in the list of Animations under the specified name
     */
    public static final Animation getAnimation(String name) {
        return animations.get(name);
    }
    
    /**
     * Returns the Sound in the list of Sounds under the specified name, or null
     * if there is none.
     * @param name The name of the Sound to return
     * @return The Sound in the list of Sounds under the specified name
     */
    public static final Sound getSound(String name) {
        return sounds.get(name);
    }
    
    /**
     * Returns the Music track in the list of Music tracks under the specified
     * name, or null if there is none.
     * @param name The name of the Music track to return
     * @return The Music track in the list of Music tracks under the specified
     * name
     */
    public static final Music getMusic(String name) {
        return musics.get(name);
    }
    
}
