package cell2D.level;

import cell2D.CellGame;
import cell2D.ThinkerState;

public abstract class LevelThinkerState<T extends CellGame> extends ThinkerState<T,LevelState<T>,LevelThinker<T>,LevelThinkerState<T>> {
    
    public void beforeMovementActions(T game, LevelState<T> levelState) {}
    
    public void afterMovementActions(T game, LevelState<T> levelState) {}
    
}
