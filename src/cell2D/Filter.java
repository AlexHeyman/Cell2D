package cell2D;

import java.awt.image.BufferedImage;
import org.newdawn.slick.SlickException;

abstract class Filter {
    
    private final String name;
    
    Filter(String name) {
        this.name = name;
    }
    
    final String getName() {
        return name;
    }
    
    abstract GameImage getFilteredImage(BufferedImage bufferedImage) throws SlickException;
    
}
