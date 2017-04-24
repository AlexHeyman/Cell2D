package cell2d.space;

import cell2d.CellGame;
import cell2d.CellVector;
import cell2d.TimedEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>A ThinkerObject is a SpaceObject that acts like a SpaceThinker, possessing
 * SpaceThinkerStates, timers, and various actions in response to events.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the SpaceStates that this
 * ThinkerObject can be assigned to
 */
public abstract class ThinkerObject<T extends CellGame> extends SpaceObject<T> {
    
    private final SpaceThinker<T> thinker = new SpaceThinker<T>() {
        
        @Override
        public final void timeUnitActions(T game, SpaceState<T> levelState) {
            ThinkerObject.this.timeUnitActions(game, levelState);
        }
        
        @Override
        public final void frameActions(T game, SpaceState<T> levelState) {
            ThinkerObject.this.frameActions(game, levelState);
        }
        
        @Override
        public final void beforeMovementActions(T game, SpaceState<T> levelState) {
            ThinkerObject.this.beforeMovementActions(game, levelState);
        }
        
        @Override
        public final void afterMovementActions(T game, SpaceState<T> levelState) {
            ThinkerObject.this.afterMovementActions(game, levelState);
        }
        
        @Override
        public final void addedActions(T game, SpaceState<T> levelState) {
            ThinkerObject.this.addedActions(game, levelState);
        }
        
        @Override
        public final void removedActions(T game, SpaceState<T> levelState) {
            ThinkerObject.this.removedActions(game, levelState);
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
    final CellVector displacement = new CellVector();
    
    /**
     * Creates a new ThinkerObject with the specified locator Hitbox.
     * @param locatorHitbox This ThinkerObject's locator Hitbox
     */
    public ThinkerObject(Hitbox locatorHitbox) {
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
    void addCellData() {
        super.addCellData();
        if (hasCollision && collisionHitbox != null) {
            state.addCollisionHitbox(collisionHitbox);
        }
    }
    
    @Override
    void addActions() {
        super.addActions();
        state.addThinkerObject(this);
        state.addThinker(thinker);
    }
    
    @Override
    void removeActions() {
        super.removeActions();
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
    void setTimeFactorActions(double timeFactor) {
        super.setTimeFactorActions(timeFactor);
        thinker.setTimeFactor(timeFactor);
    }
    
    @Override
    void removeNonLocatorHitboxes(Hitbox locatorHitbox) {
        super.removeNonLocatorHitboxes(locatorHitbox);
        locatorHitbox.removeChild(collisionHitbox);
    }
    
    @Override
    void addNonLocatorHitboxes(Hitbox locatorHitbox) {
        super.addNonLocatorHitboxes(locatorHitbox);
        locatorHitbox.addChild(collisionHitbox);
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
     * Returns this ThinkerObject's current SpaceThinkerState.
     * @return This ThinkerObject's current SpaceThinkerState
     */
    public final SpaceThinkerState getThinkerState() {
        return thinker.getThinkerState();
    }
    
    /**
     * Sets this ThinkerObject's current SpaceThinkerState to the specified one.
     * If this ThinkerObject is not assigned to a SpaceState, the change will
     * not occur until it is added to one, immediately before it takes its
     * addedActions().
     * @param thinkerState The new SpaceThinkerState
     */
    public final void setThinkerState(SpaceThinkerState thinkerState) {
        thinker.setThinkerState(thinkerState);
    }
    
    /**
     * Returns the remaining duration in time units of this ThinkerObject's
     * current SpaceThinkerState. A negative value indicates an infinite
     * duration.
     * @return The remaining duration in time units of this ThinkerObject's
     * current SpaceThinkerState
     */
    public final int getThinkerStateDuration() {
        return thinker.getThinkerStateDuration();
    }
    
    /**
     * Sets the remaining duration in time units of this ThinkerObject's current
     * SpaceThinkerState to the specified value. A negative value indicates an
     * infinite duration, and a value of 0 indicates that the SpaceThinkerState
     * should end as soon as possible.
     * @param duration The new duration in time units of this ThinkerObject's
     * current SpaceThinkerState
     */
    public final void setThinkerStateDuration(int duration) {
        thinker.setThinkerStateDuration(duration);
    }
    
    /**
     * Returns the current value of this ThinkerObject's timer for the specified
     * TimedEvent.
     * @param timedEvent The TimedEvent whose timer value should be returned
     * @return The current value of the timer for the specified TimedEvent
     */
    public final int getTimerValue(TimedEvent<SpaceState<T>> timedEvent) {
        return thinker.getTimerValue(timedEvent);
    }
    
    /**
     * Sets the value of this ThinkerObject's timer for the specified TimedEvent
     * to the specified value.
     * @param timedEvent The TimedEvent whose timer value should be set
     * @param value The new value of the specified TimedEvent's timer
     */
    public final void setTimerValue(TimedEvent<SpaceState<T>> timedEvent, int value) {
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
     * Actions for this ThinkerObject to take once every frame, after
     * SpaceThinkers take their timeUnitActions() but before its SpaceState
     * takes its own frameActions().
     * @param game This ThinkerObject's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void frameActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every frame, after
     * SpaceThinkers take their frameActions() but before its SpaceState moves
     * its assigned ThinkerObjects.
     * @param game This ThinkerObject's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void beforeMovementActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every frame, after its
     * SpaceState moves its assigned ThinkerObjects.
     * @param game This ThinkerObject's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void afterMovementActions(T game, SpaceState<T> state) {}
    
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
     * Returns whether this ThinkerObject collides with solid surfaces using
     * Cell2D's standard collision mechanics.
     * @return Whether this ThinkerObject collides with solid surfaces
     */
    public final boolean hasCollision() {
        return hasCollision;
    }
    
    /**
     * Sets whether this ThinkerObject collides with solid surfaces using
     * Cell2D's standard collision mechanics.
     * @param hasCollision Whether this ThinkerObject should collide with solid
     * surfaces
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
     * sub-followers will be assigned to the same SpaceState; defaults to false
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
     * ThinkerObject; defaults to false
     */
    public final void setPosition(CellVector position, boolean bringFollowers) {
        double changeX = position.getX() - getX();
        double changeY = position.getY() - getY();
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
     * ThinkerObject; defaults to false
     */
    public final void setPosition(double x, double y, boolean bringFollowers) {
        double changeX = x - getX();
        double changeY = y - getY();
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
     * ThinkerObject; defaults to false
     */
    public final void setX(double x, boolean bringFollowers) {
        double changeX = x - getX();
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
     * ThinkerObject; defaults to false
     */
    public final void setY(double y, boolean bringFollowers) {
        double changeY = y - getY();
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
     * ThinkerObject; defaults to false
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
     * ThinkerObject; defaults to false
     */
    public final void changePosition(double changeX, double changeY, boolean bringFollowers) {
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
     * ThinkerObject; defaults to false
     */
    public final void changeX(double changeX, boolean bringFollowers) {
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
     * ThinkerObject; defaults to false
     */
    public final void changeY(double changeY, boolean bringFollowers) {
        changeY(changeY);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.changeY(changeY, true);
            }
        }
    }
    
    /**
     * Moves this ThinkerObject and its followers and sub-followers by the
     * specified amount, colliding with solid surfaces 
     * @param change 
     */
    public final void doMovement(CellVector change) {
        doMovement(change.getX(), change.getY());
    }
    
    public final void doMovement(double changeX, double changeY) {
        if (state == null) {
            if (changeX != 0 || changeY != 0) {
                setPosition(getX() + changeX, getY() + changeY);
            }
        } else {
            collisions.clear();
            collisionDirections.clear();
            displacement.clear();
            state.move(this, changeX, changeY);
        }
    }
    
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
    
    public final Map<SpaceObject<T>,Set<Direction>> getCollisions() {
        Map<SpaceObject<T>,Set<Direction>> collisionMap = new HashMap<>();
        for (Map.Entry<SpaceObject<T>,Set<Direction>> entry : collisions.entrySet()) {
            collisionMap.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
        }
        return collisionMap;
    }
    
    public final Set<Direction> getCollisionDirections() {
        return EnumSet.copyOf(collisionDirections);
    }
    
    public final boolean collided() {
        return !collisions.isEmpty();
    }
    
    public final boolean collided(Direction direction) {
        return collisionDirections.contains(direction);
    }
    
    public final CellVector getVelocity() {
        return new CellVector(velocity);
    }
    
    public final double getVelocityX() {
        return velocity.getX();
    }
    
    public final double getVelocityY() {
        return velocity.getY();
    }
    
    public final double getSpeed() {
        return velocity.getMagnitude();
    }
    
    public final void setVelocity(CellVector velocity) {
        this.velocity.setCoordinates(velocity);
    }
    
    public final void setVelocity(double velocityX, double velocityY) {
        velocity.setCoordinates(velocityX, velocityY);
    }
    
    public final void setVelocityX(double velocityX) {
        velocity.setX(velocityX);
    }
    
    public final void setVelocityY(double velocityY) {
        velocity.setY(velocityY);
    }
    
    public final void setSpeed(double speed) {
        velocity.setMagnitude(speed);
    }
    
    public final CellVector getStep() {
        return new CellVector(step);
    }
    
    public final double getStepX() {
        return step.getX();
    }
    
    public final double getStepY() {
        return step.getY();
    }
    
    public final double getStepLength() {
        return step.getMagnitude();
    }
    
    public final void setStep(CellVector step) {
        this.step.setCoordinates(step);
    }
    
    public final void setStep(double stepX, double stepY) {
        step.setCoordinates(stepX, stepY);
    }
    
    public final void setStepLength(double length) {
        step.setMagnitude(length);
    }
    
    public final void changeStep(CellVector change) {
        step.add(change);
    }
    
    public final void changeStep(double changeX, double changeY) {
        step.add(changeX, changeY);
    }
    
    public final void setStepX(double stepX) {
        step.setX(stepX);
    }
    
    public final void changeStepX(double changeX) {
        step.add(changeX, 0);
    }
    
    public final void setStepY(double stepY) {
        step.setY(stepY);
    }
    
    public final void changeStepY(double changeY) {
        step.add(0, changeY);
    }
    
    public final void moveToward(CellVector position, double speed) {
        setVelocity(position.getX() - getX(), position.getY() - getY());
        setSpeed(speed);
    }
    
    public final void moveToward(double x, double y, double speed) {
        setVelocity(x - getX(), y - getY());
        setSpeed(speed);
    }
    
    public final void stepTo(CellVector position) {
        setVelocity(0, 0);
        setStep(position.getX() - getX(), position.getY() - getY());
    }
    
    public final void stepTo(double x, double y) {
        setVelocity(0, 0);
        setStep(x - getX(), y - getY());
    }
    
    public final void stepToward(CellVector position, double speed) {
        setVelocity(0, 0);
        setStep(position.getX() - getX(), position.getY() - getY());
        if (getStepLength() > speed) {
            setStepLength(speed);
        }
    }
    
    public final void stepToward(double x, double y, double speed) {
        setVelocity(0, 0);
        setStep(x - getX(), y - getY());
        if (getStepLength() > speed) {
            setStepLength(speed);
        }
    }
    
    public final CellVector getDisplacement() {
        return new CellVector(displacement);
    }
    
    public final double getDisplacementX() {
        return displacement.getX();
    }
    
    public final double getDisplacementY() {
        return displacement.getY();
    }
    
    public final double getDisplacementLength() {
        return displacement.getMagnitude();
    }
    
}
