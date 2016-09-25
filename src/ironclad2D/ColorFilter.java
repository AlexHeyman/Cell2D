package ironclad2D;

import java.awt.image.BufferedImage;
import javafx.util.Pair;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

class ColorFilter extends Filter {
    
    private final Color color;
    
    ColorFilter(String name, Color color) {
        super(name);
        this.color = color;
    }
    
    Color getColor() {
        return color;
    }
    
    @Override
    final Pair<Image,BufferedImage> getFilteredImage(BufferedImage bufferedImage) throws SlickException {
        return IroncladGame.getRecoloredImage(bufferedImage, color);
    }
    
}
