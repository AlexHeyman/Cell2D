package org.cell2d.space.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.cell2d.Drawable;
import org.cell2d.Filter;
import org.cell2d.Frac;
import org.cell2d.celick.Graphics;

/**
 * <p>A TileGrid is a Drawable that displays a rectangular grid of other
 * Drawables, called <i>tiles</i>. A TileGrid's tiles are assumed to all be
 * rectangular, of equal width and equal height, and with their origins at their
 * top left corners - though these requirements are not enforced. It is the
 * responsibility of the creators and modifiers of a TileGrid to ensure that
 * the TileGrid is not set as one of its own tiles, or otherwise involved in a
 * hierarchy of composite Drawables containing an infinite loop.</p>
 * 
 * <p>A TileGrid's columns are labeled with indices that increase from left to
 * right, and its rows are labeled with indices that increase from top to
 * bottom. A TileGrid's leftmost and rightmost column indices, and its topmost
 * and bottommost row indices, are specified upon its creation. A TileGrid's
 * origin is the top left corner of its grid cell with column index 0 and row
 * index 0 (or, if such a grid cell does not exist, where the top left corner of
 * that grid cell would be if it existed).</p>
 * 
 * <p>Each cell in a TileGrid can be assigned at most one Drawable, which is the
 * tile at that cell's location. A single Drawable can be set as the tile at
 * multiple locations. Tiles at individual locations can also be set to be drawn
 * flipped horizontally or vertically, or rotated in increments of 90 degrees.
 * (Rotation assumes that tiles are square, but again, this is not enforced.)
 * A TileGrid remembers the angle and flipped status of the tile at each
 * location, even while there is no tile at that location.</p>
 * 
 * <p>Though a TileGrid's tiles can be flipped and rotated, a TileGrid itself
 * cannot be flipped, rotated, or scaled via parameters of its draw() method;
 * such parameters are simply ignored.</p>
 * 
 * <p>The computational time taken to draw a TileGrid is proportional to the
 * number of its grid cells that are visible on screen, not to its total number
 * of grid cells. This means that memory usage is the only factor limiting a
 * TileGrid's size in practice.</p>
 * 
 * <p>The TileGrid class also contains the static methods coverObjects() and
 * coverPoints(), which are useful for compactly representing the occupied
 * regions of large grids of objects.</p>
 * @author Alex Heyman
 */
public abstract class TileGrid implements Drawable {
    
    private static List<Rectangle> coverAndClearPoints(Set<Point> points) {
        List<Rectangle> rectangles = new ArrayList<>();
        while (!points.isEmpty()) {
            //There are more points to cover, so we need an additional rectangle
            //Pick an arbitrary uncovered point to be the first point in the new rectangle
            Iterator<Point> iterator = points.iterator();
            Point startingPoint = iterator.next();
            iterator.remove();
            int x1 = startingPoint.x;
            int y1 = startingPoint.y;
            int x2 = startingPoint.x;
            int y2 = startingPoint.y;
            //Expand the rectangle as far left as possible
            while (points.remove(new Point(x1 - 1, y1))) {
                x1--;
            }
            //Expand the rectangle as far right as possible
            while (points.remove(new Point(x2 + 1, y1))) {
                x2++;
            }
            //Expand the rectangle as far up as possible
            while (true) {
                boolean canExpand = true;
                for (int x = x1; x <= x2; x++) {
                    if (!points.contains(new Point(x, y1 - 1))) {
                        canExpand = false;
                        break;
                    }
                }
                if (canExpand) {
                    for (int x = x1; x <= x2; x++) {
                        points.remove(new Point(x, y1 - 1));
                    }
                    y1--;
                } else {
                    break;
                }
            }
            //Expand the rectangle as far down as possible
            while (true) {
                boolean canExpand = true;
                for (int x = x1; x <= x2; x++) {
                    if (!points.contains(new Point(x, y2 + 1))) {
                        canExpand = false;
                        break;
                    }
                }
                if (canExpand) {
                    for (int x = x1; x <= x2; x++) {
                        points.remove(new Point(x, y2 + 1));
                    }
                    y2++;
                } else {
                    break;
                }
            }
            rectangles.add(new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1));
        }
        return rectangles;
    }
    
    /**
     * Returns a list of non-overlapping rectangles that collectively overlap or
     * "cover" all and only the non-null locations in the specified 2D object
     * array. Each of the rectangles may be of any width and any height. The
     * number of returned rectangles is not necessarily the smallest possible
     * number that can satisfy the requirements, but it is likely to be close
     * (almost certainly within a factor of 2).  The computational time taken by
     * this method is at most proportional to the number of locations in the
     * object array.
     * @param x1 The x-coordinate (in the space of the returned rectangles) of
     * column 0 of the object array
     * @param y1 The y-coordinate (in the space of the returned rectangles) of
     * row 0 of the object array
     * @param objects The 2D array of objects to cover. The array's first index
     * is the x-coordinate, and the second is the y-coordinate. An index
     * increase of 1 corresponds to an increase of 1 in the corresponding
     * coordinate in the space of the returned rectangles.
     * @return A list of non-overlapping rectangles that collectively cover the
     * non-null locations in the object array
     */
    public static List<Rectangle> coverObjects(int x1, int y1, Object[][] objects) {
        Set<Point> points = new HashSet<>();
        for (int i = 0; i < objects.length; i++) {
            Object[] column = objects[i];
            for (int j = 0; j < column.length; j++) {
                Object tile = column[j];
                if (tile != null) {
                    points.add(new Point(x1 + i, y1 + j));
                }
            }
        }
        return coverAndClearPoints(points);
    }
    
    /**
     * Returns a list of non-overlapping rectangles that collectively overlap or
     * "cover" all and only the points in the specified set. The points are
     * treated as locations in a rectangular grid with cells that have integer
     * coordinates; thus, each point is treated as having a width of 1 and a
     * height of 1 in the coordinate space of the returned rectangles. (For
     * instance, if the set contains only the point (0, 0), this method will
     * return only the rectangle with top left corner (0, 0) and bottom right
     * corner (1, 1).) Each of the rectangles may be of any width and any
     * height. The number of returned rectangles is not necessarily the smallest
     * possible number that can satisfy the requirements, but it is likely to be
     * close (almost certainly within a factor of 2). The computational time
     * taken by this method is at most proportional to the number of points in
     * the set.
     * @param points The set of points to cover
     * @return A list of non-overlapping rectangles that collectively cover the
     * points in the set
     */
    public static List<Rectangle> coverPoints(Set<Point> points) {
        return coverAndClearPoints(new HashSet<>(points));
    }
    
    private final int tileWidth, tileHeight;
    
    /**
     * Constructs a TileGrid with all of its grid cells unoccupied by tiles, and
     * with none of its tiles set to be flipped or rotated.
     * @param tileWidth The width in pixels of each of this TileGrid's tiles
     * @param tileHeight The height in pixels of each of this TileGrid's tiles
     */
    public TileGrid(int tileWidth, int tileHeight) {
        if (tileWidth <= 0) {
            throw new RuntimeException("Attempted to construct a TileGrid with non-positive tile width "
                    + tileWidth);
        }
        if (tileHeight <= 0) {
            throw new RuntimeException("Attempted to construct a TileGrid with non-positive tile height "
                    + tileHeight);
        }
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }
    
    /**
     * Returns the width in pixels of each of this TileGrid's tiles.
     * @return The width in pixels of each of this TileGrid's tiles
     */
    public final int getTileWidth() {
        return tileWidth;
    }
    
    /**
     * Returns the height in pixels of each of this TileGrid's tiles.
     * @return The height in pixels of each of this TileGrid's tiles
     */
    public final int getTileHeight() {
        return tileHeight;
    }
    
    /**
     * Returns the index of this TileGrid's leftmost column.
     * @return The index of this TileGrid's leftmost column
     */
    public abstract int getLeftmostColumn();
    
    /**
     * Returns the index of this TileGrid's rightmost column.
     * @return The index of this TileGrid's rightmost column
     */
    public abstract int getRightmostColumn();
    
    /**
     * Returns the index of this TileGrid's topmost row.
     * @return The index of this TileGrid's topmost row
     */
    public abstract int getTopmostRow();
    
    /**
     * Returns the index of this TileGrid's bottommost row.
     * @return The index of this TileGrid's bottommost row
     */
    public abstract int getBottommostRow();
    
    /**
     * Returns an unmodifiable Set view of the locations in this TileGrid that
     * are occupied by tiles. The x-coordinate of each returned point is the
     * column index of one of these locations, and the y-coordinate of that
     * point is the location's row index.
     * @return The locations in this TileGrid that are occupied by tiles
     */
    public abstract Set<Point> getTileLocations();
    
    /**
     * Returns the tile at the specified location in this TileGrid, or null if
     * there is none.
     * @param column The location's column index
     * @param row The location's row index
     * @return The tile at the specified location
     */
    public abstract Drawable getTile(int column, int row);
    
    /**
     * Sets the tile at the specified location in this TileGrid to the specified
     * Drawable. If the location is outside the bounds of this TileGrid, the
     * operation will fail and this method will do nothing.
     * @param column The location's column index
     * @param row The location's row index
     * @param tile The Drawable to set as the tile at the specified location, or
     * null if there should be no tile at that location
     * @return Whether the operation was successful
     */
    public abstract boolean setTile(int column, int row, Drawable tile);
    
    /**
     * Returns whether the tile at the specified location in this TileGrid is
     * flipped horizontally, or false if the location is outside the bounds of
     * this TileGrid.
     * @param column The location's column index
     * @param row The location's row index
     * @return Whether the tile at the specified location is flipped
     * horizontally
     */
    public abstract boolean getTileXFlip(int column, int row);
    
    /**
     * Sets whether the tile at the specified location in this TileGrid is
     * flipped horizontally. If the location is outside the bounds of this
     * TileGrid, the operation will fail and this method will do nothing.
     * @param column The location's column index
     * @param row The location's row index
     * @param xFlip Whether the tile at the specified location should be flipped
     * horizontally
     * @return Whether the operation was successful
     */
    public abstract boolean setTileXFlip(int column, int row, boolean xFlip);
    
    /**
     * Returns whether the tile at the specified location in this TileGrid is
     * flipped vertically, or false if the location is outside the bounds of
     * this TileGrid.
     * @param column The location's column index
     * @param row The location's row index
     * @return Whether the tile at the specified location is flipped vertically
     */
    public abstract boolean getTileYFlip(int column, int row);
    
    /**
     * Sets whether the tile at the specified location in this TileGrid is
     * flipped vertically. If the location is outside the bounds of this
     * TileGrid, the operation will fail and this method will do nothing.
     * @param column The location's column index
     * @param row The location's row index
     * @param yFlip Whether the tile at the specified location should be flipped
     * vertically
     * @return Whether the operation was successful
     */
    public abstract boolean setTileYFlip(int column, int row, boolean yFlip);
    
    /**
     * Returns the angle by which the tile at the specified location in this
     * TileGrid is rotated, or 0 if the location is outside the bounds of this
     * TileGrid. The returned angle is normalized to be between 0 and 360
     * degrees.
     * @param column The location's column index
     * @param row The location's row index
     * @return The angle by which the tile at the specified location is rotated
     */
    public abstract double getTileAngle(int column, int row);
    
    /**
     * Sets to the specified value the angle by which the tile at the specified
     * location in this TileGrid is rotated. If the value is not a multiple of
     * 90 degrees, or the location is outside the bounds of this TileGrid, the
     * operation will fail and this method will do nothing.
     * @param column The location's column index
     * @param row The location's row index
     * @param angle The angle in degrees by which the tile at the specified
     * location should be rotated
     * @return Whether the operation was successful
     */
    public abstract boolean setTileAngle(int column, int row, double angle);
    
    /**
     * Returns a list of non-overlapping rectangles that collectively overlap or
     * "cover" all and only the grid cells in this TileGrid that are occupied by
     * tiles. The coordinate space of the returned rectangles has this
     * TileGrid's column indices as its x-coordinates and this TileGrid's row
     * indices as its y-coordinates. (For instance, if the only tile in this
     * TileGrid is at column index 0 and row index 0, this method will return
     * only the rectangle with top left corner (0, 0) and bottom right corner
     * (1, 1).) Each of the rectangles may be of any width and any height. The
     * number of returned rectangles is not necessarily the smallest possible
     * number that can satisfy the requirements, but it is likely to be close
     * (almost certainly within a factor of 2).
     * @return A list of non-overlapping rectangles that collectively cover the
     * grid cells in this TileGrid that are occupied by tiles
     */
    public abstract List<Rectangle> cover();
    
    private void draw(Graphics g, int x, int y, double alpha, Filter filter) {
        Rectangle region = Drawable.getRenderableRegion(g);
        draw(region.x, region.y, region.x + region.width, region.y + region.height, g, x, y, alpha, filter);
    }
    
    private void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            double alpha, Filter filter) {
        Rectangle region = Drawable.getRenderableRegion(g);
        int x1 = Math.max(region.x, x + left);
        int y1 = Math.max(region.y, y + top);
        int x2 = Math.min(region.x + region.width, x + right);
        int y2 = Math.min(region.y + region.height, y + bottom);
        org.cell2d.celick.geom.Rectangle worldClip = g.getWorldClip();
        g.setWorldClip(x1, y1, x2 - x1, y2 - y1);
        draw(x1, y1, x2, y2, g, x, y, alpha, filter);
        g.setWorldClip(worldClip);
    }
    
    private void draw(int x1, int y1, int x2, int y2, Graphics g,
            int x, int y, double alpha, Filter filter) {
        int leftColumn = getLeftmostColumn();
        int left = x + leftColumn*tileWidth;
        if (left <= x1 - tileWidth) {
            int diff = Frac.intFloor(Frac.div((x1 - left)*Frac.UNIT, tileWidth*Frac.UNIT));
            leftColumn += diff;
            left += diff*tileWidth;
        }
        int topRow = getTopmostRow();
        int top = y + topRow*tileHeight;
        if (top <= y1 - tileHeight) {
            int diff = Frac.intFloor(Frac.div((y1 - top)*Frac.UNIT, tileHeight*Frac.UNIT));
            topRow += diff;
            top += diff*tileHeight;
        }
        int rightmostColumn = getRightmostColumn();
        int bottommostRow = getBottommostRow();
        int drawX = left;
        int column = leftColumn;
        while (drawX < x2 && column <= rightmostColumn) {
            int drawY = top;
            int row = topRow;
            while (drawY < y2 && row <= bottommostRow) {
                Drawable tile = getTile(column, row);
                if (tile != null) {
                    boolean tileXFlip = getTileXFlip(column, row);
                    boolean tileYFlip = getTileYFlip(column, row);
                    double tileAngle = getTileAngle(column, row);
                    boolean absXFlip, absYFlip;
                    if (tileAngle == 90 || tileAngle == 270) {
                        absXFlip = tileYFlip;
                        absYFlip = tileXFlip;
                    } else {
                        absXFlip = tileXFlip;
                        absYFlip = tileYFlip;
                    }
                    int drawOffsetX = (absXFlip ^ (tileAngle == 180 || tileAngle == 270) ? tileWidth : 0);
                    int drawOffsetY = (absYFlip ^ (tileAngle == 90 || tileAngle == 180) ? tileHeight : 0);
                    tile.draw(g, drawX + drawOffsetX, drawY + drawOffsetY,
                            tileXFlip, tileYFlip, tileAngle, alpha, filter);
                }
                drawY += tileHeight;
                row++;
            }
            drawX += tileWidth;
            column++;
        }
    }
    
    @Override
    public void draw(Graphics g, int x, int y) {
        draw(g, x, y, 1, null);
    }
    
    @Override
    public void draw(Graphics g, int x, int y,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter) {
        draw(g, x, y, alpha, filter);
    }
    
    @Override
    public void draw(Graphics g, int x, int y,
            double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter) {
        draw(g, x, y, alpha, filter);
    }
    
    @Override
    public void draw(Graphics g, int x, int y, int left, int right, int top, int bottom) {
        draw(g, x, y, left, right, top, bottom, 1, null);
    }
    
    @Override
    public void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter) {
        draw(g, x, y, left, right, top, bottom, alpha, filter);
    }
    
    @Override
    public void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter) {
        draw(g, x, y, left, right, top, bottom, alpha, filter);
    }
    
}
