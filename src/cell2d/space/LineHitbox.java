package cell2d.space;

import cell2d.CellGame;
import cell2d.CellVector;

/**
 * <p>A LineHitbox is a Hitbox shaped like a line segment, with one endpoint
 * being its position and the other being the sum of its position and a vector
 * called its difference.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the SpaceStates that can use
 * this LineHitbox
 */
public class LineHitbox<T extends CellGame> extends Hitbox<T> {
    
    private final CellVector relDifference, absDifference;
    private double left, right, top, bottom;
    
    /**
     * Creates a new LineHitbox with the specified relative position and
     * difference.
     * @param relPosition This LineHitbox's relative position
     * @param relDifference This LineHitbox's relative difference
     */
    public LineHitbox(CellVector relPosition, CellVector relDifference) {
        this(relPosition.getX(), relPosition.getY(), relDifference.getX(), relDifference.getY());
    }
    
    /**
     * Creates a new LineHitbox with the specified relative position and
     * difference.
     * @param relX The x-coordinate of this LineHitbox's relative position
     * @param relY The y-coordinate of this LineHitbox's relative position
     * @param relDX The x-coordinate of this LineHitbox's relative difference
     * @param relDY The y-coordinate of this LineHitbox's relative difference
     */
    public LineHitbox(double relX, double relY, double relDX, double relDY) {
        super(relX, relY);
        this.relDifference = new CellVector(relDX, relDY);
        absDifference = new CellVector();
        updateData();
    }
    
    @Override
    public Hitbox<T> getCopy() {
        return new LineHitbox<>(new CellVector(0, 0), relDifference);
    }
    
    private void updateData() {
        absDifference.setCoordinates(relDifference).relativeTo(this);
        left = Math.min(absDifference.getX(), 0);
        right = Math.max(absDifference.getX(), 0);
        top = Math.min(absDifference.getY(), 0);
        bottom = Math.max(absDifference.getY(), 0);
        updateBoundaries();
    }
    
    /**
     * Returns this LineHitbox's relative difference.
     * @return This LineHitbox's relative difference
     */
    public final CellVector getRelDifference() {
        return new CellVector(relDifference);
    }
    
    /**
     * Returns the x-coordinate of this LineHitbox's relative difference.
     * @return The x-coordinate of this LineHitbox's relative difference
     */
    public final double getRelDX() {
        return relDifference.getX();
    }
    
    /**
     * Returns the y-coordinate of this LineHitbox's relative difference.
     * @return The y-coordinate of this LineHitbox's relative difference
     */
    public final double getRelDY() {
        return relDifference.getY();
    }
    
    /**
     * Sets this LineHitbox's relative difference to the specified value.
     * @param difference The new relative difference
     */
    public final void setRelDifference(CellVector difference) {
        relDifference.setCoordinates(difference);
        updateData();
    }
    
    /**
     * Sets this LineHitbox's relative difference to the specified value.
     * @param relDX The x-coordinate of the new relative difference
     * @param relDY The y-coordinate of the new relative difference
     */
    public final void setRelDifference(double relDX, double relDY) {
        relDifference.setCoordinates(relDX, relDY);
        updateData();
    }
    
    /**
     * Sets the x-coordinate of this LineHitbox's relative difference to the
     * specified value.
     * @param relDX The x-coordinate of the new relative difference
     */
    public final void setRelDX(double relDX) {
        relDifference.setX(relDX);
        updateData();
    }
    
    /**
     * Sets the y-coordinate of this LineHitbox's relative difference to the
     * specified value.
     * @param relDY The y-coordinate of the new relative difference
     */
    public final void setRelDY(double relDY) {
        relDifference.setY(relDY);
        updateData();
    }
    
    /**
     * Returns this LineHitbox's absolute difference.
     * @return This LineHitbox's absolute difference
     */
    public final CellVector getAbsDifference() {
        return new CellVector(absDifference);
    }
    
    /**
     * Returns the x-coordinate of this LineHitbox's relative difference.
     * @return The x-coordinate of this LineHitbox's relative difference
     */
    public final double getAbsDX() {
        return absDifference.getX();
    }
    
    /**
     * Returns the y-coordinate of this LineHitbox's relative difference.
     * @return The y-coordinate of this LineHitbox's relative difference
     */
    public final double getAbsDY() {
        return absDifference.getY();
    }
    
    /**
     * Returns the position of this LineHitbox's second endpoint, the sum of its
     * absolute position and absolute difference.
     * @return The position of this LineHitbox's second endpoint
     */
    public final CellVector getPosition2() {
        return new CellVector(getAbsX() + absDifference.getX(), getAbsY() + absDifference.getY());
    }
    
    /**
     * Returns the x-coordinate of this LineHitbox's second endpoint, the sum of
     * its absolute position and absolute difference.
     * @return The x-coordinate of this LineHitbox's second endpoint
     */
    public final double getX2() {
        return getAbsX() + absDifference.getX();
    }
    
    /**
     * Returns the y-coordinate of this LineHitbox's second endpoint, the sum of
     * its absolute position and absolute difference.
     * @return The y-coordinate of this LineHitbox's second endpoint
     */
    public final double getY2() {
        return getAbsY() + absDifference.getY();
    }
    
    @Override
    public final double getLeftEdge() {
        return getAbsX() + left;
    }
    
    @Override
    public final double getRightEdge() {
        return getAbsX() + right;
    }
    
    @Override
    public final double getTopEdge() {
        return getAbsY() + top;
    }
    
    @Override
    public final double getBottomEdge() {
        return getAbsY() + bottom;
    }
    
    @Override
    final void updateAbsXFlipActions() {
        updateData();
    }
    
    @Override
    final void updateAbsYFlipActions() {
        updateData();
    }
    
    @Override
    final void updateAbsAngleActions() {
        updateData();
    }
    
}
