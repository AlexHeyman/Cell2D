package cell2d;

/**
 * <p>A Sound is a sound effect. Like Sprites, SpriteSheets, and Music tracks,
 * Sounds can be manually loaded and unloaded into and out of memory. Loading
 * may take a moment, but while a Sound is not loaded, it cannot play.</p>
 * @author Andrew Heyman
 */
public class Sound {
    
    private boolean loaded = false;
    private final String path;
    private Audio audio = null;
    
    /**
     * Creates a new Sound from an audio file. Files of WAV, OGG, and AIF(F)
     * formats are supported.
     * @param path The relative path to the audio file
     * @param load Whether this Sound should load upon creation
     */
    public Sound(String path, boolean load) {
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
     */
    public final boolean load() {
        if (!loaded) {
            loaded = true;
            audio = new Audio(path);
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
            audio.unload();
            audio = null;
            return true;
        }
        return false;
    }
    
    /**
     * Returns whether this Sound is currently playing.
     * @return Whether this Sound is currently playing
     */
    public final boolean isPlaying() {
        return loaded && audio.isPlaying();
    }
    
    /**
     * Plays this Sound.
     * @param loop If true, this Sound will loop indefinitely until stopped;
     * otherwise, it will play once
     */
    public final void play(boolean loop) {
        play(1, 1, loop);
    }
    
    /**
     * Plays this Sound at the specified speed and volume.
     * @param speed The speed at which to play this Sound, with 1 representing
     * no speed change
     * @param volume The volume at which to play this Sound, with 1 representing
     * no volume change
     * @param loop If true, this Sound will loop indefinitely until stopped;
     * otherwise, it will play once
     */
    public final void play(double speed, double volume, boolean loop) {
        if (loaded) {
            audio.play(speed, volume, loop);
        }
    }
    
    /**
     * Plays this Sound once.
     */
    public final void play() {
        play(1, 1, false);
    }
    
    /**
     * Plays this Sound once at the specified speed and volume.
     * @param speed The speed at which to play this Sound, with 1 representing
     * no speed change
     * @param volume The volume at which to play this Sound, with 1 representing
     * no volume change
     */
    public final void play(double speed, double volume) {
        play(speed, volume, false);
    }
    
    /**
     * Stops this Sound.
     */
    public final void stop() {
        if (loaded) {
            audio.stop();
        }
    }
    
}
