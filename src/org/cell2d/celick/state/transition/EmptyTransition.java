package org.cell2d.celick.state.transition;

import org.cell2d.celick.GameContainer;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.SlickException;
import org.cell2d.celick.state.GameState;
import org.cell2d.celick.state.StateBasedGame;

/**
 * A transition that has no effect and instantly finishes. Used as a utility for the people
 * not using transitions
 *
 * @author kevin
 */
public class EmptyTransition implements Transition {

        @Override
	public boolean isComplete() {
		return true;
	}


        @Override
	public void postRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
		// no op
	}

        @Override
	public void preRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
		// no op
	}

        @Override
	public void update(StateBasedGame game, GameContainer container, int delta) throws SlickException {
		// no op
	}

        @Override
	public void init(GameState firstState, GameState secondState) {}
        
}
