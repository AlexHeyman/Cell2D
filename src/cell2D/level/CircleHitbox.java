package cell2D.level;

public class CircleHitbox extends Hitbox {
    
    private double relRadius, absRadius;
    
    public CircleHitbox(LevelVector relPosition, double relRadius) {
        super(relPosition);
        if (!setRelRadius(relRadius)) {
            throw new RuntimeException("Attempted to give a circle hitbox a negative radius");
        }
    }
    
    public CircleHitbox(double relX, double relY, double relRadius) {
        this(new LevelVector(relX, relY), relRadius);
    }
    
    @Override
    public Hitbox getCopy() {
        return new CircleHitbox(0, 0, relRadius);
    }
    
    public final double getRelRadius() {
        return relRadius;
    }
    
    public final boolean setRelRadius(double relRadius) {
        if (relRadius >= 0) {
            this.relRadius = relRadius;
            absRadius = relRadius;
            updateCells();
            return true;
        }
        return false;
    }
    
    public final double getAbsRadius() {
        return absRadius;
    }
    
    @Override
    public double getLeftEdge() {
        return getAbsX() - absRadius;
    }
    
    @Override
    public double getRightEdge() {
        return getAbsX() + absRadius;
    }
    
    @Override
    public double getTopEdge() {
        return getAbsY() - absRadius;
    }
    
    @Override
    public double getBottomEdge() {
        return getAbsY() + absRadius;
    }
    
    @Override
    public double getCenterX() {
        return getAbsX();
    }
    
    @Override
    public double getCenterY() {
        return getAbsY();
    }
    
}
