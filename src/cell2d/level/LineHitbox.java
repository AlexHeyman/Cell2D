package cell2d.level;

import cell2d.CellGame;

/**
 * <p>A LineHitbox is a Hitbox shaped like a line segment, with one endpoint
 * being its position and the other being the sum of its position and a vector
 * called its difference.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the LevelStates that can use
 * this LineHitbox
 */
public class LineHitbox<T extends CellGame> extends Hitbox<T> {
    
    private final LevelVector relDifference, absDifference;
    private double left, right, top, bottom;
    
    /**
     * Creates a new LineHitbox with the specified relative position and
     * difference.
     * @param relPosition This LineHitbox's relative position
     * @param relDifference This LineHitbox's relative difference
     */
    public LineHitbox(LevelVector relPosition, LevelVector relDifference) {
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
        this.relDifference = new LevelVector(relDX, relDY);
        absDifference = new LevelVector();
        updateData();
    }
    
    @Override
    public Hitbox<T> getCopy() {
        return new LineHitbox<>(new LevelVector(0, 0), relDifference);
    }
    
    private void updateData() {
        absDifference.setCoordinates(relDifference).relativeTo(this);
        left = Math.min(absDifference.getX(), 0);
        right = Math.max(absDifference.getX(), 0);
        top = Math.min(absDifference.getY(), 0);
        bottom = Math.max(absDifference.getY(), 0);
        updateBoundaries();
    }
    
    public final LevelVector getRelDifference() {
        return new LevelVector(relDifference);
    }
    
    public final double getRelDX() {
        return relDifference.getX();
    }
    
    public final double getRelDY() {
        return relDifference.getY();
    }
    
    public final void setRelDifference(LevelVector difference) {
        relDifference.setCoordinates(difference);
        updateData();
    }
    
    public final void setRelDifference(double relDX, double relDY) {
        relDifference.setCoordinates(relDX, relDY);
        updateData();
    }
    
    public final void setRelDX(double relDX) {
        relDifference.setX(relDX);
        updateData();
    }
    
    public final void setRelDY(double relDY) {
        relDifference.setY(relDY);
        updateData();
    }
    
    public final LevelVector getAbsDifference() {
        return new LevelVector(absDifference);
    }
    
    public final double getAbsDX() {
        return absDifference.getX();
    }
    
    public final double getAbsDY() {
        return absDifference.getY();
    }
    
    public final LevelVector getPosition2() {
        return new LevelVector(getAbsX() + absDifference.getX(), getAbsY() + absDifference.getY());
    }
    
    public final double getX2() {
        return getAbsX() + absDifference.getX();
    }
    
    public final double getY2() {
        return getAbsY() + absDifference.getY();
    }
    
    public final double getLength() {
        return relDifference.getMagnitude();
    }
    
    public final void setLength(double length) {
        relDifference.setMagnitude(length);
        updateData();
    }
    
    public final void scale(double scaleFactor) {
        relDifference.scale(scaleFactor);
        updateData();
    }
    
    public final double getLineAngle() {
        return absDifference.getAngle();
    }
    
    public final double getLineAngleX() {
        return absDifference.getAngleX();
    }
    
    public final double getLineAngleY() {
        return absDifference.getAngleY();
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
