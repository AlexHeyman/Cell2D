package org.cell2d.control;

import java.util.Objects;

/**
 * <p>A MouseButtonControl is a Control that represents a button on the mouse.
 * The button is specified by an integer <i>button code</i> that is equal to one
 * of the MOUSE_ constants of the MouseButtonControl class. All other integers
 * are invalid as button codes.</p>
 * @author Alex Heyman
 */
public class MouseButtonControl extends Control {
    
    public static final int MOUSE_LEFT_BUTTON = 0;
    public static final int MOUSE_RIGHT_BUTTON = 1;
    public static final int MOUSE_MIDDLE_BUTTON = 2;
    
    private final int buttonCode;
    
    /**
     * Constructs a MouseButtonControl that represents the mouse button with the
     * specified button code.
     * @param buttonCode This MouseButtonControl's button code
     * @throws InvalidControlException If the button code is invalid
     */
    public MouseButtonControl(int buttonCode) throws InvalidControlException {
        if (buttonCode < 0 || buttonCode > 2) {
            throw new InvalidControlException("Attempted to construct a MouseButtonControl with invalid"
                    + " button code " + buttonCode);
        }
        this.buttonCode = buttonCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MouseButtonControl) {
            return ((MouseButtonControl)obj).buttonCode == buttonCode;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash("MouseButton", buttonCode);
    }
    
    @Override
    public String getName() {
        switch (buttonCode) {
            case MouseButtonControl.MOUSE_LEFT_BUTTON:
                return "LMB";
            case MouseButtonControl.MOUSE_RIGHT_BUTTON:
                return "RMB";
            case MouseButtonControl.MOUSE_MIDDLE_BUTTON:
                return "MMB";
        }
        return null;
    }
    
    /**
     * Returns this MouseButtonControl's button code.
     * @return This MouseButtonControl's button code
     */
    public final int getButtonCode() {
        return buttonCode;
    }
    
}
