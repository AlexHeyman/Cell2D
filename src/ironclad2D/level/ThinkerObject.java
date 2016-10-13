package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.TimedEvent;

public abstract class ThinkerObject extends AnimatedObject {
    
    private final LevelThinker thinker = new ObjectThinker();
    private final TimedEvent<LevelState> nextState;
    private ObjectState currentState = null;
    private boolean changingState = false;
    private Hitbox collisionHitbox;
    
    public ThinkerObject(Hitbox locatorHitbox, Hitbox collisionHitbox, int drawLayer) {
        super(locatorHitbox, drawLayer);
        nextState = new TimedEvent<LevelState>() {
            
            @Override
            public void eventActions(IroncladGame game, LevelState levelState) {
                changeState(game, levelState, currentState.nextState());
            }
            
        };
        this.collisionHitbox = collisionHitbox;
    }
    
    @Override
    void addActions() {
        super.addActions();
        levelState.addThinker(thinker);
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
    
    public void reactToLevel(IroncladGame game, LevelState levelState) {}
    
    public void reactToInput(IroncladGame game, LevelState levelState) {}
    
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
        public final void reactToLevel(IroncladGame game, LevelState levelState) {
            if (currentState != null) {
                currentState.reactToLevel(game, levelState);
            }
            ThinkerObject.this.reactToLevel(game, levelState);
        }
        
        @Override
        public final void reactToInput(IroncladGame game, LevelState levelState) {
            if (currentState != null) {
                currentState.reactToInput(game, levelState);
            }
            ThinkerObject.this.reactToInput(game, levelState);
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
    
    private boolean changeState(IroncladGame game, LevelState levelState, ObjectState newState) {
        if (!changingState) {
            changingState = true;
            if (currentState != null) {
                setTimerValue(nextState, -1);
                currentState.leftActions(game, levelState);
            }
            currentState = newState;
            if (currentState != null) {
                currentState.enteredActions(game, levelState);
                int duration = currentState.getDuration();
                if (duration == 0) {
                    changingState = false;
                    changeState(game, levelState, currentState.nextState());
                    return true;
                } else if (duration > 0) {
                    setTimerValue(nextState, duration);
                }
            }
            changingState = false;
            return true;
        }
        return false;
    }
    
    public final Hitbox getCollisionHitbox() {
        return collisionHitbox;
    }
    
}
