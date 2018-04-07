package cell2d;

/**
 * <p>A BasicThinker is a type of Thinker that is used by BasicGameStates, which
 * have no special capabilities. It does not automatically share any custom
 * fields or methods between itself and its CellGameStates.</p>
 * 
 * <p>It is useful to implicitly extend BasicThinker to override its methods for
 * single instances without creating completely new class files.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses this BasicThinker's
 * BasicGameStates
 */
public abstract class BasicThinker<T extends CellGame> extends Thinker<T,BasicGameState<T>,BasicThinker<T>> {
    
    /**
     * Creates a new BasicThinker.
     */
    public BasicThinker() {}
    
    @Override
    public final BasicThinker<T> getThis() {
        return this;
    }
    
}
