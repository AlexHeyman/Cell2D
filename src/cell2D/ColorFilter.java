package cell2D;

import java.awt.image.BufferedImage;
import org.newdawn.slick.Color;
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
    final GameImage getFilteredImage(BufferedImage bufferedImage) throws SlickException {
        return CellGame.getRecoloredImage(bufferedImage, color);
    }
    
}
