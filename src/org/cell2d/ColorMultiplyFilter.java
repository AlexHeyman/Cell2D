package org.cell2d;

import java.util.Objects;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.Image;
import org.cell2d.celick.SlickException;

/**
 * <p>A ColorMultiplyFilter is a Filter that multiplies the red, green, blue,
 * and alpha values of each pixel in the original image with those of a single
 * Color that it uses. For the purposes of this multiplication, the RGBA values
 * are all treated as floating-point values between 0 and 1. Thus, a
 * ColorMultiplyFilter can tint or darken an image, but not brighten it.</p>
 * @see Color
 * @author Alex Heyman
 */
public class ColorMultiplyFilter implements Filter {
    
    private final Color color;
    
    /**
     * Constructs a ColorMultiplyFilter that uses the specified Color.
     * @param color The used Color
     */
    public ColorMultiplyFilter(Color color) {
        this.color = color;
    }
    
    @Override
    public final int hashCode() {
        return Objects.hash("ColorMultiplyFilter", color);
    }
    
    /**
     * Returns whether the specified object is a ColorMultiplyFilter that is
     * equal to this ColorMultiplyFilter. Two ColorMultiplyFilters are equal if
     * and only if the Colors they use are equal.
     * @param obj The object to be compared with this ColorMultiplyFilter
     * @return Whether the specified object is a ColorMultiplyFilter that is
     * equal to this ColorMultiplyFilter
     */
    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof ColorMultiplyFilter) {
            return color.equals(((ColorMultiplyFilter)obj).color);
        }
        return false;
    }
    
    /**
     * Returns the Color that this ColorMultiplyFilter uses.
     * @return The Color that this ColorMultiplyFilter uses
     */
    public final Color getColor() {
        return color;
    }
    
    @Override
    public final Image getFilteredImage(Image image) {
        Image newImage;
        Graphics newGraphics;
        try {
            newImage = image.getBlankCopy();
            newGraphics = newImage.getGraphics();
        } catch (SlickException e) {
            throw new RuntimeException(e);
        }
        newGraphics.setDrawMode(Graphics.MODE_SCREEN);
        float rotation = image.getRotation();
        float alpha = image.getAlpha();
        image.setRotation(0);
        image.setAlpha(1);
        newGraphics.drawImage(image, 0, 0, color);
        newGraphics.flush();
        image.setRotation(rotation);
        image.setAlpha(alpha);
        return newImage;
    }
    
}
