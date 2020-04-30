package org.cell2d.space.map;

import java.awt.Rectangle;
import java.util.List;
import org.cell2d.Drawable;

/**
 * 
 * @author Alex Heyman
 */
public class ArrayTileGrid extends TileGrid {
    
    private static final byte FL_FLIPX = 1;
    private static final byte FL_FLIPY = 1 << 1;
    private static final byte FL_ROTATE90 = 1 << 2;
    private static final byte FL_ROTATE180 = FL_ROTATE90 << 1;
    
    private final int leftmostColumn, topmostRow;
    private final Drawable[][] tiles;
    private final byte[][] flags;
    
    public ArrayTileGrid(int numColumns, int numRows, int tileWidth, int tileHeight) {
        this(0, numColumns - 1, 0, numRows - 1, tileWidth, tileHeight);
    }
    
    public ArrayTileGrid(int leftmostColumn, int rightmostColumn, int topmostRow, int bottommostRow,
            int tileWidth, int tileHeight) {
        super(tileWidth, tileHeight);
        if (rightmostColumn < leftmostColumn) {
            throw new RuntimeException(
                    "Attempted to construct an ArrayTileGrid with its rightmost column index ("
                            + rightmostColumn + ") lower than its leftmost column index (" + leftmostColumn
                            + ")");
        }
        if (bottommostRow < topmostRow) {
            throw new RuntimeException(
                    "Attempted to construct an ArrayTileGrid with its bottommost row index (" + bottommostRow
                            + ") lower than its topmost row index (" + topmostRow + ")");
        }
        this.leftmostColumn = leftmostColumn;
        this.topmostRow = topmostRow;
        int numColumns = rightmostColumn - leftmostColumn + 1;
        int numRows = bottommostRow - topmostRow + 1;
        tiles = new Drawable[numColumns][numRows];
        flags = new byte[numColumns][numRows];
    }
    
    @Override
    public final int getLeftmostColumn() {
        return leftmostColumn;
    }
    
    @Override
    public final int getRightmostColumn() {
        return leftmostColumn + tiles.length - 1;
    }
    
    @Override
    public final int getTopmostRow() {
        return topmostRow;
    }
    
    @Override
    public final int getBottommostRow() {
        return topmostRow + tiles[0].length - 1;
    }
    
    @Override
    public final Drawable getTile(int column, int row) {
        int i = column - leftmostColumn;
        int j = row - topmostRow;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return null;
        }
        return tiles[i][j];
    }
    
    @Override
    public final boolean setTile(int column, int row, Drawable tile) {
        int i = column - leftmostColumn;
        int j = row - topmostRow;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        tiles[i][j] = tile;
        return true;
    }
    
    @Override
    public final boolean getTileXFlip(int column, int row) {
        int i = column - leftmostColumn;
        int j = row - topmostRow;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        return (flags[i][j] & FL_FLIPX) != 0;
    }
    
    @Override
    public final boolean setTileXFlip(int column, int row, boolean xFlip) {
        int i = column - leftmostColumn;
        int j = row - topmostRow;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        if (xFlip) {
            flags[i][j] |= FL_FLIPX;
        } else {
            flags[i][j] &= ~FL_FLIPX;
        }
        return true;
    }
    
    @Override
    public final boolean getTileYFlip(int column, int row) {
        int i = column - leftmostColumn;
        int j = row - topmostRow;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        return (flags[i][j] & FL_FLIPY) != 0;
    }
    
    @Override
    public final boolean setTileYFlip(int column, int row, boolean yFlip) {
        int i = column - leftmostColumn;
        int j = row - topmostRow;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        if (yFlip) {
            flags[i][j] |= FL_FLIPY;
        } else {
            flags[i][j] &= ~FL_FLIPY;
        }
        return true;
    }
    
    @Override
    public final double getTileAngle(int column, int row) {
        int i = column - leftmostColumn;
        int j = row - topmostRow;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return 0;
        }
        return ((flags[i][j] & (FL_ROTATE90|FL_ROTATE180))/FL_ROTATE90)*90;
    }
    
    @Override
    public final boolean setTileAngle(int column, int row, double angle) {
        int i = column - leftmostColumn;
        int j = row - topmostRow;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        double normalizedAngle = angle % 360;
        if (normalizedAngle < 0) {
            normalizedAngle += 360;
        }
        if (normalizedAngle == 0) {
            flags[i][j] &= ~(FL_ROTATE90|FL_ROTATE180);
        } else if (normalizedAngle == 90) {
            flags[i][j] |= FL_ROTATE90;
            flags[i][j] &= ~FL_ROTATE180;
        } else if (normalizedAngle == 180) {
            flags[i][j] &= ~FL_ROTATE90;
            flags[i][j] |= FL_ROTATE180;
        } else if (normalizedAngle == 270) {
            flags[i][j] |= (FL_ROTATE90|FL_ROTATE180);
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    public final List<Rectangle> cover() {
        return TileGrid.coverObjects(leftmostColumn, topmostRow, tiles);
    }
    
}
