package blah.level;

import blah.CellGame;
import org.newdawn.slick.Graphics;

/**
 * <p>A LevelLayer is a LevelThinker that renders visuals either in front of or
 * behind those of the LevelObjects in the LevelState to which it is assigned.
 * To render visuals, a LevelLayer must be assigned to a LevelState through its
 * setLayer() method. LevelLayers are intended to be used to display objects in
 * the foreground or background of a LevelState's space.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the LevelStates that this
 * LevelLayer can be assigned to
 */
public abstract class LevelLayer<T extends CellGame> extends LevelThinker<T> {
    
    /**
     * Actions for this LevelLayer to take to render its visuals through a
     * Viewport's camera.
     * @param game This LevelLayer's LevelState's CellGame
     * @param state This LevelLayer's LevelState
     * @param g The Graphics context to which this LevelLayer is rendering its
     * visuals this frame
     * @param x The camera's center x-coordinate
     * @param y The camera's center y-coordinate
     * @param x1 The x-coordinate in pixels of the Viewport's left edge on the
     * Graphics context
     * @param y1 The y-coordinate in pixels of the Viewport's top edge on the
     * Graphics context
     * @param x2 The x-coordinate in pixels of the Viewport's right edge on the
     * Graphics context
     * @param y2 The y-coordinate in pixels of the Viewport's bottom edge on the
     * Graphics context
     */
    public abstract void renderActions(T game, LevelState<T> state,
            Graphics g, double x, double y, int x1, int y1, int x2, int y2);
    
}
