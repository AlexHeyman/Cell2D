package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.TimedEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ThinkerObject extends AnimatedObject {
    
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    private final LevelThinker thinker = new ObjectThinker();
    private ObjectState currentState = null;
    private final Queue<ObjectState> upcomingStates = new LinkedList<>();
    private boolean changingState = false;
    private final TimedEvent<LevelState> nextState = new TimedEvent<LevelState>() {
        
        @Override
        public void eventActions(IroncladGame game, LevelState levelState) {
            endState(game, levelState, true);
        }
        
    };
    int movementPriority = 0;
    private boolean hasCollision = false;
    private Hitbox collisionHitbox;
    private final LevelVector velocity = new LevelVector();
    private final LevelVector displacement = new LevelVector();
    
    public ThinkerObject(Hitbox locatorHitbox, int drawLayer) {
        super(locatorHitbox, drawLayer);
        id = getNextID();
    }
    
    private static long getNextID() {
        return idCounter.getAndIncrement();
    }
    
    @Override
    void addActions() {
        super.addActions();
        levelState.addThinker(thinker);
        levelState.addThinkerObject(this);
        if (!upcomingStates.isEmpty()) {
            endState(levelState.getGame(), levelState, false);
        }
    }
    
    @Override
    void addChunkData() {
        super.addChunkData();
        if (hasCollision && collisionHitbox != null) {
            levelState.addCollisionHitbox(collisionHitbox);
        }
    }
    
    @Override
    void removeActions() {
        super.removeActions();
        levelState.removeThinker(thinker);
        levelState.removeThinkerObject(this);
        if (hasCollision && collisionHitbox != null) {
            levelState.removeCollisionHitbox(collisionHitbox);
        }
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
    
    public final LevelThinker getThinker() {
        return thinker;
    }
    
    public void timeUnitActions(IroncladGame game, LevelState levelState) {}
    
    public final int getTimerValue(TimedEvent<LevelState> timedEvent) {
        return thinker.getTimerValue(timedEvent);
    }
    
    public final void setTimerValue(TimedEvent<LevelState> timedEvent, int value) {
        thinker.setTimerValue(timedEvent, value);
    }
    
    public void stepActions(IroncladGame game, LevelState levelState) {}
    
    public void beforeMovementActions(IroncladGame game, LevelState levelState) {}
    
    public void afterMovementActions(IroncladGame game, LevelState levelState) {}
    
    public void addedActions(IroncladGame game, LevelState levelState) {}
    
    public void removedActions(IroncladGame game, LevelState levelState) {}
    
    private class ObjectThinker extends LevelThinker {
        
        private ObjectThinker() {}
        
        @Override
        public final void timeUnitActions(IroncladGame game, LevelState levelState) {
            if (currentState != null) {
                currentState.timeUnitActions(game, levelState);
            }
            ThinkerObject.this.timeUnitActions(game, levelState);
        }
        
        @Override
        public final void stepActions(IroncladGame game, LevelState levelState) {
            if (currentState != null) {
                currentState.stepActions(game, levelState);
            }
            ThinkerObject.this.stepActions(game, levelState);
        }
        
        @Override
        public final void beforeMovementActions(IroncladGame game, LevelState levelState) {
            if (currentState != null) {
                currentState.beforeMovementActions(game, levelState);
            }
            ThinkerObject.this.beforeMovementActions(game, levelState);
        }
        
        @Override
        public final void afterMovementActions(IroncladGame game, LevelState levelState) {
            if (currentState != null) {
                currentState.afterMovementActions(game, levelState);
            }
            ThinkerObject.this.afterMovementActions(game, levelState);
        }
        
        @Override
        public final void addedActions(IroncladGame game, LevelState levelState) {
            ThinkerObject.this.addedActions(game, levelState);
        }
        
        @Override
        public final void removedActions(IroncladGame game, LevelState levelState) {
            ThinkerObject.this.removedActions(game, levelState);
        }
        
    }
    
    public final ObjectState getCurrentState() {
        return currentState;
    }
    
    private void endState(IroncladGame game, LevelState levelState, boolean useNextState) {
        if (currentState != null) {
            setTimerValue(nextState, -1);
            changingState = true;
            currentState.leftActions(game, levelState);
            changingState = false;
        }
        if (!upcomingStates.isEmpty()) {
            beginState(game, levelState, upcomingStates.remove());
        } else if (useNextState) {
            beginState(game, levelState, currentState.getNextState());
        }
    }
    
    private void beginState(IroncladGame game, LevelState levelState, ObjectState newState) {
        currentState = newState;
        if (currentState != null) {
            changingState = true;
            currentState.enteredActions(game, levelState);
            changingState = false;
        }
        if (upcomingStates.isEmpty()) {
            if (currentState != null) {
                int duration = currentState.getDuration();
                if (duration > 0) {
                    setTimerValue(nextState, duration);
                } else if (duration == 0) {
                    endState(game, levelState, true);
                }
            }
        } else {
            endState(game, levelState, false);
        }
    }
    
    public final void changeState(ObjectState newState) {
        upcomingStates.add(newState);
        if (levelState != null && !changingState) {
            endState(levelState.getGame(), levelState, false);
        }
    }
    
    public final int getMovementPriority() {
        return movementPriority;
    }
    
    public final void setMovementPriority(int movementPriority) {
        if (levelState == null) {
            this.movementPriority = movementPriority;
        } else {
            levelState.changeThinkerObjectMovementPriority(this, movementPriority);
        }
    }
    
    public final boolean hasCollision() {
        return hasCollision;
    }
    
    public final void setCollision(boolean hasCollision) {
        if (levelState != null && collisionHitbox != null) {
            if (hasCollision && !this.hasCollision) {
                levelState.addCollisionHitbox(collisionHitbox);
            } else if (!hasCollision && this.hasCollision) {
                levelState.removeCollisionHitbox(collisionHitbox);
            }
        }
        this.hasCollision = hasCollision;
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
                        && !collisionHitbox.isComponentOf(locatorHitbox));
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
    
    public final LevelVector getVelocity() {
        return new LevelVector(velocity);
    }
    
    public final double getVelocityX() {
        return velocity.getX();
    }
    
    public final double getVelocityY() {
        return velocity.getY();
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
    
    public final LevelVector getDisplacement() {
        return new LevelVector(displacement);
    }
    
    public final double getDisplacementX() {
        return displacement.getX();
    }
    
    public final double getDisplacementY() {
        return displacement.getY();
    }
    
    public final void setDisplacement(LevelVector displacement) {
        this.displacement.copy(displacement);
    }
    
    public final void changeDisplacement(LevelVector displacement) {
        this.displacement.add(displacement);
    }
    
    public final void setDisplacement(double displacementX, double displacementY) {
        displacement.setCoordinates(displacementX, displacementY);
    }
    
    public final void changeDisplacement(double displacementX, double displacementY) {
        displacement.add(displacementX, displacementY);
    }
    
    public final void setDisplacementX(double displacementX) {
        displacement.setX(displacementX);
    }
    
    public final void changeDisplacementX(double displacementX) {
        displacement.add(displacementX, 0);
    }
    
    public final void setDisplacementY(double displacementY) {
        displacement.setY(displacementY);
    }
    
    public final void changeDisplacementY(double displacementY) {
        displacement.add(0, displacementY);
    }
    
}
