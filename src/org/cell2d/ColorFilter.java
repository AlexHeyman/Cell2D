package org.cell2d;

import org.celick.Color;
import java.awt.image.BufferedImage;

/**
 * <p>A ColorFilter is a Filter that blends the RGB value of each pixel in the
 * original image with that of a single Color that it uses. The alpha value of
 * the ColorFilter's Color is proportional to the strength of its influence on
 * the filtered image's RGB values. An alpha value of 0 has no effect, and an
 * alpha value of 255 completely replaces the original image's RGB values, thus
 * turning the filtered image into a colored silhouette. The alpha values of the
 * original image's pixels are left unchanged in the filtered image.</p>
 * @author Andrew Heyman
 */
public class ColorFilter extends Filter {
    
    private final Color color;
    
    /**
     * Constructs a ColorFilter that uses the specified Color.
     * @param color The used Color
     */
    public ColorFilter(Color color) {
        this.color = color;
    }
    
    /**
     * Constructs a ColorFilter that uses a Color with the specified RGBA value.
     * @param colorR The R value (0-255) of the used Color
     * @param colorG The G value (0-255) of the used Color
     * @param colorB The B value (0-255) of the used Color
     * @param colorA The alpha value (0-255) of the used Color
     */
    public ColorFilter(int colorR, int colorG, int colorB, int colorA) {
        this(new Color(colorR, colorG, colorB, colorA));
    }
    
    /**
     * Constructs a ColorFilter that uses a Color with the specified RGB value
     * and an alpha value of 255.
     * @param colorR The R value (0-255) of the used Color
     * @param colorG The G value (0-255) of the used Color
     * @param colorB The B value (0-255) of the used Color
     */
    public ColorFilter(int colorR, int colorG, int colorB) {
        this(new Color(colorR, colorG, colorB));
    }
    
    /**
     * Returns the Color that this ColorFilter uses.
     * @return The Color that this ColorFilter uses
     */
    public final Color getColor() {
        return color;
    }
    
    @Override
    final GameImage getFilteredImage(BufferedImage bufferedImage) {
        return GameImage.getRecoloredImage(bufferedImage, color);
    }
    
}
