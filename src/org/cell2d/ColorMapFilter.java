package org.cell2d;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.Image;
import org.cell2d.celick.SlickException;

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
     * Constructs a ColorMapFilter with a Map that maps the specified key Color
     * to the specified value Color.
     * @param key The key Color
     * @param value The value Color
     */
    public ColorMapFilter(Color key, Color value) {
        colorMap = new HashMap<>();
        colorMap.put(key, value);
    }
    
    /**
     * Returns an unmodifiable view of the Map that this ColorMapFilter uses.
     * @return The Map that this ColorMapFilter uses
     */
    public final Map<Color,Color> getColorMap() {
        return Collections.unmodifiableMap(colorMap);
    }
    
    @Override
    final Image getFilteredImage(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Image newImage;
        Graphics newGraphics;
        try {
            newImage = new Image(width, height, image.getFilter());
            newGraphics = newImage.getGraphics();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixelColor = image.getColor(x, y);
                Color mappedColor = colorMap.get(new Color(
                        pixelColor.getR(), pixelColor.getG(), pixelColor.getB(), 1f));
                if (mappedColor != null) {
                    pixelColor = new Color(mappedColor.getR(), mappedColor.getG(),
                            mappedColor.getB(), pixelColor.getA());
                }
                newGraphics.setColor(pixelColor);
                newGraphics.fillRect(x, y, 1, 1);
            }
        }
        image.flushPixelData();
        newGraphics.flush();
        return newImage;
    }
    
}
