package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.Thinker;

public abstract class LevelThinker extends Thinker<LevelState,LevelThinker,LevelThinkerState> {
    
    @Override
    public LevelThinker getThis() {
        return this;
    }
    
    final void beforeMovement(IroncladGame game, LevelState levelState) {
        LevelThinkerState thinkerState = getThinkerState();
        if (thinkerState != null) {
            thinkerState.beforeMovementActions(game, levelState);
        }
        beforeMovementActions(game, levelState);
    }
    
    public void beforeMovementActions(IroncladGame game, LevelState levelState) {}
    
    final void afterMovement(IroncladGame game, LevelState levelState) {
        LevelThinkerState thinkerState = getThinkerState();
        if (thinkerState != null) {
            thinkerState.afterMovementActions(game, levelState);
        }
        afterMovementActions(game, levelState);
    }
    
    public void afterMovementActions(IroncladGame game, LevelState levelState) {}
    
}
