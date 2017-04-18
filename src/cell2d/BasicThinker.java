package cell2d;

/**
 * <p>A BasicThinker is a type of Thinker that is used by BasicGameStates and
 * uses BasicThinkerStates, which have no special capabilities. It does not
 * automatically share any custom fields or methods among itself, its
 * CellGameStates, and its ThinkerStates.</p>
 * 
 * <p>As with BasicThinkerState, it is useful to implicitly extend BasicThinker
 * to override its methods for single instances without creating completely new
 * class files.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this BasicThinker is used by
 */
public abstract class BasicThinker<T extends CellGame> extends Thinker<T,BasicGameState<T>,BasicThinker<T>,BasicThinkerState<T>> {
    
    @Override
    public final BasicThinker<T> getThis() {
        return this;
    }
    
}
