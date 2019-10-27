package org.cell2d.space;

import org.cell2d.celick.Graphics;

/**
 * <p>An HUD (heads-up display) renders visuals in front of those of the
 * Viewports or SpaceStates to which it is assigned. HUDs are intended to be
 * used to display information about a SpaceState's space, as opposed to
 * displaying physical objects in it.</p>
 * @see Viewport#setHUD(org.cell2d.space.HUD)
 * @see SpaceState#setHUD(org.cell2d.space.HUD)
 * @author Alex Heyman
 */
public interface HUD {
    
    /**
     * Actions for this HUD to take to render its visuals.
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
    void renderActions(Graphics g, int x1, int y1, int x2, int y2);
    
}
