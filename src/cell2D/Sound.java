package cell2D;

import org.newdawn.slick.SlickException;

public class Sound {
    
    private boolean loaded = false;
    private final String path;
    private org.newdawn.slick.Sound sound = null;
    
    public Sound(String path, boolean load) throws SlickException {
        this.path = path;
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
            sound = new org.newdawn.slick.Sound(path);
            return true;
        }
        return false;
    }
    
    public final boolean unload() {
        if (loaded) {
            loaded = false;
            sound = null;
            return true;
        }
        return false;
    }
    
    public final boolean isPlaying() {
        return loaded && sound.playing();
    }
    
    public final void play() {
        play(1, 1);
    }
    
    public final void play(double pitch, double volume) {
        if (loaded) {
            sound.play((float)pitch, (float)volume);
        }
    }
    
    public final void loop() {
        loop(1, 1);
    }
    
    public final void loop(double pitch, double volume) {
        if (loaded) {
            sound.loop((float)pitch, (float)volume);
        }
    }
    
    public final void stop() {
        if (loaded) {
            sound.stop();
        }
    }
    
}
