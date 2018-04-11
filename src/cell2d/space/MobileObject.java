package cell2d.space;

import cell2d.CellVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>A MobileObject is a SpaceObject that can simulate continuous movement
 * through its SpaceState's space. If a MobileObject has had Cell2D's standard
 * collision mechanics enabled for it, this movement will be blocked by any
 * solid surfaces of SpaceObjects in the MobileObject's path.</p>
 * 
 * <p>A MobileObject may have a collision Hitbox that represents it for purposes
 * of colliding with solid surfaces. The collision Hitbox's rectangular bounding
 * box, rather than its exact shape, is what represents the MobileObject in
 * Cell2D's standard collision mechanics, and thus standard collision only
 * handles interactions between rectangular shapes. A MobileObject with no
 * collision Hitbox cannot collide with solid surfaces, even if collision is
 * enabled for it.</p>
 * 
 * <p>A MobileObject may have a pressing angle that causes it to press against
 * and collide with solid surfaces in the angle's direction during movement, as
 * long as it is touching them and not moving away from them. The pressing angle
 * is specified as relative (to the MobileObject's flipped status and angle of
 * rotation) or absolute.</p>
 * 
 * <p>A MobileObject may have one or more MobileObject followers, and if it
 * does, it is called those followers' leader. When a MobileObject moves, all of
 * its followers automatically move to maintain their relative positions to it.
 * A MobileObject cannot collide with its leader, super-leaders, followers, or
 * sub-followers.</p>
 * 
 * <p>A MobileObject has a velocity, as well as a vector called a step that acts
 * as a short-term adjustment to its velocity, both in fracunits per time unit.
 * Every frame, between the periods in which its SpaceState's SpaceThinkers
 * perform their beforeMovementActions() and their frameActions(), a
 * MobileObject assigned to an active SpaceState moves by the sum of its
 * velocity and step multiplied by its time factor, then resets its step to (0,
 * 0). A MobileObject's movement priority determines when it will move relative
 * to other MobileObjects. MobileObjects with higher movement priorities move
 * before those with lower ones. Also, if two solid MobileObjects would
 * otherwise collide with each other, the one with the higher movement priority
 * will push the other one along with it.</p>
 * 
 * <p>Every time a MobileObject moves, it records the SpaceObjects whose solid
 * surfaces it collided with and the Directions of the surfaces relative to it
 * when it collided with them, as well as its total displacement over the course
 * of the movement, not counting pushes from moving solid surfaces or manual
 * manipulation of its position. These records are reset when the MobileObject
 * moves again, or when it is removed from the SpaceState whose space the
 * records reflect.</p>
 * @author Andrew Heyman
 */
public abstract class MobileObject extends SpaceObject {
    
    int movementPriority = 0;
    int newMovementPriority = 0;
    private boolean hasCollision = false;
    private Hitbox collisionHitbox = null;
    private Double relPressingAngle = null;
    private MobileObject leader = null;
    final Set<MobileObject> followers = new HashSet<>();
    MobileObject effLeader = null;
    final Map<SpaceObject,Set<Direction>> collisions = new HashMap<>();
    final Set<Direction> collisionDirections = EnumSet.noneOf(Direction.class);
    private final CellVector velocity = new CellVector();
    private final CellVector step = new CellVector();
    final CellVector displacement = new CellVector();
    
    /**
     * Creates a new MobileObject with no locator Hitbox. This MobileObject must
     * be assigned a locator Hitbox with its setLocatorHitbox() method before
     * any of its other methods are called.
     * @see #setLocatorHitbox(cell2d.space.Hitbox)
     */
    public MobileObject() {}
    
    @Override
    void addNonCellData() {
        super.addNonCellData();
        state.addMobileObject(this);
    }
    
    @Override
    void addCellData() {
        super.addCellData();
        if (hasCollision && collisionHitbox != null) {
            state.addHitbox(collisionHitbox, HitboxRole.COLLISION);
        }
    }
    
    @Override
    void removeData() {
        super.removeData();
        state.removeMobileObject(this);
        if (hasCollision && collisionHitbox != null) {
            state.removeHitbox(collisionHitbox, HitboxRole.COLLISION);
        }
        collisions.clear();
        collisionDirections.clear();
        displacement.clear();
    }
    
    @Override
    void removeNonLocatorHitboxes(Hitbox locatorHitbox) {
        super.removeNonLocatorHitboxes(locatorHitbox);
        if (collisionHitbox != null) {
            locatorHitbox.removeChild(collisionHitbox);
        }
    }
    
    @Override
    void addNonLocatorHitboxes(Hitbox locatorHitbox) {
        super.addNonLocatorHitboxes(locatorHitbox);
        if (collisionHitbox != null) {
            locatorHitbox.addChild(collisionHitbox);
        }
    }
    
    /**
     * Returns this MobileObject's movement priority.
     * @return This MobileObject's movement priority
     */
    public final int getMovementPriority() {
        return movementPriority;
    }
    
    /**
     * Returns the movement priority that this MobileObject is about to have,
     * but does not yet have due to its SpaceState's MobileObject list being
     * iterated over. If this MobileObject is not about to change its movement
     * priority, this method will simply return its current movement priority.
     * @return The movement priority that this MobileObject is about to have
     */
    public final int getNewMovementPriority() {
        return newMovementPriority;
    }
    
    /**
     * Sets this MobileObject's movement priority to the specified value.
     * @param movementPriority The new movement priority
     */
    public final void setMovementPriority(int movementPriority) {
        if (state == null) {
            this.newMovementPriority = movementPriority;
            this.movementPriority = movementPriority;
        } else if (this.newMovementPriority != movementPriority) {
            this.newMovementPriority = movementPriority;
            state.changeMobileObjectMovementPriority(this, movementPriority);
        }
    }
    
    /**
     * Returns whether this MobileObject has Cell2D's standard collision
     * mechanics enabled.
     * @return Whether this MobileObject has collision enabled
     */
    public final boolean hasCollision() {
        return hasCollision;
    }
    
    /**
     * Sets whether this MobileObject has Cell2D's standard collision mechanics
     * enabled.
     * @param hasCollision Whether this MobileObject should have collision
     * enabled
     */
    public final void setCollision(boolean hasCollision) {
        if (state != null && collisionHitbox != null) {
            if (hasCollision && !this.hasCollision) {
                state.addHitbox(collisionHitbox, HitboxRole.COLLISION);
            } else if (!hasCollision && this.hasCollision) {
                state.removeHitbox(collisionHitbox, HitboxRole.COLLISION);
            }
        }
        this.hasCollision = hasCollision;
    }
    
    /**
     * Returns this MobileObject's collision Hitbox, or null if it has none.
     * @return This MobileObject's collision Hitbox
     */
    public final Hitbox getCollisionHitbox() {
        return collisionHitbox;
    }
    
    /**
     * Sets this MobileObject's collision Hitbox to the specified Hitbox. The
     * new collision Hitbox may not be a component of a CompositeHitbox or in
     * use by another SpaceObject. If the specified Hitbox is null, the current
     * collision Hitbox will be removed if there is one, but it will not be
     * replaced with anything.
     * @param collisionHitbox The new collision Hitbox
     * @return Whether the change occurred
     */
    public final boolean setCollisionHitbox(Hitbox collisionHitbox) {
        if (collisionHitbox != this.collisionHitbox) {
            boolean acceptable;
            Hitbox locatorHitbox = getLocatorHitbox();
            if (collisionHitbox == null) {
                acceptable = true;
            } else {
                SpaceObject object = collisionHitbox.getObject();
                Hitbox parent = collisionHitbox.getParent();
                acceptable = (object == null && parent == null)
                        || (collisionHitbox == locatorHitbox)
                        || (object == this && parent == locatorHitbox
                        && collisionHitbox.getComponentOf() != locatorHitbox);
            }
            if (acceptable) {
                if (this.collisionHitbox != null) {
                    this.collisionHitbox.removeAsCollisionHitbox(hasCollision);
                }
                this.collisionHitbox = collisionHitbox;
                if (collisionHitbox != null) {
                    locatorHitbox.addChild(collisionHitbox);
                    collisionHitbox.addAsCollisionHitbox(hasCollision);
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns all of the solid SpaceObjects of the specified class in this
     * MobileObject's SpaceState whose solid Hitboxes' rectangular bounding
     * boxes touch or intersect this MobileObject's collision Hitbox's
     * rectangular bounding box.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the solid SpaceObjects of the specified class whose solid
     * Hitboxes' bounding boxes meet this MobileObject's collision Hitbox's
     * bounding box
     */
    public final <O extends SpaceObject> List<O> solidBoundingBoxesMeet(Class<O> cls) {
        return (state == null || collisionHitbox == null ? new ArrayList<>() : state.solidBoundingBoxesMeet(collisionHitbox, cls));
    }
    
    /**
     * Returns this MobileObject's relative pressing angle, or null if it has
     * none.
     * @return This MobileObject's relative pressing angle
     */
    public final Double getRelPressingAngle() {
        return relPressingAngle;
    }
    
    /**
     * Sets this MobileObject's relative pressing angle to the specified value,
     * or to none if the specified value is null.
     * @param angle The new relative pressing angle
     */
    public final void setRelPressingAngle(Double angle) {
        if (angle == null) {
            relPressingAngle = null;
        } else {
            relPressingAngle = angle % 360;
            if (relPressingAngle < 0) {
                relPressingAngle += 360;
            }
        }
    }
    
    /**
     * Sets this MobileObject's relative pressing angle to the specified value.
     * @param angle The new relative pressing angle
     */
    public final void setRelPressingAngle(double angle) {
        relPressingAngle = angle % 360;
        if (relPressingAngle < 0) {
            relPressingAngle += 360;
        }
    }
    
    /**
     * Returns this MobileObject's absolute pressing angle, or null if it has
     * none.
     * @return This MobileObject's absolute pressing angle
     */
    public final Double getAbsPressingAngle() {
        if (relPressingAngle == null) {
            return null;
        }
        double angle = relPressingAngle + getAngle();
        if (getXFlip()) {
            angle = 180 - angle;
        }
        if (getYFlip()) {
            angle = -angle;
        }
        angle %= 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    /**
     * Returns whether this MobileObject's absolute pressing angle, if it has
     * one, has a component in the specified Direction.
     * @param direction The Direction to check
     * @return Whether this MobileObject's absolute pressing angle causes it to
     * press in the specified Direction
     */
    public final boolean isPressingIn(Direction direction) {
        Double angle = getAbsPressingAngle();
        return angle != null
                && ((direction == Direction.LEFT && (angle < 90 || angle > 270))
                || (direction == Direction.RIGHT && angle > 90 && angle < 270)
                || (direction == Direction.UP && angle > 0 && angle < 180)
                || (direction == Direction.DOWN && angle > 180));
    }
    
    /**
     * Returns this MobileObject's leader, or null if it has none.
     * @return This MobileObject's leader
     */
    public final MobileObject getLeader() {
        return leader;
    }
    
    /**
     * Sets this MobileObject's leader to the specified MobileObject. If it is
     * set to a null MobileObject, this MobileObject will be removed from its
     * current leader if it has one.
     * @param leader The new leader
     */
    public final void setLeader(MobileObject leader) {
        if (this.leader != null) {
            this.leader.removeFollower(this);
        }
        if (leader != null) {
            leader.addFollower(this);
        }
    }
    
    /**
     * Returns an unmodifiable Set view of this MobileObject's followers.
     * @return The Set of this MobileObject's followers
     */
    public final Set<MobileObject> getFollowers() {
        return Collections.unmodifiableSet(followers);
    }
    
    /**
     * Returns the number of followers that this MobileObject currently has.
     * @return This MobileObject's number of followers
     */
    public final int getNumFollowers() {
        return followers.size();
    }
    
    /**
     * Adds the specified MobileObject as this MobileObject's follower if it
     * does not have a leader already.
     * @param follower The new follower
     * @return Whether the addition occurred
     */
    public final boolean addFollower(MobileObject follower) {
        if (follower != this && follower.leader == null) {
            MobileObject ancestor = leader;
            while (ancestor != null) {
                if (ancestor == follower) {
                    return false;
                }
                ancestor = ancestor.leader;
            }
            followers.add(follower);
            if (follower.effLeader == follower.leader) {
                follower.effLeader = this;
            }
            follower.leader = this;
            return true;
        }
        return false;
    }
    
    /**
     * Removes the specified MobileObject as this MobileObject's follower if
     * this MobileObject is its leader.
     * @param follower The follower to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeFollower(MobileObject follower) {
        if (follower.leader == this) {
            followers.remove(follower);
            if (follower.effLeader == follower.leader) {
                follower.effLeader = null;
            }
            follower.leader = null;
            return true;
        }
        return false;
    }
    
    /**
     * Removes all of this MobileObject's followers from it.
     */
    public final void clearFollowers() {
        for (MobileObject follower : followers) {
            if (follower.effLeader == follower.leader) {
                follower.effLeader = null;
            }
            follower.leader = null;
        }
        followers.clear();
    }
    
    /**
     * Sets the SpaceState to which this MobileObject is currently assigned. If
     * it is set to a null SpaceState, this MobileObject will be removed from
     * its current SpaceState if it has one.
     * @param state The SpaceState to which this MobileObject should be assigned
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will be assigned to the same SpaceState (false by default)
     */
    public final void setGameState(SpaceState state, boolean bringFollowers) {
        setGameState(state);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.setGameState(state, true);
            }
        }
    }
    
    /**
     * Sets this MobileObject's position to the specified value.
     * @param position The new position
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will change their positions by the same amount as this
     * MobileObject (false by default)
     */
    public final void setPosition(CellVector position, boolean bringFollowers) {
        long changeX = position.getX() - getX();
        long changeY = position.getY() - getY();
        setPosition(position);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.changePosition(changeX, changeY, true);
            }
        }
    }
    
    /**
     * Sets this MobileObject's position to the specified value.
     * @param x The x-coordinate of the new position
     * @param y The y-coordinate of the new position
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will change their positions by the same amount as this
     * MobileObject (false by default)
     */
    public final void setPosition(long x, long y, boolean bringFollowers) {
        long changeX = x - getX();
        long changeY = y - getY();
        setPosition(x, y);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.changePosition(changeX, changeY, true);
            }
        }
    }
    
    /**
     * Sets the x-coordinate of this MobileObject's position to the specified
     * value.
     * @param x The x-coordinate of the new position
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will change their positions by the same amount as this
     * MobileObject (false by default)
     */
    public final void setX(long x, boolean bringFollowers) {
        long changeX = x - getX();
        setX(x);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.changeX(changeX, true);
            }
        }
    }
    
    /**
     * Sets the y-coordinate of this MobileObject's position to the specified
     * value.
     * @param y The y-coordinate of the new position
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will change their positions by the same amount as this
     * MobileObject (false by default)
     */
    public final void setY(long y, boolean bringFollowers) {
        long changeY = y - getY();
        setY(y);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.changeY(changeY, true);
            }
        }
    }
    
    /**
     * Changes this MobileObject's position by the specified amount.
     * @param change The amount to change the position by
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will change their positions by the same amount as this
     * MobileObject (false by default)
     */
    public final void changePosition(CellVector change, boolean bringFollowers) {
        changePosition(change);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.changePosition(change, true);
            }
        }
    }
    
    /**
     * Changes the coordinates of this MobileObject's position by the specified
     * amounts.
     * @param changeX The amount to change the position's x-coordinate by
     * @param changeY The amount to change the position's y-coordinate by
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will change their positions by the same amount as this
     * MobileObject (false by default)
     */
    public final void changePosition(long changeX, long changeY, boolean bringFollowers) {
        changePosition(changeX, changeY);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.changePosition(changeX, changeY, true);
            }
        }
    }
    
    /**
     * Changes the x-coordinate of this MobileObject's position by the specified
     * amount.
     * @param changeX The amount to change the position's x-coordinate by
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will change their positions by the same amount as this
     * MobileObject (false by default)
     */
    public final void changeX(long changeX, boolean bringFollowers) {
        changeX(changeX);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.changeX(changeX, true);
            }
        }
    }
    
    /**
     * Changes the y-coordinate of this MobileObject's position by the specified
     * amount.
     * @param changeY The amount to change the position's y-coordinate by
     * @param bringFollowers If true, all of this MobileObject's followers and
     * sub-followers will change their positions by the same amount as this
     * MobileObject (false by default)
     */
    public final void changeY(long changeY, boolean bringFollowers) {
        changeY(changeY);
        if (bringFollowers && !followers.isEmpty()) {
            for (MobileObject follower : followers) {
                follower.changeY(changeY, true);
            }
        }
    }
    
    /**
     * Moves this MobileObject and its followers and sub-followers by the
     * specified amount, colliding with solid surfaces if they have collision
     * enabled.
     * @param change The amount by which this MobileObject should move
     */
    public final void doMovement(CellVector change) {
        doMovement(change.getX(), change.getY());
    }
    
    /**
     * Moves this MobileObject and its followers and sub-followers by the
     * specified amount, colliding with solid surfaces if they have collision
     * enabled.
     * @param changeX The amount by which this MobileObject should move along
     * the x-axis
     * @param changeY The amount by which this MobileObject should move along
     * the y-axis
     */
    public final void doMovement(long changeX, long changeY) {
        if (state == null) {
            if (changeX != 0 || changeY != 0) {
                setPosition(getX() + changeX, getY() + changeY);
            }
        } else {
            collisions.clear();
            collisionDirections.clear();
            displacement.clear();
            displacement.setCoordinates(state.move(this, changeX, changeY));
        }
    }
    
    /**
     * This MobileObject's response to colliding with a solid surface of the
     * specified SpaceObject in the specified Direction.
     * @param object The SpaceObject whose surface this MobileObject collided
     * with
     * @param direction The Direction in which this MobileObject collided with
     * the surface
     * @return The CollisionResponse to the collision (SLIDE by default)
     */
    public CollisionResponse collide(SpaceObject object, Direction direction) {
        return CollisionResponse.SLIDE;
    }
    
    final void addCollision(SpaceObject object, Direction direction) {
        Set<Direction> collisionsWithObject = collisions.get(object);
        if (collisionsWithObject == null) {
            collisionsWithObject = EnumSet.of(direction);
            collisions.put(object, collisionsWithObject);
        } else {
            collisionsWithObject.add(direction);
        }
        collisionDirections.add(direction);
    }
    
    /**
     * Returns a Map of the SpaceObjects whose solid surfaces this MobileObject
     * collided with during its last movement to the Sets of the Directions in
     * which it collided with them. Changes to the returned Map will not be
     * reflected in this MobileObject.
     * @return A Map of this MobileObject's collisions during its last movement
     */
    public final Map<SpaceObject,Set<Direction>> getCollisions() {
        Map<SpaceObject,Set<Direction>> collisionMap = new HashMap<>();
        for (Map.Entry<SpaceObject,Set<Direction>> entry : collisions.entrySet()) {
            collisionMap.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
        }
        return collisionMap;
    }
    
    /**
     * Returns an unmodifiable Set view of the Directions in which this
     * MobileObject collided with solid surfaces during its last movement.
     * @return The Set of the Directions in which this MobileObject collided
     * with solid surfaces during its last movement
     */
    public final Set<Direction> getCollisionDirections() {
        return Collections.unmodifiableSet(collisionDirections);
    }
    
    /**
     * Returns whether this MobileObject collided with any solid surfaces during
     * its last movement.
     * @return Whether this MobileObject collided with any solid surfaces during
     * its last movement
     */
    public final boolean collided() {
        return !collisions.isEmpty();
    }
    
    /**
     * Returns whether this MobileObject collided with any solid surfaces in the
     * specified Direction during its last movement.
     * @param direction The Direction to check
     * @return Whether this MobileObject collided with any solid surfaces in the
     * specified Direction during its last movement
     */
    public final boolean collided(Direction direction) {
        return collisionDirections.contains(direction);
    }
    
    /**
     * Returns this MobileObject's velocity.
     * @return This MobileObject's velocity
     */
    public final CellVector getVelocity() {
        return new CellVector(velocity);
    }
    
    /**
     * Returns the x-component of this MobileObject's velocity.
     * @return The x-component of this MobileObject's velocity
     */
    public final long getVelocityX() {
        return velocity.getX();
    }
    
    /**
     * Returns the y-component of this MobileObject's velocity.
     * @return The y-component of this MobileObject's velocity
     */
    public final long getVelocityY() {
        return velocity.getY();
    }
    
    /**
     * Returns this MobileObject's speed, the magnitude of its velocity.
     * @return This MobileObject's speed
     */
    public final long getSpeed() {
        return velocity.getMagnitude();
    }
    
    /**
     * Sets this MobileObject's velocity to the specified value.
     * @param velocity The new velocity
     */
    public final void setVelocity(CellVector velocity) {
        this.velocity.setCoordinates(velocity);
    }
    
    /**
     * Sets this MobileObject's velocity to the specified value.
     * @param velocityX The new x-component of the velocity
     * @param velocityY The new y-component of the velocity
     */
    public final void setVelocity(long velocityX, long velocityY) {
        velocity.setCoordinates(velocityX, velocityY);
    }
    
    /**
     * Sets the x-component of this MobileObject's velocity to the specified
     * value.
     * @param velocityX The new x-component of the velocity
     */
    public final void setVelocityX(long velocityX) {
        velocity.setX(velocityX);
    }
    
    /**
     * Sets the y-component of this MobileObject's velocity to the specified
     * value.
     * @param velocityY The new y-component of the velocity
     */
    public final void setVelocityY(long velocityY) {
        velocity.setY(velocityY);
    }
    
    /**
     * Sets this MobileObject's speed, the magnitude of its velocity, to the
     * specified value.
     * @param speed The new speed
     */
    public final void setSpeed(long speed) {
        velocity.setMagnitude(speed);
    }
    
    /**
     * Sets this MobileObject's velocity to send it moving toward the specified
     * point at the specified speed.
     * @param point The point that this MobileObject should move toward
     * @param speed The speed that this MobileObject should move at
     */
    public final void moveToward(CellVector point, long speed) {
        setVelocity(point.getX() - getX(), point.getY() - getY());
        setSpeed(speed);
    }
    
    /**
     * Sets this MobileObject's velocity to send it moving toward the specified
     * point at the specified speed.
     * @param x The x-coordinate of the point that this MobileObject should
     * move toward
     * @param y The y-coordinate of the point that this MobileObject should
     * move toward
     * @param speed The speed that this MobileObject should move at
     */
    public final void moveToward(long x, long y, long speed) {
        setVelocity(x - getX(), y - getY());
        setSpeed(speed);
    }
    
    /**
     * Returns this MobileObject's step.
     * @return This MobileObject's step
     */
    public final CellVector getStep() {
        return new CellVector(step);
    }
    
    /**
     * Returns the x-component of this MobileObject's step.
     * @return The x-component of this MobileObject's step
     */
    public final long getStepX() {
        return step.getX();
    }
    
    /**
     * Returns the y-component of this MobileObject's step.
     * @return The y-component of this MobileObject's step
     */
    public final long getStepY() {
        return step.getY();
    }
    
    /**
     * Returns the length of this MobileObject's step.
     * @return The length of this MobileObject's step
     */
    public final long getStepLength() {
        return step.getMagnitude();
    }
    
    /**
     * Sets this MobileObject's step to the specified value.
     * @param step The new step
     */
    public final void setStep(CellVector step) {
        this.step.setCoordinates(step);
    }
    
    /**
     * Sets this MobileObject's step to the specified value.
     * @param stepX The x-component of the new step
     * @param stepY The y-component of the new step
     */
    public final void setStep(long stepX, long stepY) {
        step.setCoordinates(stepX, stepY);
    }
    
    /**
     * Sets the x-component of this MobileObject's step to the specified value.
     * @param stepX The new x-component of the step
     */
    public final void setStepX(long stepX) {
        step.setX(stepX);
    }
    
    /**
     * Sets the y-component of this MobileObject's step to the specified value.
     * @param stepY The new y-component of the step
     */
    public final void setStepY(long stepY) {
        step.setY(stepY);
    }
    
    /**
     * Sets the length of this MobileObject's step to the specified value.
     * @param length The new step length
     */
    public final void setStepLength(long length) {
        step.setMagnitude(length);
    }
    
    /**
     * Changes this MobileObject's step by the specified amount.
     * @param change The amount to change the step by
     */
    public final void changeStep(CellVector change) {
        step.add(change);
    }
    
    /**
     * Changes this MobileObject's step by the specified amount.
     * @param changeX The amount to change the step's x-component by
     * @param changeY The amount to change the step's y-component by
     */
    public final void changeStep(long changeX, long changeY) {
        step.add(changeX, changeY);
    }
    
    /**
     * Changes the x-component of this MobileObject's step by the specified
     * amount.
     * @param changeX The amount to change the step's x-component by
     */
    public final void changeStepX(long changeX) {
        step.add(changeX, 0);
    }
    
    /**
     * Changes the y-component of this MobileObject's step by the specified
     * amount.
     * @param changeY The amount to change the step's y-component by
     */
    public final void changeStepY(long changeY) {
        step.add(0, changeY);
    }
    
    /**
     * Returns this MobileObject's displacement during its last movement.
     * @return This MobileObject's displacement during its last movement
     */
    public final CellVector getDisplacement() {
        return new CellVector(displacement);
    }
    
    /**
     * Returns the x-component of this MobileObject's displacement during its
     * last movement.
     * @return The x-component of this MobileObject's displacement during its
     * last movement
     */
    public final long getDisplacementX() {
        return displacement.getX();
    }
    
    /**
     * Returns the y-component of this MobileObject's displacement during its
     * last movement.
     * @return The y-component of this MobileObject's displacement during its
     * last movement
     */
    public final long getDisplacementY() {
        return displacement.getY();
    }
    
    /**
     * Returns the length of this MobileObject's displacement during its last
     * movement.
     * @return The length of this MobileObject's displacement during its last
     * movement
     */
    public final long getDisplacementLength() {
        return displacement.getMagnitude();
    }
    
}
