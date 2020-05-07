package org.cell2d.space.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cell2d.Drawable;

/**
 * 
 * @author Alex Heyman
 */
public class HashTileGrid extends TileGrid {
    
    private static final byte FL_FLIPX = 1;
    private static final byte FL_FLIPY = 1 << 1;
    private static final byte FL_ROTATE90 = 1 << 2;
    private static final byte FL_ROTATE180 = FL_ROTATE90 << 1;
    
    private final int leftmostColumn, rightmostColumn, topmostRow, bottommostRow;
    private final Map<Point,Drawable> tiles = new HashMap<>();
    private final Map<Point,Integer> flags = new HashMap<>();
    
    public HashTileGrid(int numColumns, int numRows, int tileWidth, int tileHeight) {
        this(0, numColumns - 1, 0, numRows - 1, tileWidth, tileHeight);
    }
    
    public HashTileGrid(int leftmostColumn, int rightmostColumn, int topmostRow, int bottommostRow,
            int tileWidth, int tileHeight) {
        super(tileWidth, tileHeight);
        if (rightmostColumn < leftmostColumn) {
            throw new RuntimeException(
                    "Attempted to construct a HashTileGrid with its rightmost column index ("
                            + rightmostColumn + ") lower than its leftmost column index (" + leftmostColumn
                            + ")");
        }
        if (bottommostRow < topmostRow) {
            throw new RuntimeException(
                    "Attempted to construct a HashTileGrid with its bottommost row index (" + bottommostRow
                            + ") lower than its topmost row index (" + topmostRow + ")");
        }
        this.leftmostColumn = leftmostColumn;
        this.rightmostColumn = rightmostColumn;
        this.topmostRow = topmostRow;
        this.bottommostRow = bottommostRow;
    }
    
    @Override
    public final int getLeftmostColumn() {
        return leftmostColumn;
    }
    
    @Override
    public final int getRightmostColumn() {
        return rightmostColumn;
    }
    
    @Override
    public final int getTopmostRow() {
        return topmostRow;
    }
    
    @Override
    public final int getBottommostRow() {
        return bottommostRow;
    }
    
    @Override
    public final Set<Point> getTileLocations() {
        return Collections.unmodifiableSet(tiles.keySet());
    }
    
    @Override
    public final Drawable getTile(int column, int row) {
        return tiles.get(new Point(column, row));
    }
    
    @Override
    public final boolean setTile(int column, int row, Drawable tile) {
        if (column < leftmostColumn || column > rightmostColumn || row < topmostRow || row > bottommostRow) {
            return false;
        }
        if (tile == null) {
            tiles.remove(new Point(column, row));
        } else {
            tiles.put(new Point(column, row), tile);
        }
        return true;
    }
    
    private void removeFlags(Point point, int flagsToRemove) {
        int newFlags = flags.getOrDefault(point, 0) & ~flagsToRemove;
        if (newFlags == 0) {
            flags.remove(point);
        } else {
            flags.put(point, newFlags);
        }
    }
    
    @Override
    public final boolean getTileXFlip(int column, int row) {
        return (flags.getOrDefault(new Point(column, row), 0) & FL_FLIPX) != 0;
    }
    
    @Override
    public final boolean setTileXFlip(int column, int row, boolean xFlip) {
        if (column < leftmostColumn || column > rightmostColumn || row < topmostRow || row > bottommostRow) {
            return false;
        }
        Point point = new Point(column, row);
        if (xFlip) {
            flags.put(point, flags.getOrDefault(point, 0) | FL_FLIPX);
        } else {
            removeFlags(point, FL_FLIPX);
        }
        return true;
    }
    
    @Override
    public final boolean getTileYFlip(int column, int row) {
        return (flags.getOrDefault(new Point(column, row), 0) & FL_FLIPY) != 0;
    }
    
    @Override
    public final boolean setTileYFlip(int column, int row, boolean yFlip) {
        if (column < leftmostColumn || column > rightmostColumn || row < topmostRow || row > bottommostRow) {
            return false;
        }
        Point point = new Point(column, row);
        if (yFlip) {
            flags.put(point, flags.getOrDefault(point, 0) | FL_FLIPY);
        } else {
            removeFlags(point, FL_FLIPY);
        }
        return true;
    }
    
    @Override
    public final double getTileAngle(int column, int row) {
        return ((flags.getOrDefault(new Point(column, row), 0) & (FL_ROTATE90|FL_ROTATE180))/FL_ROTATE90)*90;
    }
    
    @Override
    public final boolean setTileAngle(int column, int row, double angle) {
        if (column < leftmostColumn || column > rightmostColumn || row < topmostRow || row > bottommostRow) {
            return false;
        }
        double normalizedAngle = angle % 360;
        if (normalizedAngle < 0) {
            normalizedAngle += 360;
        }
        Point point = new Point(column, row);
        if (normalizedAngle == 0) {
            removeFlags(point, FL_ROTATE90|FL_ROTATE180);
        } else if (normalizedAngle == 90) {
            flags.put(point, (flags.getOrDefault(point, 0) | FL_ROTATE90) & ~FL_ROTATE180);
        } else if (normalizedAngle == 180) {
            flags.put(point, (flags.getOrDefault(point, 0) | FL_ROTATE180) & ~FL_ROTATE90);
        } else if (normalizedAngle == 270) {
            flags.put(point, flags.getOrDefault(point, 0) | FL_ROTATE90 | FL_ROTATE180);
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    public final List<Rectangle> cover() {
        return TileGrid.coverPoints(tiles.keySet());
    }
    
}
