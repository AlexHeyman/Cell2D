package ironclad2D;

import java.awt.image.BufferedImage;
import javafx.util.Pair;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

abstract class Filter {
    
    private final String name;
    
    Filter(String name) {
        this.name = name;
    }
    
    final String getName() {
        return name;
    }
    
    abstract Pair<Image,BufferedImage> getFilteredImage(BufferedImage bufferedImage) throws SlickException;
    
}
