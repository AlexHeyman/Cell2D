package ironclad2D.level;

import ironclad2D.IroncladGame;

public abstract class ObjectState {
    
    private final int duration;
    private ObjectState nextState = null;
    private boolean nextStateSet = false;
    
    public ObjectState(int duration) {
        this.duration = duration;
    }
    
    public boolean setNextState(ObjectState nextState) {
        if (nextStateSet) {
            return false;
        }
        this.nextState = nextState;
        nextStateSet = true;
        return true;
    }
    
    public boolean nextStateSet() {
        return nextStateSet;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public ObjectState getNextState() {
        return nextState;
    }
    
    public void enteredActions(IroncladGame game, LevelState levelState) {}
    
    public void leftActions(IroncladGame game, LevelState levelState) {}
    
    public void timeUnitActions(IroncladGame game, LevelState levelState) {}
    
    public void stepActions(IroncladGame game, LevelState levelState) {}
    
    public void reactToLevel(IroncladGame game, LevelState levelState) {}
    
    public void reactToInput(IroncladGame game, LevelState levelState) {}
    
}
