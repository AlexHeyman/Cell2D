package cell2d;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.newdawn.slick.Color;

/**
 * <p>A ColorMapFilter is a Filter that uses a Map&lt;Color,Color&gt; to replace
 * some RGB values with others in the filtered image. All pixels in the original
 * image that share their RGB value with a key in the ColorMapFilter's Map will
 * have their RGB value replaced with that of the key's value. The alpha values
 * of the Colors in the Map are irrelevant to the ColorMapFilter's behavior, and
 * the alpha values of the original image's pixels are left unchanged in the
 * filtered image.</p>
 * @author Andrew Heyman
 */
public class ColorMapFilter extends Filter {
    
    private final Map<Color,Color> colorMap;
    
    /**
     * Creates a new ColorMapFilter that uses the specified Map.
     * @param colorMap The used Map
     */
    public ColorMapFilter(Map<Color,Color> colorMap) {
        this.colorMap = new HashMap(colorMap);
    }
    
    /**
     * Returns the Map that this ColorMapFilter uses.
     * @return The Map that this ColorMapFilter uses
     */
    public final Map<Color,Color> getColorMap() {
        return new HashMap<>(colorMap);
    }
    
    @Override
    final GameImage getFilteredImage(BufferedImage bufferedImage) {
        return GameImage.getRecoloredImage(bufferedImage, colorMap);
    }
    
}
