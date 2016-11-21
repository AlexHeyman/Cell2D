package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.ThinkerState;

public abstract class LevelThinkerState extends ThinkerState<LevelState,LevelThinker,LevelThinkerState> {
    
    public void beforeMovementActions(IroncladGame game, LevelState levelState) {}
    
    public void afterMovementActions(IroncladGame game, LevelState levelState) {}
    
}
