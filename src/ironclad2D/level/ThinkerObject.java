package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.TimedEvent;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ThinkerObject extends AnimatedObject {
    
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    private final LevelThinker thinker = new LevelThinker() {
        
        @Override
        public final void timeUnitActions(IroncladGame game, LevelState levelState) {
            ThinkerObject.this.timeUnitActions(game, levelState);
        }
        
        @Override
        public final void stepActions(IroncladGame game, LevelState levelState) {
            ThinkerObject.this.stepActions(game, levelState);
        }
        
        @Override
        public final void beforeMovementActions(IroncladGame game, LevelState levelState) {
            ThinkerObject.this.beforeMovementActions(game, levelState);
        }
        
        @Override
        public final void afterMovementActions(IroncladGame game, LevelState levelState) {
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
        state.addThinker(thinker);
        state.addThinkerObject(this);
    }
    
    @Override
    void addChunkData() {
        super.addChunkData();
        if (hasCollision && collisionHitbox != null) {
            state.addCollisionHitbox(collisionHitbox);
        }
    }
    
    @Override
    void removeActions() {
        super.removeActions();
        state.removeThinker(thinker);
        state.removeThinkerObject(this);
        if (hasCollision && collisionHitbox != null) {
            state.removeCollisionHitbox(collisionHitbox);
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
    
    public final int getTimerValue(TimedEvent<LevelState> timedEvent) {
        return thinker.getTimerValue(timedEvent);
    }
    
    public final void setTimerValue(TimedEvent<LevelState> timedEvent, int value) {
        thinker.setTimerValue(timedEvent, value);
    }
    
    public void timeUnitActions(IroncladGame game, LevelState levelState) {}
    
    public void stepActions(IroncladGame game, LevelState levelState) {}
    
    public void beforeMovementActions(IroncladGame game, LevelState levelState) {}
    
    public void afterMovementActions(IroncladGame game, LevelState levelState) {}
    
    public void addedActions(IroncladGame game, LevelState levelState) {}
    
    public void removedActions(IroncladGame game, LevelState levelState) {}
    
    public final LevelThinkerState getThinkerState() {
        return thinker.getThinkerState();
    }
    
    public final void changeThinkerState(LevelThinkerState newState) {
        thinker.changeThinkerState(newState);
    }
    
    public final int getMovementPriority() {
        return movementPriority;
    }
    
    public final void setMovementPriority(int movementPriority) {
        if (state == null) {
            this.movementPriority = movementPriority;
        } else {
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
    
    public final void moveTo(LevelVector position) {
        setVelocity(0, 0);
        setDisplacement(LevelVector.sub(position, getPosition()));
    }
    
    public final void moveTo(double x, double y) {
        setVelocity(0, 0);
        setDisplacement(x - getX(), y - getY());
    }
    
}
