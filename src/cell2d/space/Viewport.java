package cell2d.space;

import cell2d.CellGame;
import cell2d.Frac;

/**
 * <p>A Viewport is a SpaceThinker that represents a rectangular region of the
 * screen through which the space of the SpaceState to which it is assigned can
 * be viewed. The center of a Viewport's rectangular field of view in its
 * SpaceState is marked by a SpaceObject called the Viewport's camera. To render
 * any visuals, including its HUD if it has one, a Viewport must be assigned to
 * a SpaceState through its setViewport() method. To render the region of its
 * SpaceState in its field of view, a Viewport's camera must be assigned to the
 * same SpaceState as it. One pixel in a Viewport's rendering region on the
 * screen is equal to one fracunit in its SpaceState.</p>
 * 
 * <p>While a Viewport is rendering visuals, the region of the Graphics context
 * to which it is rendering that is outside its rendering region cannot be drawn
 * to. When drawn to the Graphics context, shapes and Drawables will
 * automatically be clipped so that they do not extend beyond the rendering
 * region.</p>
 * 
 * <p>HUDs may be assigned to Viewports to render visuals in front of the
 * Viewport's own. To render visuals, an HUD must be assigned to a Viewport
 * through its setHUD() method. Only one HUD may be assigned to a given Viewport
 * in this way at once. A Viewport's HUD uses the region of the screen that the
 * Viewport occupies as its rendering region.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the SpaceStates that this Viewport
 * can be assigned to
 */
public class Viewport<T extends CellGame> extends SpaceThinker<T> {
    
    private SpaceObject<T> camera = null;
    private HUD<T> hud = null;
    private long x1, y1, x2, y2;
    int roundX1, roundY1, roundX2, roundY2, left, right, top, bottom;
    
    /**
     * Creates a new Viewport that occupies the specified region of the screen.
     * @param x1 The x-coordinate in pixels of this Viewport's left edge on the
     * screen
     * @param y1 The y-coordinate in pixels of this Viewport's top edge on the
     * screen
     * @param x2 The x-coordinate in pixels of this Viewport's right edge on the
     * screen
     * @param y2 The y-coordinate in pixels of this Viewport's bottom edge on
     * the screen
     */
    public Viewport(long x1, long y1, long x2, long y2) {
        if (x1 > x2) {
            throw new RuntimeException("Attempted to give a Viewport a negative width");
        }
        if (y1 > y2) {
            throw new RuntimeException("Attempted to give a Viewport a negative height");
        }
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        roundX1 = Frac.toInt(x1);
        roundY1 = Frac.toInt(y1);
        roundX2 = Frac.toInt(x2);
        roundY2 = Frac.toInt(y2);
        updateXData();
        updateYData();
    }
    
    private void updateXData() {
        left = -(int)Math.round((roundX2 - roundX1)/2.0);
        right = left + roundX2 - roundX1;
    }
    
    private void updateYData() {
        top = -(int)Math.round((roundY2 - roundY1)/2.0);
        bottom = top + roundY2 - roundY1;
    }
    
    /**
     * Returns this Viewport's camera, or null if it has none.
     * @return This Viewport's camera
     */
    public final SpaceObject<T> getCamera() {
        return camera;
    }
    
    /**
     * Sets this Viewport's camera to the specified SpaceObject, or to none if
     * the specified SpaceObject is null.
     * @param camera The new camera
     */
    public final void setCamera(SpaceObject<T> camera) {
        this.camera = camera;
    }
    
    /**
     * Returns the HUD that is assigned to this Viewport, or null if there is
     * none.
     * @return This Viewport's HUD
     */
    public final HUD<T> getHUD() {
        return hud;
    }
    
    /**
     * Sets the HUD that is assigned to this Viewport to the specified HUD, if
     * it is not already assigned to a ThinkerGroup. If there is already an HUD
     * assigned to this Viewport, it will be removed. If the specified HUD is
     * null, the current HUD will be removed if there is one, but it will not be
     * replaced with anything.
     * @param hud The HUD to add
     * @return Whether the change occurred
     */
    public final boolean setHUD(HUD<T> hud) {
        if (hud == null || addThinker(hud)) {
            if (this.hud != null) {
                removeThinker(this.hud);
            }
            this.hud = hud;
            return true;
        }
        return false;
    }
    
    /**
     * Returns the x-coordinate in fracunits of this Viewport's left edge on the
     * screen.
     * @return The x-coordinate in fracunits of this Viewport's left edge
     */
    public final long getX1() {
        return x1;
    }
    
    /**
     * Sets the x-coordinate in fracunits of this Viewport's left edge on the
     * screen to the specified value, if doing so would not cause this
     * Viewport's width to be negative.
     * @param x1 The new x-coordinate in fracunits of this Viewport's left edge
     * @return Whether the change occurred
     */
    public final boolean setX1(long x1) {
        if (x1 <= x2) {
            this.x1 = x1;
            roundX1 = Frac.toInt(x1);
            updateXData();
            return true;
        }
        return false;
    }
    
    /**
     * Returns the y-coordinate in fracunits of this Viewport's top edge on the
     * screen.
     * @return The y-coordinate in fracunits of this Viewport's top edge
     */
    public final long getY1() {
        return y1;
    }
    
    /**
     * Sets the y-coordinate in fracunits of this Viewport's top edge on the
     * screen to the specified value, if doing so would not cause this
     * Viewport's height to be negative.
     * @param y1 The new y-coordinate in fracunits of this Viewport's top edge
     * @return Whether the change occurred
     */
    public final boolean setY1(long y1) {
        if (y1 <= y2) {
            this.y1 = y1;
            roundY1 = Frac.toInt(y1);
            updateYData();
            return true;
        }
        return false;
    }
    
    /**
     * Returns the x-coordinate in fracunits of this Viewport's right edge on
     * the screen.
     * @return The x-coordinate in fracunits of this Viewport's right edge
     */
    public final long getX2() {
        return x2;
    }
    
    /**
     * Sets the x-coordinate in fracunits of this Viewport's right edge on the
     * screen to the specified value, if doing so would not cause this
     * Viewport's width to be negative.
     * @param x2 The new x-coordinate in fracunits of this Viewport's right edge
     * @return Whether the change occurred
     */
    public final boolean setX2(long x2) {
        if (x1 <= x2) {
            this.x2 = x2;
            roundX2 = Frac.toInt(x2);
            updateXData();
            return true;
        }
        return false;
    }
    
    /**
     * Returns the y-coordinate in fracunits of this Viewport's bottom edge on
     * the screen.
     * @return The y-coordinate in fracunits of this Viewport's bottom edge
     */
    public final long getY2() {
        return y2;
    }
    
    /**
     * Sets the y-coordinate in fracunits of this Viewport's bottom edge on the
     * screen to the specified value, if doing so would not cause this
     * Viewport's height to be negative.
     * @param y2 The new y-coordinate in fracunits of this Viewport's bottom
     * edge
     * @return Whether the change occurred
     */
    public final boolean setY2(long y2) {
        if (y1 <= y2) {
            this.y2 = y2;
            roundY2 = Frac.toInt(y2);
            updateYData();
            return true;
        }
        return false;
    }
    
    /**
     * Returns this Viewport's width in fracunits on the screen.
     * @return This Viewport's width in fracunits
     */
    public final long getWidth() {
        return x2 - x1;
    }
    
    /**
     * Returns this Viewport's height in fracunits on the screen.
     * @return This Viewport's height in fracunits
     */
    public final long getHeight() {
        return y2 - y1;
    }
    
    /**
     * Returns the x-coordinate in pixels of this Viewport's rendering region's
     * actual left edge on the screen.
     * @return The x-coordinate in pixels of this Viewport's rendering region's
     * actual left edge
     */
    public final int getLeftEdge() {
        return roundX1;
    }
    
    /**
     * Returns the x-coordinate in pixels of this Viewport's rendering region's
     * actual right edge on the screen.
     * @return The x-coordinate in pixels of this Viewport's rendering region's
     * actual right edge
     */
    public final int getRightEdge() {
        return roundX2;
    }
    
    /**
     * Returns the y-coordinate in pixels of this Viewport's rendering region's
     * actual top edge on the screen.
     * @return The y-coordinate in pixels of this Viewport's rendering region's
     * actual top edge
     */
    public final int getTopEdge() {
        return roundY1;
    }
    
    /**
     * Returns the y-coordinate in pixels of this Viewport's rendering region's
     * actual bottom edge on the screen.
     * @return The y-coordinate in pixels of this Viewport's rendering region's
     * actual bottom edge
     */
    public final int getBottomEdge() {
        return roundY2;
    }
    
    /**
     * Returns the difference of the x-coordinates in pixels of this Viewport's
     * left edge and the position of its camera on the screen.
     * @return This Viewport's left-side difference
     */
    public final int getLeft() {
        return left;
    }
    
    /**
     * Returns the difference of the x-coordinates in pixels of this Viewport's
     * right edge and the position of its camera on the screen.
     * @return This Viewport's right-side difference
     */
    public final int getRight() {
        return right;
    }
    
    /**
     * Returns the difference of the y-coordinates in pixels of this Viewport's
     * top edge and the position of its camera on the screen.
     * @return This Viewport's top-side difference
     */
    public final int getTop() {
        return top;
    }
    
    /**
     * Returns the difference of the y-coordinates in pixels of this Viewport's
     * bottom edge and the position of its camera on the screen.
     * @return This Viewport's bottom-side difference
     */
    public final int getBottom() {
        return bottom;
    }
    
    /**
     * Returns whether any part of the specified rectangular region is visible
     * through this Viewport.
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @return Whether the specified rectangular region is visible through this
     * Viewport
     */
    public final boolean rectangleIsVisible(long x1, long y1, long x2, long y2) {
        if (camera != null && camera.newState == getNewThinkerGroup()) {
            int centerX = Frac.toInt(camera.getCenterX());
            int centerY = Frac.toInt(camera.getCenterY());
            return Frac.toInt(x1) < centerX + right && Frac.toInt(x2) > centerX + left
                    && Frac.toInt(y1) < centerY + bottom && Frac.toInt(y2) > centerY + top;
        }
        return false;
    }
    
}
