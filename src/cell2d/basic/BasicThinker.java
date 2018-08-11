package cell2d.basic;

import cell2d.CellGame;
import cell2d.SubThinker;

/**
 * <p>A BasicThinker is a type of SubThinker that is used by BasicStates, which
 * have no special capabilities, and treats their CellGames as basic CellGames.
 * It does not automatically share any custom fields or methods between itself
 * and its GameStates or their CellGames.</p>
 * @author Andrew Heyman
 */
public abstract class BasicThinker extends SubThinker<CellGame,BasicState,BasicThinker> {
    
    /**
     * Constructs a BasicThinker.
     */
    public BasicThinker() {
        super(CellGame.class, BasicState.class, BasicThinker.class);
    }
    
}
