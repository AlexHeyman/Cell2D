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
	
	@Override
	public abstract int getID();

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
	}

}
