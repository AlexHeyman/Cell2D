package ironclad2D;

import org.newdawn.slick.SlickException;

public class Sound {
    
    private final org.newdawn.slick.Sound sound;
    
    Sound(String path) throws SlickException {
        sound = new org.newdawn.slick.Sound(path);
    }
    
    public final boolean isPlaying() {
        return sound.playing();
    }
    
    public final void play() {
        play(1, 1);
    }
    
    public final void play(double pitch, double volume) {
        sound.play((float)pitch, (float)volume);
    }
    
    public final void loop() {
        loop(1, 1);
    }
    
    public final void loop(double pitch, double volume) {
        sound.loop((float)pitch, (float)volume);
    }
    
    public final void stop() {
        sound.stop();
    }
    
}
