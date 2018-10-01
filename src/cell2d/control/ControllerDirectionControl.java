package cell2d.control;

import cell2d.Direction;
import java.util.Objects;

/**
 * @author Andrew Heyman
 */
public class ControllerDirectionControl extends ControllerControl {
    
    private final Direction direction;
    
    public ControllerDirectionControl(int controllerNum, Direction direction)
            throws InvalidControlException {
        super(controllerNum);
        if (direction == null) {
            throw new InvalidControlException("Attempted to construct a ControllerDirectionControl with a "
                    + "null Direction");
        }
        this.direction = direction;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ControllerDirectionControl) {
            ControllerDirectionControl control = (ControllerDirectionControl)obj;
            return (control.getControllerNum() == getControllerNum()
                    && control.direction == direction);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash("ControllerDirection", getControllerNum(), direction);
    }
    
    @Override
    public String getName() {
        switch (direction) {
            case UP:
                return "C" + getControllerNum() + "Up";
            case DOWN:
                return "C" + getControllerNum() + "Dwn";
            case LEFT:
                return "C" + getControllerNum() + "Lft";
            case RIGHT:
                return "C" + getControllerNum() + "Rgt";
        }
        return null;
    }
    
    public final Direction getDirection() {
        return direction;
    }
    
}
