package cell2d.space;

import cell2d.Animation;
import cell2d.AnimationInstance;
import cell2d.CellGame;
import cell2d.CellVector;
import cell2d.Drawable;
import cell2d.Filter;
import cell2d.Sprite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.newdawn.slick.Graphics;

/**
 * <p>A SpaceObject is a physical object in a SpaceState's space. SpaceObjects
 * may be assigned to one SpaceState each in much the same way that Thinkers are
 * assigned to CellGameStates. A SpaceObject's assigned SpaceState will keep
 * track of time for it and its AnimationInstances. A SpaceObject's time factor
 * represents the average number of discrete time units the SpaceObject will
 * experience every frame while assigned to an active SpaceState. If its own
 * time factor is negative, a SpaceObject will use its assigned SpaceState's
 * time factor instead. If a SpaceObject is assigned to an inactive SpaceState
 * or none at all, time will not pass for it.</p>
 * 
 * <p>A SpaceObject inherits the position, flipped status, angle of rotation,
 * and rectangular bounding box of a locator Hitbox that is relative to no other
 * Hitbox. A SpaceObject may also have an overlap Hitbox that represents it for
 * purposes of overlapping other SpaceObjects and/or a solid Hitbox that
 * represents it for purposes of surface solidity and ThinkerObjects colliding
 * with it. The solid Hitbox's rectangular bounding box, rather than its exact
 * shape, is what represents the SpaceObject in Cell2D's standard collision
 * mechanics. All of a SpaceState's Hitboxes other than its locator Hitbox have
 * positions, flipped statuses, and angles of rotation that are relative to
 * those of its locator Hitbox. A SpaceObject may use a single Hitbox for more
 * than one of these purposes, but a Hitbox may not be used by multiple
 * SpaceObjects at once.</p>
 * 
 * <p>A SpaceObject has a point called a center that summarizes its location.
 * Its center has an offset that is relative to the SpaceObject's position,
 * flipped status, and angle of rotation. SpaceObjects' centers are the points
 * from which their distances and angles to other SpaceObjects are measured.</p>
 * 
 * <p>A SpaceObject has a Drawable appearance that represents it as seen through
 * a Viewport's camera, as well as an alpha (opacity) value that is normalized
 * to be between 0 to 1 and a Filter that apply to its appearance. The use of a
 * SpaceObject's appearance, alpha value, and Filter to represent it is a result
 * of its default draw() method, which can be overridden. A SpaceObject will
 * only be drawn if its locator Hitbox's rectangular bounding box intersects the
 * Viewport's field of view. A SpaceObject's draw priority determines whether it
 * will be drawn in front of or behind other SpaceObjects that intersect it.
 * SpaceObjects with higher draw priorities are drawn in front of those with
 * lower ones.</p>
 * 
 * <p>If an AnimationInstance is not already assigned to a CellGameState, it
 * may be assigned to a SpaceObject with an integer ID in the context of that
 * SpaceObject. Only one AnimationInstance may be assigned to a given
 * SpaceObject with a given ID at once. A SpaceObject will automatically set its
 * assigned AnimationInstances' time factors and add and remove them from
 * SpaceStates as appropriate to match its own time factor and assigned
 * SpaceState.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the SpaceStates that this
 * SpaceObject can be assigned to
 */
public abstract class SpaceObject<T extends CellGame> {
    
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    SpaceState<T> state = null;
    SpaceState<T> newState = null;
    private long timeFactor = -1;
    private Hitbox<T> locatorHitbox = null;
    private final Hitbox<T> centerHitbox;
    private Hitbox<T> overlapHitbox = null;
    private Hitbox<T> solidHitbox = null;
    boolean solidEvent = false;
    boolean moved = false;
    private int drawPriority = 0;
    private Drawable appearance = Sprite.BLANK;
    private final Map<Integer,AnimationInstance> animInstances = new HashMap<>();
    private double alpha = 1;
    private Filter filter = null;
    
    /**
     * Creates a new SpaceObject with the specified locator Hitbox.
     * @param locatorHitbox This SpaceObject's locator Hitbox
     */
    public SpaceObject(Hitbox<T> locatorHitbox) {
        centerHitbox = new PointHitbox<>(0, 0);
        centerHitbox.addAsCenterHitbox();
        if (!setLocatorHitbox(locatorHitbox)) {
            throw new RuntimeException("Attempted to create a SpaceObject with an invalid locator hitbox");
        }
        id = idCounter.getAndIncrement();
    }
    
    /**
     * Creates a new SpaceObject with the specified locator Hitbox that acts as
     * if it was created by the specified SpaceObject, initially copying its
     * creator's time factor, flipped status, and angle of rotation.
     * @param locatorHitbox This SpaceObject's locator Hitbox
     * @param creator This SpaceObject's creator
     */
    public SpaceObject(Hitbox<T> locatorHitbox, SpaceObject<T> creator) {
        this(locatorHitbox);
        setTimeFactor(creator.timeFactor);
        setXFlip(creator.getXFlip());
        setYFlip(creator.getYFlip());
        setAngle(creator.getAngle());
    }
    
    /**
     * Returns the SpaceState to which this SpaceObject is currently assigned,
     * or null if it is assigned to none.
     * @return The SpaceState to which this SpaceObject is currently assigned
     */
    public final SpaceState<T> getGameState() {
        return state;
    }
    
    /**
     * Returns the SpaceState to which this SpaceObject is about to be assigned,
     * but has not yet been due to one or more of the object lists involved
     * being iterated over. If this SpaceObject is about to be removed from its
     * SpaceState without being added to a new one afterward, this will be null.
     * If this SpaceObject is not about to change SpaceStates, this method will
     * simply return its current SpaceState.
     * @return The SpaceState to which this SpaceObject is about to be assigned
     */
    public final SpaceState<T> getNewGameState() {
        return newState;
    }
    
    /**
     * Sets the SpaceState to which this SpaceObject is currently assigned. If
     * it is set to a null SpaceState, this SpaceObject will be removed from its
     * current SpaceState if it has one.
     * @param state The SpaceState to which this SpaceObject should be assigned
     */
    public final void setGameState(SpaceState<T> state) {
        if (this.state != null) {
            this.state.removeObject(this);
        }
        if (state != null) {
            state.addObject(this);
        }
    }
    
    void addActions() {
        locatorHitbox.setGameState(state);
        if (!animInstances.isEmpty()) {
            for (AnimationInstance instance : animInstances.values()) {
                state.addAnimInstance(instance);
            }
        }
    }
    
    void addCellData() {
        state.addLocatorHitbox(locatorHitbox);
        state.addCenterHitbox(centerHitbox);
        if (overlapHitbox != null) {
            state.addOverlapHitbox(overlapHitbox);
        }
        if (solidHitbox != null) {
            state.addSolidHitbox(solidHitbox);
        }
    }
    
    void removeActions() {
        locatorHitbox.setGameState(null);
        state.removeLocatorHitbox(locatorHitbox);
        state.removeCenterHitbox(centerHitbox);
        if (overlapHitbox != null) {
            state.removeOverlapHitbox(overlapHitbox);
        }
        if (solidHitbox != null) {
            state.removeSolidHitbox(solidHitbox);
        }
        if (!animInstances.isEmpty()) {
            for (AnimationInstance instance : animInstances.values()) {
                state.removeAnimInstance(instance);
            }
        }
    }
    
    /**
     * Returns this SpaceObject's time factor.
     * @return This SpaceObject's time factor
     */
    public final long getTimeFactor() {
        return timeFactor;
    }
    
    /**
     * Returns this SpaceObject's effective time factor; that is, the average
     * number of time units it experiences every frame. If it is not assigned to
     * a SpaceState, this will be 0.
     * @return This SpaceObject's effective time factor
     */
    public final long getEffectiveTimeFactor() {
        return (state == null ? 0 : (timeFactor < 0 ? state.getTimeFactor() : timeFactor));
    }
    
    /**
     * Sets this SpaceObject's time factor to the specified value.
     * @param timeFactor The new time factor
     */
    public final void setTimeFactor(long timeFactor) {
        this.timeFactor = timeFactor;
        setTimeFactorActions(timeFactor);
    }
    
    void setTimeFactorActions(long timeFactor) {
        if (!animInstances.isEmpty()) {
            for (AnimationInstance instance : animInstances.values()) {
                instance.setTimeFactor(timeFactor);
            }
        }
    }
    
    /**
     * Returns this SpaceObject's locator Hitbox.
     * @return This SpaceObject's locator Hitbox
     */
    public final Hitbox getLocatorHitbox() {
        return locatorHitbox;
    }
    
    /**
     * Sets this SpaceObject's locator Hitbox to the specified Hitbox. The new
     * locator Hitbox may not be a component of a CompositeHitbox or in use by
     * another SpaceObject.
     * @param locatorHitbox The new locator Hitbox
     * @return Whether the change occurred
     */
    public final boolean setLocatorHitbox(Hitbox<T> locatorHitbox) {
        if (locatorHitbox != null) {
            SpaceObject<T> object = locatorHitbox.getObject();
            Hitbox<T> parent = locatorHitbox.getParent();
            if ((object == null && parent == null)
                    || (object == this && parent == this.locatorHitbox
                    && locatorHitbox.getComponentOf() != this.locatorHitbox)) {
                if (this.locatorHitbox != null) {
                    removeNonLocatorHitboxes(this.locatorHitbox);
                    this.locatorHitbox.removeAsLocatorHitbox();
                }
                this.locatorHitbox = locatorHitbox;
                locatorHitbox.setObject(this);
                addNonLocatorHitboxes(locatorHitbox);
                locatorHitbox.addAsLocatorHitbox(drawPriority);
                return true;
            }
        }
        return false;
    }
    
    void removeNonLocatorHitboxes(Hitbox locatorHitbox) {
        locatorHitbox.removeChild(centerHitbox);
        locatorHitbox.removeChild(overlapHitbox);
        locatorHitbox.removeChild(solidHitbox);
    }
    
    void addNonLocatorHitboxes(Hitbox locatorHitbox) {
        locatorHitbox.addChild(centerHitbox);
        locatorHitbox.addChild(overlapHitbox);
        locatorHitbox.addChild(solidHitbox);
    }
    
    /**
     * Returns this SpaceObject's position.
     * @return This SpaceObject's position
     */
    public final CellVector getPosition() {
        return locatorHitbox.getRelPosition();
    }
    
    /**
     * Returns the x-coordinate of this SpaceObject's position.
     * @return The x-coordinate of this SpaceObject's position
     */
    public final long getX() {
        return locatorHitbox.getRelX();
    }
    
    /**
     * Returns the y-coordinate of this SpaceObject's position.
     * @return The y-coordinate of this SpaceObject's position
     */
    public final long getY() {
        return locatorHitbox.getRelY();
    }
    
    /**
     * Sets this SpaceObject's position to the specified value.
     * @param position The new position
     */
    public final void setPosition(CellVector position) {
        locatorHitbox.setRelPosition(position);
    }
    
    /**
     * Sets this SpaceObject's position to the specified value.
     * @param x The x-coordinate of the new position
     * @param y The y-coordinate of the new position
     */
    public final void setPosition(long x, long y) {
        locatorHitbox.setRelPosition(x, y);
    }
    
    /**
     * Sets the x-coordinate of this SpaceObject's position to the specified
     * value.
     * @param x The x-coordinate of the new position
     */
    public final void setX(long x) {
        locatorHitbox.setRelX(x);
    }
    
    /**
     * Sets the y-coordinate of this SpaceObject's position to the specified
     * value.
     * @param y The y-coordinate of the new position
     */
    public final void setY(long y) {
        locatorHitbox.setRelY(y);
    }
    
    /**
     * Changes this SpaceObject's position by the specified amount.
     * @param change The amount to change the position by
     */
    public final void changePosition(CellVector change) {
        locatorHitbox.changeRelPosition(change);
    }
    
    /**
     * Changes the coordinates of this SpaceObject's position by the specified
     * amounts.
     * @param changeX The amount to change the position's x-coordinate by
     * @param changeY The amount to change the position's y-coordinate by
     */
    public final void changePosition(long changeX, long changeY) {
        locatorHitbox.changeRelPosition(changeX, changeY);
    }
    
    /**
     * Changes the x-coordinate of this SpaceObject's position by the specified
     * amount.
     * @param changeX The amount to change the position's x-coordinate by
     */
    public final void changeX(long changeX) {
        locatorHitbox.changeRelX(changeX);
    }
    
    /**
     * Changes the y-coordinate of this SpaceObject's position by the specified
     * amount.
     * @param changeY The amount to change the position's y-coordinate by
     */
    public final void changeY(long changeY) {
        locatorHitbox.changeRelY(changeY);
    }
    
    /**
     * Returns whether this SpaceObject is horizontally flipped.
     * @return Whether this SpaceObject is horizontally flipped
     */
    public final boolean getXFlip() {
        return locatorHitbox.getRelXFlip();
    }
    
    /**
     * Returns -1 if this SpaceObject is horizontally flipped and 1 if it is
     * not.
     * @return -1 if this SpaceObject is horizontally flipped and 1 if it is not
     */
    public final int getXSign() {
        return locatorHitbox.getRelXSign();
    }
    
    /**
     * Sets whether this SpaceObject is horizontally flipped.
     * @param xFlip Whether this SpaceObject should be horizontally flipped
     */
    public final void setXFlip(boolean xFlip) {
        locatorHitbox.setRelXFlip(xFlip);
    }
    
    /**
     * Flips this SpaceObject horizontally, making it flipped if it was not
     * before and not flipped if it was before.
     */
    public final void flipX() {
        locatorHitbox.relFlipX();
    }
    
    /**
     * Returns whether this SpaceObject is vertically flipped.
     * @return Whether this SpaceObject is vertically flipped
     */
    public final boolean getYFlip() {
        return locatorHitbox.getRelYFlip();
    }
    
    /**
     * Returns -1 if this SpaceObject is vertically flipped and 1 if it is not.
     * @return -1 if this SpaceObject is vertically flipped and 1 if it is not
     */
    public final int getYSign() {
        return locatorHitbox.getRelYSign();
    }
    
    /**
     * Sets whether this SpaceObject is vertically flipped.
     * @param yFlip Whether this SpaceObject should be vertically flipped
     */
    public final void setYFlip(boolean yFlip) {
        locatorHitbox.setRelYFlip(yFlip);
    }
    
    /**
     * Flips this SpaceObject vertically, making it flipped if it was not before
     * and not flipped if it was before.
     */
    public final void flipY() {
        locatorHitbox.relFlipY();
    }
    
    /**
     * Returns this SpaceObject's angle of rotation.
     * @return This SpaceObject's angle of rotation
     */
    public final double getAngle() {
        return locatorHitbox.getRelAngle();
    }
    
    /**
     * Returns the x-coordinate of the unit vector that points in the direction
     * of this SpaceObject's angle of rotation. This is equal to the cosine of
     * the angle.
     * @return The x-coordinate of this SpaceObject's angle of rotation
     */
    public final long getAngleX() {
        return locatorHitbox.getRelAngleX();
    }
    
    /**
     * Returns the y-coordinate of the unit vector that points in the direction
     * of this SpaceObject's angle of rotation. Since y-coordinates increase
     * going downward, this is equal to the negative sine of the angle.
     * @return The y-coordinate of this SpaceObject's angle of rotation
     */
    public final long getAngleY() {
        return locatorHitbox.getRelAngleY();
    }
    
    /**
     * Sets this SpaceObject's angle of rotation to the specified value.
     * @param angle The new angle of rotation
     */
    public final void setAngle(double angle) {
        locatorHitbox.setRelAngle(angle);
    }
    
    /**
     * Changes this SpaceObject's angle of rotation by the specified amount.
     * @param angle The amount to change the angle of rotation by
     */
    public final void changeAngle(double angle) {
        locatorHitbox.changeRelAngle(angle);
    }
    
    /**
     * Returns the x-coordinate of this SpaceObject's absolute left boundary.
     * @return The x-coordinate of this SpaceObject's absolute left boundary
     */
    public final long getLeftEdge() {
        return locatorHitbox.getLeftEdge();
    }
    
    /**
     * Returns the x-coordinate of this SpaceObject's absolute right boundary.
     * @return The x-coordinate of this SpaceObject's absolute right boundary
     */
    public final long getRightEdge() {
        return locatorHitbox.getRightEdge();
    }
    
    /**
     * Returns the y-coordinate of this SpaceObject's absolute top boundary.
     * @return The y-coordinate of this SpaceObject's absolute top boundary
     */
    public final long getTopEdge() {
        return locatorHitbox.getTopEdge();
    }
    
    /**
     * Returns the y-coordinate of this SpaceObject's absolute bottom boundary.
     * @return The y-coordinate of this SpaceObject's absolute bottom boundary
     */
    public final long getBottomEdge() {
        return locatorHitbox.getBottomEdge();
    }
    
    /**
     * Returns this SpaceObject's center's offset.
     * @return This SpaceObject's center's offset
     */
    public final CellVector getCenterOffset() {
        return centerHitbox.getRelPosition();
    }
    
    /**
     * Returns the x-coordinate of this SpaceObject's center's offset.
     * @return The x-coordinate of this SpaceObject's center's offset
     */
    public final long getCenterOffsetX() {
        return centerHitbox.getRelX();
    }
    
    /**
     * Returns the y-coordinate of this SpaceObject's center's offset.
     * @return The y-coordinate of this SpaceObject's center's offset
     */
    public final long getCenterOffsetY() {
        return centerHitbox.getRelY();
    }
    
    /**
     * Sets this SpaceObject's center's offset to the specified value.
     * @param offset 
     */
    public final void setCenterOffset(CellVector offset) {
        centerHitbox.setRelPosition(offset);
    }
    
    /**
     * Sets the coordinates of this SpaceObject's center's offset to the
     * specified values.
     * @param x The new x-coordinate of this SpaceObject's center's offset
     * @param y The new y-coordinate of this SpaceObject's center's offset
     */
    public final void setCenterOffset(long x, long y) {
        centerHitbox.setRelPosition(x, y);
    }
    
    /**
     * Sets the x-coordinate of this SpaceObject's center's offset to the
     * specified value.
     * @param x The new x-coordinate of this SpaceObject's center's offset
     */
    public final void setCenterOffsetX(long x) {
        centerHitbox.setRelX(x);
    }
    
    /**
     * Sets the y-coordinate of this SpaceObject's center's offset to the
     * specified value.
     * @param y The new y-coordinate of this SpaceObject's center's offset
     */
    public final void setCenterOffsetY(long y) {
        centerHitbox.setRelY(y);
    }
    
    /**
     * Returns the absolute position of this SpaceObject's center.
     * @return The absolute position of this SpaceObject's center
     */
    public final CellVector getCenter() {
        return centerHitbox.getAbsPosition();
    }
    
    /**
     * Returns the absolute x-coordinate of this SpaceObject's center.
     * @return The absolute x-coordinate of this SpaceObject's center
     */
    public final long getCenterX() {
        return centerHitbox.getAbsX();
    }
    
    /**
     * Returns the absolute y-coordinate of this SpaceObject's center.
     * @return The absolute y-coordinate of this SpaceObject's center
     */
    public final long getCenterY() {
        return centerHitbox.getAbsY();
    }
    
    /**
     * Returns the distance from this SpaceObject's center to the specified
     * SpaceObject's center.
     * @param object The SpaceObject to return the distance to
     * @return The distance from this SpaceObject's center to the specified
     * SpaceObject's center
     */
    public final long distanceTo(SpaceObject object) {
        return centerHitbox.distanceTo(object.centerHitbox);
    }
    
    /**
     * Returns the angle from this SpaceObject's center to the specified
     * SpaceObject's center.
     * @param object The SpaceObject to return the angle to
     * @return The angle from this SpaceObject's center to the specified
     * SpaceObject's center
     */
    public final double angleTo(SpaceObject object) {
        return centerHitbox.angleTo(object.centerHitbox);
    }
    
    /**
     * Returns the SpaceObject of the specified class in this SpaceObject's
     * SpaceState that is nearest to it.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class that is nearest to this
     * SpaceObject
     */
    public final <O extends SpaceObject<T>> O nearestObject(Class<O> cls) {
        return (state == null ? null : state.nearestObject(centerHitbox.getAbsX(), centerHitbox.getAbsY(), cls));
    }
    
    /**
     * Returns the SpaceObject of the specified class within the specified
     * rectangular region in this SpaceObject's SpaceState that is nearest to
     * it.
     * @param <O> The subclass of SpaceObject to search for
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class within the specified
     * rectangular region that is nearest to this SpaceObject
     */
    public final <O extends SpaceObject<T>> O nearestObjectWithinRectangle(long x1, long y1, long x2, long y2, Class<O> cls) {
        return (state == null ? null : state.nearestObjectWithinRectangle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), x1, y1, x2, y2, cls));
    }
    
    /**
     * Returns the SpaceObject of the specified class within the specified
     * circular region in this SpaceObject's SpaceState that is nearest to it.
     * @param <O> The subclass of SpaceObject to search for
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class within the specified
     * circular region that is nearest to this SpaceObject
     */
    public final <O extends SpaceObject<T>> O nearestObjectWithinCircle(CellVector center, long radius, Class<O> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), center.getX(), center.getY(), radius, cls));
    }
    
    /**
     * Returns the SpaceObject of the specified class within the specified
     * circular region in this SpaceObject's SpaceState that is nearest to it.
     * @param <O> The subclass of SpaceObject to search for
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class within the specified
     * circular region that is nearest to this SpaceObject
     */
    public final <O extends SpaceObject<T>> O nearestObjectWithinCircle(long centerX, long centerY, long radius, Class<O> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), centerX, centerY, radius, cls));
    }
    
    /**
     * Returns the SpaceObject of the specified class that overlaps the
     * specified Hitbox in this SpaceObject's SpaceState that is nearest to it.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return the SpaceObject of the specified class that overlaps the
     * specified Hitbox that is nearest to this SpaceObject
     */
    public final <O extends SpaceObject<T>> O nearestOverlappingObject(Hitbox hitbox, Class<O> cls) {
        return (state == null ? null : state.nearestOverlappingObject(centerHitbox.getAbsX(), centerHitbox.getAbsY(), hitbox, cls));
    }
    
    /**
     * Returns whether there are any SpaceObjects of the specified class within
     * the specified radius of this SpaceObject in its SpaceState.
     * @param <O> The subclass of SpaceObject to search for
     * @param radius The radius of this SpaceObject to search within
     * @param cls The Class object that represents the SpaceObject subclass
     * @return Whether there are any SpaceObjects of the specified class within
     * the specified radius of this SpaceObject
     */
    public final <O extends SpaceObject<T>> boolean objectIsWithinRadius(long radius, Class<O> cls) {
        return (state == null ? false : state.objectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls) != null);
    }
    
    /**
     * Returns a SpaceObject of the specified class within the specified radius
     * of this SpaceObject in its SpaceState, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param radius The radius of this SpaceObject to search within
     * @param cls The Class object that represents the SpaceObject subclass
     * @return A SpaceObject of the specified class within the specified radius
     * of this SpaceObject
     */
    public final <O extends SpaceObject<T>> O objectWithinRadius(long radius, Class<O> cls) {
        return (state == null ? null : state.objectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
    }
    
    /**
     * Returns all of the SpaceObjects of the specified class within the
     * specified radius of this SpaceObject in its SpaceState.
     * @param <O> The subclass of SpaceObject to search for
     * @param radius The radius of this SpaceObject to search within
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class within the
     * specified radius of this SpaceObject
     */
    public final <O extends SpaceObject<T>> List<O> objectsWithinRadius(long radius, Class<O> cls) {
        return (state == null ? new ArrayList<>() : state.objectsWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
    }
    
    /**
     * Returns the SpaceObject of the specified class within the specified
     * radius of this SpaceObject in its SpaceState that is nearest to it.
     * @param <O> The subclass of SpaceObject to search for
     * @param radius The radius of this SpaceObject to search within
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class within the specified
     * radius of this SpaceObject that is nearest to it
     */
    public final <O extends SpaceObject<T>> O nearestObjectWithinRadius(long radius, Class<O> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
    }
    
    /**
     * Returns this SpaceObject's overlap Hitbox, or null if it has none.
     * @return This SpaceObject's overlap Hitbox
     */
    public final Hitbox getOverlapHitbox() {
        return overlapHitbox;
    }
    
    /**
     * Sets this SpaceObject's overlap Hitbox to the specified Hitbox. The new
     * overlap Hitbox may not be a component of a CompositeHitbox or in use by
     * another SpaceObject. If the specified Hitbox is null, the current overlap
     * Hitbox will be removed if there is one, but it will not be replaced with
     * anything.
     * @param overlapHitbox The new overlap Hitbox
     * @return Whether the change occurred
     */
    public final boolean setOverlapHitbox(Hitbox<T> overlapHitbox) {
        if (overlapHitbox != this.overlapHitbox) {
            boolean acceptable;
            if (overlapHitbox == null) {
                acceptable = true;
            } else {
                SpaceObject<T> object = overlapHitbox.getObject();
                Hitbox<T> parent = overlapHitbox.getParent();
                acceptable = (object == null && parent == null)
                        || (overlapHitbox == locatorHitbox)
                        || (object == this && parent == locatorHitbox
                        && overlapHitbox.getComponentOf() != locatorHitbox);
            }
            if (acceptable) {
                if (this.overlapHitbox != null) {
                    this.overlapHitbox.removeAsOverlapHitbox();
                }
                this.overlapHitbox = overlapHitbox;
                if (overlapHitbox != null) {
                    locatorHitbox.addChild(overlapHitbox);
                    overlapHitbox.addAsOverlapHitbox();
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns whether this SpaceObject overlaps the specified SpaceObject.
     * @param object The SpaceObject to check for an overlap
     * @return Whether this SpaceObject overlaps the specified SpaceObject
     */
    public final boolean overlaps(SpaceObject<T> object) {
        return overlap(this, object);
    }
    
    /**
     * Returns whether the two specified SpaceObjects overlap.
     * @param <T> The subclass of CellGame that uses the SpaceStates that the
     * two SpaceObjects can be assigned to
     * @param object1 The first SpaceObject
     * @param object2 The second SpaceObject
     * @return Whether the two SpaceObjects overlap
     */
    public static final <T extends CellGame> boolean overlap(SpaceObject<T> object1, SpaceObject<T> object2) {
        return object1.overlapHitbox != null && object2.overlapHitbox != null
                && Hitbox.overlap(object1.overlapHitbox, object2.overlapHitbox);
    }
    
    /**
     * Returns whether this SpaceObject is overlapping a SpaceObject of the
     * specified class in its SpaceState.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return Whether this SpaceObject is overlapping a SpaceObject of the
     * specified class
     */
    public final <O extends SpaceObject<T>> boolean isOverlappingObject(Class<O> cls) {
        return (state == null || overlapHitbox == null ? false : state.overlappingObject(overlapHitbox, cls) != null);
    }
    
    /**
     * Returns a SpaceObject of the specified class in this SpaceObject's
     * SpaceState that is overlapping it, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return A SpaceObject of the specified class that is overlapping this
     * SpaceObject
     */
    public final <O extends SpaceObject<T>> O overlappingObject(Class<O> cls) {
        return (state == null || overlapHitbox == null ? null : state.overlappingObject(overlapHitbox, cls));
    }
    
    /**
     * Returns all of the SpaceObjects of the specified class in this
     * SpaceObject's SpaceState that are overlapping it.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class in this
     * SpaceObject's SpaceState that are overlapping it
     */
    public final <O extends SpaceObject<T>> List<O> overlappingObjects(Class<O> cls) {
        return (state == null || overlapHitbox == null ? new ArrayList<>() : state.overlappingObjects(overlapHitbox, cls));
    }
    
    /**
     * Returns all of the SpaceObjects of the specified class in this
     * SpaceObject's SpaceState whose overlap Hitboxes' rectangular bounding
     * boxes touch or intersect this SpaceObject's overlap Hitbox's rectangular
     * bounding box.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class whose overlap
     * Hitboxes' bounding boxes meet this SpaceObject's overlap Hitbox's
     * bounding box
     */
    public final <O extends SpaceObject<T>> List<O> boundingBoxesMeet(Class<O> cls) {
        return (state == null || overlapHitbox == null ? new ArrayList<>() : state.boundingBoxesMeet(overlapHitbox, cls));
    }
    
    /**
     * Returns whether this SpaceObject is overlapping the solid Hitbox of a
     * solid SpaceObject of the specified class in its SpaceState.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return Whether this SpaceObject is overlapping the solid Hitbox of a
     * solid SpaceObject of the specified class
     */
    public final <O extends SpaceObject<T>> boolean isIntersectingSolidObject(Class<O> cls) {
        return (state == null || overlapHitbox == null ? false : state.intersectingSolidObject(overlapHitbox, cls) != null);
    }
    
    /**
     * Returns a solid SpaceObject of the specified class in this SpaceObject's
     * SpaceState whose solid Hitbox is overlapping it, or null if there is
     * none.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return A solid SpaceObject of the specified class whose solid Hitbox is
     * overlapping this SpaceObject
     */
    public final <O extends SpaceObject<T>> O intersectingSolidObject(Class<O> cls) {
        return (state == null || overlapHitbox == null ? null : state.intersectingSolidObject(overlapHitbox, cls));
    }
    
    /**
     * Returns all of the solid SpaceObjects of the specified class in this
     * SpaceObject's SpaceState whose solid Hitboxes are overlapping it.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the solid SpaceObjects of the specified class whose solid
     * Hitboxes are overlapping this SpaceObject
     */
    public final <O extends SpaceObject<T>> List<O> intersectingSolidObjects(Class<O> cls) {
        return (state == null || overlapHitbox == null ? new ArrayList<>() : state.intersectingSolidObjects(overlapHitbox, cls));
    }
    
    /**
     * Returns this SpaceObject's solid Hitbox, or null if it has none.
     * @return This SpaceObject's solid Hitbox
     */
    public final Hitbox getSolidHitbox() {
        return solidHitbox;
    }
    
    /**
     * Sets this SpaceObject's solid Hitbox to the specified Hitbox. The new
     * solid Hitbox may not be a component of a CompositeHitbox or in use by
     * another SpaceObject. If the specified Hitbox is null, the current solid
     * Hitbox will be removed if there is one, but it will not be replaced with
     * anything.
     * @param solidHitbox The new solid Hitbox
     * @return Whether the change occurred
     */
    public final boolean setSolidHitbox(Hitbox solidHitbox) {
        if (solidHitbox != this.solidHitbox) {
            boolean acceptable;
            if (solidHitbox == null) {
                acceptable = true;
            } else {
                SpaceObject object = solidHitbox.getObject();
                Hitbox parent = solidHitbox.getParent();
                acceptable = (object == null && parent == null)
                        || (solidHitbox == locatorHitbox)
                        || (object == this && parent == locatorHitbox
                        && solidHitbox.getComponentOf() != locatorHitbox);
            }
            if (acceptable) {
                if (this.solidHitbox != null) {
                    this.solidHitbox.removeAsSolidHitbox();
                }
                this.solidHitbox = solidHitbox;
                if (solidHitbox != null) {
                    locatorHitbox.addChild(solidHitbox);
                    solidHitbox.addAsSolidHitbox();
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns whether any of this SpaceObject's surfaces are solid.
     * @return Whether any of this SpaceObject's surfaces are solid
     */
    public final boolean isSolid() {
        return (solidHitbox == null ? false : solidHitbox.isSolid());
    }
    
    /**
     * Returns whether this SpaceObject's surface in the specified Direction is
     * solid.
     * @param direction The Direction of the surface to be examined
     * @return Whether the surface in the specified Direction is solid
     */
    public final boolean surfaceIsSolid(Direction direction) {
        return (solidHitbox == null ? false : solidHitbox.surfaceIsSolid(direction));
    }
    
    /**
     * Sets whether this SpaceObject's surface in the specified Direction is
     * solid. If the surface is being made solid but this SpaceObject has no
     * solid Hitbox, a copy of its locator Hitbox will be created to serve as
     * one.
     * @param direction The Direction of the surface whose solidity is to be set
     * @param solid Whether the surface in the specified Direction should be
     * solid
     */
    public final void setSurfaceSolid(Direction direction, boolean solid) {
        if (solid && solidHitbox == null) {
            setSolidHitbox(locatorHitbox.getCopy());
        }
        if (solidHitbox != null) {
            solidHitbox.setSurfaceSolid(direction, solid);
        }
    }
    
    /**
     * Sets whether this SpaceObject's surfaces in every direction are solid. If
     * the surface is being made solid but this SpaceObject has no solid Hitbox,
     * a copy of its locator Hitbox will be created to serve as one.
     * @param solid Whether this SpaceObject's surfaces in every direction
     * should be solid
     */
    public final void setSolid(boolean solid) {
        if (solid && solidHitbox == null) {
            setSolidHitbox(locatorHitbox.getCopy());
        }
        if (solidHitbox != null) {
            solidHitbox.setSolid(solid);
        }
    }
    
    /**
     * Returns this SpaceObject's draw priority.
     * @return This SpaceObject's draw priority
     */
    public final int getDrawPriority() {
        return drawPriority;
    }
    
    /**
     * Sets this SpaceObject's draw priority to the specified value.
     * @param drawPriority The new draw priority
     */
    public final void setDrawPriority(int drawPriority) {
        this.drawPriority = drawPriority;
        locatorHitbox.changeDrawPriority(drawPriority);
    }
    
    /**
     * Returns this SpaceObject's appearance.
     * @return This SpaceObject's appearance
     */
    public final Drawable getAppearance() {
        return appearance;
    }
    
    /**
     * Sets this SpaceObject's appearance to the specified Drawable.
     * @param appearance The new appearance
     */
    public final void setAppearance(Drawable appearance) {
        this.appearance = appearance;
    }
    
    /**
     * Returns the AnimationInstance that is assigned to this SpaceObject with
     * the specified ID, or AnimationInstance.BLANK if there is none.
     * @param id The ID of the AnimationInstance to be returned
     * @return The AnimationInstance that is assigned to this SpaceObject with
     * the specified ID
     */
    public final AnimationInstance getAnimInstance(int id) {
        AnimationInstance instance = animInstances.get(id);
        return (instance == null ? AnimationInstance.BLANK : instance);
    }
    
    /**
     * Sets the AnimationInstance that is assigned to this SpaceObject with the
     * specified ID to the specified AnimationInstance, if it is not already
     * assigned to a CellGameState. If there is already an AnimationInstance
     * assigned with the specified ID, it will be removed from this SpaceObject.
     * @param id The ID with which to assign the specified AnimationInstance
     * @param instance The AnimationInstance to add with the specified ID
     * @return Whether the addition occurred
     */
    public final boolean setAnimInstance(int id, AnimationInstance instance) {
        if (instance == AnimationInstance.BLANK) {
            AnimationInstance oldInstance = animInstances.remove(id);
            if (oldInstance != null && state != null) {
                state.removeAnimInstance(oldInstance);
            }
            return true;
        }
        if (instance.getGameState() == null) {
            instance.setTimeFactor(timeFactor);
            AnimationInstance oldInstance = animInstances.put(id, instance);
            if (state != null) {
                if (oldInstance != null) {
                    state.removeAnimInstance(oldInstance);
                }
                state.addAnimInstance(instance);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Sets both this SpaceObject's appearance and its AnimationInstance with ID
     * 0 to the specified AnimationInstance, if it is not already assigned to a
     * CellGameState.
     * @param instance The new appearance and AnimationInstance with ID 0
     * @return Whether the change occurred
     */
    public final boolean setAnimInstance(AnimationInstance instance) {
        if (setAnimInstance(0, instance)) {
            appearance = instance;
            return true;
        }
        return false;
    }
    
    /**
     * Returns the Animation of the AnimationInstance assigned to this
     * SpaceObject with the specified ID, or Animation.BLANK if there is none.
     * @param id The ID of the AnimationInstance whose Animation is to be
     * returned
     * @return The Animation of the AnimationInstance assigned to this
     * SpaceObject with the specified ID
     */
    public final Animation getAnimation(int id) {
        return getAnimInstance(id).getAnimation();
    }
    
    /**
     * Sets the AnimationInstance that is assigned to this SpaceObject with the
     * specified ID to a new AnimationInstance of the specified Animation, if
     * there is not already an AnimationInstance of that Animation assigned
     * with that ID. In other words, this method will not replace an
     * AnimationInstance with another of the same Animation. If there is already
     * an AnimationInstance assigned with the specified ID, it will be removed
     * from this SpaceObject.
     * @param id The ID with which to assign the new AnimationInstance
     * @param animation The Animation to add a new AnimationInstance of
     * @return The AnimationInstance assigned with the specified ID
     */
    public final AnimationInstance setAnimation(int id, Animation animation) {
        AnimationInstance instance = getAnimInstance(id);
        if (instance.getAnimation() != animation) {
            if (animation == Animation.BLANK) {
                AnimationInstance oldInstance = animInstances.remove(id);
                if (oldInstance != null && state != null) {
                    state.removeAnimInstance(oldInstance);
                }
                return AnimationInstance.BLANK;
            }
            instance = new AnimationInstance(animation);
            instance.setTimeFactor(timeFactor);
            AnimationInstance oldInstance = animInstances.put(id, instance);
            if (state != null) {
                if (oldInstance != null) {
                    state.removeAnimInstance(oldInstance);
                }
                state.addAnimInstance(instance);
            }
        }
        return instance;
    }
    
    /**
     * Returns the Animation of the AnimationInstance assigned to this
     * SpaceObject with ID 0, or Animation.BLANK if there is none.
     * @return The Animation of the AnimationInstance assigned to this
     * SpaceObject with ID 0
     */
    public final Animation getAnimation() {
        return getAnimInstance(0).getAnimation();
    }
    
    /**
     * Sets both this SpaceObject's appearance and its AnimationInstance with ID
     * 0 to a new AnimationInstance of the specified Animation, if there is not
     * already an AnimationInstance of that Animation assigned with ID 0.
     * @param animation The Animation to make the new AnimationInstance of
     * @return Whether the change occurred
     */
    public final AnimationInstance setAnimation(Animation animation) {
        AnimationInstance instance = setAnimation(0, animation);
        appearance = instance;
        return instance;
    }
    
    /**
     * Removes from this SpaceObject all AnimationInstances that are currently
     * assigned to it.
     */
    public final void clearAnimInstances() {
        if (state != null) {
            for (AnimationInstance instance : animInstances.values()) {
                state.removeAnimInstance(instance);
            }
        }
        animInstances.clear();
    }
    
    /**
     * Returns this SpaceObject's alpha value.
     * @return This SpaceObject's alpha value
     */
    public final double getAlpha() {
        return alpha;
    }
    
    /**
     * Sets this SpaceObject's alpha value to the specified value.
     * @param alpha The new alpha value
     */
    public final void setAlpha(double alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
    }
    
    /**
     * Returns this SpaceObject's Filter, or null if it has none.
     * @return This SpaceObject's Filter
     */
    public final Filter getFilter() {
        return filter;
    }
    
    /**
     * Sets this SpaceObject's Filter to the specified Filter, or to none if the
     * specified Filter is null.
     * @param filter The new Filter
     */
    public final void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    /**
     * Returns whether any part of this SpaceObject's rectangular bounding box
     * is visible through any of its SpaceState's Viewports.
     * @return Whether this SpaceObject is visible through any of its
     * SpaceState's Viewports
     */
    public final boolean isVisible() {
        return (state == null ? false : state.rectangleIsVisible(getLeftEdge(), getTopEdge(), getRightEdge(), getBottomEdge()));
    }
    
    /**
     * Returns whether any part of this SpaceObject's rectangular bounding box
     * is visible through the specified Viewport.
     * @param viewport The Viewport to check
     * @return Whether this SpaceObject is visible through the specified
     * Viewport
     */
    public final boolean isVisible(Viewport viewport) {
        return viewport.rectangleIsVisible(getLeftEdge(), getTopEdge(), getRightEdge(), getBottomEdge());
    }
    
    /**
     * Draws this SpaceObject as seen through a Viewport's camera.
     * @param g The Graphics context to which this SpaceObject is being drawn
     * this frame
     * @param x The x-coordinate in pixels on the Graphics context that
     * corresponds to the x-coordinate of this SpaceObject's position
     * @param y The y-coordinate in pixels on the Graphics context that
     * corresponds to the y-coordinate of this SpaceObject's position
     */
    public void draw(Graphics g, int x, int y) {
        appearance.draw(g, x, y, getXFlip(), getYFlip(), getAngle(), alpha, filter);
    }
    
}
