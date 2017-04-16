package cell2D.level;

import cell2D.CellGame;

/**
 * <p>A CircleHitbox is a circular Hitbox with its origin at its center. A
 * CircleHitbox's radius cannot be negative.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the LevelStates that can use
 * this CircleHitbox
 */
public class CircleHitbox<T extends CellGame> extends Hitbox<T> {
    
    private double radius;
    
    /**
     * Creates a new CircleHitbox with the specified relative position and
     * radius.
     * @param relPosition This CircleHitbox's relative position
     * @param radius This CircleHitbox's radius
     */
    public CircleHitbox(LevelVector relPosition, double radius) {
        super(relPosition);
        if (!setRadius(radius)) {
            throw new RuntimeException("Attempted to give a CircleHitbox a negative radius");
        }
    }
    
    /**
     * Creates a new CircleHitbox with the specified relative position and
     * radius.
     * @param relX The x-coordinate of this CircleHitbox's relative position
     * @param relY The y-coordinate of this CircleHitbox's relative position
     * @param radius This CircleHitbox's radius
     */
    public CircleHitbox(double relX, double relY, double radius) {
        this(new LevelVector(relX, relY), radius);
    }
    
    @Override
    public Hitbox<T> getCopy() {
        return new CircleHitbox<>(0, 0, radius);
    }
    
    /**
     * Returns this CircleHitbox's radius.
     * @return This CircleHitbox's radius
     */
    public final double getRadius() {
        return radius;
    }
    
    /**
     * Sets this CircleHitbox's radius to the specified value.
     * @param radius The new radius
     * @return Whether the new radius was valid and the change was successful
     */
    public final boolean setRadius(double radius) {
        if (radius >= 0) {
            this.radius = radius;
            updateBoundaries();
            return true;
        }
        return false;
    }
    
    @Override
    public double getLeftEdge() {
        return getAbsX() - radius;
    }
    
    @Override
    public double getRightEdge() {
        return getAbsX() + radius;
    }
    
    @Override
    public double getTopEdge() {
        return getAbsY() - radius;
    }
    
    @Override
    public double getBottomEdge() {
        return getAbsY() + radius;
    }
    
}
