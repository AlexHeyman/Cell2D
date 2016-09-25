package ironclad2D;

import java.awt.image.BufferedImage;
import java.util.Map;
import javafx.util.Pair;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

class ColorMapFilter extends Filter {
    
    private final Map<Color,Color> colorMap;
    
    ColorMapFilter(String name, Map<Color,Color> colorMap) {
        super(name);
        this.colorMap = colorMap;
    }
    
    Map<Color,Color> getColorMap() {
        return colorMap;
    }
    
    @Override
    final Pair<Image,BufferedImage> getFilteredImage(BufferedImage bufferedImage) throws SlickException {
        return IroncladGame.getRecoloredImage(bufferedImage, colorMap);
    }
    
}
