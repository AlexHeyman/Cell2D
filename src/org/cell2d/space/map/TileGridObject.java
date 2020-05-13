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
 * <p>A TileGridObject is a type of SpaceObject that spatially instantiates a
 * TileGrid, which is specified upon the TileGridObject's creation. When a
 * TileGridObject is constructed, the TileGrid is set as the TileGridObject's
 * appearance, and the TileGridObject's locator Hitbox is set as a
 * RectangleHitbox whose origin and edges match those of the TileGrid. Since
 * TileGrids cannot be visually flipped or rotated, TileGridObjects should not
 * be flipped or rotated themselves.</p>
 * @see TileGrid
 * @author Alex Heyman
 */
public class TileGridObject extends SpaceObject {
    
    private final TileGrid grid;
    
    /**
     * Constructs a TileGridObject.
     * @param position This TileGridObject's initial position
     * @param grid The TileGrid that this TileGridObject instantiates
     * @param drawPriority This TileGridObject's initial draw priority
     * @param addAnimInstances If this is true, this TileGridObject will, find
     * all of its TileGrid's tiles that are AnimationInstances, and add them to
     * itself without integer IDs. This will synchronize the passage of time for
     * these AnimationInstances with the passage of time for the TileGridObject
     * itself. This process only occurs upon the TileGridObject's construction,
     * so if this option is used, undesirable behavior may occur if the
     * TileGrid's tiles are modified after the TileGridObject is constructed,
     * but before it is discarded.
     */
    public TileGridObject(CellVector position, TileGrid grid, int drawPriority, boolean addAnimInstances) {
        this(position.getX(), position.getY(), grid, drawPriority, addAnimInstances);
    }
    
    /**
     * Constructs a TileGridObject.
     * @param x The x-coordinate of this TileGridObject's initial position
     * @param y The y-coordinate of this TileGridObject's initial position
     * @param grid The TileGrid that this TileGridObject instantiates
     * @param drawPriority This TileGridObject's initial draw priority
     * @param addAnimInstances If this is true, this TileGridObject will, find
     * all of its TileGrid's tiles that are AnimationInstances, and add them to
     * itself without integer IDs. This will synchronize the passage of time for
     * these AnimationInstances with the passage of time for the TileGridObject
     * itself. This process only occurs upon the TileGridObject's construction,
     * so if this option is used, undesirable behavior may occur if the
     * TileGrid's tiles are modified after the TileGridObject is constructed,
     * but before it is discarded.
     */
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
    
    /**
     * Returns the TileGrid that this TileGridObject instantiates.
     * @return This TileGridObject's TileGrid
     */
    public final TileGrid getGrid() {
        return grid;
    }
    
    /**
     * Returns a list of non-overlapping RectangleHitboxes, representing regions
     * of the space in which this TileGridObject exists, that collectively
     * overlap or "cover" all and only the grid cells in this TileGridObject's
     * TileGrid that are occupied by tiles. Each of the RectangleHitboxes may be
     * of any width and any height, and each of them has its origin at its top
     * left corner. The number of returned RectangleHitboxes is not necessarily
     * the smallest possible number that can satisfy the requirements, but it is
     * likely to be close (almost certainly within a factor of 2).
     * @return A list of non-overlapping RectangleHitboxes that collectively
     * cover this TileGridObject's TileGrid's cells that are occupied by tiles
     */
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
