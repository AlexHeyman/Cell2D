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
    
}
