package org.cell2d.space.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.cell2d.AnimationInstance;
import org.cell2d.CellVector;
import org.cell2d.Drawable;
import org.cell2d.Frac;
import org.cell2d.space.RectangleHitbox;
import org.cell2d.space.SpaceObject;

/**
 *
 * @author Alex Heyman
 */
public class TileGridObject extends SpaceObject {
    
    private final TileGrid grid;
    
    public TileGridObject(CellVector position, TileGrid grid, int drawPriority, boolean addAnimInstances) {
        this(position.getX(), position.getY(), grid, drawPriority, addAnimInstances);
    }
    
    public TileGridObject(long x, long y, TileGrid grid, int drawPriority, boolean addAnimInstances) {
        long left = grid.getLeftmostColumn()*grid.getTileWidth()*Frac.UNIT;
        long right = (grid.getRightmostColumn() + 1)*grid.getTileWidth()*Frac.UNIT;
        long top = grid.getTopmostRow()*grid.getTileHeight()*Frac.UNIT;
        long bottom = (grid.getBottommostRow() + 1)*grid.getTileHeight()*Frac.UNIT;
        setLocatorHitbox(new RectangleHitbox(x, y, left, right, top, bottom));
        setCenterOffset((left + right)/2, (top + bottom)/2);
        setAppearance(grid);
        setDrawPriority(drawPriority);
        this.grid = grid;
        if (addAnimInstances) {
            for (Point point : grid.getTileLocations()) {
                Drawable tile = grid.getTile(point.x, point.y);
                if (tile instanceof AnimationInstance) {
                    addAnimInstance((AnimationInstance)tile);
                }
            }
        }
    }
    
    public final TileGrid getGrid() {
        return grid;
    }
    
    public final List<RectangleHitbox> cover() {
        List<Rectangle> rectangles = grid.cover();
        List<RectangleHitbox> hitboxes = new ArrayList<>();
        long tileWidthFrac = ((long)grid.getTileWidth()) << Frac.BITS;
        long tileHeightFrac = ((long)grid.getTileHeight()) << Frac.BITS;
        for (Rectangle rectangle : rectangles) {
            hitboxes.add(new RectangleHitbox(
                    getX() + rectangle.x*tileWidthFrac, getY() + rectangle.y*tileHeightFrac,
                    0, rectangle.width*tileWidthFrac, 0, rectangle.height*tileHeightFrac));
        }
        return hitboxes;
    }
    
}
