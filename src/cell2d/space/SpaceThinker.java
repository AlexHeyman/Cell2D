package cell2d.space;

import cell2d.CellGame;
import cell2d.Thinker;
import java.util.Iterator;

/**
 * <p>A SpaceThinker is the type of Thinker that is used by SpaceStates. A
 * SpaceThinker can take beforeMovementActions() every frame before its
 * SpaceState moves its assigned MobileObjects.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this SpaceThinker's SpaceStates
 * @param <U> The type of SpaceState that uses this SpaceThinker
 * @param <V> The type of SpaceThinker that this SpaceThinker is for SpaceState
 * interaction purposes
 */
public abstract class SpaceThinker<T extends CellGame, U extends SpaceState<T,U,V>,
        V extends SpaceThinker<T,U,V>> extends Thinker<T,U,V> {
    
    /**
     * Creates a new SpaceThinker.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this SpaceThinker's SpaceStates
     * @param stateClass The Class object representing the subclass of
     * SpaceState that uses this SpaceThinker
     * @param thinkerClass The Class object representing the subclass of
     * SpaceThinker that this SpaceThinker is for SpaceState interaction
     * purposes
     */
    public SpaceThinker(Class<? extends CellGame> gameClass,
            Class<? extends SpaceState> stateClass, Class<? extends SpaceThinker> thinkerClass) {
        super(gameClass, stateClass, thinkerClass);
    }
    
    final void beforeMovement() {
        beforeMovementActions(getGame(), getGameState());
        if (getNumThinkers() > 0) {
            Iterator<V> iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().beforeMovement();
            }
        }
    }
    
    /**
     * Actions for this SpaceThinker to take once every frame, after
     * SpaceThinkers take their timeUnitActions() but before its SpaceState
     * moves its assigned MobileObjects.
     * @param game This SpaceThinker's SpaceState's CellGame
     * @param state This SpaceThinker's SpaceState
     */
    public void beforeMovementActions(T game, U state) {}
    
}
