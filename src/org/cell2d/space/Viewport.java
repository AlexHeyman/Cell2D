package org.cell2d.space;

import java.awt.Point;
import org.cell2d.CellGame;
import org.cell2d.CellVector;
import org.cell2d.Frac;

/**
 * <p>A Viewport represents a rectangular region of the screen through which the
 * space of the SpaceState to which it is assigned can be viewed. The center of
 * a Viewport's rectangular field of view in a SpaceState is the center of a
 * SpaceObject called the Viewport's <i>camera</i>. To render the region of its
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
 * Viewport's own. Only one HUD may be assigned to a given Viewport in this
 * capacity at once. A Viewport's HUD uses the region of the screen that the
 * Viewport occupies as its rendering region.</p>
 * @see SpaceState#setViewport(int, org.cell2d.space.Viewport)
 * @see HUD
 * @param <T> The type of CellGame that uses this Viewport's SpaceStates
 * @param <U> The type of SpaceState that uses this Viewport
 * @author Alex Heyman
 */
public class Viewport<T extends CellGame, U extends SpaceState<T,U,?>> {
    
    private T game = null;
    private U state = null;
    private SpaceObject camera = null;
    private HUD hud = null;
    private long x1, y1, x2, y2;
    int roundX1, roundY1, roundX2, roundY2;
    private int left, right, top, bottom;
    
    /**
     * Constructs a Viewport that occupies the specified region of the screen.
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
        roundX1 = Frac.intRound(x1);
        roundY1 = Frac.intRound(y1);
        roundX2 = Frac.intRound(x2);
        roundY2 = Frac.intRound(y2);
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
     * Returns the CellGame of the SpaceState to which this Viewport is
     * assigned, or null if it is not assigned to a SpaceState.
     * @return This Viewport's SpaceState's CellGame
     */
    public final T getGame() {
        return game;
    }
    
    /**
     * Returns the SpaceState to which this Viewport is assigned, or null if it
     * is not assigned to one.
     * @return The SpaceState to which this Viewport is assigned
     */
    public final U getGameState() {
        return state;
    }
    
    final void setGameState(U state) {
        this.game = state.getGame();
        this.state = state;
    }
    
    /**
     * Returns this Viewport's camera, or null if it has none.
     * @return This Viewport's camera
     */
    public final SpaceObject getCamera() {
        return camera;
    }
    
    /**
     * Sets this Viewport's camera to the specified SpaceObject, or to none if
     * the specified SpaceObject is null.
     * @param camera The new camera
     */
    public final void setCamera(SpaceObject camera) {
        this.camera = camera;
    }
    
    /**
     * Returns the HUD that is assigned to this Viewport, or null if there is
     * none.
     * @return This Viewport's HUD
     */
    public final HUD getHUD() {
        return hud;
    }
    
    /**
     * Sets the HUD that is assigned to this Viewport to the specified one. If
     * there is already an HUD assigned to this Viewport, it will be removed. If
     * the specified HUD is null, the current HUD will be removed if there is
     * one, but it will not be replaced with anything.
     * @param hud The HUD to add
     */
    public final void setHUD(HUD hud) {
        this.hud = hud;
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
            roundX1 = Frac.intRound(x1);
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
            roundY1 = Frac.intRound(y1);
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
            roundX2 = Frac.intRound(x2);
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
            roundY2 = Frac.intRound(y2);
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
     * Returns the x-coordinate of the left edge of this Viewport's field of
     * view in a SpaceState.
     * @return The x-coordinate of this Viewport's field of view's left edge
     * @throws NullPointerException if this Viewport has no camera
     */
    public final long getLeftEdge() throws NullPointerException {
        return camera.getCenterX() + ((long)left << Frac.BITS);
    }
    
    /**
     * Returns the x-coordinate of the right edge of this Viewport's field of
     * view in a SpaceState.
     * @return The x-coordinate of this Viewport's field of view's right edge
     * @throws NullPointerException if this Viewport has no camera
     */
    public final long getRightEdge() throws NullPointerException {
        return camera.getCenterX() + ((long)right << Frac.BITS);
    }
    
    /**
     * Returns the y-coordinate of the top edge of this Viewport's field of view
     * in a SpaceState.
     * @return The y-coordinate of this Viewport's field of view's top edge
     * @throws NullPointerException if this Viewport has no camera
     */
    public final long getTopEdge() throws NullPointerException {
        return camera.getCenterY() + ((long)top << Frac.BITS);
    }
    
    /**
     * Returns the y-coordinate of the bottom edge of this Viewport's field of
     * view in a SpaceState.
     * @return The y-coordinate of this Viewport's field of view's bottom edge
     * @throws NullPointerException if this Viewport has no camera
     */
    public final long getBottomEdge() throws NullPointerException {
        return camera.getCenterY() + ((long)bottom << Frac.BITS);
    }
    
    /**
     * Returns the point in a SpaceState, as seen through this Viewport, that
     * corresponds to the specified point in pixels in this Viewport's on-screen
     * rendering region. If the specified point is not in this Viewport's
     * rendering region or this Viewport has no camera, this method will return
     * null.
     * @param x The x-coordinate of the screen point
     * @param y The y-coordinate of the screen point
     * @return The SpaceState point that corresponds to the specified screen
     * point
     */
    public final CellVector getSpacePoint(int x, int y) {
        if (camera != null && x >= roundX1 && x < roundX2 && y >= roundY1 && y < roundY2) {
            return new CellVector(getLeftEdge() + ((long)(x - roundX1) << Frac.BITS),
                    getTopEdge() + ((long)(y - roundY1) << Frac.BITS));
        }
        return null;
    }
    
    /**
     * Returns the point in pixels on the screen that corresponds to the
     * specified point in a SpaceState as seen through this Viewport. If the
     * specified point is not visible through this Viewport, this method will
     * return null.
     * @param spacePoint The SpaceState point
     * @return The screen point that corresponds to the specified SpaceState
     * point as seen through this Viewport
     */
    public final Point getScreenPoint(CellVector spacePoint) {
        return getScreenPoint(spacePoint.getX(), spacePoint.getY());
    }
    
    /**
     * Returns the point in pixels on the screen that corresponds to the
     * specified point in a SpaceState as seen through this Viewport. If the
     * specified point is not visible through this Viewport, this method will
     * return null.
     * @param x The x-coordinate of the SpaceState point
     * @param y The y-coordinate of the SpaceState point
     * @return The screen point that corresponds to the specified SpaceState
     * point as seen through this Viewport
     */
    public final Point getScreenPoint(long x, long y) {
        if (camera != null) {
            int fx = Frac.intFloor(x - camera.getCenterX());
            int fy = Frac.intFloor(y - camera.getCenterY());
            if (fx >= left && fx < right && fy >= top && fy < bottom) {
                return new Point(fx + roundX1 - left, fy + roundY1 - top);
            }
        }
        return null;
    }
    
    /**
     * Returns whether any part of the specified rectangular region of a
     * SpaceState's space is visible through this Viewport.
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @return Whether the specified rectangular region of a SpaceState's space
     * is visible through this Viewport
     */
    public final boolean rectangleIsVisible(long x1, long y1, long x2, long y2) {
        if (camera != null) {
            long centerX = camera.getCenterX();
            long centerY = camera.getCenterY();
            return Frac.intFloor(x1 - centerX) < right && Frac.intCeil(x2 - centerX) > left
                    && Frac.intFloor(y1 - centerY) < bottom && Frac.intCeil(y2 - centerY) > top;
        }
        return false;
    }
    
}
