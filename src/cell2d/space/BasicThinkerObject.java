package cell2d.space;

import cell2d.CellGame;

/**
 * <p>A BasicThinkerObject is a type of ThinkerObject that mimics a
 * BasicSpaceThinker, which has no special capabilities. It does not
 * automatically share any custom fields or methods between itself and its
 * SpaceStates.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this BasicThinkerObject's
 * BasicSpaceStates
 */
public abstract class BasicThinkerObject<T extends CellGame>
        extends ThinkerObject<T,BasicSpaceState<T>,BasicSpaceThinker<T>> {
    
    /**
     * Creates a new BasicThinkerObject with an assigned BasicSpaceThinker whose
     * Actions() methods call the BasicThinkerObject's own.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this BasicThinkerObject's BasicSpaceStates
     */
    public BasicThinkerObject(Class<? extends CellGame> gameClass) {
        setThinker(new BasicSpaceThinker<T>(gameClass) {
            
            @Override
            public final void addedActions(T game, BasicSpaceState<T> state) {
                BasicThinkerObject.this.addedActions(game, state);
            }
            
            @Override
            public final void removedActions(T game, BasicSpaceState<T> state) {
                BasicThinkerObject.this.removedActions(game, state);
            }
            
            @Override
            public final void timeUnitActions(T game, BasicSpaceState<T> state) {
                BasicThinkerObject.this.timeUnitActions(game, state);
            }
            
            @Override
            public final void beforeMovementActions(T game, BasicSpaceState<T> state) {
                BasicThinkerObject.this.beforeMovementActions(game, state);
            }
            
            @Override
            public final void frameActions(T game, BasicSpaceState<T> state) {
                BasicThinkerObject.this.frameActions(game, state);
            }
            
        });
    }
    
    /**
     * Actions for this BasicThinkerObject to take immediately after being added
     * to a new BasicSpaceState.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void addedActions(T game, BasicSpaceState<T> state) {}
    
    /**
     * Actions for this BasicThinkerObject to take immediately before being
     * removed from its current BasicSpaceState.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void removedActions(T game, BasicSpaceState<T> state) {}
    
    /**
     * Actions for this BasicThinkerObject to take once every time unit, after
     * AnimationInstances update their indices but before BasicSpaceThinkers
     * take their beforeMovementActions().
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void timeUnitActions(T game, BasicSpaceState<T> state) {}
    
    /**
     * Actions for this BasicThinkerObject to take once every frame, after
     * BasicSpaceThinkers take their timeUnitActions() but before its
     * BasicSpaceState moves its assigned MobileObjects.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void beforeMovementActions(T game, BasicSpaceState<T> state) {}
    
    /**
     * Actions for this BasicThinkerObject to take once every frame after its
     * BasicSpaceState moves its assigned MobileObjects.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void frameActions(T game, BasicSpaceState<T> state) {}
    
}
