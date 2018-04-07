package cell2d.space;

import cell2d.CellGame;

/**
 * <p>A ThinkerObject is a MobileObject that acts like a SpaceThinker,
 * possessing timers, various actions in response to events, and the capacity to
 * act like a ThinkerGroup, even though it is not technically one.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the SpaceStates that this
 * ThinkerObject can be assigned to
 */
public abstract class BasicThinkerObject<T extends CellGame>
        extends ThinkerObject<T,BasicSpaceState<T>,BasicSpaceThinker<T>> {
    
    /**
     * Creates a new ThinkerObject with the specified locator Hitbox.
     */
    public BasicThinkerObject() {
        setThinker(new BasicSpaceThinker<T>() {
            
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
     * Actions for this ThinkerObject to take immediately after being added to a
     * new SpaceState.
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void addedActions(T game, BasicSpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take immediately before being removed
     * from its current SpaceState.
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void removedActions(T game, BasicSpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every time unit, after
     * AnimationInstances update their indices but before SpaceThinkers take
     * their beforeMovementActions().
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void timeUnitActions(T game, BasicSpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every frame, after
     * SpaceThinkers take their timeUnitActions() but before its SpaceState
     * moves its assigned ThinkerObjects.
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void beforeMovementActions(T game, BasicSpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every frame after its
     * SpaceState moves its assigned ThinkerObjects.
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void frameActions(T game, BasicSpaceState<T> state) {}
    
}
