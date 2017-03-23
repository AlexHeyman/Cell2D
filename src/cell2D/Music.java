package cell2D;

import org.newdawn.slick.SlickException;

/**
 * A Music track is a piece of music that can be played in the background of a
 * CellGame. Because a CellGame must ensure that it is never playing more than
 * one Music track at a time, all playing, looping, stopping, pausing, and
 * fading of Music tracks is controlled by methods of the CellGame.
 * 
 * Like Sprites, SpriteSheets, and Sounds, Music tracks can be manually loaded
 * and unloaded into and out of memory. Loading may take a moment, but while a
 * Music track is not loaded, it cannot play.
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
    private org.newdawn.slick.Music music = null;
    
    private Music() {
        blank = true;
        loaded = true;
        path = null;
    }
    
    /**
     * Creates a new Music track.
     * @param path The relative path to the audio file from which this Music
     * track is created
     * @param load Whether this Music track should load upon creation
     * @throws SlickException If the Music track could not be properly loaded
     * from the specified path
     */
    public Music(String path, boolean load) throws SlickException {
        blank = false;
        this.path = path;
        if (load) {
            load();
        }
    }
    
    /**
     * Returns whether or not this Music track is loaded.
     * @return Whether or not this Music track is loaded
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Loads this Music track if it is not already loaded.
     * @return Whether the loading occurred
     * @throws SlickException If the Music track could not be properly loaded
     * from the specified path
     */
    public final boolean load() throws SlickException {
        if (blank) {
            return true;
        } else if (loaded) {
            return false;
        }
        loaded = true;
        music = new org.newdawn.slick.Music(path);
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
        music.stop();
        music = null;
        return true;
    }
    
    final boolean isPlaying() {
        return (blank ? true : loaded && music.playing());
    }
    
    final void play(double pitch, double volume) {
        if (music != null) {
            music.play((float)pitch, (float)volume);
        }
    }
    
    final void loop(double pitch, double volume) {
        if (music != null) {
            music.loop((float)pitch, (float)volume);
        }
    }
    
    final void stop() {
        if (music != null) {
            music.stop();
        }
    }
    
    final float getPosition() {
        return (music == null ? 0 : music.getPosition());
    }
    
    final void setPosition(double position) {
        if (music != null) {
            music.setPosition((float)position);
        }
    }
    
    final float getVolume() {
        return (music == null ? 0 : music.getVolume());
    }
    
    final void setVolume(double volume) {
        if (music != null) {
            music.setVolume((float)volume);
        }
    }
    
}
