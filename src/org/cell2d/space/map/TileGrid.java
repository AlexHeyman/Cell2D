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
 * 
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
    
    public static List<Rectangle> coverPoints(Set<Point> points) {
        return coverAndClearPoints(new HashSet<>(points));
    }
    
    private final int tileWidth, tileHeight;
    
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
    
    public final int getTileWidth() {
        return tileWidth;
    }
    
    public final int getTileHeight() {
        return tileHeight;
    }
    
    public abstract int getLeftmostColumn();
    
    public abstract int getRightmostColumn();
    
    public abstract int getTopmostRow();
    
    public abstract int getBottommostRow();
    
    public abstract Drawable getTile(int column, int row);
    
    public abstract boolean setTile(int column, int row, Drawable tile);
    
    public abstract boolean getTileXFlip(int column, int row);
    
    public abstract boolean setTileXFlip(int column, int row, boolean xFlip);
    
    public abstract boolean getTileYFlip(int column, int row);
    
    public abstract boolean setTileYFlip(int column, int row, boolean yFlip);
    
    public abstract double getTileAngle(int column, int row);
    
    public abstract boolean setTileAngle(int column, int row, double angle);
    
    public abstract List<Rectangle> cover();
    
    @Override
    public void draw(Graphics g, int x, int y) {
        draw(g, x, y, false, false, 0, 1, null);
    }
    
    @Override
    public void draw(Graphics g, int x, int y,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter) {
        Rectangle region = Drawable.getRenderableRegion(g);
        int x1 = region.x;
        int y1 = region.y;
        int x2 = x1 + region.width;
        int y2 = y1 + region.height;
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
                            tileXFlip, tileYFlip, tileAngle, alpha, null);
                }
                drawY += tileHeight;
                row++;
            }
            drawX += tileWidth;
            column++;
        }
    }
    
    @Override
    public void draw(Graphics g, int x, int y,
            double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter) {
        draw(g, x, y, false, false, 0, alpha, filter);
    }
    
    @Override
    public void draw(Graphics g, int x, int y, int left, int right, int top, int bottom) {
        draw(g, x, y, false, false, 0, 1, null);
    }
    
    @Override
    public void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter) {
        draw(g, x, y, false, false, 0, alpha, filter);
    }
    
    @Override
    public void draw(Graphics g, int x, int y, int left, int right, int top, int bottom,
            double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter) {
        draw(g, x, y, false, false, 0, alpha, filter);
    }
    
}
