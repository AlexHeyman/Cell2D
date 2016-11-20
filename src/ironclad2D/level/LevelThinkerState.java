package ironclad2D.level;

import ironclad2D.IroncladGame;

public abstract class LevelThinkerState extends ironclad2D.ThinkerState<LevelState,LevelThinker,LevelThinkerState> {
    
    public void beforeMovementActions(IroncladGame game, LevelState levelState) {}
    
    public void afterMovementActions(IroncladGame game, LevelState levelState) {}
    
}
