package ironclad2D.level;

public class CompositeSolidHitbox extends CompositeHitbox implements SolidHitbox {
    
    public CompositeSolidHitbox(double relX, double relY) {
        super(relX, relY);
    }
    
    @Override
    final boolean canBeComponent(Hitbox hitbox) {
        return (hitbox instanceof SolidHitbox);
    }
    
}
