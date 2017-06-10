package cell2d.space;

import cell2d.CellGame;
import cell2d.CellVector;
import cell2d.SafeIterator;
import cell2d.TimedEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>A ThinkerObject is a SpaceObject that acts like a SpaceThinker, possessing
 * SpaceThinkerStates, timers, and various actions in response to events. A
 * ThinkerObject can also simulate continuous movement through its SpaceState's
 * space. If a ThinkerObject has had Cell2D's standard collision mechanics
 * enabled for it, this movement will be blocked by any solid surfaces of
 * SpaceObjects in the ThinkerObject's path.</p>
 * 
 * <p>A ThinkerObject may have a collision Hitbox that represents it for
 * purposes of colliding with solid surfaces. The collision Hitbox's rectangular
 * bounding box, rather than its exact shape, is what represents the
 * ThinkerObject in Cell2D's standard collision mechanics, and thus standard
 * collision only handles interactions between rectangular shapes. A
 * ThinkerObject with no collision Hitbox cannot collide with solid surfaces,
 * even if collision is enabled for it.</p>
 * 
 * <p>A ThinkerObject may have a pressing angle that causes it to press against
 * and collide with solid surfaces in the angle's direction during movement, as
 * long as it is touching them and not moving away from them. The pressing angle
 * is specified as relative (to the ThinkerObject's flipped status and angle of
 * rotation) or absolute.</p>
 * 
 * <p>A ThinkerObject may have one or more ThinkerObject followers, and if it
 * does, it is called those followers' leader. When a ThinkerObject moves, all
 * of its followers automatically move to maintain their relative positions to
 * it. A ThinkerObject cannot collide with its leader, super-leaders, followers,
 * or sub-followers.</p>
 * 
 * <p>A ThinkerObject has a velocity, as well as a vector called a step that
 * acts as a short-term adjustment to its velocity, both in pixels per time
 * unit. Every frame, between its frameActions() and afterMovementActions(), a
 * ThinkerObject assigned to an active SpaceState moves by the sum of its
 * velocity and step multiplied by its time factor, then resets its step to (0,
 * 0). A ThinkerObject's movement priority determines when it will move relative
 * to other ThinkerObjects. ThinkerObjects with higher movement priorities move
 * before those with lower ones. Also, if two solid ThinkerObjects would
 * otherwise collide with each other, the one with the higher movement priority
 * will push the other one along with it.</p>
 * 
 * <p>Every time a ThinkerObject moves, it records the SpaceObjects whose solid
 * surfaces it collided with and the Directions of the surfaces relative to it
 * when it collided with them, as well as its total displacement over the course
 * of the movement, not counting pushes from moving solid surfaces or manual
 * manipulation of its position. These records are reset when the ThinkerObject
 * moves again, or when it is removed from the SpaceState whose space the
 * records reflect.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the SpaceStates that this
 * ThinkerObject can be assigned to
 */
public abstract class ThinkerObject<T extends CellGame> extends SpaceObject<T> {
    
    private final SpaceThinker<T> thinker = new SpaceThinker<T>() {
        
        @Override
        public final void timeUnitActions(T game, SpaceState<T> state) {
            ThinkerObject.this.timeUnitActions(game, state);
        }
        
        @Override
        public final void beforeMovementActions(T game, SpaceState<T> state) {
            ThinkerObject.this.beforeMovementActions(game, state);
        }
        
        @Override
        public final void frameActions(T game, SpaceState<T> state) {
            ThinkerObject.this.frameActions(game, state);
        }
        
        @Override
        public final void addedActions(T game, SpaceState<T> state) {
            ThinkerObject.this.addedActions(game, state);
        }
        
        @Override
        public final void removedActions(T game, SpaceState<T> state) {
            ThinkerObject.this.removedActions(game, state);
        }
        
    };
    int movementPriority = 0;
    int newMovementPriority = 0;
    private boolean hasCollision = false;
    private Hitbox<T> collisionHitbox = null;
    private Double relPressingAngle = null;
    private ThinkerObject leader = null;
    final Set<ThinkerObject<T>> followers = new HashSet<>();
    ThinkerObject effLeader = null;
    final Map<SpaceObject<T>,Set<Direction>> collisions = new HashMap<>();
    final Set<Direction> collisionDirections = EnumSet.noneOf(Direction.class);
    private final CellVector velocity = new CellVector();
    private final CellVector step = new CellVector();
    CellVector displacement = new CellVector();
    
    /**
     * Creates a new ThinkerObject with the specified locator Hitbox.
     * @param locatorHitbox This ThinkerObject's locator Hitbox
     */
    public ThinkerObject(Hitbox<T> locatorHitbox) {
        super(locatorHitbox);
    }
    
    /**
     * Creates a new ThinkerObject with the specified locator Hitbox that acts
     * as if it was created by the specified SpaceObject, initially copying its
     * creator's time factor, flipped status, and angle of rotation.
     * @param locatorHitbox This ThinkerObject's locator Hitbox
     * @param creator This ThinkerObject's creator
     */
    public ThinkerObject(Hitbox<T> locatorHitbox, SpaceObject<T> creator) {
        super(locatorHitbox, creator);
    }
    
    @Override
    void addNonCellData() {
        super.addNonCellData();
        state.addThinkerObject(this);
        state.addThinker(thinker);
    }
    
    @Override
    void addCellData() {
        super.addCellData();
        if (hasCollision && collisionHitbox != null) {
            state.addCollisionHitbox(collisionHitbox);
        }
    }
    
    @Override
    void removeData() {
        super.removeData();
        state.removeThinker(thinker);
        state.removeThinkerObject(this);
        if (hasCollision && collisionHitbox != null) {
            state.removeCollisionHitbox(collisionHitbox);
        }
        collisions.clear();
        collisionDirections.clear();
        displacement.clear();
    }
    
    @Override
    void setTimeFactorActions(long timeFactor) {
        super.setTimeFactorActions(timeFactor);
        thinker.setTimeFactor(timeFactor);
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
    
    public final int getNumThinkers() {
        return thinker.getNumThinkers();
    }
    
    /**
     * Returns whether any Iterators over this CellGameState's list of Thinkers
     * are currently in progress.
     * @return Whether any Iterators over this CellGameState's list of Thinkers
     * are currently in progress
     */
    public final boolean iteratingThroughThinkers() {
        return thinker.iteratingThroughThinkers();
    }
    
    /**
     * Returns a new Iterator over this CellGameState's list of Thinkers.
     * @return A new Iterator over this CellGameState's list of Thinkers
     */
    public final SafeIterator<SpaceThinker<T>> thinkerIterator() {
        return thinker.thinkerIterator();
    }
    
    /**
     * Adds the specified Thinker to this CellGameState if it is not already
     * assigned to a CellGameState.
     * @param thinker The Thinker to be added
     * @return Whether the addition occurred
     */
    public final boolean addThinker(SpaceThinker<T> thinker) {
        return this.thinker.addThinker(thinker);
    }
    
    /**
     * Removes the specified Thinker from this CellGameState if it is currently
     * assigned to it.
     * @param thinker The Thinker to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeThinker(SpaceThinker<T> thinker) {
        return this.thinker.removeThinker(thinker);
    }
    
    /**
     * Removes from this SpaceState all of the SpaceObjects that are currently
     * assigned to it.
     */
    public final void removeAllThinkers() {
        thinker.removeAllThinkers();
    }
    
    public final boolean removeLineage(SpaceThinker<T> thinker) {
        return this.thinker.removeLineage(thinker);
    }
    
    /**
     * Returns this ThinkerObject's action priority.
     * @return This ThinkerObject's action priority
     */
    public final int getActionPriority() {
        return thinker.getActionPriority();
    }
    
    /**
     * Returns the action priority that this ThinkerObject is about to have, but
     * does not yet have due to its SpaceState's SpaceThinker list being
     * iterated over. If this ThinkerObject is not about to change its action
     * priority, this method will simply return its current action priority.
     * @return The action priority that this ThinkerObject is about to have
     */
    public final int getNewActionPriority() {
        return thinker.getNewActionPriority();
    }
    
    /**
     * Sets this ThinkerObject's action priority to the specified value.
     * @param actionPriority The new action priority
     */
    public final void setActionPriority(int actionPriority) {
        thinker.setActionPriority(actionPriority);
    }
    
    /**
     * Returns the current value of this ThinkerObject's timer for the specified
     * TimedEvent.
     * @param timedEvent The TimedEvent whose timer value should be returned
     * @return The current value of the timer for the specified TimedEvent
     */
    public final int getTimerValue(TimedEvent timedEvent) {
        return thinker.getTimerValue(timedEvent);
    }
    
    /**
     * Sets the value of this ThinkerObject's timer for the specified TimedEvent
     * to the specified value.
     * @param timedEvent The TimedEvent whose timer value should be set
     * @param value The new value of the specified TimedEvent's timer
     */
    public final void setTimerValue(TimedEvent timedEvent, int value) {
        thinker.setTimerValue(timedEvent, value);
    }
    
    /**
     * Actions for this ThinkerObject to take once every time unit, after
     * AnimationInstances update their indices but before SpaceThinkers take
     * their frameActions().
     * @param game This ThinkerObject's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void timeUnitActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every frame, after its
     * SpaceState moves its assigned ThinkerObjects.
     * @param game This ThinkerObject's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void beforeMovementActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every frame, after
     * SpaceThinkers take their timeUnitActions() but before its SpaceState
     * takes its own frameActions().
     * @param game This ThinkerObject's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void frameActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take immediately after being added to a
     * new SpaceState.
     * @param game This ThinkerObject's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void addedActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take immediately before being removed
     * from its SpaceState.
     * @param game This ThinkerObject's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void removedActions(T game, SpaceState<T> state) {}
    
    /**
     * Returns this ThinkerObject's movement priority.
     * @return This ThinkerObject's movement priority
     */
    public final int getMovementPriority() {
        return movementPriority;
    }
    
    /**
     * Returns the movement priority that this ThinkerObject is about to have,
     * but does not yet have due to its SpaceState's ThinkerObject list being
     * iterated over. If this ThinkerObject is not about to change its movement
     * priority, this method will simply return its current movement priority.
     * @return The movement priority that this ThinkerObject is about to have
     */
    public final int getNewMovementPriority() {
        return newMovementPriority;
    }
    
    /**
     * Sets this ThinkerObject's movement priority to the specified value.
     * @param movementPriority The new movement priority
     */
    public final void setMovementPriority(int movementPriority) {
        if (state == null) {
            this.newMovementPriority = movementPriority;
            this.movementPriority = movementPriority;
        } else if (this.newMovementPriority != movementPriority) {
            this.newMovementPriority = movementPriority;
            state.changeThinkerObjectMovementPriority(this, movementPriority);
        }
    }
    
    /**
     * Returns whether this ThinkerObject has Cell2D's standard collision
     * mechanics enabled.
     * @return Whether this ThinkerObject has collision enabled
     */
    public final boolean hasCollision() {
        return hasCollision;
    }
    
    /**
     * Sets whether this ThinkerObject has Cell2D's standard collision mechanics
     * enabled.
     * @param hasCollision Whether this ThinkerObject should have collision
     * enabled
     */
    public final void setCollision(boolean hasCollision) {
        if (state != null && collisionHitbox != null) {
            if (hasCollision && !this.hasCollision) {
                state.addCollisionHitbox(collisionHitbox);
            } else if (!hasCollision && this.hasCollision) {
                state.removeCollisionHitbox(collisionHitbox);
            }
        }
        this.hasCollision = hasCollision;
    }
    
    /**
     * Returns this ThinkerObject's collision Hitbox, or null if it has none.
     * @return This ThinkerObject's collision Hitbox
     */
    public final Hitbox<T> getCollisionHitbox() {
        return collisionHitbox;
    }
    
    /**
     * Sets this ThinkerObject's collision Hitbox to the specified Hitbox. The
     * new collision Hitbox may not be a component of a CompositeHitbox or in
     * use by another SpaceObject. If the specified Hitbox is null, the current
     * collision Hitbox will be removed if there is one, but it will not be
     * replaced with anything.
     * @param collisionHitbox The new collision Hitbox
     * @return Whether the change occurred
     */
    public final boolean setCollisionHitbox(Hitbox<T> collisionHitbox) {
        if (collisionHitbox != this.collisionHitbox) {
            boolean acceptable;
            Hitbox locatorHitbox = getLocatorHitbox();
            if (collisionHitbox == null) {
                acceptable = true;
            } else {
                SpaceObject<T> object = collisionHitbox.getObject();
                Hitbox<T> parent = collisionHitbox.getParent();
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
     * ThinkerObject's SpaceState whose solid Hitboxes' rectangular bounding
     * boxes touch or intersect this ThinkerObject's collision Hitbox's
     * rectangular bounding box.
     * @param <O> The subclass of SpaceObject to search for
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the solid SpaceObjects of the specified class whose solid
     * Hitboxes' bounding boxes meet this ThinkerObject's collision Hitbox's
     * bounding box
     */
    public final <O extends SpaceObject<T>> List<O> solidBoundingBoxesMeet(Class<O> cls) {
        return (state == null || collisionHitbox == null ? new ArrayList<>() : state.solidBoundingBoxesMeet(collisionHitbox, cls));
    }
    
    /**
     * Returns this ThinkerObject's relative pressing angle, or null if it has
     * none.
     * @return This ThinkerObject's relative pressing angle
     */
    public final Double getRelPressingAngle() {
        return relPressingAngle;
    }
    
    /**
     * Sets this ThinkerObject's relative pressing angle to the specified value,
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
     * Sets this ThinkerObject's relative pressing angle to the specified value.
     * @param angle The new relative pressing angle
     */
    public final void setRelPressingAngle(double angle) {
        relPressingAngle = angle % 360;
        if (relPressingAngle < 0) {
            relPressingAngle += 360;
        }
    }
    
    /**
     * Returns this ThinkerObject's absolute pressing angle, or null if it has
     * none.
     * @return This ThinkerObject's absolute pressing angle
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
     * Returns whether this ThinkerObject's absolute pressing angle, if it has
     * one, has a component in the specified Direction.
     * @param direction The Direction to check
     * @return Whether this ThinkerObject's absolute pressing angle causes it to
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
     * Returns this ThinkerObject's leader, or null if it has none.
     * @return This ThinkerObject's leader
     */
    public final ThinkerObject getLeader() {
        return leader;
    }
    
    /**
     * Sets this ThinkerObject's leader to the specified ThinkerObject. If it is
     * set to a null ThinkerObject, this ThinkerObject will be removed from its
     * current leader if it has one.
     * @param leader The new leader
     */
    public final void setLeader(ThinkerObject leader) {
        if (this.leader != null) {
            this.leader.removeFollower(this);
        }
        if (leader != null) {
            leader.addFollower(this);
        }
    }
    
    /**
     * Returns the Set of this ThinkerObject's followers. Changes to the
     * returned Set will not be reflected in this ThinkerObject.
     * @return The Set of this ThinkerObject's followers
     */
    public final Set<ThinkerObject> getFollowers() {
        return new HashSet<>(followers);
    }
    
    /**
     * Returns the number of followers that this ThinkerObject currently has.
     * @return This ThinkerObject's number of followers
     */
    public final int getNumFollowers() {
        return followers.size();
    }
    
    /**
     * Adds the specified ThinkerObject as this ThinkerObject's follower if it
     * does not have a leader already.
     * @param follower The new follower
     * @return Whether the addition occurred
     */
    public final boolean addFollower(ThinkerObject follower) {
        if (follower != this && follower.leader == null) {
            ThinkerObject ancestor = leader;
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
     * Removes the specified ThinkerObject as this ThinkerObject's follower if
     * this ThinkerObject is its leader.
     * @param follower The follower to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeFollower(ThinkerObject follower) {
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
     * Removes all of this ThinkerObject's followers from it.
     */
    public final void clearFollowers() {
        for (ThinkerObject follower : followers) {
            if (follower.effLeader == follower.leader) {
                follower.effLeader = null;
            }
            follower.leader = null;
        }
        followers.clear();
    }
    
    /**
     * Sets the SpaceState to which this ThinkerObject is currently assigned. If
     * it is set to a null SpaceState, this ThinkerObject will be removed from
     * its current SpaceState if it has one.
     * @param state The SpaceState to which this ThinkerObject should be
     * assigned
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will be assigned to the same SpaceState (false by default)
     */
    public final void setGameState(SpaceState<T> state, boolean bringFollowers) {
        setGameState(state);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.setGameState(state, true);
            }
        }
    }
    
    /**
     * Sets this ThinkerObject's position to the specified value.
     * @param position The new position
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will change their positions by the same amount as this
     * ThinkerObject (false by default)
     */
    public final void setPosition(CellVector position, boolean bringFollowers) {
        long changeX = position.getX() - getX();
        long changeY = position.getY() - getY();
        setPosition(position);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changePosition(changeX, changeY, true);
            }
        }
    }
    
    /**
     * Sets this ThinkerObject's position to the specified value.
     * @param x The x-coordinate of the new position
     * @param y The y-coordinate of the new position
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will change their positions by the same amount as this
     * ThinkerObject (false by default)
     */
    public final void setPosition(long x, long y, boolean bringFollowers) {
        long changeX = x - getX();
        long changeY = y - getY();
        setPosition(x, y);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changePosition(changeX, changeY, true);
            }
        }
    }
    
    /**
     * Sets the x-coordinate of this ThinkerObject's position to the specified
     * value.
     * @param x The x-coordinate of the new position
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will change their positions by the same amount as this
     * ThinkerObject (false by default)
     */
    public final void setX(long x, boolean bringFollowers) {
        long changeX = x - getX();
        setX(x);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changeX(changeX, true);
            }
        }
    }
    
    /**
     * Sets the y-coordinate of this ThinkerObject's position to the specified
     * value.
     * @param y The y-coordinate of the new position
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will change their positions by the same amount as this
     * ThinkerObject (false by default)
     */
    public final void setY(long y, boolean bringFollowers) {
        long changeY = y - getY();
        setY(y);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changeY(changeY, true);
            }
        }
    }
    
    /**
     * Changes this ThinkerObject's position by the specified amount.
     * @param change The amount to change the position by
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will change their positions by the same amount as this
     * ThinkerObject (false by default)
     */
    public final void changePosition(CellVector change, boolean bringFollowers) {
        changePosition(change);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changePosition(change, true);
            }
        }
    }
    
    /**
     * Changes the coordinates of this ThinkerObject's position by the specified
     * amounts.
     * @param changeX The amount to change the position's x-coordinate by
     * @param changeY The amount to change the position's y-coordinate by
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will change their positions by the same amount as this
     * ThinkerObject (false by default)
     */
    public final void changePosition(long changeX, long changeY, boolean bringFollowers) {
        changePosition(changeX, changeY);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changePosition(changeX, changeY, true);
            }
        }
    }
    
    /**
     * Changes the x-coordinate of this ThinkerObject's position by the
     * specified amount.
     * @param changeX The amount to change the position's x-coordinate by
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will change their positions by the same amount as this
     * ThinkerObject (false by default)
     */
    public final void changeX(long changeX, boolean bringFollowers) {
        changeX(changeX);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changeX(changeX, true);
            }
        }
    }
    
    /**
     * Changes the y-coordinate of this ThinkerObject's position by the
     * specified amount.
     * @param changeY The amount to change the position's y-coordinate by
     * @param bringFollowers If true, all of this ThinkerObject's followers and
     * sub-followers will change their positions by the same amount as this
     * ThinkerObject (false by default)
     */
    public final void changeY(long changeY, boolean bringFollowers) {
        changeY(changeY);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changeY(changeY, true);
            }
        }
    }
    
    /**
     * Moves this ThinkerObject and its followers and sub-followers by the
     * specified amount, colliding with solid surfaces if they have collision
     * enabled.
     * @param change The amount by which this ThinkerObject should move
     */
    public final void doMovement(CellVector change) {
        doMovement(change.getX(), change.getY());
    }
    
    /**
     * Moves this ThinkerObject and its followers and sub-followers by the
     * specified amount, colliding with solid surfaces if they have collision
     * enabled.
     * @param changeX The amount by which this ThinkerObject should move along
     * the x-axis
     * @param changeY The amount by which this ThinkerObject should move along
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
            displacement = state.move(this, changeX, changeY);
        }
    }
    
    /**
     * This ThinkerObject's response to colliding with a solid surface of the
     * specified SpaceObject in the specified Direction.
     * @param object The SpaceObject whose surface this ThinkerObject collided
     * with
     * @param direction The Direction in which this ThinkerObject collided with
     * the surface
     * @return The CollisionResponse to the collision (SLIDE by default)
     */
    public CollisionResponse collide(SpaceObject<T> object, Direction direction) {
        return CollisionResponse.SLIDE;
    }
    
    final void addCollision(SpaceObject<T> object, Direction direction) {
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
     * Returns a Map of the SpaceObjects whose solid surfaces this ThinkerObject
     * collided with during its last movement to the Sets of the Directions in
     * which it collided with them. Changes to the returned Map will not be
     * reflected in this ThinkerObject.
     * @return A Map of this ThinkerObject's collisions during its last movement
     */
    public final Map<SpaceObject<T>,Set<Direction>> getCollisions() {
        Map<SpaceObject<T>,Set<Direction>> collisionMap = new HashMap<>();
        for (Map.Entry<SpaceObject<T>,Set<Direction>> entry : collisions.entrySet()) {
            collisionMap.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
        }
        return collisionMap;
    }
    
    /**
     * Returns the Set of the Directions in which this ThinkerObject collided
     * with solid surfaces during its last movement. Changes to the returned set
     * will not be reflected in this ThinkerObject.
     * @return The Set of the Directions in which this ThinkerObject collided
     * with solid surfaces during its last movement
     */
    public final Set<Direction> getCollisionDirections() {
        return EnumSet.copyOf(collisionDirections);
    }
    
    /**
     * Returns whether this ThinkerObject collided with any solid surfaces
     * during its last movement.
     * @return Whether this ThinkerObject collided with any solid surfaces
     * during its last movement
     */
    public final boolean collided() {
        return !collisions.isEmpty();
    }
    
    /**
     * Returns whether this ThinkerObject collided with any solid surfaces in
     * the specified Direction during its last movement.
     * @param direction The Direction to check
     * @return Whether this ThinkerObject collided with any solid surfaces in
     * the specified Direction during its last movement
     */
    public final boolean collided(Direction direction) {
        return collisionDirections.contains(direction);
    }
    
    /**
     * Returns this ThinkerObject's velocity.
     * @return This ThinkerObject's velocity
     */
    public final CellVector getVelocity() {
        return new CellVector(velocity);
    }
    
    /**
     * Returns the x-component of this ThinkerObject's velocity.
     * @return The x-component of this ThinkerObject's velocity
     */
    public final long getVelocityX() {
        return velocity.getX();
    }
    
    /**
     * Returns the y-component of this ThinkerObject's velocity.
     * @return The y-component of this ThinkerObject's velocity
     */
    public final long getVelocityY() {
        return velocity.getY();
    }
    
    /**
     * Returns this ThinkerObject's speed, the magnitude of its velocity.
     * @return This ThinkerObject's speed
     */
    public final long getSpeed() {
        return velocity.getMagnitude();
    }
    
    /**
     * Sets this ThinkerObject's velocity to the specified value.
     * @param velocity The new velocity
     */
    public final void setVelocity(CellVector velocity) {
        this.velocity.setCoordinates(velocity);
    }
    
    /**
     * Sets this ThinkerObject's velocity to the specified value.
     * @param velocityX The new x-component of the velocity
     * @param velocityY The new y-component of the velocity
     */
    public final void setVelocity(long velocityX, long velocityY) {
        velocity.setCoordinates(velocityX, velocityY);
    }
    
    /**
     * Sets the x-component of this ThinkerObject's velocity to the specified
     * value.
     * @param velocityX The new x-component of the velocity
     */
    public final void setVelocityX(long velocityX) {
        velocity.setX(velocityX);
    }
    
    /**
     * Sets the y-component of this ThinkerObject's velocity to the specified
     * value.
     * @param velocityY The new y-component of the velocity
     */
    public final void setVelocityY(long velocityY) {
        velocity.setY(velocityY);
    }
    
    /**
     * Sets this ThinkerObject's speed, the magnitude of its velocity, to the
     * specified value.
     * @param speed The new speed
     */
    public final void setSpeed(long speed) {
        velocity.setMagnitude(speed);
    }
    
    /**
     * Sets this ThinkerObject's velocity to send it moving toward the specified
     * point at the specified speed.
     * @param point The point that this ThinkerObject should move toward
     * @param speed The speed that this ThinkerObject should move at
     */
    public final void moveToward(CellVector point, long speed) {
        setVelocity(point.getX() - getX(), point.getY() - getY());
        setSpeed(speed);
    }
    
    /**
     * Sets this ThinkerObject's velocity to send it moving toward the specified
     * point at the specified speed.
     * @param x The x-coordinate of the point that this ThinkerObject should
     * move toward
     * @param y The y-coordinate of the point that this ThinkerObject should
     * move toward
     * @param speed The speed that this ThinkerObject should move at
     */
    public final void moveToward(long x, long y, long speed) {
        setVelocity(x - getX(), y - getY());
        setSpeed(speed);
    }
    
    /**
     * Returns this ThinkerObject's step.
     * @return This ThinkerObject's step
     */
    public final CellVector getStep() {
        return new CellVector(step);
    }
    
    /**
     * Returns the x-component of this ThinkerObject's step.
     * @return The x-component of this ThinkerObject's step
     */
    public final long getStepX() {
        return step.getX();
    }
    
    /**
     * Returns the y-component of this ThinkerObject's step.
     * @return The y-component of this ThinkerObject's step
     */
    public final long getStepY() {
        return step.getY();
    }
    
    /**
     * Returns the length of this ThinkerObject's step.
     * @return The length of this ThinkerObject's step
     */
    public final long getStepLength() {
        return step.getMagnitude();
    }
    
    /**
     * Sets this ThinkerObject's step to the specified value.
     * @param step The new step
     */
    public final void setStep(CellVector step) {
        this.step.setCoordinates(step);
    }
    
    /**
     * Sets this ThinkerObject's step to the specified value.
     * @param stepX The x-component of the new step
     * @param stepY The y-component of the new step
     */
    public final void setStep(long stepX, long stepY) {
        step.setCoordinates(stepX, stepY);
    }
    
    /**
     * Sets the x-component of this ThinkerObject's step to the specified value.
     * @param stepX The new x-component of the step
     */
    public final void setStepX(long stepX) {
        step.setX(stepX);
    }
    
    /**
     * Sets the y-component of this ThinkerObject's step to the specified value.
     * @param stepY The new y-component of the step
     */
    public final void setStepY(long stepY) {
        step.setY(stepY);
    }
    
    /**
     * Sets the length of this ThinkerObject's step to the specified value.
     * @param length The new step length
     */
    public final void setStepLength(long length) {
        step.setMagnitude(length);
    }
    
    /**
     * Changes this ThinkerObject's step by the specified amount.
     * @param change The amount to change the step by
     */
    public final void changeStep(CellVector change) {
        step.add(change);
    }
    
    /**
     * Changes this ThinkerObject's step by the specified amount.
     * @param changeX The amount to change the step's x-component by
     * @param changeY The amount to change the step's y-component by
     */
    public final void changeStep(long changeX, long changeY) {
        step.add(changeX, changeY);
    }
    
    /**
     * Changes the x-component of this ThinkerObject's step by the specified
     * amount.
     * @param changeX The amount to change the step's x-component by
     */
    public final void changeStepX(long changeX) {
        step.add(changeX, 0);
    }
    
    /**
     * Changes the y-component of this ThinkerObject's step by the specified
     * amount.
     * @param changeY The amount to change the step's y-component by
     */
    public final void changeStepY(long changeY) {
        step.add(0, changeY);
    }
    
    /**
     * Returns this ThinkerObject's displacement during its last movement.
     * @return This ThinkerObject's displacement during its last movement
     */
    public final CellVector getDisplacement() {
        return new CellVector(displacement);
    }
    
    /**
     * Returns the x-component of this ThinkerObject's displacement during its
     * last movement.
     * @return The x-component of this ThinkerObject's displacement during its
     * last movement
     */
    public final long getDisplacementX() {
        return displacement.getX();
    }
    
    /**
     * Returns the y-component of this ThinkerObject's displacement during its
     * last movement.
     * @return The y-component of this ThinkerObject's displacement during its
     * last movement
     */
    public final long getDisplacementY() {
        return displacement.getY();
    }
    
    /**
     * Returns the length of this ThinkerObject's displacement during its last
     * movement.
     * @return The length of this ThinkerObject's displacement during its last
     * movement
     */
    public final long getDisplacementLength() {
        return displacement.getMagnitude();
    }
    
}
