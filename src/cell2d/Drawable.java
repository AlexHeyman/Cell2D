package cell2d;

import org.newdawn.slick.Graphics;

/**
 * <p>A Drawable object is one that visually represents itself with an image
 * that is contextless by itself but can be drawn to a Graphics context. Points
 * on the image have x-coordinates, which are 0 at its left edge and increase
 * from left to right, and y-coordinates, which are 0 at its top edge and
 * increase from top to bottom. The drawn image has an origin point somewhere on
 * (or off) it around which it is flipped, rotated, and scaled.</p>
 * @author Andrew Heyman
 */
public interface Drawable {
    
    /**
     * Draws this Drawable to the specified Graphics context.
     * @param g The Graphics context to draw this Drawable to
     * @param x The x-coordinate on the Graphics context of the origin
     * @param y The y-coordinate on the Graphics context of the origin
     */
    public abstract void draw(Graphics g, int x, int y);
    
    /**
     * Draws this Drawable to the specified Graphics context.
     * @param g The Graphics context to draw this Drawable to
     * @param x The x-coordinate on the Graphics context of the origin
     * @param y The y-coordinate on the Graphics context of the origin
     * @param xFlip If true, the drawn image is flipped along a vertical line
     * through the origin
     * @param yFlip If true, the drawn image is flipped along a horizontal line
     * through the origin
     * @param angle The angle in degrees by which to rotate the drawn image and
     * its xFlip and yFlip lines counterclockwise around the origin
     * @param alpha The drawn image's alpha (opacity) value from 0 to 1
     * @param filter The Filter to apply to the drawn image, or null if none
     * should be applied. Not every Filter has an effect on every Drawable.
     */
    public abstract void draw(Graphics g, int x, int y,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter);
    
    /**
     * Draws this Drawable to the specified Graphics context.
     * @param g The Graphics context to draw this Drawable to
     * @param x The x-coordinate on the Graphics context of the origin
     * @param y The y-coordinate on the Graphics context of the origin
     * @param scale The factor by which to scale the drawn image around the
     * origin
     * @param xFlip If true, the drawn image is flipped along a vertical line
     * through the origin
     * @param yFlip If true, the drawn image is flipped along a horizontal line
     * through the origin
     * @param alpha The drawn image's alpha (opacity) value from 0 to 1
     * @param filter The Filter to apply to the drawn image, or null if none
     * should be applied. Not every Filter has an effect on every Drawable.
     */
    public abstract void draw(Graphics g, int x, int y, double scale,
            boolean xFlip, boolean yFlip, double alpha, Filter filter);
    
    /**
     * Draws a rectangular region of this Drawable to the specified Graphics
     * context.
     * @param g The Graphics context to draw this Drawable to
     * @param x The x-coordinate on the Graphics context of the origin
     * @param y The y-coordinate on the Graphics context of the origin
     * @param left The x-coordinate on the image, relative to the origin, of the
     * drawn region's left edge
     * @param right The x-coordinate on the image, relative to the origin, of
     * the drawn region's right edge
     * @param top The y-coordinate on the image, relative to the origin, of the
     * drawn region's top edge
     * @param bottom The y-coordinate on the image, relative to the origin, of
     * the drawn region's bottom edge
     */
    public abstract void draw(Graphics g, int x, int y, int left, int right, int top, int bottom);
    
    /**
     * Draws a rectangular region of this Drawable to the specified Graphics
     * context.
     * @param g The Graphics context to draw this Drawable to
     * @param x The x-coordinate on the Graphics context of the origin
     * @param y The y-coordinate on the Graphics context of the origin
     * @param left The x-coordinate on the image, relative to the origin, of the
     * drawn region's left edge
     * @param right The x-coordinate on the image, relative to the origin, of
     * the drawn region's right edge
     * @param top The y-coordinate on the image, relative to the origin, of the
     * drawn region's top edge
     * @param bottom The y-coordinate on the image, relative to the origin, of
     * the drawn region's bottom edge
     * @param xFlip If true, the drawn region is flipped along a vertical line
     * through the image's origin
     * @param yFlip If true, the drawn region is flipped along a horizontal line
     * through the image's origin
     * @param angle The angle in degrees by which to rotate the drawn region and
     * its xFlip and yFlip lines counterclockwise around the image's origin
     * @param alpha The drawn region's alpha (opacity) value from 0 to 1
     * @param filter The Filter to apply to the drawn region, or null if none
     * should be applied. Not every Filter has an effect on every Drawable.
     */
    public abstract void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter);
    
    /**
     * Draws a rectangular region of this Drawable to the specified Graphics
     * context.
     * @param g The Graphics context to draw this Drawable to
     * @param x The x-coordinate on the Graphics context of the origin
     * @param y The y-coordinate on the Graphics context of the origin
     * @param left The x-coordinate on the image, relative to the origin, of the
     * drawn region's left edge
     * @param right The x-coordinate on the image, relative to the origin, of
     * the drawn region's right edge
     * @param top The y-coordinate on the image, relative to the origin, of the
     * drawn region's top edge
     * @param bottom The y-coordinate on the image, relative to the origin, of
     * the drawn region's bottom edge
     * @param scale The factor by which to scale the drawn region around the
     * origin
     * @param xFlip If true, the drawn region is flipped along a vertical line
     * through the image's origin
     * @param yFlip If true, the drawn region is flipped along a horizontal line
     * through the image's origin
     * @param alpha The drawn region's alpha (opacity) value from 0 to 1
     * @param filter The Filter to apply to the drawn region, or null if none
     * should be applied. Not every Filter has an effect on every Drawable.
     */
    public abstract void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter);
    
}
