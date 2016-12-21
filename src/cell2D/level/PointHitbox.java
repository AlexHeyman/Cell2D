package cell2D.level;

public class PointHitbox extends Hitbox {
    
    public PointHitbox(LevelVector relPosition) {
        super(relPosition);
    }
    
    public PointHitbox(double relX, double relY) {
        super(relX, relY);
    }
    
    @Override
    public Hitbox getCopy() {
        return new PointHitbox(0, 0);
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
