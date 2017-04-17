package cell2D.level;

import cell2D.Animation;
import cell2D.AnimationInstance;
import cell2D.CellGame;
import cell2D.Drawable;
import cell2D.Filter;
import cell2D.Sprite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.newdawn.slick.Graphics;

/**
 * <p>A LevelObject is a physical object in a LevelState's space. LevelObjects
 * may be assigned to one LevelState each in much the same way that Thinkers are
 * assigned to CellGameStates. A LevelObject's assigned LevelState will keep
 * track of time for it and its AnimationInstances. A LevelObject's time factor
 * represents how many time units the LevelObject will experience every frame
 * while assigned to an active LevelState. If its own time factor is negative,
 * a LevelObject will use its assigned LevelState's time factor instead. If a
 * LevelObject is assigned to an inactive LevelState or none at all, time will
 * not pass for it.</p>
 * 
 * <p>A LevelObject inherits the position, flipped status, angle of rotation,
 * and rectangular bounding box of a locator Hitbox that is relative to no other
 * Hitbox. A LevelObject may also have an overlap Hitbox that represents it for
 * purposes of overlapping other LevelObjects and/or a solid Hitbox that
 * represents it for purposes of surface solidity and ThinkerObjects colliding
 * with it. The solid Hitbox's rectangular bounding box, rather than its exact
 * shape, is what represents the LevelObject in Cell2D's standard collision
 * mechanics. All of a LevelState's Hitboxes other than its locator Hitbox have
 * positions, flipped statuses, and angles of rotation that are relative to
 * those of its locator Hitbox. A LevelObject may use a single Hitbox for more
 * than one of these purposes, but a Hitbox may not be used by multiple
 * LevelObjects at once.</p>
 * 
 * <p>A LevelObject has a point called a center that summarizes its location.
 * Its center has an offset that is relative to the LevelObject's position,
 * flipped status, and angle of rotation. LevelObjects' centers are the points
 * from which their distances and angles to other LevelObjects are measured.</p>
 * 
 * <p>A LevelObject has a Drawable appearance that represents it as seen through
 * a Viewport's camera, as well as an alpha (opacity) value that is normalized
 * to be between 0 to 1 and a Filter that apply to its appearance. The use of a
 * LevelObject's appearance, alpha value, and Filter to represent it is a result
 * of its default draw() method, which can be overridden. A LevelObject will
 * only be drawn if its locator Hitbox's rectangular bounding box intersects the
 * Viewport's field of view. A LevelObject's draw priority determines whether it
 * will be drawn in front of or behind other LevelObjects that intersect it.
 * LevelObjects with higher draw priorities are drawn in front of those with
 * lower ones.</p>
 * 
 * <p>If an AnimationInstance is not already assigned to a CellGameState, it
 * may be assigned to a LevelObject with an integer ID in the context of that
 * LevelObject. Only one AnimationInstance may be assigned to a given
 * LevelObject with a given ID at once. A LevelObject will automatically set its
 * assigned AnimationInstances' time factors and add and remove them from
 * LevelStates as appropriate to match its own time factor and assigned
 * LevelState.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the LevelStates that can use
 * this LevelObject
 */
public abstract class LevelObject<T extends CellGame> {
    
    LevelState<T> state = null;
    LevelState<T> newState = null;
    private double timeFactor = -1;
    private Hitbox<T> locatorHitbox = null;
    private final Hitbox<T> centerHitbox;
    private Hitbox<T> overlapHitbox = null;
    private Hitbox<T> solidHitbox = null;
    private int drawPriority = 0;
    private Drawable appearance = Sprite.BLANK;
    private final Map<Integer,AnimationInstance> animInstances = new HashMap<>();
    private double alpha = 1;
    private Filter filter = null;
    
    /**
     * Creates a new LevelObject with the specified locator Hitbox.
     * @param locatorHitbox This LevelObject's locator Hitbox
     */
    public LevelObject(Hitbox<T> locatorHitbox) {
        centerHitbox = new PointHitbox<>(0, 0);
        centerHitbox.addAsCenterHitbox();
        if (!setLocatorHitbox(locatorHitbox)) {
            throw new RuntimeException("Attempted to create a LevelObject with an invalid locator hitbox");
        }
    }
    
    /**
     * Creates a new LevelObject with the specified locator Hitbox that acts as
     * if it was created by the specified LevelObject, initially copying its
     * creator's time factor, flipped status, and angle of rotation.
     * @param locatorHitbox This LevelObject's locator Hitbox
     * @param creator This LevelObject's creator
     */
    public LevelObject(Hitbox<T> locatorHitbox, LevelObject<T> creator) {
        this(locatorHitbox);
        setTimeFactor(creator.timeFactor);
        setXFlip(creator.getXFlip());
        setYFlip(creator.getYFlip());
        setAngle(creator.getAngle());
    }
    
    /**
     * Returns the LevelState to which this LevelObject is currently assigned,
     * or null if it is assigned to none.
     * @return The LevelState to which this LevelObject is currently assigned
     */
    public final LevelState<T> getGameState() {
        return state;
    }
    
    /**
     * Returns the LevelState to which this LevelObject is about to be assigned,
     * but has not yet been due to one or more of the object lists involved
     * being iterated over. If this LevelObject is about to be removed from its
     * LevelState without being added to a new one afterward, this will be null.
     * If this LevelObject is not about to change LevelStates, this method will
     * simply return its current LevelState.
     * @return The LevelState to which this LevelObject is about to be assigned
     */
    public final LevelState<T> getNewGameState() {
        return newState;
    }
    
    /**
     * Sets the LevelState to which this LevelObject is currently assigned. If
     * it is set to a null LevelState, this LevelObject will be removed from its
     * current LevelState if it has one.
     * @param state The LevelState to which this LevelObject should be assigned
     */
    public final void setGameState(LevelState<T> state) {
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
            state.addAllSolidSurfaces(solidHitbox);
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
            state.removeAllSolidSurfaces(solidHitbox);
        }
        if (!animInstances.isEmpty()) {
            for (AnimationInstance instance : animInstances.values()) {
                state.removeAnimInstance(instance);
            }
        }
    }
    
    /**
     * Returns this LevelObject's time factor.
     * @return This LevelObject's time factor
     */
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    /**
     * Returns this LevelObject's effective time factor; that is, how many time
     * units it experiences every frame. If it is not assigned to a LevelState,
     * this will be 0.
     * @return This LevelObject's effective time factor
     */
    public final double getEffectiveTimeFactor() {
        return (state == null ? 0 : (timeFactor < 0 ? state.getTimeFactor() : timeFactor));
    }
    
    /**
     * Sets this LevelObject's time factor to the specified value.
     * @param timeFactor The new time factor
     */
    public final void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
        setTimeFactorActions(timeFactor);
    }
    
    void setTimeFactorActions(double timeFactor) {
        if (!animInstances.isEmpty()) {
            for (AnimationInstance instance : animInstances.values()) {
                instance.setTimeFactor(timeFactor);
            }
        }
    }
    
    /**
     * Returns this LevelObject's locator Hitbox.
     * @return This LevelObject's locator Hitbox
     */
    public final Hitbox getLocatorHitbox() {
        return locatorHitbox;
    }
    
    /**
     * Sets this LevelObject's locator Hitbox to the specified Hitbox. The new
     * locator Hitbox may not be a component of a CompositeHitbox or in use by
     * another LevelObject.
     * @param locatorHitbox The new locator Hitbox
     * @return Whether the change occurred
     */
    public final boolean setLocatorHitbox(Hitbox<T> locatorHitbox) {
        if (locatorHitbox != null) {
            LevelObject<T> object = locatorHitbox.getObject();
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
     * Returns this LevelObject's position.
     * @return This LevelObject's position
     */
    public final LevelVector getPosition() {
        return locatorHitbox.getRelPosition();
    }
    
    /**
     * Returns the x-coordinate of this LevelObject's position.
     * @return The x-coordinate of this LevelObject's position
     */
    public final double getX() {
        return locatorHitbox.getRelX();
    }
    
    /**
     * Returns the y-coordinate of this LevelObject's position.
     * @return The y-coordinate of this LevelObject's position
     */
    public final double getY() {
        return locatorHitbox.getRelY();
    }
    
    /**
     * Sets this LevelObject's position to the specified value.
     * @param position The new position
     */
    public final void setPosition(LevelVector position) {
        locatorHitbox.setRelPosition(position);
    }
    
    /**
     * Sets this LevelObject's position to the specified value.
     * @param x The x-coordinate of the new position
     * @param y The y-coordinate of the new position
     */
    public final void setPosition(double x, double y) {
        locatorHitbox.setRelPosition(x, y);
    }
    
    /**
     * Sets the x-coordinate of this LevelObject's position to the specified
     * value.
     * @param x The x-coordinate of the new position
     */
    public final void setX(double x) {
        locatorHitbox.setRelX(x);
    }
    
    /**
     * Sets the y-coordinate of this LevelObject's position to the specified
     * value.
     * @param y The y-coordinate of the new position
     */
    public final void setY(double y) {
        locatorHitbox.setRelY(y);
    }
    
    /**
     * Changes this LevelObject's position by the specified amount.
     * @param change The amount to change the position by
     */
    public final void changePosition(LevelVector change) {
        locatorHitbox.changeRelPosition(change);
    }
    
    /**
     * Changes the coordinates of this LevelObject's position by the specified
     * amounts.
     * @param changeX The amount to change the position's x-coordinate by
     * @param changeY The amount to change the position's y-coordinate by
     */
    public final void changePosition(double changeX, double changeY) {
        locatorHitbox.changeRelPosition(changeX, changeY);
    }
    
    /**
     * Changes the x-coordinate of this LevelObject's position by the specified
     * amount.
     * @param changeX The amount to change the position's x-coordinate by
     */
    public final void changeX(double changeX) {
        locatorHitbox.changeRelX(changeX);
    }
    
    /**
     * Changes the y-coordinate of this LevelObject's position by the specified
     * amount.
     * @param changeY The amount to change the position's y-coordinate by
     */
    public final void changeY(double changeY) {
        locatorHitbox.changeRelY(changeY);
    }
    
    /**
     * Returns whether this LevelObject is horizontally flipped.
     * @return Whether this LevelObject is horizontally flipped
     */
    public final boolean getXFlip() {
        return locatorHitbox.getRelXFlip();
    }
    
    /**
     * Returns -1 if this LevelObject is horizontally flipped and 1 if it is
     * not.
     * @return -1 if this LevelObject is horizontally flipped and 1 if it is not
     */
    public final int getXSign() {
        return locatorHitbox.getRelXSign();
    }
    
    /**
     * Sets whether this LevelObject is horizontally flipped.
     * @param xFlip Whether this LevelObject should be horizontally flipped
     */
    public final void setXFlip(boolean xFlip) {
        locatorHitbox.setRelXFlip(xFlip);
    }
    
    /**
     * Flips this LevelObject horizontally, making it flipped if it was not
     * before and not flipped if it was before.
     */
    public final void flipX() {
        locatorHitbox.relFlipX();
    }
    
    /**
     * Returns whether this LevelObject is vertically flipped.
     * @return Whether this LevelObject is vertically flipped
     */
    public final boolean getYFlip() {
        return locatorHitbox.getRelYFlip();
    }
    
    /**
     * Returns -1 if this LevelObject is vertically flipped and 1 if it is not.
     * @return -1 if this LevelObject is vertically flipped and 1 if it is not
     */
    public final int getYSign() {
        return locatorHitbox.getRelYSign();
    }
    
    /**
     * Sets whether this LevelObject is vertically flipped.
     * @param yFlip Whether this LevelObject should be vertically flipped
     */
    public final void setYFlip(boolean yFlip) {
        locatorHitbox.setRelYFlip(yFlip);
    }
    
    /**
     * Flips this LevelObject vertically, making it flipped if it was not before
     * and not flipped if it was before.
     */
    public final void flipY() {
        locatorHitbox.relFlipY();
    }
    
    /**
     * Returns this LevelObject's angle of rotation.
     * @return This LevelObject's angle of rotation
     */
    public final double getAngle() {
        return locatorHitbox.getRelAngle();
    }
    
    /**
     * Returns the x-coordinate of the unit vector that points in the direction
     * of this LevelObject's angle of rotation. This is equal to the cosine of
     * the angle.
     * @return The x-coordinate of this LevelObject's angle of rotation
     */
    public final double getAngleX() {
        return locatorHitbox.getRelAngleX();
    }
    
    /**
     * Returns the y-coordinate of the unit vector that points in the direction
     * of this LevelObject's angle of rotation. Since y-coordinates increase
     * going downward, this is equal to the negative sine of the angle.
     * @return The y-coordinate of this LevelObject's angle of rotation
     */
    public final double getAngleY() {
        return locatorHitbox.getRelAngleY();
    }
    
    /**
     * Sets this LevelObject's angle of rotation to the specified value.
     * @param angle The new angle of rotation
     */
    public final void setAngle(double angle) {
        locatorHitbox.setRelAngle(angle);
    }
    
    /**
     * Changes this LevelObject's angle of rotation by the specified amount.
     * @param angle The amount to change the angle of rotation by
     */
    public final void changeAngle(double angle) {
        locatorHitbox.changeRelAngle(angle);
    }
    
    /**
     * Returns the x-coordinate of this LevelObject's absolute left boundary.
     * @return The x-coordinate of this LevelObject's absolute left boundary
     */
    public final double getLeftEdge() {
        return locatorHitbox.getLeftEdge();
    }
    
    /**
     * Returns the x-coordinate of this LevelObject's absolute right boundary.
     * @return The x-coordinate of this LevelObject's absolute right boundary
     */
    public final double getRightEdge() {
        return locatorHitbox.getRightEdge();
    }
    
    /**
     * Returns the y-coordinate of this LevelObject's absolute top boundary.
     * @return The y-coordinate of this LevelObject's absolute top boundary
     */
    public final double getTopEdge() {
        return locatorHitbox.getTopEdge();
    }
    
    /**
     * Returns the y-coordinate of this LevelObject's absolute bottom boundary.
     * @return The y-coordinate of this LevelObject's absolute bottom boundary
     */
    public final double getBottomEdge() {
        return locatorHitbox.getBottomEdge();
    }
    
    /**
     * Returns this LevelObject's center's offset.
     * @return This LevelObject's center's offset
     */
    public final LevelVector getCenterOffset() {
        return centerHitbox.getRelPosition();
    }
    
    /**
     * Returns the x-coordinate of this LevelObject's center's offset.
     * @return The x-coordinate of this LevelObject's center's offset
     */
    public final double getCenterOffsetX() {
        return centerHitbox.getRelX();
    }
    
    /**
     * Returns the y-coordinate of this LevelObject's center's offset.
     * @return The y-coordinate of this LevelObject's center's offset
     */
    public final double getCenterOffsetY() {
        return centerHitbox.getRelY();
    }
    
    /**
     * Sets this LevelObject's center's offset to the specified value.
     * @param offset 
     */
    public final void setCenterOffset(LevelVector offset) {
        centerHitbox.setRelPosition(offset);
    }
    
    /**
     * Sets the coordinates of this LevelObject's center's offset to the
     * specified values.
     * @param x The new x-coordinate of this LevelObject's center's offset
     * @param y The new y-coordinate of this LevelObject's center's offset
     */
    public final void setCenterOffset(double x, double y) {
        centerHitbox.setRelPosition(x, y);
    }
    
    /**
     * Sets the x-coordinate of this LevelObject's center's offset to the
     * specified value.
     * @param x The new x-coordinate of this LevelObject's center's offset
     */
    public final void setCenterOffsetX(double x) {
        centerHitbox.setRelX(x);
    }
    
    /**
     * Sets the y-coordinate of this LevelObject's center's offset to the
     * specified value.
     * @param y The new y-coordinate of this LevelObject's center's offset
     */
    public final void setCenterOffsetY(double y) {
        centerHitbox.setRelY(y);
    }
    
    /**
     * Returns the absolute position of this LevelObject's center.
     * @return The absolute position of this LevelObject's center
     */
    public final LevelVector getCenter() {
        return centerHitbox.getAbsPosition();
    }
    
    /**
     * Returns the absolute x-coordinate of this LevelObject's center.
     * @return The absolute x-coordinate of this LevelObject's center
     */
    public final double getCenterX() {
        return centerHitbox.getAbsX();
    }
    
    /**
     * Returns the absolute y-coordinate of this LevelObject's center.
     * @return The absolute y-coordinate of this LevelObject's center
     */
    public final double getCenterY() {
        return centerHitbox.getAbsY();
    }
    
    /**
     * Returns the distance from this LevelObject's center to the specified
     * LevelObject's center.
     * @param object The LevelObject to return the distance to
     * @return The distance from this LevelObject's center to the specified
     * LevelObject's center
     */
    public final double distanceTo(LevelObject object) {
        return centerHitbox.distanceTo(object.centerHitbox);
    }
    
    /**
     * Returns the angle from this LevelObject's center to the specified
     * LevelObject's center.
     * @param object The LevelObject to return the angle to
     * @return The angle from this LevelObject's center to the specified
     * LevelObject's center
     */
    public final double angleTo(LevelObject object) {
        return centerHitbox.angleTo(object.centerHitbox);
    }
    
    /**
     * Returns the LevelObject of the specified class in this LevelObject's
     * LevelState that is nearest to it.
     * @param <O> The subclass of LevelObject to search for
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class that is nearest to this
     * LevelObject
     */
    public final <O extends LevelObject<T>> O nearestObject(Class<O> cls) {
        return (state == null ? null : state.nearestObject(centerHitbox.getAbsX(), centerHitbox.getAbsY(), cls));
    }
    
    /**
     * Returns the LevelObject of the specified class within the specified
     * rectangular region in this LevelObject's LevelState that is nearest to
     * it.
     * @param <O> The subclass of LevelObject to search for
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class within the specified
     * rectangular region that is nearest to this LevelObject
     */
    public final <O extends LevelObject<T>> O nearestObjectWithinRectangle(double x1, double y1, double x2, double y2, Class<O> cls) {
        return (state == null ? null : state.nearestObjectWithinRectangle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), x1, y1, x2, y2, cls));
    }
    
    /**
     * Returns the LevelObject of the specified class within the specified
     * circular region in this LevelObject's LevelState that is nearest to it.
     * @param <O> The subclass of LevelObject to search for
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class within the specified
     * circular region that is nearest to this LevelObject
     */
    public final <O extends LevelObject<T>> O nearestObjectWithinCircle(LevelVector center, double radius, Class<O> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), center.getX(), center.getY(), radius, cls));
    }
    
    /**
     * Returns the LevelObject of the specified class within the specified
     * circular region in this LevelObject's LevelState that is nearest to it.
     * @param <O> The subclass of LevelObject to search for
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class within the specified
     * circular region that is nearest to this LevelObject
     */
    public final <O extends LevelObject<T>> O nearestObjectWithinCircle(double centerX, double centerY, double radius, Class<O> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), centerX, centerY, radius, cls));
    }
    
    /**
     * Returns the LevelObject of the specified class that overlaps the
     * specified Hitbox in this LevelObject's LevelState that is nearest to it.
     * @param <O> The subclass of LevelObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return the LevelObject of the specified class that overlaps the
     * specified Hitbox that is nearest to this LevelObject
     */
    public final <O extends LevelObject<T>> O nearestOverlappingObject(Hitbox hitbox, Class<O> cls) {
        return (state == null ? null : state.nearestOverlappingObject(centerHitbox.getAbsX(), centerHitbox.getAbsY(), hitbox, cls));
    }
    
    /**
     * Returns whether there are any LevelObjects of the specified class within
     * the specified radius of this LevelObject in its LevelState.
     * @param <O> The subclass of LevelObject to search for
     * @param radius The radius of this LevelObject to search within
     * @param cls The Class object that represents the LevelObject subclass
     * @return Whether there are any LevelObjects of the specified class within
     * the specified radius of this LevelObject
     */
    public final <O extends LevelObject<T>> boolean objectIsWithinRadius(double radius, Class<O> cls) {
        return (state == null ? false : state.objectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls) != null);
    }
    
    /**
     * Returns a LevelObject of the specified class within the specified radius
     * of this LevelObject in its LevelState, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param radius The radius of this LevelObject to search within
     * @param cls The Class object that represents the LevelObject subclass
     * @return A LevelObject of the specified class within the specified radius
     * of this LevelObject
     */
    public final <O extends LevelObject<T>> O objectWithinRadius(double radius, Class<O> cls) {
        return (state == null ? null : state.objectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
    }
    
    /**
     * Returns all of the LevelObjects of the specified class within the
     * specified radius of this LevelObject in its LevelState.
     * @param <O> The subclass of LevelObject to search for
     * @param radius The radius of this LevelObject to search within
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class within the
     * specified radius of this LevelObject
     */
    public final <O extends LevelObject<T>> List<O> objectsWithinRadius(double radius, Class<O> cls) {
        return (state == null ? new ArrayList<>() : state.objectsWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
    }
    
    /**
     * Returns the LevelObject of the specified class within the specified
     * radius of this LevelObject in its LevelState that is nearest to it.
     * @param <O> The subclass of LevelObject to search for
     * @param radius The radius of this LevelObject to search within
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class within the specified
     * radius of this LevelObject that is nearest to it
     */
    public final <O extends LevelObject<T>> O nearestObjectWithinRadius(double radius, Class<O> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
    }
    
    /**
     * Returns this LevelObject's overlap Hitbox, or null if it has none.
     * @return This LevelObject's overlap Hitbox
     */
    public final Hitbox getOverlapHitbox() {
        return overlapHitbox;
    }
    
    /**
     * Sets this LevelObject's overlap Hitbox to the specified Hitbox. The new
     * overlap Hitbox may not be a component of a CompositeHitbox or in use by
     * another LevelObject.
     * @param overlapHitbox The new overlap Hitbox
     * @return Whether the change occurred
     */
    public final boolean setOverlapHitbox(Hitbox<T> overlapHitbox) {
        if (overlapHitbox != this.overlapHitbox) {
            boolean acceptable;
            if (overlapHitbox == null) {
                acceptable = true;
            } else {
                LevelObject<T> object = overlapHitbox.getObject();
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
     * Returns whether this LevelObject overlaps the specified LevelObject.
     * @param object The LevelObject to check for an overlap
     * @return Whether this LevelObject overlaps the specified LevelObject
     */
    public final boolean overlaps(LevelObject<T> object) {
        return overlap(this, object);
    }
    
    /**
     * Returns whether the two specified LevelObjects overlap.
     * @param <T> The subclass of CellGame that uses the LevelStates that the
     * two LevelObjects can be assigned to
     * @param object1 The first LevelObject
     * @param object2 The second LevelObject
     * @return Whether the two LevelObjects overlap
     */
    public static final <T extends CellGame> boolean overlap(LevelObject<T> object1, LevelObject<T> object2) {
        return object1.overlapHitbox != null && object2.overlapHitbox != null
                && Hitbox.overlap(object1.overlapHitbox, object2.overlapHitbox);
    }
    
    /**
     * Returns whether this LevelObject is overlapping a LevelObject of the
     * specified class in its LevelState.
     * @param <O> The subclass of LevelObject to search for
     * @param cls The Class object that represents the LevelObject subclass
     * @return Whether this LevelObject is overlapping a LevelObject of the
     * specified class
     */
    public final <O extends LevelObject<T>> boolean isOverlappingObject(Class<O> cls) {
        return (state == null || overlapHitbox == null ? false : state.overlappingObject(overlapHitbox, cls) != null);
    }
    
    /**
     * Returns a LevelObject of the specified class in this LevelObject's
     * LevelState that is overlapping it, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param cls The Class object that represents the LevelObject subclass
     * @return A LevelObject of the specified class that is overlapping this
     * LevelObject
     */
    public final <O extends LevelObject<T>> O overlappingObject(Class<O> cls) {
        return (state == null || overlapHitbox == null ? null : state.overlappingObject(overlapHitbox, cls));
    }
    
    /**
     * Returns all of the LevelObjects of the specified class in this
     * LevelObject's LevelState that are overlapping it.
     * @param <O> The subclass of LevelObject to search for
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class in this
     * LevelObject's LevelState that are overlapping it
     */
    public final <O extends LevelObject<T>> List<O> overlappingObjects(Class<O> cls) {
        return (state == null || overlapHitbox == null ? new ArrayList<>() : state.overlappingObjects(overlapHitbox, cls));
    }
    
    /**
     * Returns all of the LevelObjects of the specified class in this
     * LevelObject's LevelState whose overlap Hitboxes' rectangular bounding
     * boxes touch or intersect this LevelObject's overlap Hitbox's rectangular
     * bounding box.
     * @param <O> The subclass of LevelObject to search for
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class whose overlap
     * Hitboxes' bounding boxes meet this LevelObject's overlap Hitbox's
     * bounding box
     */
    public final <O extends LevelObject<T>> List<O> boundingBoxesMeet(Class<O> cls) {
        return (state == null || overlapHitbox == null ? new ArrayList<>() : state.boundingBoxesMeet(overlapHitbox, cls));
    }
    
    /**
     * Returns whether this LevelObject is overlapping the solid Hitbox of a
     * solid LevelObject of the specified class in its LevelState.
     * @param <O> The subclass of LevelObject to search for
     * @param cls The Class object that represents the LevelObject subclass
     * @return Whether this LevelObject is overlapping the solid Hitbox of a
     * solid LevelObject of the specified class
     */
    public final <O extends LevelObject<T>> boolean isIntersectingSolidObject(Class<O> cls) {
        return (state == null || overlapHitbox == null ? false : state.intersectingSolidObject(overlapHitbox, cls) != null);
    }
    
    /**
     * Returns a solid LevelObject of the specified class in this LevelObject's
     * LevelState whose solid Hitbox is overlapping it, or null if there is
     * none.
     * @param <O> The subclass of LevelObject to search for
     * @param cls The Class object that represents the LevelObject subclass
     * @return A solid LevelObject of the specified class whose solid Hitbox is
     * overlapping this LevelObject
     */
    public final <O extends LevelObject<T>> O intersectingSolidObject(Class<O> cls) {
        return (state == null || overlapHitbox == null ? null : state.intersectingSolidObject(overlapHitbox, cls));
    }
    
    /**
     * Returns all of the solid LevelObjects of the specified class in this
     * LevelObject's LevelState whose solid Hitboxes are overlapping it.
     * @param <O> The subclass of LevelObject to search for
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the solid LevelObjects of the specified class whose solid
     * Hitboxes are overlapping this LevelObject
     */
    public final <O extends LevelObject<T>> List<O> intersectingSolidObjects(Class<O> cls) {
        return (state == null || overlapHitbox == null ? new ArrayList<>() : state.intersectingSolidObjects(overlapHitbox, cls));
    }
    
    /**
     * Returns this LevelObject's solid Hitbox, or null if it has none.
     * @return This LevelObject's solid Hitbox
     */
    public final Hitbox getSolidHitbox() {
        return solidHitbox;
    }
    
    /**
     * Sets this LevelObject's solid Hitbox to the specified Hitbox. The new
     * solid Hitbox may not be a component of a CompositeHitbox or in use by
     * another LevelObject.
     * @param solidHitbox The new solid Hitbox
     * @return Whether the change occurred
     */
    public final boolean setSolidHitbox(Hitbox solidHitbox) {
        if (solidHitbox != this.solidHitbox) {
            boolean acceptable;
            if (solidHitbox == null) {
                acceptable = true;
            } else {
                LevelObject object = solidHitbox.getObject();
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
     * Returns whether any of this LevelObject's surfaces are solid.
     * @return Whether any of this LevelObject's surfaces are solid
     */
    public final boolean isSolid() {
        return (solidHitbox == null ? false : solidHitbox.isSolid());
    }
    
    /**
     * Returns whether this LevelObject's surface in the specified Direction is
     * solid.
     * @param direction The Direction of the surface to be examined
     * @return Whether the surface in the specified Direction is solid
     */
    public final boolean surfaceIsSolid(Direction direction) {
        return (solidHitbox == null ? false : solidHitbox.surfaceIsSolid(direction));
    }
    
    /**
     * Sets whether this LevelObject's surface in the specified Direction is
     * solid. If the surface is being made solid but this LevelObject has no
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
     * Sets whether this LevelObject's surfaces in every direction are solid. If
     * the surface is being made solid but this LevelObject has no solid Hitbox,
     * a copy of its locator Hitbox will be created to serve as one.
     * @param solid Whether this LevelObject's surfaces in every direction
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
     * Returns this LevelObject's draw priority.
     * @return This LevelObject's draw priority
     */
    public final int getDrawPriority() {
        return drawPriority;
    }
    
    /**
     * Sets this LevelObject's draw priority to the specified value.
     * @param drawPriority The new draw priority
     */
    public final void setDrawPriority(int drawPriority) {
        this.drawPriority = drawPriority;
        locatorHitbox.changeDrawPriority(drawPriority);
    }
    
    /**
     * Returns this LevelObject's appearance.
     * @return This LevelObject's appearance
     */
    public final Drawable getAppearance() {
        return appearance;
    }
    
    /**
     * Sets this LevelObject's appearance to the specified Drawable.
     * @param appearance The new appearance
     */
    public final void setAppearance(Drawable appearance) {
        this.appearance = appearance;
    }
    
    /**
     * Returns the AnimationInstance that is assigned to this LevelObject with
     * the specified ID.
     * @param id The ID of the AnimationInstance to be returned
     * @return The AnimationInstance that is assigned to this LevelObject with
     * the specified ID
     */
    public final AnimationInstance getAnimInstance(int id) {
        AnimationInstance instance = animInstances.get(id);
        return (instance == null ? AnimationInstance.BLANK : instance);
    }
    
    /**
     * Sets the AnimationInstance that is assigned to this LevelObject with the
     * specified ID to the specified AnimationInstance, if it is not already
     * assigned to a CellGameState. If there is already an AnimationInstance
     * assigned with the specified ID, it will be removed from this LevelObject.
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
     * Sets both this LevelObject's appearance and its AnimationInstance with ID
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
     * LevelObject with the specified ID, if there is one.
     * @param id The ID of the AnimationInstance whose Animation is to be
     * returned
     * @return The Animation of the AnimationInstance assigned to this
     * LevelObject with the specified ID
     */
    public final Animation getAnimation(int id) {
        return getAnimInstance(id).getAnimation();
    }
    
    /**
     * Sets the AnimationInstance that is assigned to this LevelObject with the
     * specified ID to a new AnimationInstance of the specified Animation, if
     * there is not already an AnimationInstance of that Animation assigned
     * with that ID. In other words, this method will not replace an
     * AnimationInstance with another of the same Animation. If there is already
     * an AnimationInstance assigned with the specified ID, it will be removed
     * from this LevelObject.
     * @param id The ID with which to assign the new AnimationInstance
     * @param animation The Animation to add a new AnimationInstance of
     * @return The new AnimationInstance
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
     * Sets both this LevelObject's appearance and its AnimationInstance with ID
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
     * Removes from this LevelObject all AnimationInstances that are currently
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
     * Returns this LevelObject's alpha value.
     * @return This LevelObject's alpha value
     */
    public final double getAlpha() {
        return alpha;
    }
    
    /**
     * Sets this LevelObject's alpha value to the specified value.
     * @param alpha The new alpha value
     */
    public final void setAlpha(double alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
    }
    
    /**
     * Returns this LevelObject's Filter.
     * @return This LevelObject's Filter
     */
    public final Filter getFilter() {
        return filter;
    }
    
    /**
     * Sets this LevelObject's Filter to the specified Filter.
     * @param filter The new Filter
     */
    public final void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    /**
     * Returns whether any part of this LevelObject's rectangular bounding box
     * is visible through any of its LevelState's Viewports.
     * @return Whether this LevelObject is visible through any of its
     * LevelState's Viewports
     */
    public final boolean isVisible() {
        return (state == null ? false : state.rectangleIsVisible(getLeftEdge(), getTopEdge(), getRightEdge(), getBottomEdge()));
    }
    
    /**
     * Returns whether any part of this LevelObject's rectangular bounding box
     * is visible through the specified Viewport.
     * @param viewport The Viewport to check
     * @return Whether this LevelObject is visible through the specified
     * Viewport
     */
    public final boolean isVisible(Viewport viewport) {
        return viewport.rectangleIsVisible(getLeftEdge(), getTopEdge(), getRightEdge(), getBottomEdge());
    }
    
    /**
     * Draws this LevelObject as seen through a Viewport's camera.
     * @param g The Graphics context to which this LevelObject is being drawn
     * this frame
     * @param x The x-coordinate in pixels on the Graphics context that
     * corresponds to the x-coordinate of this LevelObject's position
     * @param y The y-coordinate in pixels on the Graphics context that
     * corresponds to the y-coordinate of this LevelObject's position
     */
    public void draw(Graphics g, int x, int y) {
        appearance.draw(g, x, y, getXFlip(), getYFlip(), getAngle(), alpha, filter);
    }
    
}
