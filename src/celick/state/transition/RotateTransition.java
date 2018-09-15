package celick.state.transition;

import celick.Color;
import celick.GameContainer;
import celick.Graphics;
import celick.SlickException;
import celick.state.GameState;
import celick.state.StateBasedGame;

/**
 * A transition that causes the previous state to rotate and scale down into
 * the new state.
 * 
 * This is an enter transition
 * 
 * @author kevin
 */
public class RotateTransition implements Transition {
	/** The previous state */
	private GameState prev;
	/** The current angle of rotation */
	private float ang;
	/** True if the state has finished */
	private boolean finish;
	/** The current scale */
	private float scale = 1;
	/** The background applied under the previous state if any */
	private Color background;

	/**
	 * Create a new transition
	 */
	public RotateTransition() {
		
	}

	/**
	 * Create a new transition
	 * 
	 * @param background The background colour to draw under the previous state
	 */
	public RotateTransition(Color background) {
		this.background = background;
	}
	
	/**
	 * @see celick.state.transition.Transition#init(celick.state.GameState, celick.state.GameState)
	 */
	public void init(GameState firstState, GameState secondState) {
		prev = secondState;
	}

	/**
	 * @see celick.state.transition.Transition#isComplete()
	 */
	public boolean isComplete() {
		return finish;
	}

	/**
	 * @see celick.state.transition.Transition#postRender(celick.state.StateBasedGame, celick.GameContainer, celick.Graphics)
	 */
	public void postRender(StateBasedGame game, GameContainer container, Graphics g) throws SlickException {
		g.translate(container.getWidth()/2, container.getHeight()/2);
		g.scale(scale,scale);
		g.rotate(0, 0, ang);
		g.translate(-container.getWidth()/2, -container.getHeight()/2);
		if (background != null) {
			Color c = g.getColor();
			g.setColor(background);
			g.fillRect(0,0,container.getWidth(),container.getHeight());
			g.setColor(c);
		}
		prev.render(container, game, g);
		g.translate(container.getWidth()/2, container.getHeight()/2);
		g.rotate(0, 0, -ang);
		g.scale(1/scale,1/scale);
		g.translate(-container.getWidth()/2, -container.getHeight()/2);
	}

	/**
	 * @see celick.state.transition.Transition#preRender(celick.state.StateBasedGame, celick.GameContainer, celick.Graphics)
	 */
	public void preRender(StateBasedGame game, GameContainer container,
			Graphics g) throws SlickException {
	}

	/**
	 * @see celick.state.transition.Transition#update(celick.state.StateBasedGame, celick.GameContainer, int)
	 */
	public void update(StateBasedGame game, GameContainer container, int delta)
			throws SlickException {
		ang += delta * 0.5f;
		if (ang > 500) {
			finish = true;
		}
		scale -= delta * 0.001f;
		if (scale < 0) {
			scale = 0;
		}
	}

}
