package cell2d;

/**
 * <p>A BasicGameState is a type of CellGameState that uses BasicThinkers, which
 * have no special capabilities. It is designed to be easily extended by types
 * of CellGameStates that do not require custom fields or methods to be
 * automatically shared between themselves and their Thinkers.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this BasicGameState
 */
public abstract class BasicGameState<T extends CellGame>
        extends CellGameState<T,BasicGameState<T>,BasicThinker<T>> {
    
    /**
     * Creates a new BasicGameState of the specified CellGame with the specified
     * ID.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this BasicGameState
     * @param game The CellGame to which this BasicGameState belongs
     * @param id This BasicGameState's ID
     */
    public BasicGameState(Class<? extends CellGame> gameClass, T game, int id) {
        super(gameClass, BasicGameState.class, BasicThinker.class, game, id);
    }
    
}
