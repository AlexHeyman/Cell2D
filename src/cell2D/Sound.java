package cell2D;

import org.newdawn.slick.SlickException;

public class Sound {
    
    private boolean isLoaded = false;
    private final String path;
    private org.newdawn.slick.Sound sound = null;
    
    Sound(String path) {
        this.path = path;
    }
    
    public final boolean isLoaded() {
        return isLoaded;
    }
    
    public final boolean load() throws SlickException {
        if (!isLoaded) {
            isLoaded = true;
            sound = new org.newdawn.slick.Sound(path);
            return true;
        }
        return false;
    }
    
    public final boolean unload() {
        if (isLoaded) {
            isLoaded = false;
            sound = null;
            return true;
        }
        return false;
    }
    
    public final boolean isPlaying() {
        return sound.playing();
    }
    
    public final void play() {
        play(1, 1);
    }
    
    public final void play(double pitch, double volume) {
        if (isLoaded) {
            sound.play((float)pitch, (float)volume);
        }
    }
    
    public final void loop() {
        loop(1, 1);
    }
    
    public final void loop(double pitch, double volume) {
        if (isLoaded) {
            sound.loop((float)pitch, (float)volume);
        }
    }
    
    public final void stop() {
        if (isLoaded) {
            sound.stop();
        }
    }
    
}
