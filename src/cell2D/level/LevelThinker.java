package cell2D.level;

import cell2D.CellGame;
import cell2D.Thinker;

public abstract class LevelThinker<T extends CellGame> extends Thinker<T,LevelState<T>,LevelThinker<T>,LevelThinkerState<T>> {
    
    @Override
    public final LevelThinker<T> getThis() {
        return this;
    }
    
    final void beforeMovement(T game, LevelState<T> levelState) {
        LevelThinkerState<T> thinkerState = getThinkerState();
        if (thinkerState != null) {
            thinkerState.beforeMovementActions(game, levelState);
        }
        beforeMovementActions(game, levelState);
    }
    
    public void beforeMovementActions(T game, LevelState<T> levelState) {}
    
    final void afterMovement(T game, LevelState<T> levelState) {
        LevelThinkerState<T> thinkerState = getThinkerState();
        if (thinkerState != null) {
            thinkerState.afterMovementActions(game, levelState);
        }
        afterMovementActions(game, levelState);
    }
    
    public void afterMovementActions(T game, LevelState<T> levelState) {}
    
}
