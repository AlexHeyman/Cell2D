package cell2D;

import java.util.HashMap;
import java.util.Map;

/**
 * The Assets class stores references to Filters, Sprites, SpriteSheets,
 * Animations, Sounds, and Music tracks under String names that are unique among
 * each of those types of assets. Assets must be added to the Assets class
 * manually, such as in the main() method after CellGame.loadNatives() has been
 * called, after which they can be accessed at any time.
 * @author Andrew Heyman
 */
public class Assets {
    
    private static final Map<String,Filter> filters = new HashMap<>();
    private static final Map<String,Sprite> sprites = new HashMap<>();
    private static final Map<String,SpriteSheet> spriteSheets = new HashMap<>();
    private static final Map<String,Animation> animations = new HashMap<>();
    private static final Map<String,Sound> sounds = new HashMap<>();
    private static final Map<String,Music> musics = new HashMap<>();
    
    /**
     * Adds the specified Filter to the list of Filters under the specified
     * name. This method will do nothing if a Filter has already been added
     * under that name.
     * @param name The name under which the specified Filter is to be added
     * @param filter The Filter to be added to the list of Filters
     * @return Whether the addition was successful
     */
    public static final boolean add(String name, Filter filter) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a Filter with a null name");
        }
        if (filters.get(name) != null) {
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
     * @return Whether the addition was successful
     */
    public static final boolean add(String name, Sprite sprite) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a Sprite with a null name");
        }
        if (sprites.get(name) != null) {
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
     * @return Whether the addition was successful
     */
    public static final boolean add(String name, SpriteSheet spriteSheet) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a SpriteSheet with a null name");
        }
        if (spriteSheets.get(name) != null) {
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
     * @return Whether the addition was successful
     */
    public static final boolean add(String name, Animation animation) {
        if (name == null) {
            throw new RuntimeException("Attempted to add an Animation with a null name");
        }
        if (animations.get(name) != null) {
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
     * @return Whether the addition was successful
     */
    public static final boolean add(String name, Sound sound) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a Sound with a null name");
        }
        if (sounds.get(name) != null) {
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
     * @return Whether the addition was successful
     */
    public static final boolean add(String name, Music music) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a music track with a null name");
        }
        if (musics.get(name) != null) {
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
