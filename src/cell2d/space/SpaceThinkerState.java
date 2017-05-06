package cell2d.space;

import cell2d.CellGame;
import cell2d.ThinkerState;

/**
 * <p>A SpaceThinkerState is the type of ThinkerState that is used by
 * SpaceStates and SpaceThinkers. A SpaceThinkerState can take
 * afterMovementActions() every frame after its SpaceThinker's SpaceState moves
 * its assigned ThinkerObjects.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this SpaceThinkerState's
 SpaceThinker's SpaceState is used by
 */
public abstract class SpaceThinkerState<T extends CellGame> extends ThinkerState<T,SpaceState<T>,SpaceThinker<T>,SpaceThinkerState<T>> {
    
    /**
     * Actions for this SpaceThinkerState to take once every frame, immediately
     * before its SpaceThinker takes its own afterMovementActions().
     * @param game This SpaceThinkerState's SpaceThinker's CellGame
     * @param state This SpaceThinkerState's SpaceThinker's SpaceState
     */
    public void afterMovementActions(T game, SpaceState<T> state) {}
    
}
