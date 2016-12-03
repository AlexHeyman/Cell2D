package cell2D.level;

import cell2D.CellGame;
import cell2D.ThinkerState;

public abstract class LevelThinkerState extends ThinkerState<LevelState,LevelThinker,LevelThinkerState> {
    
    public void beforeMovementActions(CellGame game, LevelState levelState) {}
    
    public void afterMovementActions(CellGame game, LevelState levelState) {}
    
}
