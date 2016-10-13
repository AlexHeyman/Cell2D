package ironclad2D.level;

import ironclad2D.IroncladGame;

public abstract class ObjectState {
    
    private final int duration;
    
    public ObjectState(int duration) {
        this.duration = duration;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public abstract ObjectState nextState();
    
    public void enteredActions(IroncladGame game, LevelState levelState) {}
    
    public void leftActions(IroncladGame game, LevelState levelState) {}
    
    public void timeUnitActions(IroncladGame game, LevelState levelState) {}
    
    public void stepActions(IroncladGame game, LevelState levelState) {}
    
    public void reactToLevel(IroncladGame game, LevelState levelState) {}
    
    public void reactToInput(IroncladGame game, LevelState levelState) {}
    
}
