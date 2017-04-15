package cell2D;

/**
 * <p>A BasicGameState is a type of CellGameState that uses BasicThinkers and
 * BasicThinkerStates, both of which have no special capabilities. It is
 * designed to be easily extended by types of CellGameStates that do not require
 * custom fields or methods to be automatically shared among themselves, their
 * Thinkers, and their ThinkerStates.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this BasicGameState is used by
 */
public abstract class BasicGameState<T extends CellGame> extends CellGameState<T,BasicGameState<T>,BasicThinker<T>,BasicThinkerState<T>> {
    
    /**
     * Creates a BasicGameState of the specified CellGame with the specified ID.
     * @param game The CellGame of which this BasicGameState is
     * @param id This BasicGameState's ID
     */
    public BasicGameState(T game, int id) {
        super(game, id);
    }
    
    @Override
    public BasicGameState<T> getThis() {
        return this;
    }
    
}
