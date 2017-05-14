package cell2d;

import java.awt.image.BufferedImage;
import org.newdawn.slick.Color;

/**
 * <p>A ColorFilter is a Filter that replaces the RGB value of every pixel in
 * the original image with that of the one Color that it uses, thus turning the
 * filtered image into a colored silhouette. The alpha value of the
 * ColorFilter's Color is irrelevant to its behavior, and the alpha values of
 * the original image's pixels are left unchanged in the filtered image.</p>
 * @author Andrew Heyman
 */
public class ColorFilter extends Filter {
    
    private final Color color;
    
    /**
     * Creates a new ColorFilter that uses the specified Color.
     * @param color The used Color
     */
    public ColorFilter(Color color) {
        this.color = color;
    }
    
    /**
     * Creates a new ColorFilter that uses a Color with the specified RGB value
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
