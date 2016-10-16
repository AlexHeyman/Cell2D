package ironclad2D.level;

import ironclad2D.IroncladGame;

public abstract class ObjectState {
    
    public abstract int getDuration();
    
    public abstract ObjectState getNextState();
    
    public void enteredActions(IroncladGame game, LevelState levelState) {}
    
    public void leftActions(IroncladGame game, LevelState levelState) {}
    
    public void timeUnitActions(IroncladGame game, LevelState levelState) {}
    
    public void stepActions(IroncladGame game, LevelState levelState) {}
    
    public void beforeMovementActions(IroncladGame game, LevelState levelState) {}
    
    public void afterMovementActions(IroncladGame game, LevelState levelState) {}
    
}
