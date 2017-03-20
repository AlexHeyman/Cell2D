package cell2D;

import org.newdawn.slick.SlickException;

public class Music {
    
    public static final Music BLANK = new Music();
    
    private final boolean blank;
    private boolean loaded = false;
    private final String path;
    org.newdawn.slick.Music music = null;
    
    public Music() {
        blank = true;
        loaded = true;
        path = null;
    }
    
    public Music(String path, boolean load) throws SlickException {
        blank = false;
        this.path = path;
        if (load) {
            load();
        }
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
        music = new org.newdawn.slick.Music(path);
        return true;
    }
    
    public final boolean unload() {
        if (blank) {
            return true;
        } else if (loaded) {
            return false;
        }
        loaded = false;
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
