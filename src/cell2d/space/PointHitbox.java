package cell2d.space;

import cell2d.CellVector;

/**
 * A PointHitbox is a Hitbox that consists only of the point that is its
 * absolute position.
 * @author Andrew Heyman
 */
public class PointHitbox extends Hitbox {
    
    /**
     * Creates a new PointHitbox with the specified relative position.
     * @param relPosition This PointHitbox's relative position
     */
    public PointHitbox(CellVector relPosition) {
        super(relPosition);
    }
    
    /**
     * Creates a new PointHitbox with the specified relative position.
     * @param relX The x-coordinate of this PointHitbox's relative position
     * @param relY The y-coordinate of this PointHitbox's relative position
     */
    public PointHitbox(long relX, long relY) {
        super(relX, relY);
    }
    
    @Override
    public Hitbox getCopy() {
        return new PointHitbox(0, 0);
    }
    
    @Override
    public final long getLeftEdge() {
        return getAbsX();
    }
    
    @Override
    public final long getRightEdge() {
        return getAbsX();
    }
    
    @Override
    public final long getTopEdge() {
        return getAbsY();
    }
    
    @Override
    public final long getBottomEdge() {
        return getAbsY();
    }
    
}
