package cell2D.level;

import cell2D.CellGame;
import cell2D.TimedEvent;
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
    private boolean hasCollision = false;
    private Hitbox collisionHitbox;
    private final LevelVector velocity = new LevelVector();
    private final LevelVector displacement = new LevelVector();
    
    public ThinkerObject(Hitbox locatorHitbox, int drawPriority) {
        super(locatorHitbox, drawPriority);
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
            newMovementPriority = movementPriority;
            this.movementPriority = movementPriority;
        } else if (newMovementPriority != movementPriority) {
            newMovementPriority = movementPriority;
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
    
    public final void changeDisplacement(LevelVector change) {
        this.displacement.add(change);
    }
    
    public final void setDisplacement(double displacementX, double displacementY) {
        displacement.setCoordinates(displacementX, displacementY);
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
    
    public final void move(LevelVector change) {
        move(change.getX(), change.getY());
    }
    
    public final void move(double dx, double dy) {
        if (state == null) {
            setPosition(getX() + dx, getY() + dy);
        } else {
            state.move(this, dx, dy);
        }
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
    
}
