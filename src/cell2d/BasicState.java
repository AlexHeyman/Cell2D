package cell2d;

/**
 * <p>A BasicState is a type of GameState that uses BasicThinkers, which have no
 * special capabilities. It is designed to be easily extended by types of
 * GameStates that do not require custom fields or methods to be automatically
 * shared between themselves and their Thinkers.</p>
 * @see BasicThinker
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this BasicState
 */
public abstract class BasicState<T extends CellGame> extends GameState<T,BasicState<T>,BasicThinker<T>> {
    
    /**
     * Creates a new BasicState of the specified CellGame with the specified ID.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this BasicState
     * @param game The CellGame to which this BasicState belongs
     * @param id This BasicState's ID
     */
    public BasicState(Class<? extends CellGame> gameClass, T game, int id) {
        super(gameClass, BasicState.class, BasicThinker.class, game, id);
    }
    
}
