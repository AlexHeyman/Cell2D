package ironclad2D.level;

public class PointHitbox extends Hitbox {
    
    public PointHitbox(double relX, double relY) {
        super(relX, relY);
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
    
    @Override
    public final double getCenterX() {
        return getAbsX();
    }
    
    @Override
    public final double getCenterY() {
        return getAbsY();
    }
    
}
