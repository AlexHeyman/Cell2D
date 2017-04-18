package cell2d.level;

import cell2d.CellGame;

/**
 * A PointHitbox is a Hitbox that consists only of the point that is its
 * absolute position.
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the LevelStates that can use
 * this PointHitbox
 */
public class PointHitbox<T extends CellGame> extends Hitbox<T> {
    
    /**
     * Creates a new PointHitbox with the specified relative position.
     * @param relPosition This PointHitbox's relative position
     */
    public PointHitbox(LevelVector relPosition) {
        super(relPosition);
    }
    
    /**
     * Creates a new PointHitbox with the specified relative position.
     * @param relX The x-coordinate of this PointHitbox's relative position
     * @param relY The y-coordinate of this PointHitbox's relative position
     */
    public PointHitbox(double relX, double relY) {
        super(relX, relY);
    }
    
    @Override
    public Hitbox<T> getCopy() {
        return new PointHitbox<>(0, 0);
    }
    
    @Override
    public final double getLeftEdge() {
        return getAbsX();
    }
    
    @Override
    public final double getRightEdge() {
        return getAbsX();
    }
    
    @Override
    public final double getTopEdge() {
        return getAbsY();
    }
    
    @Override
    public final double getBottomEdge() {
        return getAbsY();
    }
    
}
