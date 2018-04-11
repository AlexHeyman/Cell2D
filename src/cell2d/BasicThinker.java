package cell2d;

/**
 * <p>A BasicThinker is a type of Thinker that is used by BasicGameStates, which
 * have no special capabilities. It does not automatically share any custom
 * fields or methods between itself and its CellGameStates.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this BasicThinker's BasicGameStates
 */
public abstract class BasicThinker<T extends CellGame> extends Thinker<T,BasicGameState<T>,BasicThinker<T>> {
    
    /**
     * Creates a new BasicThinker.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this BasicThinker's BasicGameStates
     */
    public BasicThinker(Class<? extends CellGame> gameClass) {
        super(gameClass, BasicGameState.class, BasicThinker.class);
    }
    
}
