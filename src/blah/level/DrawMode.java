package blah.level;

/**
 * <p>A DrawMode is a procedure by which a LevelState determines the order in
 * which to draw LevelObjects with the same draw priority over one another.</p>
 * @author Andrew Heyman
 */
public enum DrawMode {
    /**
     * LevelObjects with the same draw priority are drawn in an arbitrary order.
     * This is the most efficient DrawMode when the plane of the level is
     * supposed to be perpendicular to the camera.
     */
    FLAT,
    /**
     * LevelObjects with the same draw priority are drawn with the ones with
     * higher y-coordinates in front. This creates the illusion that the camera
     * is looking diagonally down from over the plane of the level.
     */
    OVER,
    /**
     * LevelObjects with the same draw priority are drawn with the ones with
     * lower y-coordinates in front. This creates the illusion that the camera
     * is looking diagonally up from under the plane of the level.
     */
    UNDER
}
