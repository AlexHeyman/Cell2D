package ironclad2D.level;

public class CompositeCollisionHitbox extends CompositeHitbox implements OverlapHitbox, SolidHitbox, CollisionHitbox {
    
    public CompositeCollisionHitbox(double relX, double relY) {
        super(relX, relY);
    }
    
    @Override
    final boolean canBeComponent(Hitbox hitbox) {
        return (hitbox instanceof CollisionHitbox);
    }
    
}
