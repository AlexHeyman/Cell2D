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
     * Constructs a BasicThinkerObject with an assigned BasicSpaceThinker whose
     * Actions() methods call the BasicThinkerObject's own.
     */
    public BasicThinkerObject() {
        setThinker(new BasicSpaceThinker() {
            
            @Override
            public final void frameActions(CellGame game, BasicSpaceState state) {
                BasicThinkerObject.this.frameActions(game, state);
            }
            
            @Override
            public final void addSubThinkerActions(CellGame game,
                    BasicSpaceState state, BasicSpaceThinker subThinker) {
                super.addSubThinkerActions(game, state, subThinker);
                BasicThinkerObject.this.addSubThinkerActions(game, state, subThinker);
            }
            
            @Override
            public final void removeSubThinkerActions(CellGame game,
                    BasicSpaceState state, BasicSpaceThinker subThinker) {
                BasicThinkerObject.this.removeSubThinkerActions(game, state, subThinker);
                super.removeSubThinkerActions(game, state, subThinker);
            }
            
            @Override
            public final void addedActions(CellGame game, BasicSpaceState state) {
                BasicThinkerObject.this.addedActions(game, state);
            }
            
            @Override
            public final void removedActions(CellGame game, BasicSpaceState state) {
                BasicThinkerObject.this.removedActions(game, state);
            }
            
            @Override
            public final void beforeMovementActions(CellGame game, BasicSpaceState state) {
                BasicThinkerObject.this.beforeMovementActions(game, state);
            }
            
        });
    }
    
    /**
     * Actions for this BasicThinkerObject to take once each frame after its
     * BasicSpaceState moves its assigned MobileObjects.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void frameActions(CellGame game, BasicSpaceState state) {}
    
    /**
     * Actions for this BasicThinkerObject to take immediately after adding a
     * BasicSpaceThinker to itself, before the added BasicSpaceThinker takes its
     * addedActions().
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame, or null
     * if it has no BasicSpaceState
     * @param state This BasicThinkerObject's BasicSpaceState, or null if it has
     * none
     * @param subThinker The BasicSpaceThinker that was added
     */
    public void addSubThinkerActions(CellGame game, BasicSpaceState state, BasicSpaceThinker subThinker) {}
    
    /**
     * Actions for this BasicThinkerObject to take immediately before removing a
     * BasicSpaceThinker from itself, after the soon-to-be-removed
     * BasicSpaceThinker takes its removedActions().
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame, or null
     * if it has no BasicSpaceState
     * @param state This BasicThinkerObject's BasicSpaceState, or null if it has
     * none
     * @param subThinker The BasicSpaceThinker that is about to be removed
     */
    public void removeSubThinkerActions(CellGame game,
            BasicSpaceState state, BasicSpaceThinker subThinker) {}
    
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
     * Actions for this BasicThinkerObject to take once each frame, after all
     * Thinkers have experienced all of their time units for that frame, but
     * before its BasicSpaceState moves its assigned MobileObjects.
     * @param game This BasicThinkerObject's BasicSpaceState's CellGame
     * @param state This BasicThinkerObject's BasicSpaceState
     */
    public void beforeMovementActions(CellGame game, BasicSpaceState state) {}
    
}
