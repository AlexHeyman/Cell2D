package ironclad2D.level;

public class CompositePhysicsHitbox extends CompositeHitbox implements PhysicsHitbox {
    
    public CompositePhysicsHitbox(double relX, double relY) {
        super(relX, relY);
    }
    
    @Override
    final boolean canBeComponent(Hitbox hitbox) {
        return (hitbox instanceof PhysicsHitbox);
    }
    
}
