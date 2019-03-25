package org.cell2d.control;

import java.util.Objects;
import org.cell2d.Direction;

/**
 * <p>A ControllerDirectionControl is a ControllerControl that represents a
 * Direction - up, down, left, or right - in which the primary axes of a game
 * controller can be moved. A controller's primary axes are typically those of a
 * directional pad or left control stick. A ControllerDirectionControl is
 * considered pressed when the axes are moved into its Direction, relative to
 * their neutral position, and released when they are moved out of its
 * Direction. The axes can be in a pair of adjacent Directions, such as up and
 * right, at the same time.</p>
 * @see org.cell2d.Direction
 * @author Andrew Heyman
 */
public class ControllerDirectionControl extends ControllerControl {
    
    private final Direction direction;
    
    /**
     * Constructs a ControllerDirectionControl that represents the specified
     * Direction on the primary axes of the controller with the specified
     * number.
     * @param controllerNum The number of this ControllerDirectionControl's
     * controller
     * @param direction This ControllerDirectionControl's Direction
     * @throws InvalidControlException If the specified controller number is
     * invalid or the specified Direction is null
     */
    public ControllerDirectionControl(int controllerNum, Direction direction)
            throws InvalidControlException {
        super(controllerNum);
        if (direction == null) {
            throw new InvalidControlException("Attempted to construct a ControllerDirectionControl with a"
                    + " null Direction");
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
    
    /**
     * Returns this ControllerButtonControl's Direction.
     * @return This ControllerButtonControl's Direction
     */
    public final Direction getDirection() {
        return direction;
    }
    
}
