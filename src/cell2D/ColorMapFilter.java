package cell2D;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;

public class ColorMapFilter extends Filter {
    
    private final Map<Color,Color> colorMap;
    
    public ColorMapFilter(Map<Color,Color> colorMap) {
        this.colorMap = new HashMap(colorMap);
    }
    
    public final Map<Color,Color> getColorMap() {
        return new HashMap<>(colorMap);
    }
    
    @Override
    final GameImage getFilteredImage(BufferedImage bufferedImage) throws SlickException {
        return CellGame.getRecoloredImage(bufferedImage, colorMap);
    }
    
}
