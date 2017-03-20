package cell2D;

import java.awt.image.BufferedImage;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;

public class ColorFilter extends Filter {
    
    private final Color color;
    
    public ColorFilter(Color color) {
        this.color = color;
    }
    
    public ColorFilter(int colorR, int colorG, int colorB) {
        this(new Color(colorR, colorG, colorB));
    }
    
    public final Color getColor() {
        return color;
    }
    
    @Override
    final GameImage getFilteredImage(BufferedImage bufferedImage) throws SlickException {
        return CellGame.getRecoloredImage(bufferedImage, color);
    }
    
}
