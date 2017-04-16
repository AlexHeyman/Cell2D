package cell2D.level;

/**
 * <p>A CollisionResponse is a possible response that a ThinkerObject can have
 * to colliding with a solid surface.</p>
 * @author Andrew Heyman
 */
public enum CollisionResponse {
    /**
     * The ThinkerObject passes through the surface without its velocity
     * changing.
     */
    NONE,
    /**
     * The ThinkerObject's movement is blocked by the surface, and the component
     * of its velocity perpendicular to the surface is eliminated.
     */
    SLIDE,
    /**
     * The ThinkerObject's movement is blocked by the surface, and its velocity
     * is eliminated.
     */
    STOP,
    /**
     * The ThinkerObject's movement is blocked by the surface, and the component
     * of its velocity perpendicular to the surface is flipped.
     */
    BOUNCE
}
