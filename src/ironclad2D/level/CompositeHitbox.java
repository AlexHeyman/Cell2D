package ironclad2D.level;

import java.util.HashMap;
import java.util.Map;

public abstract class CompositeHitbox extends Hitbox {
    
    final Map<Integer,Hitbox> components = new HashMap<>();
    private double left = 0;
    private double right = 0;
    private double top = 0;
    private double bottom = 0;
    
    public CompositeHitbox(double relX, double relY) {
        super(relX, relY);
    }
    
    private void updateData() {
        if (components.isEmpty()) {
            left = 0;
            right = 0;
            top = 0;
            bottom = 0;
        } else {
            boolean compare = false;
            double newLeft = 0;
            double newRight = 0;
            double newTop = 0;
            double newBottom = 0;
            for (Hitbox component : components.values()) {
                if (compare) {
                    newLeft = Math.min(newLeft, component.getLeftEdge());
                    newRight = Math.max(newRight, component.getRightEdge());
                    newTop = Math.min(newTop, component.getTopEdge());
                    newBottom = Math.max(newBottom, component.getBottomEdge());
                } else {
                    newLeft = component.getLeftEdge();
                    newRight = component.getRightEdge();
                    newTop = component.getTopEdge();
                    newBottom = component.getBottomEdge();
                }
                compare = true;
            }
            double x = getAbsX();
            double y = getAbsY();
            left = newLeft - x;
            right = newRight - x;
            top = newTop - y;
            bottom = newBottom - y;
        }
    }
    
    public final Hitbox getComponent(int id) {
        return components.get(id);
    }
    
    public final boolean setComponent(int id, Hitbox hitbox) {
        if (hitbox == null) {
            return removeComponent(id);
        }
        if (canBeComponent(hitbox) && addChild(hitbox)) {
            Hitbox oldHitbox = components.put(id, hitbox);
            if (oldHitbox == null) {
                double x = getAbsX();
                double y = getAbsY();
                left = Math.min(left, hitbox.getLeftEdge() - x);
                right = Math.max(right, hitbox.getRightEdge() - x);
                top = Math.min(top, hitbox.getTopEdge() - y);
                bottom = Math.max(bottom, hitbox.getBottomEdge() - y);
            } else {
                removeChild(oldHitbox);
                updateData();
            }
            return true;
        }
        return false;
    }
    
    abstract boolean canBeComponent(Hitbox hitbox);
    
    public final boolean removeComponent(int id) {
        Hitbox hitbox = components.remove(id);
        if (hitbox != null) {
            removeChild(hitbox);
            updateData();
            return true;
        }
        return false;
    }
    
    @Override
    public final double getLeftEdge() {
        return getAbsX() + left;
    }
    
    @Override
    public final double getRightEdge() {
        return getAbsX() + right;
    }
    
    @Override
    public final double getTopEdge() {
        return getAbsY() + top;
    }
    
    @Override
    public final double getBottomEdge() {
        return getAbsY() + bottom;
    }
    
    @Override
    public final double getCenterX() {
        return getAbsX() + (left + right)/2;
    }
    
    @Override
    public final double getCenterY() {
        return getAbsY() + (top + bottom)/2;
    }
    
    @Override
    final void updateAbsXFlipActions() {
        updateData();
    }
    
    @Override
    final void updateAbsYFlipActions() {
        updateData();
    }
    
    @Override
    final void updateAbsAngleActions() {
        updateData();
    }
    
}
