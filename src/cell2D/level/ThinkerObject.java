package cell2D.level;

import cell2D.CellGame;
import cell2D.TimedEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ThinkerObject extends AnimatedObject {
    
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    private final LevelThinker thinker = new LevelThinker() {
        
        @Override
        public final void timeUnitActions(CellGame game, LevelState levelState) {
            ThinkerObject.this.timeUnitActions(game, levelState);
        }
        
        @Override
        public final void stepActions(CellGame game, LevelState levelState) {
            ThinkerObject.this.stepActions(game, levelState);
        }
        
        @Override
        public final void beforeMovementActions(CellGame game, LevelState levelState) {
            ThinkerObject.this.beforeMovementActions(game, levelState);
        }
        
        @Override
        public final void afterMovementActions(CellGame game, LevelState levelState) {
            ThinkerObject.this.afterMovementActions(game, levelState);
        }
        
        @Override
        public final void addedActions(CellGame game, LevelState levelState) {
            ThinkerObject.this.addedActions(game, levelState);
        }
        
        @Override
        public final void removedActions(CellGame game, LevelState levelState) {
            ThinkerObject.this.removedActions(game, levelState);
        }
        
    };
    int movementPriority = 0;
    int newMovementPriority = 0;
    private CollisionMode collisionMode = CollisionMode.NONE;
    private Hitbox collisionHitbox = null;
    private final Map<LevelObject,Set<CollisionType>> collisions = new HashMap<>();
    private final Set<CollisionType> collisionTypes = EnumSet.noneOf(CollisionType.class);
    private final LevelVector velocity = new LevelVector();
    private final LevelVector displacement = new LevelVector();
    private ThinkerObject leader = null;
    final Set<ThinkerObject> followers = new HashSet<>();
    
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
        if (collisionMode != CollisionMode.NONE && collisionHitbox != null) {
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
        if (collisionMode != CollisionMode.NONE && collisionHitbox != null) {
            state.removeCollisionHitbox(collisionHitbox);
        }
        clearCollisions();
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
    
    public final int getTimerValue(TimedEvent<LevelState> timedEvent) {
        return thinker.getTimerValue(timedEvent);
    }
    
    public final void setTimerValue(TimedEvent<LevelState> timedEvent, int value) {
        thinker.setTimerValue(timedEvent, value);
    }
    
    public void timeUnitActions(CellGame game, LevelState levelState) {}
    
    public void stepActions(CellGame game, LevelState levelState) {}
    
    public void beforeMovementActions(CellGame game, LevelState levelState) {}
    
    public void afterMovementActions(CellGame game, LevelState levelState) {}
    
    public void addedActions(CellGame game, LevelState levelState) {}
    
    public void removedActions(CellGame game, LevelState levelState) {}
    
    public final LevelThinkerState getThinkerState() {
        return thinker.getThinkerState();
    }
    
    public final void changeThinkerState(LevelThinkerState newState) {
        thinker.changeThinkerState(newState);
    }
    
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
    
    public final CollisionMode getCollisionMode() {
        return collisionMode;
    }
    
    public final void setCollisionMode(CollisionMode collisionMode) {
        if (state != null && collisionHitbox != null) {
            if (collisionMode != CollisionMode.NONE && this.collisionMode == CollisionMode.NONE) {
                state.addCollisionHitbox(collisionHitbox);
            } else if (collisionMode == CollisionMode.NONE && this.collisionMode != CollisionMode.NONE) {
                state.removeCollisionHitbox(collisionHitbox);
            }
        }
        this.collisionMode = collisionMode;
    }
    
    public final Hitbox getCollisionHitbox() {
        return collisionHitbox;
    }
    
    public final boolean setCollisionHitbox(Hitbox collisionHitbox) {
        if (collisionHitbox != this.collisionHitbox) {
            boolean acceptable;
            Hitbox locatorHitbox = getLocatorHitbox();
            if (collisionHitbox == null) {
                acceptable = true;
            } else {
                LevelObject object = collisionHitbox.getObject();
                Hitbox parent = collisionHitbox.getParent();
                acceptable = (object == null && parent == null)
                        || (collisionHitbox == locatorHitbox)
                        || (object == this && parent == locatorHitbox
                        && collisionHitbox.getComponentOf() != locatorHitbox);
            }
            if (acceptable) {
                if (this.collisionHitbox != null) {
                    this.collisionHitbox.removeAsCollisionHitbox(collisionMode);
                }
                this.collisionHitbox = collisionHitbox;
                if (collisionHitbox != null) {
                    locatorHitbox.addChild(collisionHitbox);
                    collisionHitbox.addAsCollisionHitbox(collisionMode);
                }
                return true;
            }
        }
        return false;
    }
    
    public final <T extends LevelObject> boolean isIntersectingSolidObject(Class<T> cls) {
        return (state == null ? false : state.intersectingSolidObject(this, cls) != null);
    }
    
    public final <T extends LevelObject> T intersectingSolidObject(Class<T> cls) {
        return (state == null ? null : state.intersectingSolidObject(this, cls));
    }
    
    public final <T extends LevelObject> List<T> intersectingSolidObjects(Class<T> cls) {
        return (state == null ? new ArrayList<>() : state.intersectingSolidObjects(this, cls));
    }
    
    public final void doMovement(LevelVector change) {
        doMovement(change.getX(), change.getY());
    }
    
    public final void doMovement(double dx, double dy) {
        if (state == null) {
            setPosition(getX() + dx, getY() + dy);
        } else {
            state.move(this, dx, dy);
        }
    }
    
    public boolean checkCollision(LevelObject object, CollisionType collisionType) {
        return true;
    }
    
    final void clearCollisions() {
        collisions.clear();
        collisionTypes.clear();
    }
    
    final void addCollision(LevelObject object, CollisionType collisionType) {
        Set<CollisionType> collisionsWithObject = collisions.get(object);
        if (collisionsWithObject == null) {
            collisionsWithObject = EnumSet.of(collisionType);
            collisions.put(object, collisionsWithObject);
        } else {
            collisionsWithObject.add(collisionType);
        }
        collisionTypes.add(collisionType);
    }
    
    public final Map<LevelObject,Set<CollisionType>> getCollisions() {
        Map<LevelObject,Set<CollisionType>> collisionMap = new HashMap<>();
        for (Map.Entry<LevelObject,Set<CollisionType>> entry : collisions.entrySet()) {
            collisionMap.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
        }
        return collisionMap;
    }
    
    public final Set<CollisionType> getCollisionTypes() {
        return EnumSet.copyOf(collisionTypes);
    }
    
    public final boolean collided() {
        return !collisions.isEmpty();
    }
    
    public final boolean collided(CollisionType collisionType) {
        return collisionTypes.contains(collisionType);
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
        return velocity.getLength();
    }
    
    public final void setVelocity(LevelVector velocity) {
        this.velocity.copy(velocity);
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
        velocity.setLength(speed);
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
    
    public final double getDisplacementMagnitude() {
        return displacement.getLength();
    }
    
    public final void setDisplacement(LevelVector displacement) {
        this.displacement.copy(displacement);
    }
    
    public final void setDisplacement(double displacementX, double displacementY) {
        displacement.setCoordinates(displacementX, displacementY);
    }
    
    public final void setDisplacementMegnitude(double magnitude) {
        displacement.setLength(magnitude);
    }
    
    public final void changeDisplacement(LevelVector change) {
        displacement.add(change);
    }
    
    public final void changeDisplacement(double changeX, double changeY) {
        displacement.add(changeX, changeY);
    }
    
    public final void setDisplacementX(double displacementX) {
        displacement.setX(displacementX);
    }
    
    public final void changeDisplacementX(double changeX) {
        displacement.add(changeX, 0);
    }
    
    public final void setDisplacementY(double displacementY) {
        displacement.setY(displacementY);
    }
    
    public final void changeDisplacementY(double changeY) {
        displacement.add(0, changeY);
    }
    
    public final void moveTo(LevelVector position) {
        setVelocity(0, 0);
        setDisplacement(LevelVector.sub(position, getPosition()));
    }
    
    public final void moveTo(double x, double y) {
        setVelocity(0, 0);
        setDisplacement(x - getX(), y - getY());
    }
    
    public final void moveToward(LevelVector position, double speed) {
        setVelocity(0, 0);
        setDisplacement(LevelVector.sub(position, getPosition()));
        if (displacement.getLength() > speed) {
            displacement.setLength(speed);
        }
    }
    
    public final void moveToward(double x, double y, double speed) {
        setVelocity(0, 0);
        setDisplacement(x - getX(), y - getY());
        if (displacement.getLength() > speed) {
            displacement.setLength(speed);
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
    
    public final boolean addToLeader(ThinkerObject leader) {
        return leader.addFollower(this);
    }
    
    public final boolean removeFromLeader() {
        return (leader == null ? false : leader.removeFollower(this));
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
            follower.leader = this;
            return true;
        }
        return false;
    }
    
    public final boolean removeFollower(ThinkerObject follower) {
        if (follower != null && follower.leader == this) {
            followers.remove(follower);
            follower.leader = null;
            return true;
        }
        return false;
    }
    
}
