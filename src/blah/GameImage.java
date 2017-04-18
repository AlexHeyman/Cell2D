package blah;

import java.awt.image.BufferedImage;
import org.newdawn.slick.Image;

class GameImage {
    
    private final Image image;
    private final BufferedImage bufferedImage;
    
    GameImage(Image image, BufferedImage bufferedImage) {
        this.image = image;
        this.bufferedImage = bufferedImage;
    }
    
    final Image getImage() {
        return image;
    }
    
    final BufferedImage getBufferedImage() {
        return bufferedImage;
    }
    
}
