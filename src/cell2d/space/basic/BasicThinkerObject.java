package cell2d.space.basic;

import cell2d.CellGame;
import cell2d.space.ThinkerObject;

/**
 * <p>A BasicThinkerObject is a type of ThinkerObject that mimics a
 * BasicSpaceThinker, which has no special capabilities. It does not
 * automatically share any custom fields or methods between itself and its
 * SpaceStates or their CellGames.</p>
 * @author Andrew Heyman
 */
public abstract class BasicThinkerObject extends ThinkerObject<CellGame,BasicSpaceState,BasicSpaceThinker> {
    
    /**
     * Creates a new BasicThinkerObject with an assigned BasicSpaceThinker whose
     * Actions() methods call the BasicThinkerObject's own.
     */
    public BasicThinkerObject() {
        setThinker(new BasicSpaceThinker() {
            
            @Override
            public final void addedActions(CellGame game, BasicSpaceState state) {
                BasicThinkerObject.this.addedActions(game, state);
            }
            
            @Override
            public final void removedActions(CellGame game, BasicSpaceState state) {
                BasicThinkerObject.this.removedActions(game, state);
            }
            
            @Override
            public final void timeUnitActions(CellGame game, BasicSpaceState state) {
                BasicThinkerObject.this.timeUnitActions(game, state);
            }
            
            @Override
            public final void beforeMovementActions(CellGame game, BasicSpaceState state) {
                BasicThinkerObject.this.beforeMovementActions(game, state);
            }
            
            @Override
            public final void frameActions(CellGame game, BasicSpaceState state) {
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
    public void addedActions(CellGame game, BasicSpaceState state) {}
    
    /**
     * Actions for this BasicThinkerObject to take immediately before being
     * removed from its current BasicSpaceState.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void removedActions(CellGame game, BasicSpaceState state) {}
    
    /**
     * Actions for this BasicThinkerObject to take once every time unit, after
     * AnimationInstances update their indices but before BasicSpaceThinkers
     * take their beforeMovementActions().
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void timeUnitActions(CellGame game, BasicSpaceState state) {}
    
    /**
     * Actions for this BasicThinkerObject to take once every frame, after
     * BasicSpaceThinkers take their timeUnitActions() but before its
     * BasicSpaceState moves its assigned MobileObjects.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void beforeMovementActions(CellGame game, BasicSpaceState state) {}
    
    /**
     * Actions for this BasicThinkerObject to take once every frame after its
     * BasicSpaceState moves its assigned MobileObjects.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void frameActions(CellGame game, BasicSpaceState state) {}
    
}
