package org.celick.state;

import org.celick.GameContainer;
import org.celick.SlickException;

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
