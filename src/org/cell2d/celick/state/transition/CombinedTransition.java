package org.cell2d.celick.state.transition;

import org.cell2d.celick.GameContainer;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.SlickException;
import org.cell2d.celick.state.GameState;
import org.cell2d.celick.state.StateBasedGame;
import java.util.ArrayList;

/**
 * A transition thats built of a set of other transitions which are chained
 * together to build the overall effect.
 *
 * @author kevin
 */
public class CombinedTransition implements Transition {
	/** The list of transitions to be combined */
    private ArrayList transitions = new ArrayList();

    /**
     * Create an empty transition
     */
    public CombinedTransition() {
    }

    /**
     * Add a transition to the list that will be combined to form
     * the final transition
     * 
     * @param t The transition to add
     */
    public void addTransition(Transition t) {
          transitions.add(t);
    }

	/**
	 * @see celick.state.transition.Transition#isComplete()
	 */
	public boolean isComplete() {
        for (int i=0;i<transitions.size();i++) {
            if (!((Transition) transitions.get(i)).isComplete()) {
            	return false;
            }
        }
        
        return true;
	}

	/**
	 * @see celick.state.transition.Transition#postRender(celick.state.StateBasedGame, celick.GameContainer, celick.Graphics)
	 */
	public void postRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
        for (int i=transitions.size()-1;i>=0;i--) {
            ((Transition) transitions.get(i)).postRender(game, container, g);
        }
	}

	/**
	 * @see celick.state.transition.Transition#preRender(celick.state.StateBasedGame, celick.GameContainer, celick.Graphics)
	 */
	public void preRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
        for (int i=0;i<transitions.size();i++) {
            ((Transition) transitions.get(i)).postRender(game, container, g);
        }
	}

	/**
	 * @see celick.state.transition.Transition#update(celick.state.StateBasedGame, celick.GameContainer, int)
	 */
	public void update(StateBasedGame game, GameContainer container, int delta) throws SlickException {
        for (int i=0;i<transitions.size();i++) {
        	Transition t = (Transition) transitions.get(i);
        	
        	if (!t.isComplete()) {
        		t.update(game, container, delta);
        	}
        }
	}

	public void init(GameState firstState, GameState secondState) {
	   for (int i = transitions.size() - 1; i >= 0; i--) {
	      ((Transition)transitions.get(i)).init(firstState, secondState);
	   }
	}
}
