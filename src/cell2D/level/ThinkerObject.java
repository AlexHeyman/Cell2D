package cell2d.level;

import cell2d.CellGame;
import cell2d.TimedEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>A ThinkerObject is a LevelObject that acts like a LevelThinker, possessing
 * LevelThinkerStates, timers, and various actions in response to events.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the LevelStates that this
 * ThinkerObject can be assigned to
 */
public abstract class ThinkerObject<T extends CellGame> extends LevelObject<T> {
    
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    private final LevelThinker<T> thinker = new LevelThinker<T>() {
        
        @Override
        public final void timeUnitActions(T game, LevelState<T> levelState) {
            ThinkerObject.this.timeUnitActions(game, levelState);
        }
        
        @Override
        public final void frameActions(T game, LevelState<T> levelState) {
            ThinkerObject.this.frameActions(game, levelState);
        }
        
        @Override
        public final void beforeMovementActions(T game, LevelState<T> levelState) {
            ThinkerObject.this.beforeMovementActions(game, levelState);
        }
        
        @Override
        public final void afterMovementActions(T game, LevelState<T> levelState) {
            ThinkerObject.this.afterMovementActions(game, levelState);
        }
        
        @Override
        public final void addedActions(T game, LevelState<T> levelState) {
            ThinkerObject.this.addedActions(game, levelState);
        }
        
        @Override
        public final void removedActions(T game, LevelState<T> levelState) {
            ThinkerObject.this.removedActions(game, levelState);
        }
        
    };
    int movementPriority = 0;
    int newMovementPriority = 0;
    private boolean hasCollision = false;
    private Hitbox<T> collisionHitbox = null;
    private Double pressingAngle = null;
    private ThinkerObject leader = null;
    final Set<ThinkerObject<T>> followers = new HashSet<>();
    ThinkerObject effLeader = null;
    final Map<LevelObject<T>,Set<Direction>> collisions = new HashMap<>();
    final Set<Direction> collisionDirections = EnumSet.noneOf(Direction.class);
    private final LevelVector velocity = new LevelVector();
    private final LevelVector step = new LevelVector();
    final LevelVector displacement = new LevelVector();
    
    public ThinkerObject(Hitbox locatorHitbox) {
        super(locatorHitbox);
        id = getNextID();
    }
    
    private static long getNextID() {
        return idCounter.getAndIncrement();
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
    
    public final int getActionPriority() {
        return thinker.getActionPriority();
    }
    
    public final int getNewActionPriority() {
        return thinker.getNewActionPriority();
    }
    
    public final void setActionPriority(int actionPriority) {
        thinker.setActionPriority(actionPriority);
    }
    
    public final LevelThinkerState getThinkerState() {
        return thinker.getThinkerState();
    }
    
    public final void setThinkerState(LevelThinkerState thinkerState) {
        thinker.setThinkerState(thinkerState);
    }
    
    public final int getThinkerStateDuration() {
        return thinker.getThinkerStateDuration();
    }
    
    public final void setThinkerStateDuration(int duration) {
        thinker.setThinkerStateDuration(duration);
    }
    
    public final int getTimerValue(TimedEvent<LevelState<T>> timedEvent) {
        return thinker.getTimerValue(timedEvent);
    }
    
    public final void setTimerValue(TimedEvent<LevelState<T>> timedEvent, int value) {
        thinker.setTimerValue(timedEvent, value);
    }
    
    public void timeUnitActions(T game, LevelState<T> levelState) {}
    
    public void frameActions(T game, LevelState<T> levelState) {}
    
    public void beforeMovementActions(T game, LevelState<T> levelState) {}
    
    public void afterMovementActions(T game, LevelState<T> levelState) {}
    
    public void addedActions(T game, LevelState<T> levelState) {}
    
    public void removedActions(T game, LevelState<T> levelState) {}
    
    public final int getMovementPriority() {
        return movementPriority;
    }
    
    public final int getNewMovementPriority() {
        return newMovementPriority;
    }
    
    public final void setMovementPriority(int movementPriority) {
        if (state == null) {
            this.newMovementPriority = movementPriority;
            this.movementPriority = movementPriority;
        } else if (this.newMovementPriority != movementPriority) {
            this.newMovementPriority = movementPriority;
            state.changeThinkerObjectMovementPriority(this, movementPriority);
        }
    }
    
    public final boolean hasCollision() {
        return hasCollision;
    }
    
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
    
    public final Hitbox<T> getCollisionHitbox() {
        return collisionHitbox;
    }
    
    public final boolean setCollisionHitbox(Hitbox<T> collisionHitbox) {
        if (collisionHitbox != this.collisionHitbox) {
            boolean acceptable;
            Hitbox locatorHitbox = getLocatorHitbox();
            if (collisionHitbox == null) {
                acceptable = true;
            } else {
                LevelObject<T> object = collisionHitbox.getObject();
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
    
    public final Double getPressingAngle() {
        return pressingAngle;
    }
    
    public final void setPressingAngle(Double angle) {
        if (angle == null) {
            pressingAngle = null;
        } else {
            pressingAngle = angle % 360;
            if (pressingAngle < 0) {
                pressingAngle += 360;
            }
        }
    }
    
    public final ThinkerObject getLeader() {
        return leader;
    }
    
    public final void setLeader(ThinkerObject leader) {
        if (this.leader != null) {
            this.leader.removeFollower(this);
        }
        if (leader != null) {
            leader.addFollower(this);
        }
    }
    
    public final int getNumFollowers() {
        return followers.size();
    }
    
    public final Set<ThinkerObject> getFollowers() {
        return new HashSet<>(followers);
    }
    
    public final boolean addFollower(ThinkerObject follower) {
        if (follower != null && follower != this && follower.leader == null) {
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
    
    public final boolean removeFollower(ThinkerObject follower) {
        if (follower != null && follower.leader == this) {
            followers.remove(follower);
            if (follower.effLeader == follower.leader) {
                follower.effLeader = null;
            }
            follower.leader = null;
            return true;
        }
        return false;
    }
    
    public final void clearFollowers() {
        for (ThinkerObject follower : followers) {
            if (follower.effLeader == follower.leader) {
                follower.effLeader = null;
            }
            follower.leader = null;
        }
        followers.clear();
    }
    
    public final void setGameState(LevelState<T> state, boolean bringFollowers) {
        setGameState(state);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.setGameState(state, true);
            }
        }
    }
    
    public final void setPosition(LevelVector position, boolean bringFollowers) {
        double deltaX = position.getX() - getX();
        double deltaY = position.getY() - getY();
        setPosition(position);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.setPosition(follower.getX() + deltaX, follower.getY() + deltaY, true);
            }
        }
    }
    
    public final void setPosition(double x, double y, boolean bringFollowers) {
        double deltaX = x - getX();
        double deltaY = y - getY();
        setPosition(x, y);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.setPosition(follower.getX() + deltaX, follower.getY() + deltaY, true);
            }
        }
    }
    
    public final void setX(double x, boolean bringFollowers) {
        double deltaX = x - getX();
        setX(x);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.setX(follower.getX() + deltaX, true);
            }
        }
    }
    
    public final void setY(double y, boolean bringFollowers) {
        double deltaY = y - getY();
        setY(y);
        if (bringFollowers && !followers.isEmpty()) {
            for (ThinkerObject follower : followers) {
                follower.setY(follower.getY() + deltaY, true);
            }
        }
    }
    
    public final void doMovement(LevelVector change) {
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
            if (changeX != 0 || changeY != 0) {
                state.move(this, changeX, changeY);
            }
        }
    }
    
    public CollisionResponse collide(LevelObject<T> object, Direction direction) {
        return CollisionResponse.SLIDE;
    }
    
    final void addCollision(LevelObject<T> object, Direction direction) {
        Set<Direction> collisionsWithObject = collisions.get(object);
        if (collisionsWithObject == null) {
            collisionsWithObject = EnumSet.of(direction);
            collisions.put(object, collisionsWithObject);
        } else {
            collisionsWithObject.add(direction);
        }
        collisionDirections.add(direction);
    }
    
    public final Map<LevelObject<T>,Set<Direction>> getCollisions() {
        Map<LevelObject<T>,Set<Direction>> collisionMap = new HashMap<>();
        for (Map.Entry<LevelObject<T>,Set<Direction>> entry : collisions.entrySet()) {
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
    
    public final LevelVector getVelocity() {
        return new LevelVector(velocity);
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
    
    public final void setVelocity(LevelVector velocity) {
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
    
    public final LevelVector getStep() {
        return new LevelVector(step);
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
    
    public final void setStep(LevelVector step) {
        this.step.setCoordinates(step);
    }
    
    public final void setStep(double stepX, double stepY) {
        step.setCoordinates(stepX, stepY);
    }
    
    public final void setStepLength(double length) {
        step.setMagnitude(length);
    }
    
    public final void changeStep(LevelVector change) {
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
    
    public final void moveToward(LevelVector position, double speed) {
        setVelocity(position.getX() - getX(), position.getY() - getY());
        setSpeed(speed);
    }
    
    public final void moveToward(double x, double y, double speed) {
        setVelocity(x - getX(), y - getY());
        setSpeed(speed);
    }
    
    public final void stepTo(LevelVector position) {
        setVelocity(0, 0);
        setStep(position.getX() - getX(), position.getY() - getY());
    }
    
    public final void stepTo(double x, double y) {
        setVelocity(0, 0);
        setStep(x - getX(), y - getY());
    }
    
    public final void stepToward(LevelVector position, double speed) {
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
    
    public final LevelVector getDisplacement() {
        return new LevelVector(displacement);
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
