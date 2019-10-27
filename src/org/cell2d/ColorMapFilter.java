package org.cell2d;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>A ColorMapFilter is a Filter that uses a Map&lt;Color,Color&gt; to replace
 * some RGB values with others in the filtered image. For each key in the
 * ColorMapFilter's Map with an alpha value of 1, all pixels in the original
 * image that share their RGB value with that key will have their RGB value
 * replaced with that of the key's value in the Map. The Map's values' alpha
 * values are irrelevant to the ColorMapFilter's behavior, and the alpha values
 * of the original image's pixels are left unchanged in the filtered image.</p>
 * @author Alex Heyman
 */
public class ColorMapFilter extends Filter {
    
    private final Map<Color,Color> colorMap;
    
    /**
     * Constructs a ColorMapFilter that uses a copy of the specified Map.
     * @param colorMap The Map whose copy should be used
     */
    public ColorMapFilter(Map<Color,Color> colorMap) {
        this.colorMap = new HashMap<>(colorMap);
    }
    
    /**
     * Returns an unmodifiable view of the Map that this ColorMapFilter uses.
     * @return The Map that this ColorMapFilter uses
     */
    public final Map<Color,Color> getColorMap() {
        return Collections.unmodifiableMap(colorMap);
    }
    
    @Override
    final GameImage getFilteredImage(BufferedImage bufferedImage) {
        return GameImage.getRecoloredImage(bufferedImage, colorMap);
    }
    
}
