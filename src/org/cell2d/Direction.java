package org.cell2d;

/**
 * <p>A Direction is one of the four orthogonal directions.</p>
 * @author Alex Heyman
 */
public enum Direction {
    /**
     * Left.
     */
    LEFT,
    /**
     * Right.
     */
    RIGHT,
    /**
     * Up.
     */
    UP,
    /**
     * Down.
     */
    DOWN;
    
    /**
     * Returns the Direction opposite this one. For instance, DOWN.opposite()
     * returns UP.
     * @return The Direction opposite this one
     */
    public final Direction opposite() {
        switch (this) {
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            case UP:
                return DOWN;
            default:
                return UP;
        }
    }
    
    /**
     * Returns the Direction 90 degrees clockwise from this one. For instance,
     * DOWN.clockwise() returns LEFT.
     * @return The Direction 90 degrees clockwise from this one
     */
    public final Direction clockwise() {
        switch (this) {
            case LEFT:
                return UP;
            case UP:
                return RIGHT;
            case RIGHT:
                return DOWN;
            default:
                return LEFT;
        }
    }
    
    /**
     * Returns the Direction 90 degrees counterclockwise from this one. For
     * instance, DOWN.counterclockwise() returns RIGHT.
     * @return The Direction 90 degrees counterclockwise from this one
     */
    public final Direction counterclockwise() {
        switch (this) {
            case LEFT:
                return DOWN;
            case DOWN:
                return RIGHT;
            case RIGHT:
                return UP;
            default:
                return LEFT;
        }
    }
    
    /**
     * Returns the angle in degrees, going clockwise from directly right, that
     * corresponds to this Direction. For instance, DOWN.toAngle() returns 270.
     * @return The angle that corresponds to this Direction
     */
    public final double toAngle() {
        switch (this) {
            case RIGHT:
                return 0;
            case UP:
                return 90;
            case LEFT:
                return 180;
            default:
                return 270;
        }
    }
    
    /**
     * Returns the Direction that the specified angle most closely points in.
     * For instance, Direction.ofAngle(250) returns DOWN. This method is
     * effectively the inverse of toAngle().
     * @param angle The angle to approximate with a Direction
     * @return The Direction that the specified angle most closely points in
     */
    public static Direction ofAngle(double angle) {
        if (angle < 45) {
            return RIGHT;
        } else if (angle < 135) {
            return UP;
        } else if (angle < 225) {
            return LEFT;
        } else if (angle < 315) {
            return DOWN;
        } else {
            return RIGHT;
        }
    }
    
}
