package cell2D.level;

import cell2D.CellGame;
import cell2D.Thinker;

/**
 * <p>A LevelThinker is the type of Thinker that is used by LevelStates and uses
 * LevelThinkerStates. A LevelThinker can take beforeMovementActions() and
 * afterMovementActions() every frame before and after its LevelState moves its
 * assigned ThinkerObjects, respectively.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this LevelThinker's LevelState is
 * used by
 */
public abstract class LevelThinker<T extends CellGame> extends Thinker<T,LevelState<T>,LevelThinker<T>,LevelThinkerState<T>> {
    
    @Override
    public final LevelThinker<T> getThis() {
        return this;
    }
    
    final void beforeMovement(T game, LevelState<T> state) {
        LevelThinkerState<T> thinkerState = getThinkerState();
        if (thinkerState != null) {
            thinkerState.beforeMovementActions(game, state);
        }
        beforeMovementActions(game, state);
    }
    
    /**
     * Actions for this LevelThinker to take once every frame, after
     * LevelThinkers take their frameActions() but before its LevelState moves
     * its assigned ThinkerObjects.
     * @param game This LevelThinker's CellGame
     * @param state This LevelThinker's LevelState
     */
    public void beforeMovementActions(T game, LevelState<T> state) {}
    
    final void afterMovement(T game, LevelState<T> state) {
        LevelThinkerState<T> thinkerState = getThinkerState();
        if (thinkerState != null) {
            thinkerState.afterMovementActions(game, state);
        }
        afterMovementActions(game, state);
    }
    
    /**
     * Actions for this LevelThinker to take once every frame, after its
     * LevelState moves its assigned ThinkerObjects.
     * @param game This LevelThinker's CellGame
     * @param state This LevelThinker's LevelState
     */
    public void afterMovementActions(T game, LevelState<T> state) {}
    
}
