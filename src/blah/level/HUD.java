package blah.level;

import blah.CellGame;
import org.newdawn.slick.Graphics;

/**
 * <p>An HUD (heads-up display) is a LevelThinker that renders visuals in front
 * of those of the Viewport or LevelState to which it is assigned. To render
 * visuals, an HUD must be assigned to a Viewport or LevelState through its
 * setHUD() method. HUDs are intended to be used to display information about a
 * LevelState's space, rather than physical objects in it.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the LevelStates that this HUD can
 * be assigned to
 */
public abstract class HUD<T extends CellGame> extends LevelThinker<T> {
    
    /**
     * Actions for this HUD to take to render its visuals.
     * @param game This HUD's LevelState's CellGame
     * @param state This HUD's LevelState
     * @param g The Graphics context to which this HUD is rendering its visuals
     * this frame
     * @param x1 The x-coordinate in pixels of this HUD's rendering region's
     * left edge on the Graphics context
     * @param y1 The y-coordinate in pixels of this HUD's rendering region's
     * top edge on the Graphics context
     * @param x2 The x-coordinate in pixels of this HUD's rendering region's
     * right edge on the Graphics context
     * @param y2 The y-coordinate in pixels of this HUD's rendering region's
     * bottom edge on the Graphics context
     */
    public abstract void renderActions(T game, LevelState<T> state,
            Graphics g, int x1, int y1, int x2, int y2);
    
}
