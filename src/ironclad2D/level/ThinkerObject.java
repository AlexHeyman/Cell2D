package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.TimedEvent;
import java.util.LinkedList;
import java.util.Queue;

public abstract class ThinkerObject extends AnimatedObject {
    
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
    private boolean hasCollision = false;
    private Hitbox collisionHitbox;
    
    public ThinkerObject(Hitbox locatorHitbox, Hitbox collisionHitbox, int drawLayer) {
        super(locatorHitbox, drawLayer);
        this.collisionHitbox = collisionHitbox;
    }
    
    @Override
    void addActions() {
        super.addActions();
        levelState.addThinker(thinker);
        if (!upcomingStates.isEmpty()) {
            endState(levelState.getGame(), levelState, false);
        }
    }
    
    @Override
    void removeActions() {
        super.removeActions();
        levelState.removeThinker(thinker);
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
    
    public final boolean hasCollision() {
        return hasCollision;
    }
    
    public final void setCollision(boolean hasCollision) {
        this.hasCollision = hasCollision;
    }
    
    public final Hitbox getCollisionHitbox() {
        return collisionHitbox;
    }
    
}
