package org.cell2d.space.basic;

import org.cell2d.CellGame;
import org.cell2d.space.SpaceThinker;

/**
 * <p>A BasicSpaceThinker is a type of SpaceThinker that is used by
 * BasicSpaceStates, which have no special capabilities, and treats their
 * CellGames as basic CellGames. It does not automatically share any custom
 * fields or methods between itself and its SpaceStates or their CellGames.</p>
 * @see BasicThinkerObject
 * @author Alex Heyman
 */
public abstract class BasicSpaceThinker extends SpaceThinker<CellGame,BasicSpaceState,BasicSpaceThinker> {
    
    /**
     * Constructs a BasicSpaceThinker.
     */
    public BasicSpaceThinker() {
        super(CellGame.class, BasicSpaceState.class, BasicSpaceThinker.class);
    }
    
}
