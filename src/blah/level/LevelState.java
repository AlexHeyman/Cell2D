package blah.level;

import blah.CellGame;
import blah.CellGameState;
import blah.SafeIterator;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.util.Pair;
import org.newdawn.slick.Graphics;

/**
 * <p>A LevelState is a CellGameState that handles gameplay in a 2D space,
 * intended for action games in which events happen in real time. Space in a
 * LevelState is divided into rectangular cells of equal and positive widths and
 * heights, both of which are specified externally. A LevelState automatically
 * creates more cells as LevelObjects enter where they would be if they existed.
 * </p>
 * 
 * <p>LevelObjects may be assigned to one LevelState each in much the same way
 * that Thinkers are assigned to CellGameStates. Similarly to Thinkers, the
 * actual addition or removal of a LevelObject to or from a LevelState is
 * delayed until any and all current iterations through its LevelThinkers,
 * LevelObjects, or ThinkerObjects, such as the periods during which
 * ThinkerObjects move or LevelThinkers perform their various types of actions,
 * have been completed. Multiple delayed instructions may be successfully given
 * to LevelStates regarding the same LevelObject without having to wait until
 * all iterations have finished.</p>
 * 
 * <p>LevelStates use cells to organize LevelObjects by location, improving the
 * efficiency of processes like ThinkerObject movement that are concerned only
 * with LevelObjects in a small region of space. For maximum efficiency, cells
 * should be set to be large enough that LevelObjects do not change which cells
 * they are in too frequently, but small enough that not too many LevelObjects
 * are in each cell at any one time.</p>
 * 
 * <p>Every frame, between the periods in which its LevelThinkers perform their
 * beforeMovementActions() and afterMovementActions(), a LevelState moves each
 * of its ThinkerObjects by the sum of its velocity and step vectors multiplied
 * by its time factor, then resets its step vector to (0, 0). This, along with
 * manual calls to the ThinkerObject's doMovement() method, is when the
 * ThinkerObject interacts with the solid surfaces of LevelObjects in its path
 * if it has collision enabled.</p>
 * 
 * <p>LevelLayers may be assigned to one LevelState each with an integer ID in
 * the context of that LevelState. Only one LevelLayer may be assigned to a
 * given LevelState with a given ID at once. LevelLayers with higher IDs are
 * rendered in front of those with lower ones. LevelLayers with positive IDs
 * are rendered in front of the LevelState's LevelObjects, and LevelLayers with
 * negative IDs are rendered behind its LevelObjects. LevelLayers may not be
 * added with an ID of 0.</p>
 * 
 * <p>HUDs may be assigned to one LevelState each. Only one HUD may be assigned
 * to a given LevelState at once. A LevelState's HUD uses the entire screen as
 * its rendering region.</p>
 * 
 * <p>Viewports may be assigned to one LevelState each with an integer ID in
 * the context of that LevelState. Only one Viewport may be assigned to a
 * given LevelState with a given ID at once.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses this LevelState
 */
public class LevelState<T extends CellGame> extends CellGameState<T,LevelState<T>,LevelThinker<T>,LevelThinkerState<T>> {
    
    private static abstract class LevelComparator<T> implements Comparator<T>, Serializable {}
    
    private Comparator<Hitbox<T>> drawPriorityComparator = new LevelComparator<Hitbox<T>>() {
        
        @Override
        public final int compare(Hitbox<T> hitbox1, Hitbox<T> hitbox2) {
            int drawPriorityDifference = hitbox1.drawPriority - hitbox2.drawPriority;
            return (drawPriorityDifference == 0 ? Long.signum(hitbox1.id - hitbox2.id) : drawPriorityDifference);
        }
        
    };
    private Comparator<Pair<Hitbox<T>,Iterator<Hitbox<T>>>> drawPriorityIteratorComparator = new LevelComparator<Pair<Hitbox<T>,Iterator<Hitbox<T>>>>() {
        
        @Override
        public final int compare(Pair<Hitbox<T>, Iterator<Hitbox<T>>> pair1, Pair<Hitbox<T>, Iterator<Hitbox<T>>> pair2) {
            return drawPriorityComparator.compare(pair1.getKey(), pair2.getKey());
        }
        
    };
    private Comparator<Hitbox<T>> overModeComparator = new LevelComparator<Hitbox<T>>() {
        
        @Override
        public final int compare(Hitbox<T> hitbox1, Hitbox<T> hitbox2) {
            int drawPriorityDifference = hitbox1.drawPriority - hitbox2.drawPriority;
            if (drawPriorityDifference == 0) {
                int yDiffSignum = (int)Math.signum(hitbox1.getRelY() - hitbox2.getRelY());
                return (yDiffSignum == 0 ? Long.signum(hitbox1.id - hitbox2.id) : yDiffSignum);
            }
            return drawPriorityDifference;
        }
        
    };
    private Comparator<Hitbox<T>> underModeComparator = new LevelComparator<Hitbox<T>>() {
        
        @Override
        public final int compare(Hitbox<T> hitbox1, Hitbox<T> hitbox2) {
            int drawPriorityDifference = hitbox1.drawPriority - hitbox2.drawPriority;
            if (drawPriorityDifference == 0) {
                int yDiffSignum = (int)Math.signum(hitbox2.getRelY() - hitbox1.getRelY());
                return (yDiffSignum == 0 ? Long.signum(hitbox1.id - hitbox2.id) : yDiffSignum);
            }
            return drawPriorityDifference;
        }
        
    };
    private Comparator<ThinkerObject<T>> movementPriorityComparator = new LevelComparator<ThinkerObject<T>>() {
        
        @Override
        public final int compare(ThinkerObject<T> object1, ThinkerObject<T> object2) {
            int priorityDifference = object2.movementPriority - object1.movementPriority;
            return (priorityDifference == 0 ? Long.signum(object2.id - object1.id) : priorityDifference);
        }
        
    };
    
    private int frameState = 0;
    private final Set<LevelObject<T>> levelObjects = new HashSet<>();
    private int objectIterators = 0;
    private final Queue<ObjectChangeData<T>> objectChanges = new LinkedList<>();
    private boolean updatingObjectList = false;
    private final SortedSet<ThinkerObject<T>> thinkerObjects = new TreeSet<>(movementPriorityComparator);
    private int thinkerObjectIterators = 0;
    private final Queue<ThinkerObjectChangeData<T>> thinkerObjectChanges = new LinkedList<>();
    private double cellWidth, cellHeight;
    private final Map<Point,Cell> cells = new HashMap<>();
    private int cellLeft = 0;
    private int cellRight = 0;
    private int cellTop = 0;
    private int cellBottom = 0;
    private DrawMode drawMode;
    private final SortedMap<Integer,LevelLayer<T>> levelLayers = new TreeMap<>();
    private HUD<T> hud = null;
    private final Map<Integer,Viewport<T>> viewports = new HashMap<>();
    
    /**
     * Creates a new LevelState of the specified CellGame with the specified ID.
     * @param game The CellGame to which this LevelState belongs
     * @param id This LevelState's ID
     * @param cellWidth The width of each of this LevelState's cells
     * @param cellHeight The height of each of this LevelState's cells
     * @param drawMode This LevelState's DrawMode
     */
    public LevelState(T game, int id, double cellWidth, double cellHeight, DrawMode drawMode) {
        super(game, id);
        setCellDimensions(cellWidth, cellHeight);
        setDrawMode(drawMode);
    }
    
    @Override
    public final LevelState<T> getThis() {
        return this;
    }
    
    private class Cell {
        
        private int x, y;
        private double left, right, top, bottom;
        private Set<Hitbox<T>> locatorHitboxes;
        private final Set<Hitbox<T>> centerHitboxes = new HashSet<>();
        private final Set<Hitbox<T>> overlapHitboxes = new HashSet<>();
        private final Set<Hitbox<T>> solidHitboxes = new HashSet<>();
        private final Map<Direction,Set<Hitbox<T>>> solidSurfaces = new EnumMap<>(Direction.class);
        private final Set<Hitbox<T>> collisionHitboxes = new HashSet<>();
        
        private Cell(int x, int y) {
            this.x = x;
            this.y = y;
            left = x*cellWidth;
            right = left + cellWidth;
            top = y*cellHeight;
            bottom = top + cellHeight;
            initializeLocatorHitboxes();
        }
        
        private void initializeLocatorHitboxes() {
            if (drawMode == DrawMode.FLAT) {
                locatorHitboxes = new TreeSet<>(drawPriorityComparator);
            } else {
                locatorHitboxes = new HashSet<>();
            }
        }
        
        private Set<Hitbox<T>> getSolidSurfaces(Direction direction) {
            Set<Hitbox<T>> hitboxes = solidSurfaces.get(direction);
            if (hitboxes == null) {
                hitboxes = new HashSet<>();
                solidSurfaces.put(direction, hitboxes);
            }
            return hitboxes;
        }
        
    }
    
    private Cell getCell(Point point) {
        Cell cell = cells.get(point);
        if (cell == null) {
            int x = (int)point.getX();
            int y = (int)point.getY();
            if (cells.isEmpty()) {
                cellLeft = x;
                cellRight = x;
                cellTop = y;
                cellBottom = y;
            } else {
                if (x < cellLeft) {
                    cellLeft = x;
                } else if (x > cellRight) {
                    cellRight = x;
                }
                if (y < cellTop) {
                    cellTop = y;
                } else if (y > cellBottom) {
                    cellBottom = y;
                }
            }
            cell = new Cell(x, y);
            cells.put(point, cell);
        }
        return cell;
    }
    
    private int[] getCellRangeInclusive(double x1, double y1, double x2, double y2) {
        int[] cellRange = {(int)Math.ceil(x1/cellWidth) - 1, (int)Math.ceil(y1/cellHeight) - 1, (int)Math.floor(x2/cellWidth), (int)Math.floor(y2/cellHeight)};
        return cellRange;
    }
    
    private int[] getCellRangeInclusive(Hitbox<T> hitbox) {
        return getCellRangeInclusive(hitbox.getLeftEdge(), hitbox.getTopEdge(), hitbox.getRightEdge(), hitbox.getBottomEdge());
    }
    
    private int[] getCellRangeExclusive(double x1, double y1, double x2, double y2) {
        int[] cellRange = {(int)Math.floor(x1/cellWidth), (int)Math.floor(y1/cellHeight), (int)Math.ceil(x2/cellWidth) - 1, (int)Math.ceil(y2/cellHeight) - 1};
        if (cellRange[0] == cellRange[2] + 1) {
            cellRange[0]--;
        }
        if (cellRange[1] == cellRange[3] + 1) {
            cellRange[1]--;
        }
        return cellRange;
    }
    
    private int[] getCellRangeExclusive(Hitbox<T> hitbox) {
        return getCellRangeExclusive(hitbox.getLeftEdge(), hitbox.getTopEdge(), hitbox.getRightEdge(), hitbox.getBottomEdge());
    }
    
    private void updateCellRange(Hitbox<T> hitbox) {
        hitbox.cellRange = getCellRangeInclusive(hitbox);
    }
    
    private class ReadCellRangeIterator implements Iterator<Cell> {
        
        private final int left, right, top, bottom;
        private int xPos, yPos;
        private Cell nextCell;
        
        private ReadCellRangeIterator(int x1, int y1, int x2, int y2) {
            left = Math.max(x1, cellLeft);
            right = Math.min(x2, cellRight);
            top = Math.max(y1, cellTop);
            bottom = Math.min(y2, cellBottom);
            xPos = left;
            yPos = (left > right || top > bottom ? bottom + 1 : top);
            advance();
        }
        
        private ReadCellRangeIterator(int[] cellRange) {
            this(cellRange[0], cellRange[1], cellRange[2], cellRange[3]);
        }
        
        private void advance() {
            nextCell = null;
            while (nextCell == null && yPos <= bottom) {
                nextCell = cells.get(new Point(xPos, yPos));
                if (xPos == right) {
                    xPos = left;
                    yPos++;
                } else {
                    xPos++;
                }
            }
        }
        
        @Override
        public boolean hasNext() {
            return nextCell != null;
        }
        
        @Override
        public Cell next() {
            Cell next = nextCell;
            advance();
            return next;
        }
        
    }
    
    private class WriteCellRangeIterator implements Iterator<Cell> {
        
        private final int[] cellRange;
        private int xPos, yPos;
        
        private WriteCellRangeIterator(int[] cellRange) {
            this.cellRange = cellRange;
            xPos = cellRange[0];
            yPos = cellRange[1];
        }
        
        @Override
        public boolean hasNext() {
            return yPos <= cellRange[3];
        }
        
        @Override
        public Cell next() {
            Cell next = getCell(new Point(xPos, yPos));
            if (xPos == cellRange[2]) {
                xPos = cellRange[0];
                yPos++;
            } else {
                xPos++;
            }
            return next;
        }
        
    }
    
    private List<Cell> getCells(int[] cellRange) {
        List<Cell> cellList = new ArrayList<>((cellRange[2] - cellRange[0] + 1)*(cellRange[3] - cellRange[1] + 1));
        Iterator<Cell> iterator = new WriteCellRangeIterator(cellRange);
        while (iterator.hasNext()) {
            cellList.add(iterator.next());
        }
        return cellList;
    }
    
    /**
     * Returns the width of each of this LevelState's cells.
     * @return The width of each of this LevelState's cells
     */
    public final double getCellWidth() {
        return cellWidth;
    }
    
    /**
     * Returns the height of each of this LevelState's cells.
     * @return The height of each of this LevelState's cells
     */
    public final double getCellHeight() {
        return cellHeight;
    }
    
    /**
     * Sets the dimensions of each of this LevelState's cells to the specified
     * values. The more LevelObjects are currently assigned to this LevelState,
     * the longer this operation takes, as LevelObjects need to be reorganized.
     * @param cellWidth The new width of each of this LevelState's cells
     * @param cellHeight The new height of each of this LevelState's cells
     */
    public final void setCellDimensions(double cellWidth, double cellHeight) {
        if (cellWidth <= 0) {
            throw new RuntimeException("Attempted to give a LevelState a non-positive cell width");
        }
        if (cellHeight <= 0) {
            throw new RuntimeException("Attempted to give a LevelState a non-positive cell height");
        }
        cells.clear();
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        if (!levelObjects.isEmpty()) {
            for (LevelObject<T> object : levelObjects) {
                object.addCellData();
            }
        }
    }
    
    /**
     * Removes any cells that no longer have LevelObjects in them, freeing up
     * the memory that they occupied. The more cells this LevelState has, the
     * longer this operation takes.
     */
    public final void clearEmptyCells() {
        boolean firstCell = true;
        Iterator<Cell> iterator = cells.values().iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (cell.locatorHitboxes.isEmpty()
                    && cell.centerHitboxes.isEmpty()
                    && cell.overlapHitboxes.isEmpty()
                    && cell.solidHitboxes.isEmpty()
                    && cell.collisionHitboxes.isEmpty()) {
                iterator.remove();
            } else if (firstCell) {
                firstCell = false;
                cellLeft = cell.x;
                cellRight = cell.x;
                cellTop = cell.y;
                cellBottom = cell.y;
            } else {
                if (cell.x < cellLeft) {
                    cellLeft = cell.x;
                } else if (cell.x > cellRight) {
                    cellRight = cell.x;
                }
                if (cell.y < cellTop) {
                    cellTop = cell.y;
                } else if (cell.y > cellBottom) {
                    cellBottom = cell.y;
                }
            }
        }
    }
    
    /**
     * Returns this LevelState's DrawMode.
     * @return This LevelState's DrawMode
     */
    public final DrawMode getDrawMode() {
        return drawMode;
    }
    
    /**
     * Sets this LevelState's DrawMode. The more cells this LevelState has, the
     * longer this operation may take, as cells' records of LevelObjects may
     * need to be reorganized.
     * @param drawMode The new DrawMode
     */
    public final void setDrawMode(DrawMode drawMode) {
        this.drawMode = drawMode;
        if (!cells.isEmpty()) {
            for (Cell cell : cells.values()) {
                Set<Hitbox<T>> oldLocatorHitboxes = cell.locatorHitboxes;
                cell.initializeLocatorHitboxes();
                for (Hitbox<T> hitbox : oldLocatorHitboxes) {
                    cell.locatorHitboxes.add(hitbox);
                }
            }
        }
    }
    
    /**
     * Loads the specified Area about the specified origin point.
     * @param origin The origin point about which to load the Area
     * @param area The Area to load
     */
    public final void loadArea(LevelVector origin, Area<T> area) {
        loadArea(origin.getX(), origin.getY(), area);
    }
    
    /**
     * Loads the specified Area about the specified origin point.
     * @param originX The x-coordinate of the origin point about which to load
     * the Area
     * @param originY The y-coordinate of the origin point about which to load
     * the Area
     * @param area The Area to load
     */
    public final void loadArea(double originX, double originY, Area<T> area) {
        for (LevelObject<T> object : area.load(getGame(), this)) {
            if (object.state == null && object.newState == null) {
                object.changePosition(originX, originY);
                object.newState = this;
                objectChanges.add(new ObjectChangeData(object, this));
            }
        }
        updateObjectList();
    }
    
    final void updateCells(Hitbox<T> hitbox) {
        int[] oldRange = hitbox.cellRange;
        updateCellRange(hitbox);
        int[] newRange = hitbox.cellRange;
        if (oldRange[0] != newRange[0] || oldRange[1] != newRange[1]
                || oldRange[2] != newRange[2] || oldRange[3] != newRange[3]) {
            int[] addRange;
            if (oldRange == null) {
                addRange = newRange;
            } else {
                int[] removeRange = oldRange;
                Iterator<Cell> iterator = new WriteCellRangeIterator(removeRange);
                while (iterator.hasNext()) {
                    Cell cell = iterator.next();
                    if (hitbox.roles[0]) {
                        cell.locatorHitboxes.remove(hitbox);
                    }
                    if (hitbox.roles[1]) {
                        cell.centerHitboxes.remove(hitbox);
                    }
                    if (hitbox.roles[2]) {
                        cell.overlapHitboxes.remove(hitbox);
                    }
                    if (hitbox.roles[3]) {
                        cell.solidHitboxes.remove(hitbox);
                        for (Direction direction : hitbox.solidSurfaces) {
                            cell.getSolidSurfaces(direction).remove(hitbox);
                        }
                    }
                    if (hitbox.roles[4]) {
                        cell.collisionHitboxes.remove(hitbox);
                    }
                }
                addRange = newRange;
            }
            Iterator<Cell> iterator = new WriteCellRangeIterator(addRange);
            while (iterator.hasNext()) {
                Cell cell = iterator.next();
                if (hitbox.roles[0]) {
                    cell.locatorHitboxes.add(hitbox);
                }
                if (hitbox.roles[1]) {
                    cell.centerHitboxes.add(hitbox);
                }
                if (hitbox.roles[2]) {
                    cell.overlapHitboxes.add(hitbox);
                }
                if (hitbox.roles[3]) {
                    cell.solidHitboxes.add(hitbox);
                    for (Direction direction : hitbox.solidSurfaces) {
                        cell.getSolidSurfaces(direction).add(hitbox);
                    }
                }
                if (hitbox.roles[4]) {
                    cell.collisionHitboxes.add(hitbox);
                }
            }
        }
    }
    
    final void addLocatorHitbox(Hitbox<T> hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().locatorHitboxes.add(hitbox);
        }
    }
    
    final void removeLocatorHitbox(Hitbox<T> hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().locatorHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    final void changeLocatorHitboxDrawPriority(Hitbox<T> hitbox, int drawPriority) {
        List<Cell> cellList = getCells(hitbox.cellRange);
        for (Cell cell : cellList) {
            cell.locatorHitboxes.remove(hitbox);
        }
        hitbox.drawPriority = drawPriority;
        for (Cell cell : cellList) {
            cell.locatorHitboxes.add(hitbox);
        }
    }
    
    final void addCenterHitbox(Hitbox<T> hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().centerHitboxes.add(hitbox);
        }
    }
    
    final void removeCenterHitbox(Hitbox<T> hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().centerHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    final void addOverlapHitbox(Hitbox<T> hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().overlapHitboxes.add(hitbox);
        }
    }
    
    final void removeOverlapHitbox(Hitbox<T> hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().overlapHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    private void addSolidHitbox(Hitbox<T> hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().solidHitboxes.add(hitbox);
        }
    }
    
    private void removeSolidHitbox(Hitbox<T> hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().solidHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    final void addSolidSurface(Hitbox<T> hitbox, Direction direction) {
        if (hitbox.solidSurfaces.size() == 1) {
            addSolidHitbox(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().getSolidSurfaces(direction).add(hitbox);
        }
    }
    
    final void removeSolidSurface(Hitbox<T> hitbox, Direction direction) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().getSolidSurfaces(direction).remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.solidSurfaces.isEmpty()) {
            removeSolidHitbox(hitbox);
        }
    }
    
    final void addAllSolidSurfaces(Hitbox<T> hitbox) {
        if (!hitbox.solidSurfaces.isEmpty()) {
            addSolidHitbox(hitbox);
            hitbox.numCellRoles += hitbox.solidSurfaces.size();
            Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
            while (iterator.hasNext()) {
                Cell cell = iterator.next();
                for (Direction direction : hitbox.solidSurfaces) {
                    cell.getSolidSurfaces(direction).add(hitbox);
                }
            }
        }
    }
    
    final void removeAllSolidSurfaces(Hitbox<T> hitbox) {
        if (!hitbox.solidSurfaces.isEmpty()) {
            Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
            while (iterator.hasNext()) {
                Cell cell = iterator.next();
                for (Direction direction : hitbox.solidSurfaces) {
                    cell.getSolidSurfaces(direction).remove(hitbox);
                }
            }
            hitbox.numCellRoles -= hitbox.solidSurfaces.size();
            removeSolidHitbox(hitbox);
        }
    }
    
    final void completeSolidSurfaces(Hitbox<T> hitbox) {
        if (hitbox.solidSurfaces.isEmpty()) {
            addSolidHitbox(hitbox);
        }
        Set<Direction> directionsToAdd = EnumSet.complementOf(hitbox.solidSurfaces);
        hitbox.numCellRoles += directionsToAdd.size();
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            for (Direction direction : directionsToAdd) {
                cell.getSolidSurfaces(direction).add(hitbox);
            }
        }
    }
    
    final void addCollisionHitbox(Hitbox<T> hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().collisionHitboxes.add(hitbox);
        }
    }
    
    final void removeCollisionHitbox(Hitbox<T> hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().collisionHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    @Override
    public final void addThinkerActions(T game, LevelThinker<T> thinker) {
        if (frameState == 1) {
            thinker.beforeMovement(game, this);
        } else if (frameState == 2) {
            thinker.afterMovement(game, this);
        }
    }
    
    @Override
    public final void updateThinkerListActions(T game) {
        updateObjectList();
    }
    
    /**
     * Returns the number of LevelObjects that are currently assigned to this
     * LevelState.
     * @return The number of LevelObjects that are currently assigned to this
     * LevelState
     */
    public final int getNumObjects() {
        return levelObjects.size();
    }
    
    private class ObjectIterator implements SafeIterator<LevelObject<T>> {
        
        private boolean stopped = false;
        private final Iterator<LevelObject<T>> iterator = levelObjects.iterator();
        private LevelObject<T> lastObject = null;
        
        private ObjectIterator() {
            objectIterators++;
        }
        
        @Override
        public final boolean hasNext() {
            if (stopped) {
                return false;
            }
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                stop();
            }
            return hasNext;
        }
        
        @Override
        public final LevelObject<T> next() {
            if (stopped) {
                return null;
            }
            lastObject = iterator.next();
            return lastObject;
        }
        
        @Override
        public final void remove() {
            if (!stopped && lastObject != null) {
                removeObject(lastObject);
                lastObject = null;
            }
        }
        
        @Override
        public final void stop() {
            if (!stopped) {
                stopped = true;
                objectIterators--;
                updateObjectList();
            }
        }
        
    }
    
    /**
     * Returns whether any Iterators over this LevelState's list of LevelObjects
     * are currently in progress.
     * @return Whether any Iterators over this LevelState's list of LevelObjects
     * are currently in progress
     */
    public final boolean iteratingThroughObjects() {
        return objectIterators > 0;
    }
    
    /**
     * Returns a new Iterator over this LevelState's list of LevelObjects.
     * @return A new Iterator over this LevelState's list of LevelObjects
     */
    public final SafeIterator<LevelObject<T>> objectIterator() {
        return new ObjectIterator();
    }
    
    private static class ObjectChangeData<T extends CellGame> {
        
        private boolean used = false;
        private final LevelObject<T> object;
        private final LevelState<T> newState;
        
        private ObjectChangeData(LevelObject<T> object, LevelState<T> newState) {
            this.object = object;
            this.newState = newState;
        }
        
    }
    
    /**
     * Adds the specified LevelObject to this LevelState if it is not already
     * assigned to a levelState.
     * @param object The LevelObject to be added
     * @return Whether the addition occurred
     */
    public final boolean addObject(LevelObject<T> object) {
        if (object.newState == null) {
            addObjectChangeData(object, this);
            return true;
        }
        return false;
    }
    
    /**
     * Removes the specified LevelObject from this LevelState if it is currently
     * assigned to it.
     * @param object The LevelObject to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeObject(LevelObject<T> object) {
        if (object.newState == this) {
            addObjectChangeData(object, null);
            return true;
        }
        return false;
    }
    
    private void addObjectChangeData(LevelObject<T> object, LevelState<T> newState) {
        object.newState = newState;
        ObjectChangeData<T> data = new ObjectChangeData<>(object, newState);
        if (object.state != null) {
            object.state.objectChanges.add(data);
            object.state.updateObjectList();
        }
        if (newState != null) {
            newState.objectChanges.add(data);
            newState.updateObjectList();
        }
    }
    
    private void addActions(LevelObject<T> object) {
        levelObjects.add(object);
        object.state = this;
        object.addCellData();
        object.addActions();
    }
    
    private void removeActions(LevelObject<T> object) {
        object.removeActions();
        levelObjects.remove(object);
        object.state = null;
    }
    
    private void updateObjectList() {
        if (objectIterators == 0 && thinkerObjectIterators == 0
                && !iteratingThroughThinkers() && !updatingObjectList) {
            updatingObjectList = true;
            while (!objectChanges.isEmpty()) {
                ObjectChangeData<T> data = objectChanges.remove();
                if (!data.used) {
                    data.used = true;
                    if (data.object.state != null) {
                        data.object.state.removeActions(data.object);
                    }
                    if (data.newState != null) {
                        data.newState.addActions(data.object);
                    }
                }
            }
            updatingObjectList = false;
        }
    }
    
    /**
     * Removes from this LevelState all of the LevelObjects that are currently
     * assigned to it.
     */
    public final void removeAllObjects() {
        for (LevelObject<T> object : levelObjects) {
            if (object.newState == this) {
                object.newState = null;
                objectChanges.add(new ObjectChangeData(object, null));
            }
        }
        updateObjectList();
        clearEmptyCells();
    }
    
    /**
     * Removes from this LevelState all of its LevelObjects that exist entirely
     * inside the specified rectangular region.
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     */
    public final void removeRectangle(double x1, double y1, double x2, double y2) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox<T> locatorHitbox : iterator.next().locatorHitboxes) {
                if (!locatorHitbox.scanned) {
                    LevelObject<T> object = locatorHitbox.getObject();
                    if (object.newState == this
                            && locatorHitbox.getLeftEdge() >= x1
                            && locatorHitbox.getRightEdge() <= x2
                            && locatorHitbox.getTopEdge() >= y1
                            && locatorHitbox.getBottomEdge() <= y2) {
                        object.newState = null;
                        objectChanges.add(new ObjectChangeData(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjectList();
    }
    
    /**
     * Removes from this LevelState all of its LevelObjects that exist entirely
     * outside the specified rectangular region.
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     */
    public final void removeOutsideRectangle(double x1, double y1, double x2, double y2) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        for (Cell cell : cells.values()) {
            if (cell.left < x1 || cell.right > x2 || cell.top < y1 || cell.bottom > y2) {
                for (Hitbox<T> locatorHitbox : cell.locatorHitboxes) {
                    if (!locatorHitbox.scanned) {
                        LevelObject<T> object = locatorHitbox.getObject();
                        if (object.newState == this
                                && (locatorHitbox.getLeftEdge() >= x2
                                || locatorHitbox.getRightEdge() <= x1
                                || locatorHitbox.getTopEdge() >= y2
                                || locatorHitbox.getBottomEdge() <= y1)) {
                            object.newState = null;
                            objectChanges.add(new ObjectChangeData(object, null));
                        }
                        locatorHitbox.scanned = true;
                    }
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjectList();
    }
    
    /**
     * Removes from this LevelState all of its LevelObjects that exist entirely
     * to the left of the specified vertical line.
     * @param x The line's x-coordinate
     */
    public final void removeLeftOfLine(double x) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(cellLeft, cellTop, (int)Math.ceil(x/cellWidth), cellBottom);
        while (iterator.hasNext()) {
            for (Hitbox<T> locatorHitbox : iterator.next().locatorHitboxes) {
                if (!locatorHitbox.scanned) {
                    LevelObject<T> object = locatorHitbox.getObject();
                    if (object.newState == this && locatorHitbox.getRightEdge() <= x) {
                        object.newState = null;
                        objectChanges.add(new ObjectChangeData(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjectList();
    }
    
    /**
     * Removes from this LevelState all of its LevelObjects that exist entirely
     * to the right of the specified vertical line.
     * @param x The line's x-coordinate
     */
    public final void removeRightOfLine(double x) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator((int)Math.floor(x/cellWidth), cellTop, cellRight, cellBottom);
        while (iterator.hasNext()) {
            for (Hitbox<T> locatorHitbox : iterator.next().locatorHitboxes) {
                if (!locatorHitbox.scanned) {
                    LevelObject<T> object = locatorHitbox.getObject();
                    if (object.newState == this && locatorHitbox.getLeftEdge() >= x) {
                        object.newState = null;
                        objectChanges.add(new ObjectChangeData(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjectList();
    }
    
    /**
     * Removes from this LevelState all of its LevelObjects that exist entirely
     * above the specified horizontal line.
     * @param y The line's y-coordinate
     */
    public final void removeAboveLine(double y) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(cellLeft, cellTop, cellRight, (int)Math.ceil(y/cellHeight));
        while (iterator.hasNext()) {
            for (Hitbox<T> locatorHitbox : iterator.next().locatorHitboxes) {
                if (!locatorHitbox.scanned) {
                    LevelObject<T> object = locatorHitbox.getObject();
                    if (object.newState == this && locatorHitbox.getBottomEdge() <= y) {
                        object.newState = null;
                        objectChanges.add(new ObjectChangeData(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjectList();
    }
    
    /**
     * Removes from this LevelState all of its LevelObjects that exist entirely
     * below the specified horizontal line.
     * @param y The line's y-coordinate
     */
    public final void removeBelowLine(double y) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(cellLeft, (int)Math.floor(y/cellHeight), cellRight, cellBottom);
        while (iterator.hasNext()) {
            for (Hitbox<T> locatorHitbox : iterator.next().locatorHitboxes) {
                if (!locatorHitbox.scanned) {
                    LevelObject<T> object = locatorHitbox.getObject();
                    if (object.newState == this && locatorHitbox.getTopEdge() >= y) {
                        object.newState = null;
                        objectChanges.add(new ObjectChangeData(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjectList();
    }
    
    /**
     * Returns the number of ThinkerObjects that are currently assigned to this
     * LevelState.
     * @return The number of ThinkerObjects that are currently assigned to this
     * LevelState
     */
    public final int getNumThinkerObjects() {
        return thinkerObjects.size();
    }
    
    private class ThinkerObjectIterator implements SafeIterator<ThinkerObject<T>> {
        
        private boolean stopped = false;
        private final Iterator<ThinkerObject<T>> iterator = thinkerObjects.iterator();
        private ThinkerObject lastObject = null;
        
        private ThinkerObjectIterator() {
            thinkerObjectIterators++;
        }
        
        @Override
        public final boolean hasNext() {
            if (stopped) {
                return false;
            }
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                stop();
            }
            return hasNext;
        }
        
        @Override
        public final ThinkerObject next() {
            if (stopped) {
                return null;
            }
            lastObject = iterator.next();
            return lastObject;
        }
        
        @Override
        public final void remove() {
            if (!stopped && lastObject != null) {
                removeObject(lastObject);
                lastObject = null;
            }
        }
        
        @Override
        public final void stop() {
            if (!stopped) {
                stopped = true;
                thinkerObjectIterators--;
                updateThinkerObjectList();
            }
        }
        
    }
    
    /**
     * Returns whether any Iterators over this LevelState's list of
     * ThinkerObjects are currently in progress.
     * @return Whether any Iterators over this LevelState's list of
     * ThinkerObjects are currently in progress
     */
    public final boolean iteratingThroughThinkerObjects() {
        return thinkerObjectIterators > 0;
    }
    
    /**
     * Returns a new Iterator over this LevelState's list of ThinkerObjects.
     * @return A new Iterator over this LevelState's list of ThinkerObjects
     */
    public final SafeIterator<ThinkerObject<T>> thinkerObjectIterator() {
        return new ThinkerObjectIterator();
    }
    
    private static class ThinkerObjectChangeData<T extends CellGame> {
        
        private boolean used = false;
        private final boolean changePriority;
        private final ThinkerObject<T> object;
        private final boolean add;
        private final int movementPriority;
        
        private ThinkerObjectChangeData(ThinkerObject<T> object, boolean add) {
            changePriority = false;
            this.object = object;
            this.add = add;
            movementPriority = 0;
        }
        
        private ThinkerObjectChangeData(ThinkerObject<T> object, int movementPriority) {
            changePriority = true;
            this.object = object;
            add = false;
            this.movementPriority = movementPriority;
        }
        
    }
    
    final void addThinkerObject(ThinkerObject<T> object) {
        thinkerObjectChanges.add(new ThinkerObjectChangeData<>(object, true));
        updateThinkerObjectList();
    }
    
    final void removeThinkerObject(ThinkerObject<T> object) {
        thinkerObjectChanges.add(new ThinkerObjectChangeData<>(object, false));
        updateThinkerObjectList();
    }
    
    final void changeThinkerObjectMovementPriority(ThinkerObject<T> object, int movementPriority) {
        thinkerObjectChanges.add(new ThinkerObjectChangeData<>(object, movementPriority));
        updateThinkerObjectList();
    }
    
    private void updateThinkerObjectList() {
        if (thinkerObjectIterators == 0) {
            while (!thinkerObjectChanges.isEmpty()) {
                ThinkerObjectChangeData<T> data = thinkerObjectChanges.remove();
                if (!data.used) {
                    data.used = true;
                    if (data.changePriority) {
                        if (data.object.state == null) {
                            data.object.movementPriority = data.movementPriority;
                        } else {
                            thinkerObjects.remove(data.object);
                            data.object.movementPriority = data.movementPriority;
                            thinkerObjects.add(data.object);
                        }
                    } else if (data.add) {
                        thinkerObjects.add(data.object);
                    } else {
                        thinkerObjects.remove(data.object);
                    }
                }
            }
            updateObjectList();
        }
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class whose center
     * is nearest to the specified point, or null if this LevelState has no
     * LevelObjects of that class.
     * @param <O> The subclass of LevelObject to search for
     * @param point The point to check distance to
     * @param cls The Class object that represents the LevelObject subclass
     * @return The nearest LevelObject of the specified class to the specified
     * point
     */
    public final <O extends LevelObject<T>> O nearestObject(LevelVector point, Class<O> cls) {
        return nearestObject(point.getX(), point.getY(), cls);
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class whose center
     * is nearest to the specified point, or null if this LevelState has no
     * LevelObjects of that class.
     * @param <O> The subclass of LevelObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param cls The Class object that represents the LevelObject subclass
     * @return The nearest LevelObject of the specified class to the specified
     * point
     */
    public final <O extends LevelObject<T>> O nearestObject(double pointX, double pointY, Class<O> cls) {
        O nearest = null;
        double nearestDistance = -1;
        for (LevelObject<T> object : (ThinkerObject.class.isAssignableFrom(cls) ? thinkerObjects : levelObjects)) {
            if (cls.isAssignableFrom(object.getClass())) {
                double distance = LevelVector.distanceBetween(pointX, pointY, object.getCenterX(), object.getCenterY());
                if (nearestDistance < 0 || distance < nearestDistance) {
                    nearest = cls.cast(object);
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }
    
    /**
     * Returns whether this LevelState has any LevelObjects of the specified
     * class with their centers within the specified rectangular region.
     * @param <O> The subclass of LevelObject to search for
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the LevelObject subclass
     * @return Whether there are any LevelObjects of the specified class within
     * the specified rectangular region
     */
    public final <O extends LevelObject<T>> boolean objectIsWithinRectangle(double x1, double y1, double x2, double y2, Class<O> cls) {
        return objectWithinRectangle(x1, y1, x2, y2, cls) != null;
    }
    
    /**
     * Returns one of this LevelState's LevelObjects of the specified class with
     * its center within the specified rectangular region, or null if there is
     * none.
     * @param <O> The subclass of LevelObject to search for
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the LevelObject subclass
     * @return A LevelObject of the specified class within the specified
     * rectangular region
     */
    public final <O extends LevelObject<T>> O objectWithinRectangle(double x1, double y1, double x2, double y2, Class<O> cls) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox<T> centerHitbox : iterator.next().centerHitboxes) {
                if (!centerHitbox.scanned) {
                    if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                            && centerHitbox.getAbsX() >= x1
                            && centerHitbox.getAbsY() >= y1
                            && centerHitbox.getAbsX() <= x2
                            && centerHitbox.getAbsY() <= y2) {
                        for (Hitbox<T> hitbox : scanned) {
                            hitbox.scanned = false;
                        }
                        return cls.cast(centerHitbox.getObject());
                    }
                    centerHitbox.scanned = true;
                    scanned.add(centerHitbox);
                }
            }
        }
        for (Hitbox<T> hitbox : scanned) {
            hitbox.scanned = false;
        }
        return null;
    }
    
    /**
     * Returns all of this LevelState's LevelObjects of the specified class with
     * their centers within the specified rectangular region.
     * @param <O> The subclass of LevelObject to search for
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class within the
     * specified rectangular region
     */
    public final <O extends LevelObject<T>> List<O> objectsWithinRectangle(double x1, double y1, double x2, double y2, Class<O> cls) {
        List<O> within = new ArrayList<>();
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox<T> centerHitbox : iterator.next().centerHitboxes) {
                if (!centerHitbox.scanned) {
                    if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                            && centerHitbox.getAbsX() >= x1
                            && centerHitbox.getAbsY() >= y1
                            && centerHitbox.getAbsX() <= x2
                            && centerHitbox.getAbsY() <= y2) {
                        within.add(cls.cast(centerHitbox.getObject()));
                    }
                    centerHitbox.scanned = true;
                    scanned.add(centerHitbox);
                }
            }
        }
        for (Hitbox<T> hitbox : scanned) {
            hitbox.scanned = false;
        }
        return within;
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class within the
     * specified rectangular region whose center is nearest to the specified
     * point, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param point The point to check the distance to
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class within the specified
     * rectangular region that is nearest to the specified point
     */
    public final <O extends LevelObject<T>> O nearestObjectWithinRectangle(LevelVector point, double x1, double y1, double x2, double y2, Class<O> cls) {
        return nearestObjectWithinRectangle(point.getX(), point.getY(), x1, y1, x2, y2, cls);
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class within the
     * specified rectangular region whose center is nearest to the specified
     * point, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class within the specified
     * rectangular region that is nearest to the specified point
     */
    public final <O extends LevelObject<T>> O nearestObjectWithinRectangle(double pointX, double pointY, double x1, double y1, double x2, double y2, Class<O> cls) {
        O nearest = null;
        double nearestDistance = -1;
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox<T> centerHitbox : iterator.next().centerHitboxes) {
                if (!centerHitbox.scanned) {
                    LevelObject<T> object = centerHitbox.getObject();
                    if (cls.isAssignableFrom(object.getClass())
                            && centerHitbox.getAbsX() >= x1
                            && centerHitbox.getAbsY() >= y1
                            && centerHitbox.getAbsX() <= x2
                            && centerHitbox.getAbsY() <= y2) {
                        double distance = LevelVector.distanceBetween(pointX, pointY, centerHitbox.getAbsX(), centerHitbox.getAbsY());
                        if (nearestDistance < 0 || distance < nearestDistance) {
                            nearest = cls.cast(object);
                            nearestDistance = distance;
                        }
                    }
                    centerHitbox.scanned = true;
                    scanned.add(centerHitbox);
                }
            }
        }
        for (Hitbox<T> hitbox : scanned) {
            hitbox.scanned = false;
        }
        return nearest;
    }
    
    private static boolean circleMeetsOrthogonalLine(double cu, double cv, double radius, double u1, double u2, double v) {
        v -= cv;
        if (Math.abs(v) <= radius) {
            double rangeRadius = Math.sqrt(radius*radius - v*v);
            return u1 <= cu + rangeRadius && u2 >= cu - rangeRadius;
        }
        return false;
    }
    
    private static boolean circleMeetsRectangle(double cx, double cy, double radius, double x1, double y1, double x2, double y2) {
        if (cx >= x1 && cx <= x2 && cy >= y1 && cy <= y2) {
            return true;
        }
        return circleMeetsOrthogonalLine(cx, cy, radius, x1, x2, y1)
                || circleMeetsOrthogonalLine(cx, cy, radius, x1, x2, y2)
                || circleMeetsOrthogonalLine(cy, cx, radius, y1, y2, x1)
                || circleMeetsOrthogonalLine(cy, cx, radius, y1, y2, x2);
    }
    
    /**
     * Returns whether this LevelState has any LevelObjects of the specified
     * class with their centers within the specified circular region.
     * @param <O> The subclass of LevelObject to search for
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return Whether there are any LevelObjects of the specified class within
     * the specified circular region
     */
    public final <O extends LevelObject<T>> boolean objectIsWithinCircle(LevelVector center, double radius, Class<O> cls) {
        return objectWithinCircle(center.getX(), center.getY(), radius, cls) != null;
    }
    
    /**
     * Returns whether this LevelState has any LevelObjects of the specified
     * class with their centers within the specified circular region.
     * @param <O> The subclass of LevelObject to search for
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return Whether there are any LevelObjects of the specified class within
     * the specified circular region
     */
    public final <O extends LevelObject<T>> boolean objectIsWithinCircle(double centerX, double centerY, double radius, Class<O> cls) {
        return objectWithinCircle(centerX, centerY, radius, cls) != null;
    }
    
    /**
     * Returns one of this LevelState's LevelObjects of the specified class with
     * its center within the specified circular region, or null if there is
     * none.
     * @param <O> The subclass of LevelObject to search for
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return A LevelObject of the specified class within the specified
     * circular region
     */
    public final <O extends LevelObject<T>> O objectWithinCircle(LevelVector center, double radius, Class<O> cls) {
        return objectWithinCircle(center.getX(), center.getY(), radius, cls);
    }
    
    /**
     * Returns one of this LevelState's LevelObjects of the specified class with
     * its center within the specified circular region, or null if there is
     * none.
     * @param <O> The subclass of LevelObject to search for
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return A LevelObject of the specified class within the specified
     * circular region
     */
    public final <O extends LevelObject<T>> O objectWithinCircle(double centerX, double centerY, double radius, Class<O> cls) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox<T> centerHitbox : cell.centerHitboxes) {
                    if (!centerHitbox.scanned) {
                        if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                                && LevelVector.distanceBetween(centerX, centerY, centerHitbox.getAbsX(), centerHitbox.getAbsY()) <= radius) {
                            for (Hitbox<T> hitbox : scanned) {
                                hitbox.scanned = false;
                            }
                            return cls.cast(centerHitbox.getObject());
                        }
                        centerHitbox.scanned = true;
                        scanned.add(centerHitbox);
                    }
                }
            }
        }
        for (Hitbox<T> hitbox : scanned) {
            hitbox.scanned = false;
        }
        return null;
    }
    
    /**
     * Returns all of this LevelState's LevelObjects of the specified class with
     * their centers within the specified circular region.
     * @param <O> The subclass of LevelObject to search for
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class within the
     * specified circular region
     */
    public final <O extends LevelObject<T>> List<O> objectsWithinCircle(LevelVector center, double radius, Class<O> cls) {
        return objectsWithinCircle(center.getX(), center.getY(), radius, cls);
    }
    
    /**
     * Returns all of this LevelState's LevelObjects of the specified class with
     * their centers within the specified circular region.
     * @param <O> The subclass of LevelObject to search for
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class within the
     * specified circular region
     */
    public final <O extends LevelObject<T>> List<O> objectsWithinCircle(double centerX, double centerY, double radius, Class<O> cls) {
        List<O> within = new ArrayList<>();
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox<T> centerHitbox : cell.centerHitboxes) {
                    if (!centerHitbox.scanned) {
                        if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                                && LevelVector.distanceBetween(centerX, centerY, centerHitbox.getAbsX(), centerHitbox.getAbsY()) <= radius) {
                            within.add(cls.cast(centerHitbox.getObject()));
                        }
                        centerHitbox.scanned = true;
                        scanned.add(centerHitbox);
                    }
                }
            }
        }
        for (Hitbox<T> hitbox : scanned) {
            hitbox.scanned = false;
        }
        return within;
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class within the
     * specified circular region whose center is nearest to the specified point,
     * or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param point The point to check the distance to
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class within the specified
     * circular region that is nearest to the specified point
     */
    public final <O extends LevelObject<T>> O nearestObjectWithinCircle(LevelVector point, LevelVector center, double radius, Class<O> cls) {
        return nearestObjectWithinCircle(point.getX(), point.getY(), center.getX(), center.getY(), radius, cls);
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class within the
     * specified circular region whose center is nearest to the specified point,
     * or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class within the specified
     * circular region that is nearest to the specified point
     */
    public final <O extends LevelObject<T>> O nearestObjectWithinCircle(double pointX, double pointY, double centerX, double centerY, double radius, Class<O> cls) {
        O nearest = null;
        double nearestDistance = -1;
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox<T> centerHitbox : cell.centerHitboxes) {
                    if (!centerHitbox.scanned) {
                        LevelObject<T> object = centerHitbox.getObject();
                        if (cls.isAssignableFrom(object.getClass())
                                && LevelVector.distanceBetween(centerX, centerY, centerHitbox.getAbsX(), centerHitbox.getAbsY()) <= radius) {
                            double distance = LevelVector.distanceBetween(pointX, pointY, centerHitbox.getAbsX(), centerHitbox.getAbsY());
                            if (nearestDistance < 0 || distance < nearestDistance) {
                                nearest = cls.cast(object);
                                nearestDistance = distance;
                            }
                        }
                        centerHitbox.scanned = true;
                        scanned.add(centerHitbox);
                    }
                }
            }
        }
        for (Hitbox<T> hitbox : scanned) {
            hitbox.scanned = false;
        }
        return nearest;
    }
    
    /**
     * Returns whether this LevelState has any LevelObjects of the specified
     * class that overlap the specified Hitbox.
     * @param <O> The subclass of LevelObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return Whether there are any LevelObjects of the specified class that
     * overlap the specified Hitbox.
     */
    public final <O extends LevelObject<T>> boolean isOverlappingObject(Hitbox<T> hitbox, Class<O> cls) {
        return overlappingObject(hitbox, cls) != null;
    }
    
    /**
     * Returns one of this LevelState's LevelObjects of the specified class that
     * overlaps the specified Hitbox, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return A LevelObject of the specified class that overlaps the specified
     * Hitbox
     */
    public final <O extends LevelObject<T>> O overlappingObject(Hitbox<T> hitbox, Class<O> cls) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox<T> overlapHitbox : iterator.next().overlapHitboxes) {
                if (!overlapHitbox.scanned) {
                    if (cls.isAssignableFrom(overlapHitbox.getObject().getClass())
                            && Hitbox.overlap(hitbox, overlapHitbox)) {
                        for (Hitbox<T> scannedHitbox : scanned) {
                            scannedHitbox.scanned = false;
                        }
                        return cls.cast(overlapHitbox.getObject());
                    }
                    overlapHitbox.scanned = true;
                    scanned.add(overlapHitbox);
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return null;
    }
    
    /**
     * Returns all of this LevelState's LevelObjects of the specified class that
     * overlap the specified Hitbox.
     * @param <O> The subclass of LevelObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class that overlap the
     * specified Hitbox
     */
    public final <O extends LevelObject<T>> List<O> overlappingObjects(Hitbox<T> hitbox, Class<O> cls) {
        List<O> overlapping = new ArrayList<>();
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox<T> overlapHitbox : iterator.next().overlapHitboxes) {
                if (!overlapHitbox.scanned) {
                    if (cls.isAssignableFrom(overlapHitbox.getObject().getClass())
                            && Hitbox.overlap(hitbox, overlapHitbox)) {
                        overlapping.add(cls.cast(overlapHitbox.getObject()));
                    }
                    overlapHitbox.scanned = true;
                    scanned.add(overlapHitbox);
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return overlapping;
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class that
     * overlaps the specified Hitbox whose center is nearest to the specified
     * point, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param point The point to check the distance to
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class that overlaps the
     * specified Hitbox that is nearest to the specified point
     */
    public final <O extends LevelObject<T>> O nearestOverlappingObject(LevelVector point, Hitbox hitbox, Class<O> cls) {
        return nearestOverlappingObject(point.getX(), point.getY(), hitbox, cls);
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class that
     * overlaps the specified Hitbox whose center is nearest to the specified
     * point, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class that overlaps the
     * specified Hitbox that is nearest to the specified point
     */
    public final <O extends LevelObject<T>> O nearestOverlappingObject(double pointX, double pointY, Hitbox hitbox, Class<O> cls) {
        O nearest = null;
        double nearestDistance = -1;
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox<T> overlapHitbox : iterator.next().overlapHitboxes) {
                if (!overlapHitbox.scanned) {
                    LevelObject<T> object = overlapHitbox.getObject();
                    if (cls.isAssignableFrom(object.getClass())
                            && Hitbox.overlap(hitbox, overlapHitbox)) {
                        double distance = LevelVector.distanceBetween(pointX, pointY, object.getCenterX(), object.getCenterY());
                        if (nearestDistance < 0 || distance < nearestDistance) {
                            nearest = cls.cast(object);
                            nearestDistance = distance;
                        }
                    }
                    overlapHitbox.scanned = true;
                    scanned.add(overlapHitbox);
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return nearest;
    }
    
    /**
     * Returns all of this LevelState's LevelObjects of the specified class
     * whose overlap Hitboxes' rectangular bounding boxes touch or intersect the
     * specified Hitbox's rectangular bounding box.
     * @param <O> The subclass of LevelObject to search for
     * @param hitbox The Hitbox whose bounding box to check
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class whose overlap
     * Hitboxes' bounding boxes meet the specified Hitbox's bounding box
     */
    public final <O extends LevelObject<T>> List<O> boundingBoxesMeet(Hitbox<T> hitbox, Class<O> cls) {
        List<O> meeting = new ArrayList<>();
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox<T> overlapHitbox : iterator.next().overlapHitboxes) {
                if (!overlapHitbox.scanned) {
                    if (cls.isAssignableFrom(overlapHitbox.getObject().getClass())
                            && hitbox.getLeftEdge() <= overlapHitbox.getRightEdge()
                            && hitbox.getRightEdge() >= overlapHitbox.getLeftEdge()
                            && hitbox.getTopEdge() <= overlapHitbox.getBottomEdge()
                            && hitbox.getBottomEdge() >= overlapHitbox.getTopEdge()) {
                        meeting.add(cls.cast(overlapHitbox.getObject()));
                    }
                    overlapHitbox.scanned = true;
                    scanned.add(overlapHitbox);
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return meeting;
    }
    
    /**
     * Returns whether this LevelState has any LevelObjects of the specified
     * class whose solid Hitboxes overlap the specified Hitbox.
     * @param <O> The subclass of LevelObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return Whether there are any LevelObjects of the specified class whose
     * solid Hitboxes overlap the specified Hitbox
     */
    public final <O extends LevelObject<T>> boolean isIntersectingSolidObject(Hitbox<T> hitbox, Class<O> cls) {
        return intersectingSolidObject(hitbox, cls) != null;
    }
    
    /**
     * Returns one of this LevelState's LevelObjects of the specified class
     * whose solid Hitbox overlaps the specified Hitbox, or null if there is
     * none.
     * @param <O> The subclass of LevelObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return A LevelObject of the specified class whose solid Hitbox overlaps
     * the specified Hitbox
     */
    public final <O extends LevelObject<T>> O intersectingSolidObject(Hitbox<T> hitbox, Class<O> cls) {
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox<T> solidHitbox : iterator.next().solidHitboxes) {
                if (!solidHitbox.scanned) {
                    if (cls.isAssignableFrom(solidHitbox.getObject().getClass())
                            && Hitbox.overlap(hitbox, solidHitbox)) {
                        for (Hitbox<T> scannedHitbox : scanned) {
                            scannedHitbox.scanned = false;
                        }
                        return cls.cast(solidHitbox.getObject());
                    }
                    solidHitbox.scanned = true;
                    scanned.add(solidHitbox);
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return null;
    }
    
    /**
     * Returns all of this LevelState's LevelObjects of the specified class
     * whose solid Hitboxes overlap the specified Hitbox.
     * @param <O> The subclass of LevelObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return All of the LevelObjects of the specified class whose solid
     * Hitboxes overlap the specified Hitbox
     */
    public final <O extends LevelObject<T>> List<O> intersectingSolidObjects(Hitbox<T> hitbox, Class<O> cls) {
        List<O> intersecting = new ArrayList<>();
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox<T> solidHitbox : iterator.next().solidHitboxes) {
                if (!solidHitbox.scanned) {
                    if (cls.isAssignableFrom(solidHitbox.getObject().getClass())
                            && Hitbox.overlap(hitbox, solidHitbox)) {
                        intersecting.add(cls.cast(solidHitbox.getObject()));
                    }
                    solidHitbox.scanned = true;
                    scanned.add(solidHitbox);
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return intersecting;
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class whose solid
     * Hitbox overlaps the specified Hitbox whose center is nearest to the
     * specified point, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param point The point to check the distance to
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class whose solid Hitbox
     * overlaps the specified Hitbox that is nearest to the specified point
     */
    public final <O extends LevelObject<T>> O nearestIntersectingSolidObject(LevelVector point, Hitbox<T> hitbox, Class<O> cls) {
        return nearestIntersectingSolidObject(point.getX(), point.getY(), hitbox, cls);
    }
    
    /**
     * Returns this LevelState's LevelObject of the specified class whose solid
     * Hitbox overlaps the specified Hitbox whose center is nearest to the
     * specified point, or null if there is none.
     * @param <O> The subclass of LevelObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the LevelObject subclass
     * @return The LevelObject of the specified class whose solid Hitbox
     * overlaps the specified Hitbox that is nearest to the specified point
     */
    public final <O extends LevelObject<T>> O nearestIntersectingSolidObject(double pointX, double pointY, Hitbox<T> hitbox, Class<O> cls) {
        O nearest = null;
        double nearestDistance = -1;
        List<Hitbox<T>> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox<T> solidHitbox : iterator.next().solidHitboxes) {
                if (!solidHitbox.scanned) {
                    LevelObject<T> object = solidHitbox.getObject();
                    if (cls.isAssignableFrom(object.getClass())
                            && Hitbox.overlap(hitbox, solidHitbox)) {
                        double distance = LevelVector.distanceBetween(pointX, pointY, object.getCenterX(), object.getCenterY());
                        if (nearestDistance < 0 || distance < nearestDistance) {
                            nearest = cls.cast(object);
                            nearestDistance = distance;
                        }
                    }
                    solidHitbox.scanned = true;
                    scanned.add(solidHitbox);
                }
            }
        }
        for (Hitbox<T> scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return nearest;
    }
    
    /**
     * Returns the number of LevelLayers that are currently assigned to this
     * LevelState.
     * @return The number of LevelLayers that are currently assigned to this
     * LevelState
     */
    public final int getNumLayers() {
        return levelLayers.size();
    }
    
    /**
     * Returns the LevelLayer that is assigned to this LevelState with the
     * specified ID.
     * @param id The ID of the LevelLayer to be returned
     * @return The LevelLayer that is assigned to this LevelState with the
     * specified ID
     */
    public final LevelLayer<T> getLayer(int id) {
        return levelLayers.get(id);
    }
    
    /**
     * Sets the LevelLayer that is assigned to this LevelState with the
     * specified ID to the specified LevelLayer, if it is not already assigned
     * to a LevelState. If there is already a LevelLayer assigned with the
     * specified ID, it will be removed from this LevelState. If the specified
     * LevelLayer is null, the LevelLayer with the specified ID will be removed
     * if there is one, but it will not be replaced with anything.
     * @param id The ID with which to assign the specified LevelLayer
     * @param layer The LevelLayer to add with the specified ID
     * @return Whether the addition occurred
     */
    public final boolean setLayer(int id, LevelLayer<T> layer) {
        if (id == 0) {
            throw new RuntimeException("Attempted to set a LevelLayer with an ID of 0");
        }
        if (layer == null) {
            LevelLayer<T> oldLayer = levelLayers.get(id);
            if (oldLayer != null) {
                removeThinker(oldLayer);
                levelLayers.remove(id);
                return true;
            }
            return false;
        }
        if (addThinker(layer)) {
            LevelLayer<T> oldLayer = levelLayers.get(id);
            if (oldLayer != null) {
                removeThinker(oldLayer);
            }
            levelLayers.put(id, layer);
            return true;
        }
        return false;
    }
    
    /**
     * Removes from this LevelState all LevelLayers that are currently assigned
     * to it.
     */
    public final void clearLayers() {
        for (LevelLayer<T> layer : levelLayers.values()) {
            removeThinker(layer);
        }
        levelLayers.clear();
    }
    
    /**
     * Returns the HUD that is currently assigned to this LevelState, or null if
     * there is none.
     * @return This LevelState's HUD
     */
    public final HUD<T> getHUD() {
        return hud;
    }
    
    /**
     * Sets the HUD that is assigned to this LevelState to the specified HUD, if
     * it is not already assigned to a LevelState. If there is already an HUD
     * assigned to this LevelState, it will be removed. If the specified HUD is
     * null, the current HUD will be removed if there is one, but it will not be
     * replaced with anything.
     * @param hud The HUD to add
     * @return Whether the addition occurred
     */
    public final boolean setHUD(HUD<T> hud) {
        if (hud == null || addThinker(hud)) {
            if (this.hud != null) {
                removeThinker(this.hud);
            }
            this.hud = hud;
            return true;
        }
        return false;
    }
    
    /**
     * Returns the number of Viewports that are currently assigned to this
     * LevelState.
     * @return The number of Viewports that are currently assigned to this
     * LevelState
     */
    public final int getNumViewports() {
        return viewports.size();
    }
    
    /**
     * Returns the Viewport that is assigned to this LevelState with the
     * specified ID.
     * @param id The ID of the Viewport to be returned
     * @return The Viewport that is assigned to this LevelState with the
     * specified ID
     */
    public final Viewport<T> getViewport(int id) {
        return viewports.get(id);
    }
    
    /**
     * Sets the Viewport that is assigned to this LevelState with the specified
     * ID to the specified Viewport, if it is not already assigned to a
     * LevelState. If there is already a Viewport assigned with the specified
     * ID, it will be removed from this LevelState. If the specified Viewport is
     * null, the Viewport with the specified ID will be removed if there is one,
     * but it will not be replaced with anything.
     * @param id The ID with which to assign the specified Viewport
     * @param viewport The Viewport to add with the specified ID
     * @return Whether the addition occurred
     */
    public final boolean setViewport(int id, Viewport<T> viewport) {
        if (viewport == null) {
            Viewport<T> oldViewport = viewports.get(id);
            if (oldViewport != null) {
                removeThinker(oldViewport);
                viewports.remove(id);
                return true;
            }
            return false;
        }
        if (addThinker(viewport)) {
            Viewport<T> oldViewport = viewports.get(id);
            if (oldViewport != null) {
                removeThinker(oldViewport);
            }
            viewports.put(id, viewport);
            return true;
        }
        return false;
    }
    
    /**
     * Removes from this LevelState all Viewports that are currently assigned to
     * it.
     */
    public final void clearViewports() {
        for (Viewport<T> viewport : viewports.values()) {
            removeThinker(viewport);
        }
        viewports.clear();
    }
    
    /**
     * Returns whether any part of the specified rectangular region is visible
     * through any of this LevelState's Viewports.
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @return Whether the specified rectangular region is visible through any
     * of this LevelState's Viewports
     */
    public final boolean rectangleIsVisible(double x1, double y1, double x2, double y2) {
        x1 = Math.round(x1);
        y1 = Math.round(y1);
        x2 = Math.round(x2);
        y2 = Math.round(y2);
        for (Viewport viewport : viewports.values()) {
            if (viewport.getCamera() != null && viewport.getCamera().newState == this) {
                double centerX = Math.round(viewport.getCamera().getCenterX());
                double centerY = Math.round(viewport.getCamera().getCenterY());
                if (x1 < centerX + viewport.right && x2 > centerX + viewport.left
                        && y1 < centerY + viewport.bottom && y2 > centerY + viewport.top) {
                    return true;
                }
            }
        }
        return false;
    }
    
    final void move(ThinkerObject<T> object, double changeX, double changeY) {
        double left, right, top, bottom;
        if (changeX > 0) {
            left = 0;
            right = changeX;
        } else {
            left = changeX;
            right = 0;
        }
        if (changeY > 0) {
            top = 0;
            bottom = changeY;
        } else {
            top = changeY;
            bottom = 0;
        }
        if (object.hasCollision() && object.getCollisionHitbox() != null) {
            Hitbox collisionHitbox = object.getCollisionHitbox();
            double leftEdge = collisionHitbox.getLeftEdge() + left;
            double rightEdge = collisionHitbox.getRightEdge() + right;
            double topEdge = collisionHitbox.getTopEdge() + top;
            double bottomEdge = collisionHitbox.getBottomEdge() + bottom;
            Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(leftEdge, rightEdge, topEdge, bottomEdge));
            if (changeX > 0) {
                if (changeY > 0) {
                    bottom = changeY;
                } else if (changeY < 0) {
                    top = changeY;
                } else {
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.getSolidSurfaces(Direction.LEFT)) {
                            
                        }
                    }
                }
                right = changeX;
            } else if (changeX < 0) {
                if (changeY > 0) {
                    bottom = changeY;
                } else if (changeY < 0) {
                    top = changeY;
                } else {
                    
                }
                left = changeX;
            } else {
                if (changeY > 0) {
                    bottom = changeY;
                } else {
                    top = changeY;
                }
            }
        }
        Map<ThinkerObject<T>,LevelVector> objectsToMove = null;
        if (object.isSolid()) {
            Hitbox solidHitbox = object.getSolidHitbox();
            double leftEdge = solidHitbox.getLeftEdge() + left;
            double rightEdge = solidHitbox.getRightEdge() + right;
            double topEdge = solidHitbox.getTopEdge() + top;
            double bottomEdge = solidHitbox.getBottomEdge() + bottom;
            Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(leftEdge, rightEdge, topEdge, bottomEdge));
            if (changeX > 0) {
                if (changeY > 0) {
                    
                } else if (changeY < 0) {
                    
                } else {
                    
                }
            } else if (changeX < 0) {
                if (changeY > 0) {
                    
                } else if (changeY < 0) {
                    
                } else {
                    
                }
            } else {
                if (changeY > 0) {
                    
                } else {
                    
                }
            }
        }
        object.setPosition(object.getX() + changeX, object.getY() + changeY);
        if (objectsToMove == null) {
            for (ThinkerObject<T> follower : object.followers) {
                move(follower, changeX, changeY);
            }
        } else {
            for (Map.Entry<ThinkerObject<T>,LevelVector> movement : objectsToMove.entrySet()) {
                LevelVector change = movement.getValue();
                move(movement.getKey(), change.getX(), change.getY());
            }
        }
    }
    
    @Override
    public final void frameActions(T game) {
        if (getTimeFactor() > 0) {
            frameState = 1;
            Iterator<LevelThinker<T>> iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().beforeMovement(game, this);
            }
            for (ThinkerObject object : thinkerObjects) {
                object.collisions.clear();
                object.collisionDirections.clear();
                object.displacement.clear();
            }
            Iterator<ThinkerObject<T>> thinkerObjectIterator = thinkerObjectIterator();
            while (thinkerObjectIterator.hasNext()) {
                ThinkerObject object = thinkerObjectIterator.next();
                double objectTimeFactor = object.getEffectiveTimeFactor();
                double dx = objectTimeFactor*(object.getVelocityX() + object.getStepX());
                double dy = objectTimeFactor*(object.getVelocityY() + object.getStepY());
                object.setStep(0, 0);
                if (dx != 0 || dy != 0) {
                    move(object, dx, dy);
                }
            }
            frameState = 2;
            iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().afterMovement(game, this);
            }
            frameState = 0;
        }
    }
    
    private void draw(Graphics g, Hitbox<T> locatorHitbox,
            int left, int right, int top, int bottom, int xOffset, int yOffset) {
        if (locatorHitbox.getLeftEdge() < right
                && locatorHitbox.getRightEdge() > left
                && locatorHitbox.getTopEdge() < bottom
                && locatorHitbox.getBottomEdge() > top) {
            locatorHitbox.getObject().draw(g,
                    (int)Math.round(locatorHitbox.getAbsX()) + xOffset,
                    (int)Math.round(locatorHitbox.getAbsY()) + yOffset);
        }
    }
    
    @Override
    public void renderActions(T game, Graphics g, int x1, int y1, int x2, int y2) {
        g.clearWorldClip();
        for (Viewport viewport : viewports.values()) {
            if (viewport.roundX1 != viewport.roundX2 && viewport.roundY1 != viewport.roundY2) {
                int vx1 = x1 + viewport.roundX1;
                int vy1 = y1 + viewport.roundY1;
                int vx2 = x1 + viewport.roundX2;
                int vy2 = y1 + viewport.roundY2;
                g.setWorldClip(vx1, vy1, vx2 - vx1, vy2 - vy1);
                if (viewport.getCamera() != null && viewport.getCamera().newState == this) {
                    double cx = viewport.getCamera().getCenterX();
                    double cy = viewport.getCamera().getCenterY();
                    for (LevelLayer layer : levelLayers.headMap(0).values()) {
                        layer.renderActions(game, this, g, cx, cy, vx1, vy1, vx2, vy2);
                    }
                    int rx = (int)Math.round(cx);
                    int ry = (int)Math.round(cy);
                    int left = rx + viewport.left;
                    int right = rx + viewport.right;
                    int top = ry + viewport.top;
                    int bottom = ry + viewport.bottom;
                    int xOffset = vx1 - left;
                    int yOffset = vy1 - top;
                    int[] cellRange = getCellRangeExclusive(left, top, right, bottom);
                    if (drawMode == DrawMode.FLAT) {
                        if (cellRange[0] == cellRange[2] && cellRange[1] == cellRange[3]) {
                            for (Hitbox<T> locatorHitbox : getCell(new Point(cellRange[0], cellRange[1])).locatorHitboxes) {
                                draw(g, locatorHitbox, left, right, top, bottom, xOffset, yOffset);
                            }
                        } else {
                            List<Set<Hitbox<T>>> hitboxesList = new ArrayList<>((cellRange[2] - cellRange[0] + 1)*(cellRange[3] - cellRange[1] + 1));
                            Iterator<Cell> iterator = new ReadCellRangeIterator(cellRange);
                            while (iterator.hasNext()) {
                                hitboxesList.add(iterator.next().locatorHitboxes);
                            }
                            if (hitboxesList.size() == 2) {
                                Iterator<Hitbox<T>> iterator1 = hitboxesList.get(0).iterator();
                                Hitbox<T> hitbox1 = (iterator1.hasNext() ? iterator1.next() : null);
                                Iterator<Hitbox<T>> iterator2 = hitboxesList.get(1).iterator();
                                Hitbox<T> hitbox2 = (iterator2.hasNext() ? iterator2.next() : null); 
                                Hitbox<T> lastHitbox = null;
                                while (hitbox1 != null || hitbox2 != null) {
                                    if (hitbox1 == null) {
                                        do {
                                            draw(g, hitbox2, left, right, top, bottom, xOffset, yOffset);
                                            hitbox2 = (iterator2.hasNext() ? iterator2.next() : null); 
                                        } while (hitbox2 != null);
                                        break;
                                    } else if (hitbox2 == null) {
                                        do {
                                            draw(g, hitbox1, left, right, top, bottom, xOffset, yOffset);
                                            hitbox1 = (iterator1.hasNext() ? iterator1.next() : null); 
                                        } while (hitbox1 != null);
                                        break;
                                    } else {
                                        double comparison = drawPriorityComparator.compare(hitbox1, hitbox2);
                                        if (comparison > 0) {
                                            if (hitbox1 != lastHitbox) {
                                                draw(g, hitbox1, left, right, top, bottom, xOffset, yOffset);
                                                lastHitbox = hitbox1;
                                            }
                                            hitbox1 = (iterator1.hasNext() ? iterator1.next() : null);
                                        } else {
                                            if (hitbox2 != lastHitbox) {
                                                draw(g, hitbox2, left, right, top, bottom, xOffset, yOffset);
                                                lastHitbox = hitbox2;
                                            }
                                            hitbox2 = (iterator2.hasNext() ? iterator2.next() : null);
                                        }
                                    }
                                }
                            } else {
                                PriorityQueue<Pair<Hitbox<T>,Iterator<Hitbox<T>>>> queue = new PriorityQueue<>(drawPriorityIteratorComparator);
                                for (Set<Hitbox<T>> locatorHitboxes : hitboxesList) {
                                    if (!locatorHitboxes.isEmpty()) {
                                        Iterator<Hitbox<T>> hitboxIterator = locatorHitboxes.iterator();
                                        queue.add(new Pair<>(hitboxIterator.next(), hitboxIterator));
                                    }
                                }
                                Hitbox<T> lastHitbox = null;
                                while (!queue.isEmpty()) {
                                    Pair<Hitbox<T>,Iterator<Hitbox<T>>> pair = queue.poll();
                                    Hitbox<T> locatorHitbox = pair.getKey();
                                    if (locatorHitbox != lastHitbox) {
                                        draw(g, locatorHitbox, left, right, top, bottom, xOffset, yOffset);
                                        lastHitbox = locatorHitbox;
                                    }
                                    Iterator<Hitbox<T>> hitboxIterator = pair.getValue();
                                    if (hitboxIterator.hasNext()) {
                                        queue.add(new Pair<>(hitboxIterator.next(), hitboxIterator));
                                    }
                                }
                            }
                        }
                    } else {
                        SortedSet<Hitbox<T>> toDraw = new TreeSet<>(drawMode == DrawMode.OVER ? overModeComparator : underModeComparator);
                        Iterator<Cell> iterator = new ReadCellRangeIterator(cellRange);
                        while (iterator.hasNext()) {
                            for (Hitbox<T> locatorHitbox : iterator.next().locatorHitboxes) {
                                toDraw.add(locatorHitbox);
                            }
                        }
                        for (Hitbox<T> locatorHitbox : toDraw) {
                            draw(g, locatorHitbox, left, right, top, bottom, xOffset, yOffset);
                        }
                    }
                    for (LevelLayer layer : levelLayers.tailMap(0).values()) {
                        layer.renderActions(game, this, g, cx, cy, vx1, vy1, vx2, vy2);
                    }
                }
                if (viewport.getHUD() != null && viewport.getHUD().getGameState() == this) {
                    viewport.getHUD().renderActions(game, this, g, vx1, vy1, vx2, vy2);
                }
                g.clearWorldClip();
            }
        }
        g.setWorldClip(x1, y1, x2 - x1, y2 - y1);
        if (hud != null && hud.getGameState() == this) {
            hud.renderActions(game, this, g, x1, y1, x2, y2);
        }
    }
    
}
