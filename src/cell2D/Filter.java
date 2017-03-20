package cell2D;

import java.awt.image.BufferedImage;
import org.newdawn.slick.SlickException;

public abstract class Filter {
    
    abstract GameImage getFilteredImage(BufferedImage bufferedImage) throws SlickException;
    
}
