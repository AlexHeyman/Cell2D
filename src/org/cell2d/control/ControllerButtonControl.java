package org.cell2d.control;

import java.util.Objects;

/**
 * <p>A ControllerButtonControl is a ControllerControl that represents a button
 * on a game controller. The button is specified by a number that ranges from 0
 * to MAX_BUTTONS - 1 inclusive.</p>
 * @author Alex Heyman
 */
public class ControllerButtonControl extends ControllerControl {
    
    /**
     * The maximum number of buttons on each controller that Cell2D recognizes.
     */
    public static final int MAX_BUTTONS = 100;
    
    private final int buttonNum;
    
    /**
     * Constructs a ControllerButtonControl that represents the button with the
     * specified number on the controller with the specified number.
     * @param controllerNum The number of this ControllerButtonControl's
     * controller
     * @param buttonNum The number of this ControllerButtonControl's button
     * @throws InvalidControlException if the specified controller or button
     * number is invalid
     */
    public ControllerButtonControl(int controllerNum, int buttonNum) throws InvalidControlException {
        super(controllerNum);
        if (buttonNum < 0 || buttonNum >= MAX_BUTTONS) {
            throw new InvalidControlException("Attempted to construct a ControllerButtonControl with invalid"
                    + " button number " + buttonNum);
        }
        this.buttonNum = buttonNum;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ControllerButtonControl) {
            ControllerButtonControl control = (ControllerButtonControl)obj;
            return (control.getControllerNum() == getControllerNum()
                    && control.buttonNum == buttonNum);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash("ControllerButton", getControllerNum(), buttonNum);
    }
    
    @Override
    public String getName() {
        return "C" + getControllerNum() + "B" + buttonNum;
    }
    
    /**
     * Returns the number of this ControllerButtonControl's button.
     * @return The number of this ControllerButtonControl's button
     */
    public final int getButtonNum() {
        return buttonNum;
    }
    
}
