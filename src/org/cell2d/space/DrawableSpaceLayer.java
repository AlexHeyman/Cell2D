package org.cell2d.space;

import org.cell2d.Drawable;
import org.cell2d.Frac;
import org.cell2d.celick.Graphics;

/**
 * <p>A DrawableSpaceLayer is a type of SpaceLayer that displays a Drawable,
 * optionally repeated horizontally and/or vertically across the rendering
 * region. It can also be configured to shift its Drawable relative to the
 * position of the camera to simulate parallax.</p>
 * @see Drawable
 * @author Alex Heyman
 */
public class DrawableSpaceLayer implements SpaceLayer {
    
    private Drawable drawable;
    private int repeatX, repeatY;
    private long parallaxX, parallaxY, parallaxOriginX, parallaxOriginY;
    
    /**
     * Constructs a DrawableSpaceLayer.
     * @param drawable The Drawable that this DrawableSpaceLayer displays
     * @param repeatX The distance in pixels between horizontal repetitions of
     * the Drawable. If this is 0 or negative, the Drawable will not repeat
     * horizontally.
     * @param repeatY The distance in pixels between vertical repetitions of
     * the Drawable. If this is 0 or negative, the Drawable will not repeat
     * vertically.
     * @param parallaxX The number of pixels (in fracunit scale) that the
     * Drawable moves <i>left</i> for every one fracunit that the camera moves
     * <i>right</i>, and vice versa. Values between 0 and 1 are appropriate for
     * backgrounds, and values of at least 1 are appropriate for foregrounds. A
     * value of 0 will make the Drawable horizontally stationary, and a value of
     * 1 will make it match the SpaceObject layer. Negative values will make the
     * Drawable look like it is across from the SpaceObject layer on the inside
     * of a horizontally curved surface.
     * @param parallaxY The number of pixels (in fracunit scale) that the
     * Drawable moves <i>up</i> for every one fracunit that the camera moves
     * <i>down</i>, and vice versa. Values between 0 and 1 are appropriate for
     * backgrounds, and values of at least 1 are appropriate for foregrounds. A
     * value of 0 will make the Drawable vertically stationary, and a value of
     * 1 will make it match the SpaceObject layer. Negative values will make the
     * Drawable look like it is across from the SpaceObject layer on the inside
     * of a vertically curved surface.
     * @param parallaxOriginX The x position in the SpaceState's space where the
     * Drawable's origin x-coordinate aligns with the x position of the camera
     * on the rendering region. If parallaxX is 0, the above description is true
     * of any x position, and this value is meaningless.
     * @param parallaxOriginY The y position in the SpaceState's space where the
     * Drawable's origin y-coordinate aligns with the y position of the camera
     * on the rendering region. If parallaxY is 0, the above description is true
     * of any y position, and this value is meaningless.
     */
    public DrawableSpaceLayer(Drawable drawable, int repeatX, int repeatY,
            long parallaxX, long parallaxY, long parallaxOriginX, long parallaxOriginY) {
        this.drawable = drawable;
        this.repeatX = repeatX;
        this.repeatY = repeatY;
        this.parallaxX = parallaxX;
        this.parallaxY = parallaxY;
        this.parallaxOriginX = parallaxOriginX;
        this.parallaxOriginY = parallaxOriginY;
    }
    
    /**
     * Returns the Drawable that this DrawableSpaceLayer displays.
     * @return This DrawableSpaceLayer's displayed Drawable
     */
    public final Drawable getDrawable() {
        return drawable;
    }
    
    /**
     * Sets the Drawable that this DrawableSpaceLayer displays to the specified
     * Drawable.
     * @param drawable This DrawableSpaceLayer's new displayed Drawable
     */
    public final void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
    
    /**
     * Returns this DrawableSpaceLayer's horizontal repetition spacing.
     * @return This DrawableSpaceLayer's horizontal repetition spacing
     */
    public final int getRepeatX() {
        return repeatX;
    }
    
    /**
     * Sets this DrawableSpaceLayer's horizontal repetition spacing to the
     * specified value.
     * @param repeatX This DrawableSpaceLayer's new horizontal repetition
     * spacing
     */
    public final void setRepeatX(int repeatX) {
        this.repeatX = repeatX;
    }
    
    /**
     * Returns this DrawableSpaceLayer's vertical repetition spacing.
     * @return This DrawableSpaceLayer's vertical repetition spacing
     */
    public final int getRepeatY() {
        return repeatY;
    }
    
    /**
     * Sets this DrawableSpaceLayer's vertical repetition spacing to the
     * specified value.
     * @param repeatY This DrawableSpaceLayer's new vertical repetition spacing
     */
    public final void setRepeatY(int repeatY) {
        this.repeatY = repeatY;
    }
    
    /**
     * Returns this DrawableSpaceLayer's horizontal parallax.
     * @return This DrawableSpaceLayer's horizontal parallax
     */
    public long getParallaxX() {
        return parallaxX;
    }
    
    /**
     * Sets this DrawableSpaceLayer's horizontal parallax to the specified
     * value.
     * @param parallaxX This DrawableSpaceLayer's new horizontal parallax
     */
    public void setParallaxX(long parallaxX) {
        this.parallaxX = parallaxX;
    }
    
    /**
     * Returns this DrawableSpaceLayer's vertical parallax.
     * @return This DrawableSpaceLayer's vertical parallax
     */
    public long getParallaxY() {
        return parallaxY;
    }
    
    /**
     * Sets this DrawableSpaceLayer's vertical parallax to the specified value.
     * @param parallaxY This DrawableSpaceLayer's new vertical parallax
     */
    public void setParallaxY(long parallaxY) {
        this.parallaxY = parallaxY;
    }
    
    /**
     * Returns this DrawableSpaceLayer's horizontal parallax origin.
     * @return This DrawableSpaceLayer's horizontal parallax origin
     */
    public long getParallaxOriginX() {
        return parallaxOriginX;
    }
    
    /**
     * Sets this DrawableSpaceLayer's horizontal parallax origin to the
     * specified value.
     * @param parallaxOriginX This DrawableSpaceLayer's new horizontal parallax
     * origin
     */
    public void setParallaxOriginX(long parallaxOriginX) {
        this.parallaxOriginX = parallaxOriginX;
    }
    
    /**
     * Returns this DrawableSpaceLayer's vertical parallax origin.
     * @return This DrawableSpaceLayer's vertical parallax origin
     */
    public long getParallaxOriginY() {
        return parallaxOriginY;
    }
    
    /**
     * Sets this DrawableSpaceLayer's vertical parallax origin to the specified
     * value.
     * @param parallaxOriginY This DrawableSpaceLayer's new vertical parallax
     * origin
     */
    public void setParallaxOriginY(long parallaxOriginY) {
        this.parallaxOriginY = parallaxOriginY;
    }
    
    @Override
    public void renderActions(Graphics g, long cx, long cy, int x, int y, int x1, int y1, int x2, int y2) {
        int left = x + Frac.intRound(Frac.mul(parallaxOriginX - cx, parallaxX));
        int top = y + Frac.intRound(Frac.mul(parallaxOriginY - cy, parallaxY));
        int xRepeatCount, yRepeatCount;
        if (repeatX > 0) {
            left += Frac.intFloor(Frac.div((x1 - left)*Frac.UNIT, repeatX*Frac.UNIT))*repeatX;
            xRepeatCount = Frac.intCeil(Frac.div((x2 - left)*Frac.UNIT, repeatX*Frac.UNIT));
        } else {
            xRepeatCount = 1;
        }
        if (repeatY > 0) {
            top += Frac.intFloor(Frac.div((y1 - top)*Frac.UNIT, repeatY*Frac.UNIT))*repeatY;
            yRepeatCount = Frac.intCeil(Frac.div((y2 - top)*Frac.UNIT, repeatY*Frac.UNIT));
        } else {
            yRepeatCount = 1;
        }
        for (int i = 0; i < xRepeatCount; i++) {
            for (int j = 0; j < yRepeatCount; j++) {
                drawable.draw(g, left + i*repeatX, top + j*repeatY);
            }
        }
    }
    
}
