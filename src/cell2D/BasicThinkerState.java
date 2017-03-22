package cell2D;

/**
 * A BasicThinkerState is a type of ThinkerState that is used by BasicGameStates
 * and BasicThinkers, which have no special capabilities. It does not
 * automatically share any custom fields or methods among itself, its
 * CellGameStates, and its Thinkers.
 * 
 * As with BasicThinker, it is useful to implicitly extend BasicThinkerState to
 * override its methods for single instances without creating completely new
 * class files.
 * @author Andrew Heyman
 */
public abstract class BasicThinkerState extends ThinkerState<BasicGameState,BasicThinker,BasicThinkerState> {}
