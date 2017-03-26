package cell2D;

import org.newdawn.slick.SlickException;

/**
 * <p>A Sound is a sound effect. Like Sprites, SpriteSheets, and Music tracks,
 * Sounds can be manually loaded and unloaded into and out of memory. Loading
 * may take a moment, but while a Sound is not loaded, it cannot play.</p>
 * @author Andrew Heyman
 */
public class Sound {
    
    private boolean loaded = false;
    private final String path;
    private org.newdawn.slick.Sound sound = null;
    
    /**
     * Creates a new Sound from an audio file. Files of WAV, OGG, MOD, and XM
     * formats are supported.
     * @param path The relative path to the audio file
     * @param load Whether this Sound should load upon creation
     * @throws SlickException If the Sound could not be properly loaded from the
     * specified path
     */
    public Sound(String path, boolean load) throws SlickException {
        this.path = path;
        if (load) {
            load();
        }
    }
    
    /**
     * Returns whether this Sound is loaded.
     * @return Whether this Sound is loaded
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Loads this Sound if it is not already loaded.
     * @return Whether the loading occurred
     * @throws SlickException If the Sound could not be properly loaded
     */
    public final boolean load() throws SlickException {
        if (!loaded) {
            loaded = true;
            sound = new org.newdawn.slick.Sound(path);
            return true;
        }
        return false;
    }
    
    /**
     * Unloads this Sound if it is currently loaded.
     * @return Whether the unloading occurred
     */
    public final boolean unload() {
        if (loaded) {
            loaded = false;
            sound.stop();
            sound = null;
            return true;
        }
        return false;
    }
    
    /**
     * Returns whether this Sound is currently playing.
     * @return Whether this Sound is currently playing
     */
    public final boolean isPlaying() {
        return loaded && sound.playing();
    }
    
    /**
     * Plays this Sound once.
     */
    public final void play() {
        play(1, 1);
    }
    
    /**
     * Plays this Sound once at the specified pitch and volume.
     * @param pitch The pitch at which to play this Sound, with 1 representing
     * no pitch change
     * @param volume The volume at which to play this Sound, with 1 representing
     * no volume change
     */
    public final void play(double pitch, double volume) {
        if (loaded) {
            sound.play((float)pitch, (float)volume);
        }
    }
    
    /**
     * Loops this Sound indefinitely.
     */
    public final void loop() {
        loop(1, 1);
    }
    
    /**
     * Loops this Sound indefinitely at the specified pitch and volume.
     * @param pitch The pitch at which to play this Sound, with 1 representing
     * no pitch change
     * @param volume The volume at which to play this Sound, with 1 representing
     * no volume change
     */
    public final void loop(double pitch, double volume) {
        if (loaded) {
            sound.loop((float)pitch, (float)volume);
        }
    }
    
    /**
     * Stops this Sound.
     */
    public final void stop() {
        if (loaded) {
            sound.stop();
        }
    }
    
}
