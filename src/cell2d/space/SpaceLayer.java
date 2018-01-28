package cell2d.space;

import cell2d.CellGame;
import org.newdawn.slick.Graphics;

/**
 * <p>A SpaceLayer is a SpaceThinker that renders visuals either in front of or
 * behind those of the SpaceObjects in the SpaceState to which it is assigned.
 * SpaceLayers are intended to be used to display objects in the foreground or
 * background of a SpaceState's space.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the SpaceStates that this
 * SpaceLayer can be assigned to
 */
public abstract class SpaceLayer<T extends CellGame> extends SpaceThinker<T> {
    
    /**
     * Actions for this SpaceLayer to take to render its visuals through a
     * Viewport's camera.
     * @param game This SpaceLayer's SpaceState's CellGame
     * @param state This SpaceLayer's SpaceState
     * @param g The Graphics context to which this SpaceLayer is rendering its
     * visuals this frame
     * @param cx The camera's center x-coordinate
     * @param cy The camera's center y-coordinate
     * @param x The x-coordinate in pixels on the Graphics context that
     * corresponds to the camera's center x-coordinate
     * @param y The y-coordinate in pixels on the Graphics context that
     * corresponds to the camera's center y-coordinate
     * @param x1 The x-coordinate in pixels of the Viewport's left edge on the
     * Graphics context
     * @param y1 The y-coordinate in pixels of the Viewport's top edge on the
     * Graphics context
     * @param x2 The x-coordinate in pixels of the Viewport's right edge on the
     * Graphics context
     * @param y2 The y-coordinate in pixels of the Viewport's bottom edge on the
     * Graphics context
     */
    public abstract void renderActions(T game, SpaceState<T> state,
            Graphics g, long cx, long cy, int x, int y, int x1, int y1, int x2, int y2);
    
}
