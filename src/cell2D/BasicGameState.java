package cell2D;

/**
 * A BasicGameState is a type of CellGameState that uses BasicThinkers and
 * BasicThinkerStates, both of which have no special capabilities. It is
 * designed to be easily extended by types of CellGameStates that do not require
 * custom fields or methods to be automatically shared among themselves, their
 * Thinkers, and their ThinkerStates.
 * @author Andrew Heyman
 */
public abstract class BasicGameState extends CellGameState<BasicGameState,BasicThinker,BasicThinkerState> {
    
    /**
     * Creates a BasicGameState of the specified CellGame with the specified ID.
     * @param game The CellGame of which this BasicGameState is
     * @param id This BasicGameState's ID
     */
    public BasicGameState(CellGame game, int id) {
        super(game, id);
    }
    
    @Override
    public BasicGameState getThis() {
        return this;
    }
    
}
