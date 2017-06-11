package cell2d.space;

import cell2d.CellGame;
import cell2d.Thinker;
import java.util.Iterator;

/**
 * <p>A SpaceThinker is the type of Thinker that is used by SpaceStates. A
 * SpaceThinker can take beforeMovementActions() every frame before its
 * SpaceState moves its assigned ThinkerObjects.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this SpaceThinker's SpaceState is
 used by
 */
public abstract class SpaceThinker<T extends CellGame> extends Thinker<T,SpaceState<T>,SpaceThinker<T>> {
    
    @Override
    public final SpaceThinker<T> getThis() {
        return this;
    }
    
    final void beforeMovement() {
        beforeMovementActions(getGame(), getGameState());
        if (getNumThinkers() > 0) {
            Iterator<SpaceThinker<T>> iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().beforeMovement();
            }
        }
    }
    
    /**
     * Actions for this SpaceThinker to take once every frame, after
     * SpaceThinkers take their timeUnitActions() but before its SpaceState
     * moves its assigned ThinkerObjects.
     * @param game This SpaceThinker's SpaceState's CellGame
     * @param state This SpaceThinker's SpaceState
     */
    public void beforeMovementActions(T game, SpaceState<T> state) {}
    
}
