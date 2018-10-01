package cell2d.control;

import java.util.Objects;

/**
 * @author Andrew Heyman
 * @author joverton
 * @author kevin
 */
public class MouseButtonControl extends Control {
    
    public static final int MOUSE_LEFT_BUTTON = 0;
    public static final int MOUSE_RIGHT_BUTTON = 1;
    public static final int MOUSE_MIDDLE_BUTTON = 2;
    
    private final int buttonCode;
    
    public MouseButtonControl(int buttonCode) throws InvalidControlException {
        if (buttonCode < 0 || buttonCode > 2) {
            throw new InvalidControlException("Attempted to construct a MouseButtonControl with invalid "
                    + "button code " + buttonCode);
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
    
    public final int getButtonCode() {
        return buttonCode;
    }
    
}
