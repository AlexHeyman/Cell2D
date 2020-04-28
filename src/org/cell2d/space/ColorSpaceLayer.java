package org.cell2d.space;

import org.cell2d.Color;
import org.cell2d.celick.Graphics;

/**
 * <p>A ColorSpaceLayer is a type of SpaceLayer that fills the rendering region
 * with a single Color. It is useful for creating solid-color backgrounds and,
 * when displaying a semi-transparent Color, for creating fog effects.
 * </p>
 * @see Color
 * @author Alex Heyman
 */
public class ColorSpaceLayer implements SpaceLayer {
    
    private Color color;
    
    /**
     * Constructs a ColorSpaceLayer that displays the specified Color.
     * @param color This ColorSpaceLayer's displayed Color
     */
    public ColorSpaceLayer(Color color) {
        this.color = color;
    }
    
    /**
     * Returns the Color that this ColorSpaceLayer displays.
     * @return This ColorSpaceLayer's displayed Color
     */
    public final Color getColor() {
        return color;
    }
    
    /**
     * Sets the Color that this ColorSpaceLayer displays to the specified Color.
     * @param color This ColorSpaceLayer's new displayed Color
     */
    public final void setColor(Color color) {
        this.color = color;
    }
    
    @Override
    public void renderActions(Graphics g, long cx, long cy, int x, int y, int x1, int y1, int x2, int y2) {
        g.setColor(color);
        g.fillRect(x1, y1, x2 - x1, y2 - y1);
    }
    
}
