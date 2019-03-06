package org.celick.state.transition;

import org.celick.GameContainer;
import org.celick.Graphics;
import org.celick.SlickException;
import org.celick.state.GameState;
import org.celick.state.StateBasedGame;

/**
 * A transition that has no effect and instantly finishes. Used as a utility for the people
 * not using transitions
 *
 * @author kevin
 */
public class EmptyTransition implements Transition {

	/**
	 * @see celick.state.transition.Transition#isComplete()
	 */
	public boolean isComplete() {
		return true;
	}


	/**
	 * @see celick.state.transition.Transition#postRender(celick.state.StateBasedGame, celick.GameContainer, celick.Graphics)
	 */
	public void postRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
		// no op
	}

	/**
	 * @see celick.state.transition.Transition#preRender(celick.state.StateBasedGame, celick.GameContainer, celick.Graphics)
	 */
	public void preRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
		// no op
	}

	/**
	 * @see celick.state.transition.Transition#update(celick.state.StateBasedGame, celick.GameContainer, int)
	 */
	public void update(StateBasedGame game, GameContainer container, int delta) throws SlickException {
		// no op
	}


	public void init(GameState firstState, GameState secondState) {
		// TODO Auto-generated method stub
		
	}
}
