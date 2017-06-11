package cell2d.space;

import cell2d.CellGame;
import org.newdawn.slick.Graphics;

/**
 * <p>An HUD (heads-up display) is a SpaceThinker that renders visuals in front
 * of those of the Viewport or SpaceState to which it is assigned. HUDs are
 * intended to be used to display information about a SpaceState's space, as
 * opposed to displaying physical objects in it.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the SpaceStates that this HUD can
 * be assigned to
 */
public abstract class HUD<T extends CellGame> extends SpaceThinker<T> {
    
    /**
     * Actions for this HUD to take to render its visuals.
     * @param game This HUD's SpaceState's CellGame
     * @param state This HUD's SpaceState
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
    public abstract void renderActions(T game, SpaceState<T> state,
            Graphics g, int x1, int y1, int x2, int y2);
    
}
