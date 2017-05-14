package cell2d;

/**
 * <p>A Music track is a piece of music that can be played in the background of
 * a CellGame. Because a CellGame must ensure that it is never playing more than
 * one Music track at a time, all playing, looping, stopping, pausing, and
 * fading of Music tracks is controlled by methods of the CellGame.</p>
 * 
 * <p>Like Sprites, SpriteSheets, and Sounds, Music tracks can be manually
 * loaded and unloaded into and out of memory. Loading may take a moment, but
 * while a Music track is not loaded, it cannot play.</p>
 * @author Andrew Heyman
 */
public class Music {
    
    /**
     * A blank Music track that produces no sound and plays indefinitely.
     */
    public static final Music BLANK = new Music();
    
    private final boolean blank;
    private boolean loaded = false;
    private final String path;
    private Audio audio = null;
    
    private Music() {
        blank = true;
        loaded = true;
        path = null;
    }
    
    /**
     * Creates a new Music track from an audio file. Files of WAV, OGG, and
     * AIF(F) formats are supported.
     * @param path The relative path to the audio file
     * @param load Whether this Music track should load upon creation
     */
    public Music(String path, boolean load) {
        blank = false;
        this.path = path;
        if (load) {
            load();
        }
    }
    
    /**
     * Returns whether this Music track is loaded.
     * @return Whether this Music track is loaded
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Loads this Music track if it is not already loaded.
     * @return Whether the loading occurred
     */
    public final boolean load() {
        if (blank) {
            return true;
        } else if (loaded) {
            return false;
        }
        loaded = true;
        audio = new Audio(path);
        return true;
    }
    
    /**
     * Unloads this Music track if it is currently loaded.
     * @return Whether the unloading was successful
     */
    public final boolean unload() {
        if (blank) {
            return true;
        } else if (loaded) {
            return false;
        }
        loaded = false;
        audio.unload();
        audio = null;
        return true;
    }
    
    final boolean isPlaying() {
        return (blank ? true : loaded && audio.isPlaying());
    }
    
    final void play(double pitch, double volume) {
        if (audio != null) {
            audio.play((float)pitch, (float)volume, false);
        }
    }
    
    final void loop(double pitch, double volume) {
        if (audio != null) {
            audio.play((float)pitch, (float)volume, true);
        }
    }
    
    final void stop() {
        if (audio != null) {
            audio.stop();
        }
    }
    
    final double getPosition() {
        return (audio == null ? 0 : audio.getPosition());
    }
    
    final void setPosition(double position) {
        if (audio != null) {
            audio.setPosition((float)position);
        }
    }
    
    final void setVolume(double volume) {
        if (audio != null) {
            audio.setVolume((float)volume);
        }
    }
    
}
