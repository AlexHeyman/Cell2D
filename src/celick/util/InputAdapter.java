package celick.util;

import celick.Input;
import celick.InputListener;

/**
 * An implement implementation of the InputListener interface
 *
 * @author kevin
 */
public class InputAdapter implements InputListener {
	/** A flag to indicate if we're accepting input here */
	private boolean acceptingInput = true;
	
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
	 * @see celick.InputListener#inputEnded()
	 */
	public void inputEnded() {
	}

	/**
	 * @see celick.InputListener#isAcceptingInput()
	 */
	public boolean isAcceptingInput() {
		return acceptingInput;
	}

	/**
	 * Indicate if we should be accepting input of any sort
	 * 
	 * @param acceptingInput True if we should accept input
	 */
	public void setAcceptingInput(boolean acceptingInput) {
		this.acceptingInput = acceptingInput;
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
	 * @see celick.InputListener#mouseWheelMoved(int)
	 */
	public void mouseWheelMoved(int change) {
	}

	/**
	 * @see celick.InputListener#setInput(celick.Input)
	 */
	public void setInput(Input input) {
	}

	/**
	 * @see celick.InputListener#mouseClicked(int, int, int, int)
	 */
	public void mouseClicked(int button, int x, int y, int clickCount) {
	}

	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
	}

	/**
	 * @see celick.ControlledInputReciever#inputStarted()
	 */
	public void inputStarted() {
		
	}
}
