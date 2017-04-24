package cell2d.space;

import cell2d.CellVector;
import cell2d.CellGame;

/**
 * <p>A RectangleHitbox is a rectangular Hitbox with sides that remain
 * orthogonal regardless of its angle of rotation. Horizontal and vertical
 * flipping will flip a RectangleHitbox across axes through its position that
 * are absolutely horizontal and vertical, respectively, but its angle of
 * rotation affects only any other Hitboxes that are relative to it. A
 * RectangleHitbox cannot have a negative width or height.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the SpaceStates that can use
 * this RectangleHitbox
 */
public class RectangleHitbox<T extends CellGame> extends Hitbox<T> {
    
    private double relLeft, relRight, relTop, relBottom, absLeft, absRight, absTop, absBottom;
    
    /**
     * Creates a new RectangleHitbox with the specified relative position and
     * dimensions.
     * @param relPosition This RectangleHitbox's relative position
     * @param relLeft The difference of the x-coordinates of this
     * RectangleHitbox's relative left edge and relative position
     * @param relRight The difference of the x-coordinates of this
     * RectangleHitbox's relative right edge and relative position
     * @param relTop The difference of the y-coordinates of this
     * RectangleHitbox's relative top edge and relative position
     * @param relBottom The difference of the y-coordinates of this
     * RectangleHitbox's relative bottom edge and relative position
     */
    public RectangleHitbox(CellVector relPosition, double relLeft, double relRight, double relTop, double relBottom) {
        this(relPosition.getX(), relPosition.getY(), relLeft, relRight, relTop, relBottom);
    }
    
    /**
     * Creates a new RectangleHitbox with the specified relative position and
     * dimensions.
     * @param relX The x-coordinate of this RectangleHitbox's relative position
     * @param relY The y-coordinate of this RectangleHitbox's relative position
     * @param relLeft The difference of the x-coordinates of this
     * RectangleHitbox's relative left edge and relative position
     * @param relRight The difference of the x-coordinates of this
     * RectangleHitbox's relative right edge and relative position
     * @param relTop The difference of the y-coordinates of this
     * RectangleHitbox's relative top edge and relative position
     * @param relBottom The difference of the y-coordinates of this
     * RectangleHitbox's relative bottom edge and relative position
     */
    public RectangleHitbox(double relX, double relY, double relLeft, double relRight, double relTop, double relBottom) {
        super(relX, relY);
        if (relLeft > relRight) {
            throw new RuntimeException("Attempted to give a RectangleHitbox a negative width");
        }
        if (relTop > relBottom) {
            throw new RuntimeException("Attempted to give a RectangleHitbox a negative height");
        }
        this.relLeft = relLeft;
        this.relRight = relRight;
        this.relTop = relTop;
        this.relBottom = relBottom;
        absLeft = relLeft;
        absRight = relRight;
        absTop = relTop;
        absBottom = relBottom;
    }
    
    @Override
    public Hitbox<T> getCopy() {
        return new RectangleHitbox<>(0, 0, relLeft, relRight, relTop, relBottom);
    }
    
    /**
     * Returns the difference of the x-coordinates of this RectangleHitbox's
     * relative left edge and relative position.
     * @return This RectangleHitbox's relative left-side difference
     */
    public final double getRelLeft() {
        return relLeft;
    }
    
    /**
     * Sets the difference of the x-coordinates of this RectangleHitbox's
     * relative left edge and relative position to the specified value, if doing
     * so would not cause this RectangleHitbox's width to be negative.
     * @param relLeft The new relative left-side difference
     * @return Whether the change occurred
     */
    public final boolean setRelLeft(double relLeft) {
        if (relLeft <= relRight) {
            this.relLeft = relLeft;
            if (getAbsXFlip()) {
                absRight = -relLeft;
            } else {
                absLeft = relLeft;
            }
            updateBoundaries();
            return true;
        }
        return false;
    }
    
    /**
     * Returns the difference of the x-coordinates of this RectangleHitbox's
     * absolute left edge and absolute position.
     * @return This RectangleHitbox's absolute left-side difference
     */
    public final double getAbsLeft() {
        return absLeft;
    }
    
    /**
     * Returns the difference of the x-coordinates of this RectangleHitbox's
     * relative right edge and relative position.
     * @return This RectangleHitbox's relative right-side difference
     */
    public final double getRelRight() {
        return relRight;
    }
    
    /**
     * Sets the difference of the x-coordinates of this RectangleHitbox's
     * relative right edge and relative position to the specified value, if
     * doing so would not cause this RectangleHitbox's width to be negative.
     * @param relRight The new relative right-side difference
     * @return Whether the change occurred
     */
    public final boolean setRelRight(double relRight) {
        if (relLeft <= relRight) {
            this.relRight = relRight;
            if (getAbsXFlip()) {
                absLeft = -relRight;
            } else {
                absRight = relRight;
            }
            updateBoundaries();
            return true;
        }
        return false;
    }
    
    /**
     * Returns the difference of the x-coordinates of this RectangleHitbox's
     * absolute right edge and absolute position.
     * @return This RectangleHitbox's absolute right-side difference
     */
    public final double getAbsRight() {
        return absRight;
    }
    
    /**
     * Returns the difference of the y-coordinates of this RectangleHitbox's
     * relative top edge and relative position.
     * @return This RectangleHitbox's relative top-side difference
     */
    public final double getRelTop() {
        return relTop;
    }
    
    /**
     * Sets the difference of the y-coordinates of this RectangleHitbox's
     * relative top edge and relative position to the specified value, if doing
     * so would not cause this RectangleHitbox's height to be negative.
     * @param relTop The new relative top-side difference
     * @return Whether the change occurred
     */
    public final boolean setRelTop(double relTop) {
        if (relTop <= relBottom) {
            this.relTop = relTop;
            if (getAbsYFlip()) {
                absBottom = -relTop;
            } else {
                absTop = relTop;
            }
            updateBoundaries();
            return true;
        }
        return false;
    }
    
    /**
     * Returns the difference of the y-coordinates of this RectangleHitbox's
     * absolute top edge and absolute position.
     * @return This RectangleHitbox's absolute top-side difference
     */
    public final double getAbsTop() {
        return absTop;
    }
    
    /**
     * Returns the difference of the y-coordinates of this RectangleHitbox's
     * relative bottom edge and relative position.
     * @return This RectangleHitbox's relative bottom-side difference
     */
    public final double getRelBottom() {
        return relBottom;
    }
    
    /**
     * Sets the difference of the y-coordinates of this RectangleHitbox's
     * relative bottom edge and relative position to the specified value, if
     * doing so would not cause this RectangleHitbox's height to be negative.
     * @param relBottom The new relative bottom-side difference
     * @return Whether the change occurred
     */
    public final boolean setRelBottom(double relBottom) {
        if (relTop <= relBottom) {
            this.relBottom = relBottom;
            if (getAbsYFlip()) {
                absTop = -relBottom;
            } else {
                absBottom = relBottom;
            }
            updateBoundaries();
            return true;
        }
        return false;
    }
    
    /**
     * Returns the difference of the y-coordinates of this RectangleHitbox's
     * absolute bottom edge and absolute position.
     * @return This RectangleHitbox's absolute bottom-side difference
     */
    public final double getAbsBottom() {
        return absBottom;
    }
    
    /**
     * Returns this RectangleHitbox's width.
     * @return This RectangleHitbox's width
     */
    public final double getWidth() {
        return absRight - absLeft;
    }
    
    /**
     * Returns this RectangleHitbox's height.
     * @return This RectangleHitbox's height
     */
    public final double getHeight() {
        return absBottom - absTop;
    }
    
    @Override
    public final double getLeftEdge() {
        return getAbsX() + absLeft;
    }
    
    @Override
    public final double getRightEdge() {
        return getAbsX() + absRight;
    }
    
    @Override
    public final double getTopEdge() {
        return getAbsY() + absTop;
    }
    
    @Override
    public final double getBottomEdge() {
        return getAbsY() + absBottom;
    }
    
    @Override
    final void updateAbsXFlipActions() {
        if (getAbsXFlip()) {
            absLeft = -relRight;
            absRight = -relLeft;
        } else {
            absLeft = relLeft;
            absRight = relRight;
        }
        updateBoundaries();
    }
    
    @Override
    final void updateAbsYFlipActions() {
        if (getAbsYFlip()) {
            absTop = -relBottom;
            absBottom = -relTop;
        } else {
            absTop = relTop;
            absBottom = relBottom;
        }
        updateBoundaries();
    }
    
}
