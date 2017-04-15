package cell2D;

/**
 * <p>A BasicThinkerState is a type of ThinkerState that is used by
 * BasicGameStates and BasicThinkers, which have no special capabilities. It
 * does not automatically share any custom fields or methods among itself, its
 * CellGameStates, and its Thinkers.</p>
 * 
 * <p>As with BasicThinker, it is useful to implicitly extend BasicThinkerState
 * to override its methods for single instances without creating completely new
 * class files.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this BasicThinkerState is used by
 */
public abstract class BasicThinkerState<T extends CellGame> extends ThinkerState<T,BasicGameState<T>,BasicThinker<T>,BasicThinkerState<T>> {}
