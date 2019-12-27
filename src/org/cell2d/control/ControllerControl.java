package org.cell2d.control;

/**
 * <p>A ControllerControl is a Control that represents an input on a game
 * controller. The controller is specified by a number that ranges from 0 to
 * MAX_CONTROLLERS - 1 inclusive.</p>
 * @author Alex Heyman
 */
public abstract class ControllerControl extends Control {
    
    /**
     * The maximum number of controllers that Cell2D recognizes.
     */
    public static final int MAX_CONTROLLERS = 100;
    
    private final int controllerNum;
    
    /**
     * Constructs a ControllerControl that represents an input on the controller
     * with the specified number.
     * @param controllerNum The number of this ControllerControl's controller
     * @throws InvalidControlException if the specified controller number is
     * invalid
     */
    public ControllerControl(int controllerNum) throws InvalidControlException {
        if (controllerNum < 0 || controllerNum >= MAX_CONTROLLERS) {
            throw new InvalidControlException("Attempted to construct a ControllerControl with invalid"
                    + " controller number " + controllerNum);
        }
        this.controllerNum = controllerNum;
    }
    
    /**
     * Returns the number of this ControllerControl's controller.
     * @return The number of this ControllerControl's controller
     */
    public final int getControllerNum() {
        return controllerNum;
    }
    
}
