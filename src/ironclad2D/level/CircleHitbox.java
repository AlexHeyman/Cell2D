package ironclad2D.level;

public class CircleHitbox extends Hitbox implements CollisionHitbox {
    
    private double relRadius, absRadius;
    
    public CircleHitbox(double relX, double relY, double relRadius) {
        super(relX, relY);
        setRelRadius(relRadius);
    }
    
    public final double getRelRadius() {
        return relRadius;
    }
    
    public final void setRelRadius(double relRadius) {
        if (relRadius < 0) {
            throw new RuntimeException("Attempted to give a circle hitbox a negative radius");
        }
        this.relRadius = relRadius;
        absRadius = relRadius;
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
