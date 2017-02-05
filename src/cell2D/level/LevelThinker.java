package cell2D.level;

import cell2D.CellGame;
import cell2D.Thinker;

public abstract class LevelThinker extends Thinker<LevelState,LevelThinker,LevelThinkerState> {
    
    @Override
    public final LevelThinker getThis() {
        return this;
    }
    
    final void beforeMovement(CellGame game, LevelState levelState) {
        LevelThinkerState thinkerState = getThinkerState();
        if (thinkerState != null) {
            thinkerState.beforeMovementActions(game, levelState);
        }
        beforeMovementActions(game, levelState);
    }
    
    public void beforeMovementActions(CellGame game, LevelState levelState) {}
    
    final void afterMovement(CellGame game, LevelState levelState) {
        LevelThinkerState thinkerState = getThinkerState();
        if (thinkerState != null) {
            thinkerState.afterMovementActions(game, levelState);
        }
        afterMovementActions(game, levelState);
    }
    
    public void afterMovementActions(CellGame game, LevelState levelState) {}
    
}
