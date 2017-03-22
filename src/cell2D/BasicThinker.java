package cell2D;

/**
 * A BasicThinker is a type of Thinker that is used by BasicGameStates and uses
 * BasicThinkerStates, which have no special capabilities. It does not
 * automatically share any custom fields or methods among itself, its
 * CellGameStates, and its ThinkerStates.
 * 
 * As with BasicThinkerState, it is useful to implicitly extend BasicThinker to
 * override its methods for single instances without creating completely new
 * class files.
 * @author Andrew Heyman
 */
public abstract class BasicThinker extends Thinker<BasicGameState,BasicThinker,BasicThinkerState> {
    
    @Override
    public final BasicThinker getThis() {
        return this;
    }
    
}
