package org.cell2d;

import org.cell2d.celick.Graphics;

/**
 * <p>A Drawable object is one that visually represents itself with an image
 * that is contextless by itself but can be drawn to a Graphics context. Points
 * on the image have x-coordinates that increase from left to right, as well as
 * y-coordinates that increase from top to bottom. The image has an origin point
 * somewhere on (or off) it around which it is flipped, rotated, and scaled.</p>
 * @author Alex Heyman
 */
public interface Drawable {
    
    /**
     * Draws this Drawable's image to the specified Graphics context.
     * @param g The Graphics context to draw the image to
     * @param x The x-coordinate on the Graphics context of the drawn image's
     * origin
     * @param y The y-coordinate on the Graphics context of the drawn image's
     * origin
     */
    void draw(Graphics g, int x, int y);
    
    /**
     * Draws this Drawable's image to the specified Graphics context.
     * @param g The Graphics context to draw the image to
     * @param x The x-coordinate on the Graphics context of the drawn image's
     * origin
     * @param y The y-coordinate on the Graphics context of the drawn image's
     * origin
     * @param xFlip If true, the drawn image is flipped along a vertical line
     * through its origin
     * @param yFlip If true, the drawn image is flipped along a horizontal line
     * through its origin
     * @param angle The angle in degrees by which to rotate the drawn image and
     * its xFlip and yFlip lines counterclockwise around its origin
     * @param alpha The drawn image's alpha (opacity) value from 0 to 1
     * @param filter The Filter to apply to the drawn image, or null if none
     * should be applied. Not every Filter has an effect on every Drawable.
     */
    void draw(Graphics g, int x, int y, boolean xFlip, boolean yFlip,
            double angle, double alpha, Filter filter);
    
    /**
     * Draws this Drawable's image to the specified Graphics context.
     * @param g The Graphics context to draw the image to
     * @param x The x-coordinate on the Graphics context of the drawn image's
     * origin
     * @param y The y-coordinate on the Graphics context of the drawn image's
     * origin
     * @param scale The factor by which to scale the drawn image around its
     * origin
     * @param xFlip If true, the drawn image is flipped along a vertical line
     * through its origin
     * @param yFlip If true, the drawn image is flipped along a horizontal line
     * through its origin
     * @param alpha The drawn image's alpha (opacity) value from 0 to 1
     * @param filter The Filter to apply to the drawn image, or null if none
     * should be applied. Not every Filter has an effect on every Drawable.
     */
    void draw(Graphics g, int x, int y, double scale,
            boolean xFlip, boolean yFlip, double alpha, Filter filter);
    
    /**
     * Draws a rectangular region of this Drawable's image to the specified
     * Graphics context.
     * @param g The Graphics context to draw the region to
     * @param x The x-coordinate on the Graphics context of the image's origin
     * @param y The y-coordinate on the Graphics context of the image's origin
     * @param left The x-coordinate on the image, relative to its origin, of the
     * region's left edge
     * @param right The x-coordinate on the image, relative to its origin, of
     * the region's right edge
     * @param top The y-coordinate on the image, relative to its origin, of the
     * region's top edge
     * @param bottom The y-coordinate on the image, relative to its origin, of
     * the region's bottom edge
     */
    void draw(Graphics g, int x, int y, int left, int right, int top, int bottom);
    
    /**
     * Draws a rectangular region of this Drawable's image to the specified
     * Graphics context.
     * @param g The Graphics context to draw the region to
     * @param x The x-coordinate on the Graphics context of the image's origin
     * @param y The y-coordinate on the Graphics context of the image's origin
     * @param left The x-coordinate on the image, relative to its origin, of the
     * region's left edge
     * @param right The x-coordinate on the image, relative to its origin, of
     * the region's right edge
     * @param top The y-coordinate on the image, relative to its origin, of the
     * region's top edge
     * @param bottom The y-coordinate on the image, relative to its origin, of
     * the region's bottom edge
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
    void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter);
    
    /**
     * Draws a rectangular region of this Drawable's image to the specified
     * Graphics context.
     * @param g The Graphics context to draw the region to
     * @param x The x-coordinate on the Graphics context of the image's origin
     * @param y The y-coordinate on the Graphics context of the image's origin
     * @param left The x-coordinate on the image, relative to its origin, of the
     * region's left edge
     * @param right The x-coordinate on the image, relative to its origin, of
     * the region's right edge
     * @param top The y-coordinate on the image, relative to its origin, of the
     * region's top edge
     * @param bottom The y-coordinate on the image, relative to its origin, of
     * the region's bottom edge
     * @param scale The factor by which to scale the drawn region around the
     * image's origin
     * @param xFlip If true, the drawn region is flipped along a vertical line
     * through the image's origin
     * @param yFlip If true, the drawn region is flipped along a horizontal line
     * through the image's origin
     * @param alpha The drawn region's alpha (opacity) value from 0 to 1
     * @param filter The Filter to apply to the drawn region, or null if none
     * should be applied. Not every Filter has an effect on every Drawable.
     */
    void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter);
    
}
