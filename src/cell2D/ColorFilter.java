package cell2D;

import java.awt.image.BufferedImage;
import javafx.util.Pair;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

class ColorFilter extends Filter {
    
    private final Color color;
    
    ColorFilter(String name, Color color) {
        super(name);
        this.color = new Color(color);
    }
    
    final Color getColor() {
        return color;
    }
    
    @Override
    final Pair<Image,BufferedImage> getFilteredImage(BufferedImage bufferedImage) throws SlickException {
        return CellGame.getRecoloredImage(bufferedImage, color);
    }
    
}
