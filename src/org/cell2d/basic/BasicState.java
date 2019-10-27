package org.cell2d.basic;

import org.cell2d.CellGame;
import org.cell2d.GameState;

/**
 * <p>A BasicState is a type of GameState that uses BasicThinkers, which have no
 * special capabilities, and treats its CellGame as a basic CellGame. It is
 * designed to be easily extended by types of GameStates that do not require
 * custom fields or methods to be automatically shared between themselves and
 * their SubThinkers or CellGames.</p>
 * @author Alex Heyman
 */
public abstract class BasicState extends GameState<CellGame,BasicState,BasicThinker> {
    
    /**
     * Constructs a BasicState of the specified CellGame with the specified ID.
     * @param game The CellGame to which this BasicState belongs
     * @param id This BasicState's ID
     */
    public BasicState(CellGame game, int id) {
        super(CellGame.class, BasicState.class, BasicThinker.class, game, id);
    }
    
}
