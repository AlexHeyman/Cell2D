package cell2d.control;

/**
 * @author Andrew Heyman
 */
public abstract class ControllerControl extends Control {
    
    public static final int MAX_CONTROLLERS = 100;
    
    private final int controllerNum;
    
    public ControllerControl(int controllerNum) throws InvalidControlException {
        if (controllerNum < 0 || controllerNum >= MAX_CONTROLLERS) {
            throw new InvalidControlException("Attempted to construct a ControllerControl with "
                    + "out-of-bounds controller number " + controllerNum);
        }
        this.controllerNum = controllerNum;
    }
    
    public final int getControllerNum() {
        return controllerNum;
    }
    
}
