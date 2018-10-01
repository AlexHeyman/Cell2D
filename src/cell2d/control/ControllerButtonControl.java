package cell2d.control;

import java.util.Objects;

/**
 * @author Andrew Heyman
 */
public class ControllerButtonControl extends ControllerControl {
    
    public static final int MAX_CTLR_BUTTONS = 100;
    
    private final int buttonNum;
    
    public ControllerButtonControl(int controllerNum, int buttonNum) throws InvalidControlException {
        super(controllerNum);
        if (buttonNum < 0 || buttonNum >= MAX_CTLR_BUTTONS) {
            throw new InvalidControlException("Attempted to construct a ControllerButtonControl with "
                    + "out-of-bounds button number " + buttonNum);
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
    
    public final int getButtonNum() {
        return buttonNum;
    }
    
}
