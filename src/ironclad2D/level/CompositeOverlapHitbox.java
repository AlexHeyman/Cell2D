package ironclad2D.level;

public class CompositeOverlapHitbox extends CompositeHitbox implements OverlapHitbox {
    
    public CompositeOverlapHitbox(double relX, double relY) {
        super(relX, relY);
    }
    
    @Override
    final boolean canBeComponent(Hitbox hitbox) {
        return (hitbox instanceof OverlapHitbox);
    }
    
}
