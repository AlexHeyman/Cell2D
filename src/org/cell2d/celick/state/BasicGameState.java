package org.cell2d.celick.state;

import org.cell2d.celick.GameContainer;
import org.cell2d.celick.SlickException;

/**
 * A simple state used an adapter so we don't have to implement all the event methods
 * every time.
 *
 * @author kevin
 */
public abstract class BasicGameState implements GameState {
	
	/**
	 * @see celick.state.GameState#getID()
	 */
	public abstract int getID();

	/**
	 * @see celick.state.GameState#enter(celick.GameContainer, celick.state.StateBasedGame)
	 */
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
	}

	/**
	 * @see celick.state.GameState#leave(celick.GameContainer, celick.state.StateBasedGame)
	 */
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
	}

}
