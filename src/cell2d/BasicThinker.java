package cell2d;

/**
 * <p>A BasicThinker is a type of Thinker that is used by BasicStates, which
 * have no special capabilities. It does not automatically share any custom
 * fields or methods between itself and its GameStates.</p>
 * @see BasicState
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this BasicThinker's BasicStates
 */
public abstract class BasicThinker<T extends CellGame> extends Thinker<T,BasicState<T>,BasicThinker<T>> {
    
    /**
     * Creates a new BasicThinker.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this BasicThinker's BasicStates
     */
    public BasicThinker(Class<? extends CellGame> gameClass) {
        super(gameClass, BasicState.class, BasicThinker.class);
    }
    
}
