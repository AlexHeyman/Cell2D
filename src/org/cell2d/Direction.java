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
     * Returns the Direction opposite this one. For instance, RIGHT.opposite()
     * returns LEFT.
     * @return The Direction opposite this one
     */
    public Direction opposite() {
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
     * RIGHT.clockwise() returns DOWN.
     * @return The Direction 90 degrees clockwise from this one
     */
    public Direction clockwise() {
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
     * instance, RIGHT.counterclockwise() returns UP.
     * @return The Direction 90 degrees counterclockwise from this one
     */
    public Direction counterclockwise() {
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
    
}
