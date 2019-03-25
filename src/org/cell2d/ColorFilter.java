package org.cell2d;

import java.awt.image.BufferedImage;

/**
 * <p>A ColorFilter is a Filter that blends the RGB value of each pixel in the
 * original image with that of a single Color that it uses. The alpha value of
 * the ColorFilter's Color is proportional to the strength of its influence on
 * the filtered image's RGB values. An alpha value of 0 has no effect, and an
 * alpha value of 1 completely replaces the original image's RGB values, thus
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
