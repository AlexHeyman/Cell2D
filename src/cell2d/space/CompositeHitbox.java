package cell2d.space;

import cell2d.CellVector;
import java.util.Collections;
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
 */
public class CompositeHitbox extends Hitbox {
    
    final Map<Integer,Hitbox> components = new HashMap<>();
    private long left = 0;
    private long right = 0;
    private long top = 0;
    private long bottom = 0;
    
    /**
     * Constructs a CompositeHitbox with the specified relative position.
     * @param relPosition This CompositeHitbox's relative position
     */
    public CompositeHitbox(CellVector relPosition) {
        super(relPosition);
    }
    
    /**
     * Constructs a CompositeHitbox with the specified relative position.
     * @param relX The x-coordinate of this CompositeHitbox's relative position
     * @param relY The y-coordinate of this CompositeHitbox's relative position
     */
    public CompositeHitbox(long relX, long relY) {
        super(relX, relY);
    }
    
    @Override
    public Hitbox getCopy() {
        CompositeHitbox copy = new CompositeHitbox(0, 0);
        for (Map.Entry<Integer,Hitbox> entry : components.entrySet()) {
            Hitbox component = entry.getValue();
            Hitbox componentCopy = component.getCopy();
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
            long newLeft = 0;
            long newRight = 0;
            long newTop = 0;
            long newBottom = 0;
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
            long x = getAbsX();
            long y = getAbsY();
            left = newLeft - x;
            right = newRight - x;
            top = newTop - y;
            bottom = newBottom - y;
        }
        updateBoundaries();
    }
    
    /**
     * Returns an unmodifiable Map to this CompositeHitbox's components from
     * their respective IDs.
     * @return A Map to this CompositeHitbox's components from their IDs
     */
    public final Map<Integer,Hitbox> getComponents() {
        return Collections.unmodifiableMap(components);
    }
    
    /**
     * Returns the component Hitbox that is assigned to this CompositeHitbox
     * with the specified ID.
     * @param id The ID of the component Hitbox to be returned
     * @return The component Hitbox that is assigned to this CompositeHitbox
     * with the specified ID
     */
    public final Hitbox getComponent(int id) {
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
    public final boolean setComponent(int id, Hitbox hitbox) {
        if (hitbox == null) {
            Hitbox oldHitbox = components.remove(id);
            if (oldHitbox != null) {
                removeChild(oldHitbox);
                oldHitbox.componentOf = null;
                updateShape();
                return true;
            }
        } else if (addChild(hitbox)) {
            hitbox.componentOf = this;
            Hitbox oldHitbox = components.put(id, hitbox);
            if (oldHitbox == null) {
                long x = getAbsX();
                long y = getAbsY();
                left = Math.min(left, hitbox.getLeftEdge() - x);
                right = Math.max(right, hitbox.getRightEdge() - x);
                top = Math.min(top, hitbox.getTopEdge() - y);
                bottom = Math.max(bottom, hitbox.getBottomEdge() - y);
                updateBoundaries();
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
        for (Hitbox hitbox : components.values()) {
            removeChild(hitbox);
        }
        components.clear();
        updateShape();
    }
    
    @Override
    public final long getLeftEdge() {
        return getAbsX() + left;
    }
    
    @Override
    public final long getRightEdge() {
        return getAbsX() + right;
    }
    
    @Override
    public final long getTopEdge() {
        return getAbsY() + top;
    }
    
    @Override
    public final long getBottomEdge() {
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
