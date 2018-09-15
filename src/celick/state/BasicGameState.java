package celick.state;

import celick.GameContainer;
import celick.Input;
import celick.SlickException;

/**
 * A simple state used an adapter so we don't have to implement all the event methods
 * every time.
 *
 * @author kevin
 */
public abstract class BasicGameState implements GameState {
	/**
	 * @see celick.ControlledInputReciever#inputStarted()
	 */
	public void inputStarted() {
		
	}
	
	/**
	 * @see celick.InputListener#isAcceptingInput()
	 */
	public boolean isAcceptingInput() {
		return true;
	}
	
	/**
	 * @see celick.InputListener#setInput(celick.Input)
	 */
	public void setInput(Input input) {
	}
	
	/**
	 * @see celick.InputListener#inputEnded()
	 */
	public void inputEnded() {
	}
	
	/**
	 * @see celick.state.GameState#getID()
	 */
	public abstract int getID();

	/**
	 * @see celick.InputListener#controllerButtonPressed(int, int)
	 */
	public void controllerButtonPressed(int controller, int button) {
	}

	/**
	 * @see celick.InputListener#controllerButtonReleased(int, int)
	 */
	public void controllerButtonReleased(int controller, int button) {
	}

	/**
	 * @see celick.InputListener#controllerDownPressed(int)
	 */
	public void controllerDownPressed(int controller) {
	}

	/**
	 * @see celick.InputListener#controllerDownReleased(int)
	 */
	public void controllerDownReleased(int controller) {
	}

	/**
	 * @see celick.InputListener#controllerLeftPressed(int)
	 */
	public void controllerLeftPressed(int controller) {
		
	}

	/**
	 * @see celick.InputListener#controllerLeftReleased(int)
	 */
	public void controllerLeftReleased(int controller) {
	}

	/**
	 * @see celick.InputListener#controllerRightPressed(int)
	 */
	public void controllerRightPressed(int controller) {
	}

	/**
	 * @see celick.InputListener#controllerRightReleased(int)
	 */
	public void controllerRightReleased(int controller) {
	}

	/**
	 * @see celick.InputListener#controllerUpPressed(int)
	 */
	public void controllerUpPressed(int controller) {
	}

	/**
	 * @see celick.InputListener#controllerUpReleased(int)
	 */
	public void controllerUpReleased(int controller) {
	}

	/**
	 * @see celick.InputListener#keyPressed(int, char)
	 */
	public void keyPressed(int key, char c) {
	}

	/**
	 * @see celick.InputListener#keyReleased(int, char)
	 */
	public void keyReleased(int key, char c) {
	}

	/**
	 * @see celick.InputListener#mouseMoved(int, int, int, int)
	 */
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
	}

	/**
	 * @see celick.InputListener#mouseDragged(int, int, int, int)
	 */
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
	}

	/**
	 * @see celick.InputListener#mouseClicked(int, int, int, int)
	 */
	public void mouseClicked(int button, int x, int y, int clickCount) {
	}
	
	/**
	 * @see celick.InputListener#mousePressed(int, int, int)
	 */
	public void mousePressed(int button, int x, int y) {
	}

	/**
	 * @see celick.InputListener#mouseReleased(int, int, int)
	 */
	public void mouseReleased(int button, int x, int y) {
	}

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

	/**
	 * @see celick.InputListener#mouseWheelMoved(int)
	 */
	public void mouseWheelMoved(int newValue) {
	}

}
