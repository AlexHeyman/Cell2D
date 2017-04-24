package cell2d.space;

import cell2d.CellVector;
import cell2d.CellGame;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>A CompositeHitbox is a Hitbox that is composed of other Hitboxes whose
 * positions, flipped statuses, and angles of rotation are all relative to those
 * of the CompositeHitbox itself. Each of a CompositeHitbox's component Hitboxes
 * is assigned to it with an integer ID that is unique in the context of the
 * CompositeHitbox. Only one Hitbox may be assigned to a given CompositeHitbox
 * with a given ID at once. A CompositeHitbox with no component Hitboxes is a
 * point at its absolute position that cannot overlap other Hitboxes.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the SpaceStates that can use
 * this CompositeHitbox
 */
public class CompositeHitbox<T extends CellGame> extends Hitbox<T> {
    
    final Map<Integer,Hitbox<T>> components = new HashMap<>();
    private double left = 0;
    private double right = 0;
    private double top = 0;
    private double bottom = 0;
    
    /**
     * Creates a new CompositeHitbox with the specified relative position.
     * @param relPosition This CompositeHitbox's relative position
     */
    public CompositeHitbox(CellVector relPosition) {
        super(relPosition);
    }
    
    /**
     * Creates a new CompositeHitbox with the specified relative position.
     * @param relX The x-coordinate of this CompositeHitbox's relative position
     * @param relY The y-coordinate of this CompositeHitbox's relative position
     */
    public CompositeHitbox(double relX, double relY) {
        super(relX, relY);
    }
    
    @Override
    public Hitbox<T> getCopy() {
        CompositeHitbox<T> copy = new CompositeHitbox(0, 0);
        for (Map.Entry<Integer,Hitbox<T>> entry : components.entrySet()) {
            Hitbox<T> component = entry.getValue();
            Hitbox<T> componentCopy = component.getCopy();
            componentCopy.setRelPosition(component.getRelX(), component.getRelY());
            componentCopy.setRelXFlip(component.getRelXFlip());
            componentCopy.setRelYFlip(component.getRelYFlip());
            componentCopy.setRelAngle(component.getRelAngle());
            copy.setComponent(entry.getKey(), componentCopy);
        }
        return copy;
    }
    
    final void updateShape() {
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
        updateBoundaries();
    }
    
    /**
     * Returns all of this CompositeHitbox's components as values in a Map with
     * their respective IDs as keys. Changes to the returned Map will not be
     * reflected in this CompositeHitbox.
     * @return A Map to this CompositeHitbox's components from their IDs
     */
    public final Map<Integer,Hitbox<T>> getComponents() {
        return new HashMap<>(components);
    }
    
    /**
     * Returns the component Hitbox that is assigned to this CompositeHitbox
     * with the specified ID.
     * @param id The ID of the component Hitbox to be returned
     * @return The component Hitbox that is assigned to this CompositeHitbox
     * with the specified ID
     */
    public final Hitbox<T> getComponent(int id) {
        return components.get(id);
    }
    
    /**
     * Sets the component Hitbox that is assigned to this CompositeHitbox with
     * the specified ID to the specified Hitbox. The specified Hitbox cannot be
     * in use by a SpaceObject, and this CompositeHitbox cannot be its
     * component, direct or indirect, or vice versa. If there is already a
     * component Hitbox assigned with the specified ID, it will be removed from
     * this CompositeHitbox. If the specified Hitbox is null, the component
     * Hitbox with the specified ID will be removed if there is one, but it will
     * not be replaced with anything.
     * @param id The ID with which to assign the specified Hitbox
     * @param hitbox The Hitbox to add as a component with the specified ID
     * @return Whether the addition occurred
     */
    public final boolean setComponent(int id, Hitbox<T> hitbox) {
        if (hitbox == null) {
            Hitbox<T> oldHitbox = components.remove(id);
            if (oldHitbox != null) {
                removeChild(oldHitbox);
                oldHitbox.componentOf = null;
                updateShape();
                return true;
            }
        } else if (addChild(hitbox)) {
            hitbox.componentOf = this;
            Hitbox<T> oldHitbox = components.put(id, hitbox);
            if (oldHitbox == null) {
                double x = getAbsX();
                double y = getAbsY();
                left = Math.min(left, hitbox.getLeftEdge() - x);
                right = Math.max(right, hitbox.getRightEdge() - x);
                top = Math.min(top, hitbox.getTopEdge() - y);
                bottom = Math.max(bottom, hitbox.getBottomEdge() - y);
            } else {
                removeChild(oldHitbox);
                oldHitbox.componentOf = null;
                updateShape();
            }
            return true;
        }
        return false;
    }
    
    /**
     * Removes all of this CompositeHitbox's component Hitboxes from it.
     */
    public final void clearComponents() {
        for (Hitbox<T> hitbox : components.values()) {
            removeChild(hitbox);
        }
        components.clear();
        updateShape();
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
    final void updateAbsXFlipActions() {
        updateShape();
    }
    
    @Override
    final void updateAbsYFlipActions() {
        updateShape();
    }
    
    @Override
    final void updateAbsAngleActions() {
        updateShape();
    }
    
}
