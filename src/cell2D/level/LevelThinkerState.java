package cell2D.level;

import cell2D.CellGame;
import cell2D.ThinkerState;

/**
 * <p>A LevelThinkerState is the type of ThinkerState that is used by
 * LevelStates and LevelThinkers. A LevelThinkerState can take
 * beforeMovementActions() and afterMovementActions() every frame before and
 * after its LevelThinker's LevelState moves its assigned ThinkerObjects,
 * respectively.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this LevelThinkerState's
 * LevelThinker's LevelState is used by
 */
public abstract class LevelThinkerState<T extends CellGame> extends ThinkerState<T,LevelState<T>,LevelThinker<T>,LevelThinkerState<T>> {
    
    /**
     * Actions for this LevelThinkerState to take once every frame, immediately
     * before its LevelThinker takes its own beforeMovementActions().
     * @param game This LevelThinkerState's LevelThinker's CellGame
     * @param state This LevelThinkerState's LevelThinker's LevelState
     */
    public void beforeMovementActions(T game, LevelState<T> state) {}
    
    /**
     * Actions for this LevelThinkerState to take once every frame, immediately
     * before its LevelThinker takes its own afterMovementActions().
     * @param game This LevelThinkerState's LevelThinker's CellGame
     * @param state This LevelThinkerState's LevelThinker's LevelState
     */
    public void afterMovementActions(T game, LevelState<T> state) {}
    
}
