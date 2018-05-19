package cell2d.basic;

import cell2d.CellGame;
import cell2d.Thinker;

/**
 * <p>A BasicThinker is a type of Thinker that is used by BasicStates, which
 * have no special capabilities, and treats their CellGames as basic CellGames.
 * It does not automatically share any custom fields or methods between itself
 * and its GameStates or their CellGames.</p>
 * @author Andrew Heyman
 */
public abstract class BasicThinker extends Thinker<CellGame,BasicState,BasicThinker> {
    
    /**
     * Creates a new BasicThinker.
     */
    public BasicThinker() {
        super(CellGame.class, BasicState.class, BasicThinker.class);
    }
    
}
