package org.cell2d.space;

import java.awt.Point;
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
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.util.Pair;
import org.cell2d.CellGame;
import org.cell2d.CellVector;
import org.cell2d.Direction;
import org.cell2d.EventGroup;
import org.cell2d.Frac;
import org.cell2d.GameState;
import org.cell2d.SafeIterator;
import org.cell2d.celick.Graphics;

/**
 * <p>A SpaceState is a type of GameState that handles gameplay in a continuous
 * two-dimensional space. Space in a SpaceState is divided into rectangular
 * <i>cells</i> of equal width and equal height. A SpaceState automatically
 * creates more cells as SpaceObjects enter where they would be if they existed.
 * </p>
 * 
 * <p>SpaceObjects may be assigned to one SpaceState each in much the same way
 * that SubThinkers are assigned to Thinkers. Similarly to SubThinkers, the
 * actual addition or removal of a SpaceObject to or from a SpaceState is
 * delayed until any and all current iterations over its SpaceThinkers,
 * SpaceObjects, or MobileObjects, such as the periods during which
 * MobileObjects move or SpaceThinkers take their time-dependent actions, have
 * been completed. Multiple delayed instructions may be successfully given to
 * SpaceStates regarding the same SpaceObject without having to wait until all
 * iterations have finished.</p>
 * 
 * <p>SpaceStates use cells to organize SpaceObjects by location, improving the
 * efficiency of processes like MobileObject movement that are concerned only
 * with SpaceObjects in a small region of space. For maximum efficiency, cells
 * should be set to be large enough that SpaceObjects do not change which cells
 * they are in too frequently, but small enough that not too many SpaceObjects
 * are in each cell at any one time.</p>
 * 
 * <p>Like the SpaceThinkers that it uses, a SpaceState has an EventGroup of
 * <i>before-movement Events</i> that it performs once each frame. A SpaceState
 * performs these Events at the beginning of its frameActions(). Immediately
 * afterward, the SpaceState moves each of its MobileObjects by the sum of its
 * velocity and step multiplied by its time factor, then resets its step to (0,
 * 0). This, along with manual calls to the MobileObject's doMovement() method,
 * is when the MobileObject interacts with the solid surfaces of SpaceObjects in
 * its path if it has Cell2D's standard collision mechanics enabled.</p>
 * 
 * <p>Viewports may be assigned to one SpaceState each with an integer ID in
 * the context of that SpaceState. Only one Viewport may be assigned to a
 * given SpaceState with a given ID at once.</p>
 * 
 * <p>HUDs may be assigned to a SpaceState to render visuals in front of the
 * SpaceState's own. Only one HUD may be assigned to a given SpaceState in this
 * capacity at once. A SpaceState's HUD uses the entire screen as its rendering
 * region.</p>
 * 
 * <p>SpaceLayers may be assigned to a SpaceState with an integer ID in the
 * context of that SpaceState. Only one SpaceLayer may be assigned to a given
 * SpaceState with a given ID at once. SpaceLayers with higher IDs are rendered
 * in front of those with lower ones. SpaceLayers with positive IDs are rendered
 * in front of the SpaceState's SpaceObjects, and SpaceLayers with negative IDs
 * are rendered behind its SpaceObjects. SpaceLayers may not be assigned with an
 * ID of 0.</p>
 * @see SpaceThinker
 * @see SpaceObject
 * @see Viewport
 * @see HUD
 * @see SpaceLayer
 * @param <T> The type of CellGame that uses this SpaceState
 * @param <U> The type of SpaceState that this SpaceState is for SpaceThinker
 * interaction purposes
 * @param <V> The type of SpaceThinker that this SpaceState uses
 * @author Andrew Heyman
 */
public abstract class SpaceState<T extends CellGame,
        U extends SpaceState<T,U,V>, V extends SpaceThinker<T,U,V>> extends GameState<T,U,V> {
    
    private Comparator<MobileObject> movementPriorityComparator = (object1, object2) -> {
        int priorityDiff = object2.movementPriority - object1.movementPriority;
        return (priorityDiff == 0 ?
                System.identityHashCode(object1) - System.identityHashCode(object2) : priorityDiff);
    };
    
    private Comparator<MoveEvent> moveComparator = (event1, event2) -> {
        long metricDiff = event1.metric - event2.metric;
        if (metricDiff == 0) {
            int typeDiff = event1.type - event2.type;
            return (typeDiff == 0 ?
                    System.identityHashCode(event1) - System.identityHashCode(event2) : typeDiff);
        }
        return (int)Math.signum(metricDiff);        
    };
    
    private Comparator<Hitbox> drawPriorityComparator = (hitbox1, hitbox2) -> {
        int priorityDiff = hitbox1.drawPriority - hitbox2.drawPriority;
        return (priorityDiff == 0 ?
                System.identityHashCode(hitbox1) - System.identityHashCode(hitbox2) : priorityDiff);
    };
    
    private Comparator<Pair<Hitbox,Iterator<Hitbox>>> drawPriorityIteratorComparator = (pair1, pair2) -> {
        return drawPriorityComparator.compare(pair1.getKey(), pair2.getKey());
    };
    
    private Comparator<Hitbox> overModeComparator = (hitbox1, hitbox2) -> {
        int priorityDiff = hitbox1.drawPriority - hitbox2.drawPriority;
        if (priorityDiff == 0) {
            long yDiff = hitbox1.getRelY() - hitbox2.getRelY();
            return (yDiff == 0 ?
                    System.identityHashCode(hitbox1) - System.identityHashCode(hitbox2)
                    : (int)Math.signum(yDiff));
        }
        return priorityDiff;        
    };
    
    private Comparator<Hitbox> underModeComparator = (hitbox1, hitbox2) -> {
        int priorityDiff = hitbox1.drawPriority - hitbox2.drawPriority;
        if (priorityDiff == 0) {
            long yDiff = hitbox2.getRelY() - hitbox1.getRelY();
            return (yDiff == 0 ?
                    System.identityHashCode(hitbox1) - System.identityHashCode(hitbox2)
                    : (int)Math.signum(yDiff));
        }
        return priorityDiff;        
    };
    
    private final EventGroup<T,U> beforeMovementEvents = new EventGroup<>();
    private final Set<SpaceObject> spaceObjects = new HashSet<>();
    private int objectIterators = 0;
    private final Queue<ObjectChange> objectChanges = new LinkedList<>();
    private boolean updatingObjects = false;
    private final SortedSet<MobileObject> mobileObjects = new TreeSet<>(movementPriorityComparator);
    private int mobileObjectIterators = 0;
    private final Queue<MobileObjectChange> mobileObjectChanges = new LinkedList<>();
    private long cellWidth, cellHeight;
    private final Map<Point,Cell> cells = new HashMap<>();
    private int cellLeft = 0;
    private int cellRight = 0;
    private int cellTop = 0;
    private int cellBottom = 0;
    private DrawMode drawMode;
    private final Map<Integer,Viewport<T,U>> viewports = new HashMap<>();
    private HUD hud = null;
    private final SortedMap<Integer,SpaceLayer> spaceLayers = new TreeMap<>();
    
    /**
     * Constructs a SpaceState of the specified CellGame with the specified ID.
     * @param gameClass The Class object representing the type of CellGame that
     * uses this SpaceState
     * @param stateClass The Class object representing the type of SpaceState
     * that this SpaceState is for SpaceThinker interaction purposes
     * @param subThinkerClass The Class object representing the type of
     * SpaceThinker that this SpaceState uses
     * @param game The CellGame to which this SpaceState belongs
     * @param id This SpaceState's ID
     * @param cellWidth The width of each of this SpaceState's cells
     * @param cellHeight The height of each of this SpaceState's cells
     * @param drawMode This SpaceState's DrawMode
     */
    public SpaceState(Class<T> gameClass, Class<U> stateClass, Class<V> subThinkerClass,
            T game, int id, long cellWidth, long cellHeight, DrawMode drawMode) {
        super(gameClass, stateClass, subThinkerClass, game, id);
        setCellDimensions(cellWidth, cellHeight);
        setDrawMode(drawMode);
    }
    
    @Override
    public void addSubThinkerActions(T game, U state, V subThinker) {
        beforeMovementEvents.add(subThinker.beforeMovement, subThinker.getBeforeMovementPriority());
        subThinker.superBeforeMovementEvents = beforeMovementEvents;
    }
    
    @Override
    public void removeSubThinkerActions(T game, U state, V subThinker) {
        beforeMovementEvents.remove(subThinker.beforeMovement, subThinker.getBeforeMovementPriority());
        subThinker.superBeforeMovementEvents = null;
    }
    
    /**
     * Returns the EventGroup of this SpaceState's before-movement Events.
     * @return The EventGroup of this SpaceState's before-movement Events
     */
    public final EventGroup<T,U> getBeforeMovementEvents() {
        return beforeMovementEvents;
    }
    
    private class Cell {
        
        private int x, y;
        private long left, right, top, bottom;
        private final Map<HitboxRole,Set<Hitbox>> hitboxes = new EnumMap<>(HitboxRole.class);
        
        private Cell(int x, int y) {
            this.x = x;
            this.y = y;
            left = x*cellWidth;
            right = left + cellWidth;
            top = y*cellHeight;
            bottom = top + cellHeight;
            initializeLocatorHitboxes();
            for (HitboxRole role : EnumSet.complementOf(EnumSet.of(HitboxRole.LOCATOR))) {
                hitboxes.put(role, new HashSet<>());
            }
        }
        
        private void initializeLocatorHitboxes() {
            hitboxes.put(HitboxRole.LOCATOR, (drawMode == DrawMode.FLAT ?
                    new TreeSet<>(drawPriorityComparator) : new HashSet<>()));
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
    
    private int[] getCellRangeInclusive(long x1, long y1, long x2, long y2) {
        int[] cellRange = {(int)Math.ceil(x1/cellWidth) - 1, (int)Math.ceil(y1/cellHeight) - 1,
            (int)Math.floor(x2/cellWidth), (int)Math.floor(y2/cellHeight)};
        return cellRange;
    }
    
    private int[] getCellRangeInclusive(Hitbox hitbox) {
        return getCellRangeInclusive(hitbox.getLeftEdge(), hitbox.getTopEdge(),
                hitbox.getRightEdge(), hitbox.getBottomEdge());
    }
    
    private int[] getCellRangeExclusive(long x1, long y1, long x2, long y2) {
        int[] cellRange = {(int)Math.floor(x1/cellWidth), (int)Math.floor(y1/cellHeight),
            (int)Math.ceil(x2/cellWidth) - 1, (int)Math.ceil(y2/cellHeight) - 1};
        if (cellRange[0] == cellRange[2] + 1) {
            cellRange[0]--;
        }
        if (cellRange[1] == cellRange[3] + 1) {
            cellRange[1]--;
        }
        return cellRange;
    }
    
    private int[] getCellRangeExclusive(Hitbox hitbox) {
        return getCellRangeExclusive(hitbox.getLeftEdge(), hitbox.getTopEdge(),
                hitbox.getRightEdge(), hitbox.getBottomEdge());
    }
    
    private void updateCellRange(Hitbox hitbox) {
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
        public final boolean hasNext() {
            return nextCell != null;
        }
        
        @Override
        public final Cell next() {
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
        public final boolean hasNext() {
            return yPos <= cellRange[3];
        }
        
        @Override
        public final Cell next() {
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
        List<Cell> cellList = new ArrayList<>(
                (cellRange[2] - cellRange[0] + 1)*(cellRange[3] - cellRange[1] + 1));
        Iterator<Cell> iterator = new WriteCellRangeIterator(cellRange);
        while (iterator.hasNext()) {
            cellList.add(iterator.next());
        }
        return cellList;
    }
    
    /**
     * Returns the width of each of this SpaceState's cells.
     * @return The width of each of this SpaceState's cells
     */
    public final long getCellWidth() {
        return cellWidth;
    }
    
    /**
     * Returns the height of each of this SpaceState's cells.
     * @return The height of each of this SpaceState's cells
     */
    public final long getCellHeight() {
        return cellHeight;
    }
    
    /**
     * Sets the dimensions of each of this SpaceState's cells to the specified
     * values. The more SpaceObjects are currently assigned to this SpaceState,
     * the longer this operation takes, as SpaceObjects need to be reorganized.
     * @param cellWidth The new width of each of this SpaceState's cells
     * @param cellHeight The new height of each of this SpaceState's cells
     */
    public final void setCellDimensions(long cellWidth, long cellHeight) {
        if (cellWidth <= 0) {
            throw new RuntimeException("Attempted to give a SpaceState a non-positive cell width");
        }
        if (cellHeight <= 0) {
            throw new RuntimeException("Attempted to give a SpaceState a non-positive cell height");
        }
        cells.clear();
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        if (!spaceObjects.isEmpty()) {
            for (SpaceObject object : spaceObjects) {
                object.addCellData();
            }
        }
    }
    
    /**
     * Removes any cells that no longer have SpaceObjects in them, freeing up
     * the memory that they occupied. The more cells this SpaceState has, the
     * longer this operation takes.
     */
    public final void clearEmptyCells() {
        boolean firstCell = true;
        Iterator<Cell> iterator = cells.values().iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            boolean cellIsEmpty = true;
            for (HitboxRole role : HitboxRole.values()) {
                if (!cell.hitboxes.get(role).isEmpty()) {
                    cellIsEmpty = false;
                    break;
                }
            }
            if (cellIsEmpty) {
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
     * Returns this SpaceState's DrawMode.
     * @return This SpaceState's DrawMode
     */
    public final DrawMode getDrawMode() {
        return drawMode;
    }
    
    /**
     * Sets this SpaceState's DrawMode. The more cells this SpaceState has, the
     * longer this operation may take, as cells' records of SpaceObjects may
     * need to be reorganized.
     * @param drawMode The new DrawMode
     */
    public final void setDrawMode(DrawMode drawMode) {
        this.drawMode = drawMode;
        if (!cells.isEmpty()) {
            for (Cell cell : cells.values()) {
                Set<Hitbox> oldLocatorHitboxes = cell.hitboxes.get(HitboxRole.LOCATOR);
                cell.initializeLocatorHitboxes();
                Set<Hitbox> newLocatorHitboxes = cell.hitboxes.get(HitboxRole.LOCATOR);
                for (Hitbox hitbox : oldLocatorHitboxes) {
                    newLocatorHitboxes.add(hitbox);
                }
            }
        }
    }
    
    /**
     * Loads the specified Area about the specified origin point.
     * @param origin The origin point about which to load the Area
     * @param area The Area to load
     */
    public final void loadArea(CellVector origin, Area<T,U> area) {
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
    public final void loadArea(long originX, long originY, Area<T,U> area) {
        for (SpaceObject object : area.load(getGame(), getThis())) {
            if (object.state == null && object.newState == null) {
                object.changePosition(originX, originY);
                object.newState = this;
                objectChanges.add(new ObjectChange(object, this));
            }
        }
        updateObjects();
    }
    
    final void updateCells(Hitbox hitbox) {
        int[] oldRange = hitbox.cellRange;
        updateCellRange(hitbox);
        int[] newRange = hitbox.cellRange;
        if (oldRange[0] != newRange[0] || oldRange[1] != newRange[1]
                || oldRange[2] != newRange[2] || oldRange[3] != newRange[3]) {
            Iterator<Cell> iterator = new WriteCellRangeIterator(oldRange);
            while (iterator.hasNext()) {
                Cell cell = iterator.next();
                for (HitboxRole role : HitboxRole.values()) {
                    if (hitbox.roles.contains(role)) {
                        cell.hitboxes.get(role).remove(hitbox);
                    }
                }
            }
            iterator = new WriteCellRangeIterator(newRange);
            while (iterator.hasNext()) {
                Cell cell = iterator.next();
                for (HitboxRole role : HitboxRole.values()) {
                    if (hitbox.roles.contains(role)) {
                        cell.hitboxes.get(role).add(hitbox);
                    }
                }
            }
        }
    }
    
    final void addHitbox(Hitbox hitbox, HitboxRole role) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().hitboxes.get(role).add(hitbox);
        }
    }
    
    final void removeHitbox(Hitbox hitbox, HitboxRole role) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().hitboxes.get(role).remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    final void setLocatorHitboxDrawPriority(Hitbox hitbox, int drawPriority) {
        List<Cell> cellList = getCells(hitbox.cellRange);
        for (Cell cell : cellList) {
            cell.hitboxes.get(HitboxRole.LOCATOR).remove(hitbox);
        }
        hitbox.drawPriority = drawPriority;
        for (Cell cell : cellList) {
            cell.hitboxes.get(HitboxRole.LOCATOR).add(hitbox);
        }
    }
    
    /**
     * Returns the number of SpaceObjects that are assigned to this SpaceState.
     * @return The number of SpaceObjects that are assigned to this SpaceState
     */
    public final int getNumObjects() {
        return spaceObjects.size();
    }
    
    private class ObjectIterator implements SafeIterator<SpaceObject> {
        
        private boolean stopped = false;
        private final Iterator<SpaceObject> iterator = spaceObjects.iterator();
        private SpaceObject lastObject = null;
        
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
        public final SpaceObject next() {
            if (stopped) {
                throw new NoSuchElementException();
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
                updateObjects();
            }
        }
        
    }
    
    /**
     * Returns whether any Iterators over this SpaceState's list of SpaceObjects
     * are in progress.
     * @return Whether any Iterators over this SpaceState's list of SpaceObjects
     * are in progress
     */
    public final boolean iteratingThroughObjects() {
        return objectIterators > 0;
    }
    
    /**
     * Returns a new SafeIterator over this SpaceState's list of SpaceObjects.
     * @return A new SafeIterator over this SpaceState's list of SpaceObjects
     */
    public final SafeIterator<SpaceObject> objectIterator() {
        return new ObjectIterator();
    }
    
    private static class ObjectChange {
        
        private boolean made = false;
        private final SpaceObject object;
        private final SpaceState newState;
        
        private ObjectChange(SpaceObject object, SpaceState newState) {
            this.object = object;
            this.newState = newState;
        }
        
    }
    
    /**
     * Adds the specified SpaceObject to this SpaceState if it is not already
     * assigned to a SpaceState.
     * @param object The SpaceObject to be added
     * @return Whether the addition occurred
     */
    public final boolean addObject(SpaceObject object) {
        if (object.newState == null) {
            addObjectChange(object, this);
            return true;
        }
        return false;
    }
    
    /**
     * Removes the specified SpaceObject from this SpaceState if it is currently
     * assigned to it.
     * @param object The SpaceObject to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeObject(SpaceObject object) {
        if (object.newState == this) {
            addObjectChange(object, null);
            return true;
        }
        return false;
    }
    
    /**
     * Removes from this SpaceState all of the SpaceObjects that are currently
     * assigned to it.
     */
    public final void clearObjects() {
        for (SpaceObject object : spaceObjects) {
            if (object.newState == this) {
                object.newState = null;
                objectChanges.add(new ObjectChange(object, null));
            }
        }
        updateObjects();
    }
    
    /**
     * Removes from this SpaceState all of its SpaceObjects that exist entirely
     * inside the specified rectangular region.
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     */
    public final void removeRectangle(long x1, long y1, long x2, long y2) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox locatorHitbox : iterator.next().hitboxes.get(HitboxRole.LOCATOR)) {
                if (!locatorHitbox.scanned) {
                    SpaceObject object = locatorHitbox.getObject();
                    if (object.newState == this
                            && locatorHitbox.getLeftEdge() >= x1
                            && locatorHitbox.getRightEdge() <= x2
                            && locatorHitbox.getTopEdge() >= y1
                            && locatorHitbox.getBottomEdge() <= y2) {
                        object.newState = null;
                        objectChanges.add(new ObjectChange(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjects();
    }
    
    /**
     * Removes from this SpaceState all of its SpaceObjects that exist entirely
     * outside the specified rectangular region.
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     */
    public final void removeOutsideRectangle(long x1, long y1, long x2, long y2) {
        List<Hitbox> scanned = new ArrayList<>();
        for (Cell cell : cells.values()) {
            if (cell.left < x1 || cell.right > x2 || cell.top < y1 || cell.bottom > y2) {
                for (Hitbox locatorHitbox : cell.hitboxes.get(HitboxRole.LOCATOR)) {
                    if (!locatorHitbox.scanned) {
                        SpaceObject object = locatorHitbox.getObject();
                        if (object.newState == this
                                && (locatorHitbox.getLeftEdge() >= x2
                                || locatorHitbox.getRightEdge() <= x1
                                || locatorHitbox.getTopEdge() >= y2
                                || locatorHitbox.getBottomEdge() <= y1)) {
                            object.newState = null;
                            objectChanges.add(new ObjectChange(object, null));
                        }
                        locatorHitbox.scanned = true;
                    }
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjects();
    }
    
    /**
     * Removes from this SpaceState all of its SpaceObjects that exist entirely
     * to the left of the specified vertical line.
     * @param x The line's x-coordinate
     */
    public final void removeLeftOfLine(long x) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(
                cellLeft, cellTop, (int)Math.ceil(x/cellWidth), cellBottom);
        while (iterator.hasNext()) {
            for (Hitbox locatorHitbox : iterator.next().hitboxes.get(HitboxRole.LOCATOR)) {
                if (!locatorHitbox.scanned) {
                    SpaceObject object = locatorHitbox.getObject();
                    if (object.newState == this && locatorHitbox.getRightEdge() <= x) {
                        object.newState = null;
                        objectChanges.add(new ObjectChange(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjects();
    }
    
    /**
     * Removes from this SpaceState all of its SpaceObjects that exist entirely
     * to the right of the specified vertical line.
     * @param x The line's x-coordinate
     */
    public final void removeRightOfLine(long x) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(
                (int)Math.floor(x/cellWidth), cellTop, cellRight, cellBottom);
        while (iterator.hasNext()) {
            for (Hitbox locatorHitbox : iterator.next().hitboxes.get(HitboxRole.LOCATOR)) {
                if (!locatorHitbox.scanned) {
                    SpaceObject object = locatorHitbox.getObject();
                    if (object.newState == this && locatorHitbox.getLeftEdge() >= x) {
                        object.newState = null;
                        objectChanges.add(new ObjectChange(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjects();
    }
    
    /**
     * Removes from this SpaceState all of its SpaceObjects that exist entirely
     * above the specified horizontal line.
     * @param y The line's y-coordinate
     */
    public final void removeAboveLine(long y) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(
                cellLeft, cellTop, cellRight, (int)Math.ceil(y/cellHeight));
        while (iterator.hasNext()) {
            for (Hitbox locatorHitbox : iterator.next().hitboxes.get(HitboxRole.LOCATOR)) {
                if (!locatorHitbox.scanned) {
                    SpaceObject object = locatorHitbox.getObject();
                    if (object.newState == this && locatorHitbox.getBottomEdge() <= y) {
                        object.newState = null;
                        objectChanges.add(new ObjectChange(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjects();
    }
    
    /**
     * Removes from this SpaceState all of its SpaceObjects that exist entirely
     * below the specified horizontal line.
     * @param y The line's y-coordinate
     */
    public final void removeBelowLine(long y) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(
                cellLeft, (int)Math.floor(y/cellHeight), cellRight, cellBottom);
        while (iterator.hasNext()) {
            for (Hitbox locatorHitbox : iterator.next().hitboxes.get(HitboxRole.LOCATOR)) {
                if (!locatorHitbox.scanned) {
                    SpaceObject object = locatorHitbox.getObject();
                    if (object.newState == this && locatorHitbox.getTopEdge() >= y) {
                        object.newState = null;
                        objectChanges.add(new ObjectChange(object, null));
                    }
                    locatorHitbox.scanned = true;
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        updateObjects();
    }
    
    private void addObjectChange(SpaceObject object, SpaceState newState) {
        object.newState = newState;
        ObjectChange change = new ObjectChange(object, newState);
        if (object.state != null) {
            object.state.objectChanges.add(change);
            object.state.updateObjects();
        }
        if (newState != null) {
            newState.objectChanges.add(change);
            newState.updateObjects();
        }
    }
    
    private void add(SpaceObject object) {
        spaceObjects.add(object);
        object.game = getGame();
        object.state = this;
        object.addCellData();
        object.addNonCellData();
    }
    
    private void remove(SpaceObject object) {
        object.removeData();
        spaceObjects.remove(object);
        object.game = null;
        object.state = null;
    }
    
    private void updateObjects() {
        if (objectIterators == 0 && !updatingObjects) {
            updatingObjects = true;
            while (!objectChanges.isEmpty()) {
                ObjectChange change = objectChanges.remove();
                if (!change.made) {
                    change.made = true;
                    if (change.object.state != null) {
                        change.object.state.remove(change.object);
                    }
                    if (change.newState != null) {
                        change.newState.add(change.object);
                    }
                }
            }
            updatingObjects = false;
        }
    }
    
    /**
     * Returns the number of MobileObjects that are assigned to this SpaceState.
     * @return The number of MobileObjects that are assigned to this SpaceState
     */
    public final int getNumMobileObjects() {
        return mobileObjects.size();
    }
    
    private class MobileObjectIterator implements SafeIterator<MobileObject> {
        
        private boolean stopped = false;
        private final Iterator<MobileObject> iterator = mobileObjects.iterator();
        private MobileObject lastObject = null;
        
        private MobileObjectIterator() {
            mobileObjectIterators++;
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
        public final MobileObject next() {
            if (stopped) {
                throw new NoSuchElementException();
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
                mobileObjectIterators--;
                updateMobileObjects();
            }
        }
        
    }
    
    /**
     * Returns whether any Iterators over this SpaceState's list of
     * MobileObjects are in progress.
     * @return Whether any Iterators over this SpaceState's list of
     * MobileObjects are in progress
     */
    public final boolean iteratingThroughMobileObjects() {
        return mobileObjectIterators > 0;
    }
    
    /**
     * Returns a new SafeIterator over this SpaceState's list of MobileObjects.
     * @return A new SafeIterator over this SpaceState's list of MobileObjects
     */
    public final SafeIterator<MobileObject> mobileObjectIterator() {
        return new MobileObjectIterator();
    }
    
    private static class MobileObjectChange {
        
        private boolean made = false;
        private final boolean changePriority;
        private final MobileObject object;
        private final boolean add;
        private final int movementPriority;
        
        private MobileObjectChange(MobileObject object, boolean add) {
            changePriority = false;
            this.object = object;
            this.add = add;
            movementPriority = 0;
        }
        
        private MobileObjectChange(MobileObject object, int movementPriority) {
            changePriority = true;
            this.object = object;
            add = false;
            this.movementPriority = movementPriority;
        }
        
    }
    
    final void addMobileObject(MobileObject object) {
        mobileObjectChanges.add(new MobileObjectChange(object, true));
        updateMobileObjects();
    }
    
    final void removeMobileObject(MobileObject object) {
        mobileObjectChanges.add(new MobileObjectChange(object, false));
        updateMobileObjects();
    }
    
    final void changeMobileObjectMovementPriority(MobileObject object, int movementPriority) {
        mobileObjectChanges.add(new MobileObjectChange(object, movementPriority));
        updateMobileObjects();
    }
    
    private void updateMobileObjects() {
        if (mobileObjectIterators == 0) {
            while (!mobileObjectChanges.isEmpty()) {
                MobileObjectChange change = mobileObjectChanges.remove();
                if (!change.made) {
                    change.made = true;
                    if (change.changePriority) {
                        if (change.object.state == null) {
                            change.object.movementPriority = change.movementPriority;
                        } else {
                            mobileObjects.remove(change.object);
                            change.object.movementPriority = change.movementPriority;
                            mobileObjects.add(change.object);
                        }
                    } else if (change.add) {
                        mobileObjects.add(change.object);
                    } else {
                        mobileObjects.remove(change.object);
                    }
                }
            }
        }
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class whose center
     * is nearest to the specified point, or null if this SpaceState has no
     * SpaceObjects of that class.
     * @param <O> The subclass of SpaceObject to search for
     * @param point The point to check distance to
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The nearest SpaceObject of the specified class to the specified
     * point
     */
    public final <O extends SpaceObject> O nearestObject(CellVector point, Class<O> cls) {
        return nearestObject(point.getX(), point.getY(), cls);
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class whose center
     * is nearest to the specified point, or null if this SpaceState has no
     * SpaceObjects of that class.
     * @param <O> The subclass of SpaceObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The nearest SpaceObject of the specified class to the specified
     * point
     */
    public final <O extends SpaceObject> O nearestObject(long pointX, long pointY, Class<O> cls) {
        O nearest = null;
        long nearestDistance = -1;
        for (SpaceObject object :
                (MobileObject.class.isAssignableFrom(cls) ? mobileObjects : spaceObjects)) {
            if (cls.isAssignableFrom(object.getClass())) {
                long distance = CellVector.distanceBetween(
                        pointX, pointY, object.getCenterX(), object.getCenterY());
                if (nearestDistance < 0 || distance < nearestDistance) {
                    nearest = cls.cast(object);
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }
    
    /**
     * Returns whether this SpaceState has any SpaceObjects of the specified
     * class with their centers within the specified rectangular region.
     * @param <O> The subclass of SpaceObject to search for
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the SpaceObject subclass
     * @return Whether there are any SpaceObjects of the specified class within
     * the specified rectangular region
     */
    public final <O extends SpaceObject> boolean objectIsWithinRectangle(
            long x1, long y1, long x2, long y2, Class<O> cls) {
        return objectWithinRectangle(x1, y1, x2, y2, cls) != null;
    }
    
    /**
     * Returns one of this SpaceState's SpaceObjects of the specified class with
     * its center within the specified rectangular region, or null if there is
     * none.
     * @param <O> The subclass of SpaceObject to search for
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the SpaceObject subclass
     * @return A SpaceObject of the specified class within the specified
     * rectangular region
     */
    public final <O extends SpaceObject> O objectWithinRectangle(
            long x1, long y1, long x2, long y2, Class<O> cls) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox centerHitbox : iterator.next().hitboxes.get(HitboxRole.CENTER)) {
                if (!centerHitbox.scanned) {
                    if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                            && centerHitbox.getAbsX() >= x1
                            && centerHitbox.getAbsY() >= y1
                            && centerHitbox.getAbsX() <= x2
                            && centerHitbox.getAbsY() <= y2) {
                        for (Hitbox hitbox : scanned) {
                            hitbox.scanned = false;
                        }
                        return cls.cast(centerHitbox.getObject());
                    }
                    centerHitbox.scanned = true;
                    scanned.add(centerHitbox);
                }
            }
        }
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return null;
    }
    
    /**
     * Returns all of this SpaceState's SpaceObjects of the specified class with
     * their centers within the specified rectangular region.
     * @param <O> The subclass of SpaceObject to search for
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class within the
     * specified rectangular region
     */
    public final <O extends SpaceObject> List<O> objectsWithinRectangle(
            long x1, long y1, long x2, long y2, Class<O> cls) {
        List<O> within = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox centerHitbox : iterator.next().hitboxes.get(HitboxRole.CENTER)) {
                if (!centerHitbox.scanned) {
                    centerHitbox.scanned = true;
                    scanned.add(centerHitbox);
                    if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                            && centerHitbox.getAbsX() >= x1
                            && centerHitbox.getAbsY() >= y1
                            && centerHitbox.getAbsX() <= x2
                            && centerHitbox.getAbsY() <= y2) {
                        within.add(cls.cast(centerHitbox.getObject()));
                    }
                }
            }
        }
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return within;
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class within the
     * specified rectangular region whose center is nearest to the specified
     * point, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param point The point to check the distance to
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class within the specified
     * rectangular region that is nearest to the specified point
     */
    public final <O extends SpaceObject> O nearestObjectWithinRectangle(
            CellVector point, long x1, long y1, long x2, long y2, Class<O> cls) {
        return nearestObjectWithinRectangle(point.getX(), point.getY(), x1, y1, x2, y2, cls);
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class within the
     * specified rectangular region whose center is nearest to the specified
     * point, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class within the specified
     * rectangular region that is nearest to the specified point
     */
    public final <O extends SpaceObject> O nearestObjectWithinRectangle(
            long pointX, long pointY, long x1, long y1, long x2, long y2, Class<O> cls) {
        O nearest = null;
        long nearestDistance = -1;
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox centerHitbox : iterator.next().hitboxes.get(HitboxRole.CENTER)) {
                if (!centerHitbox.scanned) {
                    centerHitbox.scanned = true;
                    scanned.add(centerHitbox);
                    SpaceObject object = centerHitbox.getObject();
                    if (cls.isAssignableFrom(object.getClass())
                            && centerHitbox.getAbsX() >= x1
                            && centerHitbox.getAbsY() >= y1
                            && centerHitbox.getAbsX() <= x2
                            && centerHitbox.getAbsY() <= y2) {
                        long distance = CellVector.distanceBetween(
                                pointX, pointY, centerHitbox.getAbsX(), centerHitbox.getAbsY());
                        if (nearestDistance < 0 || distance < nearestDistance) {
                            nearest = cls.cast(object);
                            nearestDistance = distance;
                        }
                    }
                }
            }
        }
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return nearest;
    }
    
    private static boolean circleMeetsOrthogonalSeg(
            long cu, long cv, long radius, long u1, long u2, long v) {
        v -= cv;
        if (Math.abs(v) <= radius) {
            long rangeRadius = Frac.sqrt(Frac.mul(radius, radius) - Frac.mul(v, v));
            return u1 <= cu + rangeRadius && u2 >= cu - rangeRadius;
        }
        return false;
    }
    
    private static boolean circleMeetsRectangle(
            long cx, long cy, long radius, long x1, long y1, long x2, long y2) {
        if (cx >= x1 && cx <= x2 && cy >= y1 && cy <= y2) { //Circle's center is in rectangle
            return true;
        }
        //Any of rectangle's edges meet circle
        return circleMeetsOrthogonalSeg(cx, cy, radius, x1, x2, y1)
                || circleMeetsOrthogonalSeg(cx, cy, radius, x1, x2, y2)
                || circleMeetsOrthogonalSeg(cy, cx, radius, y1, y2, x1)
                || circleMeetsOrthogonalSeg(cy, cx, radius, y1, y2, x2);
    }
    
    /**
     * Returns whether this SpaceState has any SpaceObjects of the specified
     * class with their centers within the specified circular region.
     * @param <O> The subclass of SpaceObject to search for
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return Whether there are any SpaceObjects of the specified class within
     * the specified circular region
     */
    public final <O extends SpaceObject> boolean objectIsWithinCircle(
            CellVector center, long radius, Class<O> cls) {
        return objectWithinCircle(center.getX(), center.getY(), radius, cls) != null;
    }
    
    /**
     * Returns whether this SpaceState has any SpaceObjects of the specified
     * class with their centers within the specified circular region.
     * @param <O> The subclass of SpaceObject to search for
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return Whether there are any SpaceObjects of the specified class within
     * the specified circular region
     */
    public final <O extends SpaceObject> boolean objectIsWithinCircle(
            long centerX, long centerY, long radius, Class<O> cls) {
        return objectWithinCircle(centerX, centerY, radius, cls) != null;
    }
    
    /**
     * Returns one of this SpaceState's SpaceObjects of the specified class with
     * its center within the specified circular region, or null if there is
     * none.
     * @param <O> The subclass of SpaceObject to search for
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return A SpaceObject of the specified class within the specified
     * circular region
     */
    public final <O extends SpaceObject> O objectWithinCircle(
            CellVector center, long radius, Class<O> cls) {
        return objectWithinCircle(center.getX(), center.getY(), radius, cls);
    }
    
    /**
     * Returns one of this SpaceState's SpaceObjects of the specified class with
     * its center within the specified circular region, or null if there is
     * none.
     * @param <O> The subclass of SpaceObject to search for
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return A SpaceObject of the specified class within the specified
     * circular region
     */
    public final <O extends SpaceObject> O objectWithinCircle(
            long centerX, long centerY, long radius, Class<O> cls) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox centerHitbox : cell.hitboxes.get(HitboxRole.CENTER)) {
                    if (!centerHitbox.scanned) {
                        if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                                && CellVector.distanceBetween(centerX, centerY, centerHitbox.getAbsX(), centerHitbox.getAbsY()) <= radius) {
                            for (Hitbox hitbox : scanned) {
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
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return null;
    }
    
    /**
     * Returns all of this SpaceState's SpaceObjects of the specified class with
     * their centers within the specified circular region.
     * @param <O> The subclass of SpaceObject to search for
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class within the
     * specified circular region
     */
    public final <O extends SpaceObject> List<O> objectsWithinCircle(
            CellVector center, long radius, Class<O> cls) {
        return objectsWithinCircle(center.getX(), center.getY(), radius, cls);
    }
    
    /**
     * Returns all of this SpaceState's SpaceObjects of the specified class with
     * their centers within the specified circular region.
     * @param <O> The subclass of SpaceObject to search for
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class within the
     * specified circular region
     */
    public final <O extends SpaceObject> List<O> objectsWithinCircle(
            long centerX, long centerY, long radius, Class<O> cls) {
        List<O> within = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox centerHitbox : cell.hitboxes.get(HitboxRole.CENTER)) {
                    if (!centerHitbox.scanned) {
                        centerHitbox.scanned = true;
                        scanned.add(centerHitbox);
                        if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                                && CellVector.distanceBetween(centerX, centerY, centerHitbox.getAbsX(), centerHitbox.getAbsY()) <= radius) {
                            within.add(cls.cast(centerHitbox.getObject()));
                        }
                    }
                }
            }
        }
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return within;
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class within the
     * specified circular region whose center is nearest to the specified point,
     * or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param point The point to check the distance to
     * @param center The region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class within the specified
     * circular region that is nearest to the specified point
     */
    public final <O extends SpaceObject> O nearestObjectWithinCircle(
            CellVector point, CellVector center, long radius, Class<O> cls) {
        return nearestObjectWithinCircle(
                point.getX(), point.getY(), center.getX(), center.getY(), radius, cls);
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class within the
     * specified circular region whose center is nearest to the specified point,
     * or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param centerX The x-coordinate of the region's center
     * @param centerY The y-coordinate of the region's center
     * @param radius The region's radius
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class within the specified
     * circular region that is nearest to the specified point
     */
    public final <O extends SpaceObject> O nearestObjectWithinCircle(
            long pointX, long pointY, long centerX, long centerY, long radius, Class<O> cls) {
        O nearest = null;
        long nearestDistance = -1;
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox centerHitbox : cell.hitboxes.get(HitboxRole.CENTER)) {
                    if (!centerHitbox.scanned) {
                        centerHitbox.scanned = true;
                        scanned.add(centerHitbox);
                        SpaceObject object = centerHitbox.getObject();
                        if (cls.isAssignableFrom(object.getClass())
                                && CellVector.distanceBetween(centerX, centerY, centerHitbox.getAbsX(), centerHitbox.getAbsY()) <= radius) {
                            long distance = CellVector.distanceBetween(pointX, pointY, centerHitbox.getAbsX(), centerHitbox.getAbsY());
                            if (nearestDistance < 0 || distance < nearestDistance) {
                                nearest = cls.cast(object);
                                nearestDistance = distance;
                            }
                        }
                    }
                }
            }
        }
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return nearest;
    }
    
    /**
     * Returns whether this SpaceState has any SpaceObjects of the specified
     * class that overlap the specified Hitbox.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return Whether there are any SpaceObjects of the specified class that
     * overlap the specified Hitbox.
     */
    public final <O extends SpaceObject> boolean isOverlappingObject(Hitbox hitbox, Class<O> cls) {
        return overlappingObject(hitbox, cls) != null;
    }
    
    /**
     * Returns one of this SpaceState's SpaceObjects of the specified class that
     * overlaps the specified Hitbox, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return A SpaceObject of the specified class that overlaps the specified
     * Hitbox
     */
    public final <O extends SpaceObject> O overlappingObject(Hitbox hitbox, Class<O> cls) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox overlapHitbox : iterator.next().hitboxes.get(HitboxRole.OVERLAP)) {
                if (!overlapHitbox.scanned) {
                    if (cls.isAssignableFrom(overlapHitbox.getObject().getClass())
                            && Hitbox.overlap(hitbox, overlapHitbox)) {
                        for (Hitbox scannedHitbox : scanned) {
                            scannedHitbox.scanned = false;
                        }
                        return cls.cast(overlapHitbox.getObject());
                    }
                    overlapHitbox.scanned = true;
                    scanned.add(overlapHitbox);
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return null;
    }
    
    /**
     * Returns all of this SpaceState's SpaceObjects of the specified class that
     * overlap the specified Hitbox.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class that overlap the
     * specified Hitbox
     */
    public final <O extends SpaceObject> List<O> overlappingObjects(Hitbox hitbox, Class<O> cls) {
        List<O> overlapping = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox overlapHitbox : iterator.next().hitboxes.get(HitboxRole.OVERLAP)) {
                if (!overlapHitbox.scanned) {
                    overlapHitbox.scanned = true;
                    scanned.add(overlapHitbox);
                    if (cls.isAssignableFrom(overlapHitbox.getObject().getClass())
                            && Hitbox.overlap(hitbox, overlapHitbox)) {
                        overlapping.add(cls.cast(overlapHitbox.getObject()));
                    }
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return overlapping;
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class that
     * overlaps the specified Hitbox whose center is nearest to the specified
     * point, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param point The point to check the distance to
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class that overlaps the
     * specified Hitbox that is nearest to the specified point
     */
    public final <O extends SpaceObject> O nearestOverlappingObject(
            CellVector point, Hitbox hitbox, Class<O> cls) {
        return nearestOverlappingObject(point.getX(), point.getY(), hitbox, cls);
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class that
     * overlaps the specified Hitbox whose center is nearest to the specified
     * point, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class that overlaps the
     * specified Hitbox that is nearest to the specified point
     */
    public final <O extends SpaceObject> O nearestOverlappingObject(
            long pointX, long pointY, Hitbox hitbox, Class<O> cls) {
        O nearest = null;
        long nearestDistance = -1;
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox overlapHitbox : iterator.next().hitboxes.get(HitboxRole.OVERLAP)) {
                if (!overlapHitbox.scanned) {
                    overlapHitbox.scanned = true;
                    scanned.add(overlapHitbox);
                    SpaceObject object = overlapHitbox.getObject();
                    if (cls.isAssignableFrom(object.getClass())
                            && Hitbox.overlap(hitbox, overlapHitbox)) {
                        long distance = CellVector.distanceBetween(pointX, pointY, object.getCenterX(), object.getCenterY());
                        if (nearestDistance < 0 || distance < nearestDistance) {
                            nearest = cls.cast(object);
                            nearestDistance = distance;
                        }
                    }
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return nearest;
    }
    
    /**
     * Returns all of this SpaceState's SpaceObjects of the specified class
     * whose overlap Hitboxes' rectangular bounding boxes touch or intersect the
     * specified Hitbox's rectangular bounding box.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox whose bounding box to check
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class whose overlap
     * Hitboxes' bounding boxes meet the specified Hitbox's bounding box
     */
    public final <O extends SpaceObject> List<O> boundingBoxesMeet(Hitbox hitbox, Class<O> cls) {
        List<O> meeting = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox overlapHitbox : iterator.next().hitboxes.get(HitboxRole.OVERLAP)) {
                if (!overlapHitbox.scanned) {
                    overlapHitbox.scanned = true;
                    scanned.add(overlapHitbox);
                    if (cls.isAssignableFrom(overlapHitbox.getObject().getClass())
                            && hitbox.getLeftEdge() <= overlapHitbox.getRightEdge()
                            && hitbox.getRightEdge() >= overlapHitbox.getLeftEdge()
                            && hitbox.getTopEdge() <= overlapHitbox.getBottomEdge()
                            && hitbox.getBottomEdge() >= overlapHitbox.getTopEdge()) {
                        meeting.add(cls.cast(overlapHitbox.getObject()));
                    }
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return meeting;
    }
    
    /**
     * Returns whether this SpaceState has any SpaceObjects of the specified
     * class whose solid Hitboxes overlap the specified Hitbox.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return Whether there are any SpaceObjects of the specified class whose
     * solid Hitboxes overlap the specified Hitbox
     */
    public final <O extends SpaceObject> boolean isIntersectingSolidObject(Hitbox hitbox, Class<O> cls) {
        return intersectingSolidObject(hitbox, cls) != null;
    }
    
    /**
     * Returns one of this SpaceState's SpaceObjects of the specified class
     * whose solid Hitbox overlaps the specified Hitbox, or null if there is
     * none.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return A SpaceObject of the specified class whose solid Hitbox overlaps
     * the specified Hitbox
     */
    public final <O extends SpaceObject> O intersectingSolidObject(Hitbox hitbox, Class<O> cls) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox solidHitbox : iterator.next().hitboxes.get(HitboxRole.SOLID)) {
                if (!solidHitbox.scanned) {
                    if (cls.isAssignableFrom(solidHitbox.getObject().getClass())
                            && Hitbox.overlap(hitbox, solidHitbox)) {
                        for (Hitbox scannedHitbox : scanned) {
                            scannedHitbox.scanned = false;
                        }
                        return cls.cast(solidHitbox.getObject());
                    }
                    solidHitbox.scanned = true;
                    scanned.add(solidHitbox);
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return null;
    }
    
    /**
     * Returns all of this SpaceState's SpaceObjects of the specified class
     * whose solid Hitboxes overlap the specified Hitbox.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the SpaceObjects of the specified class whose solid
     * Hitboxes overlap the specified Hitbox
     */
    public final <O extends SpaceObject> List<O> intersectingSolidObjects(Hitbox hitbox, Class<O> cls) {
        List<O> intersecting = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox solidHitbox : iterator.next().hitboxes.get(HitboxRole.SOLID)) {
                if (!solidHitbox.scanned) {
                    solidHitbox.scanned = true;
                    scanned.add(solidHitbox);
                    if (cls.isAssignableFrom(solidHitbox.getObject().getClass())
                            && Hitbox.overlap(hitbox, solidHitbox)) {
                        intersecting.add(cls.cast(solidHitbox.getObject()));
                    }
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return intersecting;
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class whose solid
     * Hitbox overlaps the specified Hitbox whose center is nearest to the
     * specified point, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param point The point to check the distance to
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class whose solid Hitbox
     * overlaps the specified Hitbox that is nearest to the specified point
     */
    public final <O extends SpaceObject> O nearestIntersectingSolidObject(
            CellVector point, Hitbox hitbox, Class<O> cls) {
        return nearestIntersectingSolidObject(point.getX(), point.getY(), hitbox, cls);
    }
    
    /**
     * Returns this SpaceState's SpaceObject of the specified class whose solid
     * Hitbox overlaps the specified Hitbox whose center is nearest to the
     * specified point, or null if there is none.
     * @param <O> The subclass of SpaceObject to search for
     * @param pointX The x-coordinate of the point to check the distance to
     * @param pointY The y-coordinate of the point to check the distance to
     * @param hitbox The Hitbox to check for overlapping
     * @param cls The Class object that represents the SpaceObject subclass
     * @return The SpaceObject of the specified class whose solid Hitbox
     * overlaps the specified Hitbox that is nearest to the specified point
     */
    public final <O extends SpaceObject> O nearestIntersectingSolidObject(
            long pointX, long pointY, Hitbox hitbox, Class<O> cls) {
        O nearest = null;
        long nearestDistance = -1;
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox solidHitbox : iterator.next().hitboxes.get(HitboxRole.SOLID)) {
                if (!solidHitbox.scanned) {
                    solidHitbox.scanned = true;
                    scanned.add(solidHitbox);
                    SpaceObject object = solidHitbox.getObject();
                    if (cls.isAssignableFrom(object.getClass())
                            && Hitbox.overlap(hitbox, solidHitbox)) {
                        long distance = CellVector.distanceBetween(pointX, pointY, object.getCenterX(), object.getCenterY());
                        if (nearestDistance < 0 || distance < nearestDistance) {
                            nearest = cls.cast(object);
                            nearestDistance = distance;
                        }
                    }
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return nearest;
    }
    
    /**
     * Returns all of this SpaceState's solid SpaceObjects of the specified
     * class whose solid Hitboxes' rectangular bounding boxes touch or intersect
     * the specified Hitbox's rectangular bounding box.
     * @param <O> The subclass of SpaceObject to search for
     * @param hitbox The Hitbox whose bounding box to check
     * @param cls The Class object that represents the SpaceObject subclass
     * @return All of the solid SpaceObjects of the specified class whose solid
     * Hitboxes' bounding boxes meet the specified Hitbox's bounding box
     */
    public final <O extends SpaceObject> List<O> solidBoundingBoxesMeet(Hitbox hitbox, Class<O> cls) {
        List<O> meeting = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox solidHitbox : iterator.next().hitboxes.get(HitboxRole.SOLID)) {
                if (!solidHitbox.scanned) {
                    solidHitbox.scanned = true;
                    scanned.add(solidHitbox);
                    if (cls.isAssignableFrom(solidHitbox.getObject().getClass())
                            && hitbox.getLeftEdge() <= solidHitbox.getRightEdge()
                            && hitbox.getRightEdge() >= solidHitbox.getLeftEdge()
                            && hitbox.getTopEdge() <= solidHitbox.getBottomEdge()
                            && hitbox.getBottomEdge() >= solidHitbox.getTopEdge()) {
                        meeting.add(cls.cast(solidHitbox.getObject()));
                    }
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return meeting;
    }
    
    /**
     * Returns the number of Viewports that are assigned to this SpaceState.
     * @return The number of Viewports that are assigned to this SpaceState
     */
    public final int getNumViewports() {
        return viewports.size();
    }
    
    /**
     * Returns the Viewport that is assigned to this SpaceState with the
     * specified ID.
     * @param id The ID of the Viewport to be returned
     * @return The Viewport that is assigned to this SpaceState with the
     * specified ID
     */
    public final Viewport<T,U> getViewport(int id) {
        return viewports.get(id);
    }
    
    /**
     * Sets the Viewport that is assigned to this SpaceState with the specified
     * ID to the specified Viewport, if it is not already assigned to a
     * SpaceState. If there is already a Viewport assigned with the specified
     * ID, it will be removed from this SpaceState. If the specified Viewport is
     * null, the Viewport with the specified ID will be removed if there is one,
     * but it will not be replaced with anything.
     * @param id The ID with which to assign the specified Viewport
     * @param viewport The Viewport to add with the specified ID
     * @return Whether the addition occurred
     */
    public final boolean setViewport(int id, Viewport<T,U> viewport) {
        if (viewport == null) {
            Viewport<T,U> oldViewport = viewports.get(id);
            if (oldViewport != null) {
                oldViewport.setGameState(null);
                viewports.remove(id);
                return true;
            }
            return false;
        }
        if (viewport.getGameState() == null) {
            viewport.setGameState(getThis());
            Viewport<T,U> oldViewport = viewports.get(id);
            if (oldViewport != null) {
                oldViewport.setGameState(null);
            }
            viewports.put(id, viewport);
            return true;
        }
        return false;
    }
    
    /**
     * Removes from this SpaceState all Viewports that are currently assigned to
     * it.
     */
    public final void clearViewports() {
        for (Viewport<T,U> viewport : viewports.values()) {
            viewport.setGameState(null);
        }
        viewports.clear();
    }
    
    /**
     * Returns the point in this SpaceState, as seen through one of its
     * Viewports, that corresponds to the specified point in pixels on the
     * screen. If the specified point is not in the rendering region of one of
     * this SpaceState's Viewports with its camera in this SpaceState, this
     * method will return null.
     * @param x The x-coordinate of the screen point
     * @param y The y-coordinate of the screen point
     * @return The SpaceState point that corresponds to the specified screen
     * point
     */
    public final CellVector getSpacePoint(int x, int y) {
        for (Viewport viewport : viewports.values()) {
            if (viewport.getCamera() != null && viewport.getCamera().newState == this
                    && x >= viewport.roundX1 && x < viewport.roundX2
                    && y >= viewport.roundY1 && y < viewport.roundY2) {
                return new CellVector(viewport.getLeftEdge() + ((long)(x - viewport.roundX1) << Frac.BITS),
                        viewport.getTopEdge() + ((long)(y - viewport.roundY1) << Frac.BITS));
            }
        }
        return null;
    }
    
    /**
     * Returns whether any part of the specified rectangular region of this
     * SpaceState's space is visible through any of its Viewports.
     * @param x1 The x-coordinate of the region's left edge
     * @param y1 The y-coordinate of the region's top edge
     * @param x2 The x-coordinate of the region's right edge
     * @param y2 The y-coordinate of the region's bottom edge
     * @return Whether the specified rectangular region of this SpaceState's
     * space is visible through any of its Viewports
     */
    public final boolean rectangleIsVisible(long x1, long y1, long x2, long y2) {
        for (Viewport viewport : viewports.values()) {
            if (viewport.getCamera() != null && viewport.getCamera().newState == this) {
                long centerX = viewport.getCamera().getCenterX();
                long centerY = viewport.getCamera().getCenterY();
                if (Frac.intFloor(x1 - centerX) < viewport.getRight()
                        && Frac.intCeil(x2 - centerX) > viewport.getLeft()
                        && Frac.intFloor(y1 - centerY) < viewport.getBottom()
                        && Frac.intCeil(y2 - centerY) > viewport.getTop()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns the HUD that is assigned to this SpaceState, or null if there is
     * none.
     * @return This SpaceState's HUD
     */
    public final HUD getHUD() {
        return hud;
    }
    
    /**
     * Sets the HUD that is assigned to this SpaceState to the specified one. If
     * there is already an HUD assigned to this SpaceState, it will be removed.
     * If the specified HUD is null, the current HUD will be removed if there is
     * one, but it will not be replaced with anything.
     * @param hud The HUD to add
     */
    public final void setHUD(HUD hud) {
        this.hud = hud;
    }
    
    /**
     * Returns the number of SpaceLayers that are assigned to this SpaceState.
     * @return The number of SpaceLayers that are assigned to this SpaceState
     */
    public final int getNumLayers() {
        return spaceLayers.size();
    }
    
    /**
     * Returns the SpaceLayer that is assigned to this SpaceState with the
     * specified ID.
     * @param id The ID of the SpaceLayer to be returned
     * @return The SpaceLayer that is assigned to this SpaceState with the
     * specified ID
     */
    public final SpaceLayer getLayer(int id) {
        return spaceLayers.get(id);
    }
    
    /**
     * Sets the SpaceLayer that is assigned to this SpaceState with the
     * specified ID to the specified SpaceLayer. If there is already a
     * SpaceLayer assigned with the specified ID, it will be removed from this
     * SpaceState. If the specified SpaceLayer is null, the SpaceLayer with the
     * specified ID will be removed if there is one, but it will not be replaced
     * with anything.
     * @param id The ID with which to assign the specified SpaceLayer
     * @param layer The SpaceLayer to add with the specified ID
     */
    public final void setLayer(int id, SpaceLayer layer) {
        if (id == 0) {
            throw new RuntimeException("Attempted to set a SpaceLayer with an ID of 0");
        }
        if (layer == null) {
            spaceLayers.remove(id);
        } else {
            spaceLayers.put(id, layer);
        }
    }
    
    /**
     * Removes from this SpaceState all SpaceLayers that are currently assigned
     * to it.
     */
    public final void clearLayers() {
        spaceLayers.clear();
    }
    
    private boolean areRelated(MobileObject object1, MobileObject object2) {
        MobileObject ancestor = object1.effLeader;
        while (ancestor != null) {
            if (ancestor == object2) {
                return true;
            }
            ancestor = ancestor.effLeader;
        }
        ancestor = object2.effLeader;
        while (ancestor != null) {
            if (ancestor == object1) {
                return true;
            }
            ancestor = ancestor.effLeader;
        }
        return false;
    }
    
    private static class MoveEvent {
        
        /*
         * Type 0 = hitting the object's solid surface.
         * Type 1 = pressing against the object's solid surface as you move perpendicular to the surface.
         * Type 2 = the colliding object encountering your solid surface and you moving it along with you.
         * If multiple events happen after you travel the same distance, lower types cancel higher types
         * if the collisions are successful.
         */
        private final int type;
        private final SpaceObject object;
        private final Direction direction;
        private final long metric;
        private final long diffX, diffY;
        
        private MoveEvent(int type, SpaceObject object,
                Direction direction, long metric, long diffX, long diffY) {
            this.type = type;
            this.object = object;
            this.direction = direction;
            this.metric = metric;
            this.diffX = diffX;
            this.diffY = diffY;
        }
        
    }
    
    private static class MoveData {
        
        private final MobileObject object;
        private final boolean moveX, moveY;
        private final long diffX, diffY;
        
        private MoveData(MobileObject object, boolean moveX, boolean moveY, long diffX, long diffY) {
            this.object = object;
            this.moveX = moveX;
            this.moveY = moveY;
            this.diffX = diffX;
            this.diffY = diffY;
        }
        
    }
    
    final CellVector move(MobileObject object, long changeX, long changeY) {
        if (changeX == 0 && changeY == 0) { //Object isn't changing position
            Double pressingAngle = object.getAbsPressingAngle();
            if (object.hasCollision() && object.getCollisionHitbox() != null && pressingAngle != null) {
                //Object can collide and is pressing; check for solid objects that it's pressing against
                Hitbox collisionHitbox = object.getCollisionHitbox();
                long leftEdge = collisionHitbox.getLeftEdge();
                long rightEdge = collisionHitbox.getRightEdge();
                long topEdge = collisionHitbox.getTopEdge();
                long bottomEdge = collisionHitbox.getBottomEdge();
                boolean pressingLeft = pressingAngle > 90 && pressingAngle < 270;
                boolean pressingRight = pressingAngle < 90 || pressingAngle > 270;
                boolean pressingUp = pressingAngle > 0 && pressingAngle < 180;
                boolean pressingDown = pressingAngle > 180;
                List<Hitbox> scanned = new ArrayList<>();
                Map<SpaceObject,Direction> pressingAgainst = null;
                Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(leftEdge, topEdge, rightEdge, bottomEdge));
                while (iterator.hasNext()) {
                    Cell cell = iterator.next();
                    for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                        if (!hitbox.scanned) {
                            hitbox.scanned = true;
                            scanned.add(hitbox);
                            if (pressingLeft && hitbox.surfaceIsSolid(Direction.RIGHT)
                                    && hitbox.getRightEdge() == leftEdge
                                    && hitbox.getBottomEdge() > topEdge && hitbox.getTopEdge() < bottomEdge) {
                                if (!(hitbox.getObject() instanceof MobileObject && areRelated(object, (MobileObject)hitbox.getObject()))) {
                                    if (pressingAgainst == null) {
                                        pressingAgainst = new HashMap<>();
                                    }
                                    pressingAgainst.put(hitbox.getObject(), Direction.LEFT);
                                }
                            } else if (pressingRight && hitbox.surfaceIsSolid(Direction.LEFT)
                                    && hitbox.getLeftEdge() == rightEdge
                                    && hitbox.getBottomEdge() > topEdge && hitbox.getTopEdge() < bottomEdge) {
                                if (!(hitbox.getObject() instanceof MobileObject && areRelated(object, (MobileObject)hitbox.getObject()))) {
                                    if (pressingAgainst == null) {
                                        pressingAgainst = new HashMap<>();
                                    }
                                    pressingAgainst.put(hitbox.getObject(), Direction.RIGHT);
                                }
                            } else if (pressingUp && hitbox.surfaceIsSolid(Direction.DOWN)
                                    && hitbox.getBottomEdge() == topEdge
                                    && hitbox.getRightEdge() > leftEdge && hitbox.getLeftEdge() < rightEdge) {
                                if (!(hitbox.getObject() instanceof MobileObject && areRelated(object, (MobileObject)hitbox.getObject()))) {
                                    if (pressingAgainst == null) {
                                        pressingAgainst = new HashMap<>();
                                    }
                                    pressingAgainst.put(hitbox.getObject(), Direction.UP);
                                }
                            } else if (pressingDown && hitbox.surfaceIsSolid(Direction.UP)
                                    && hitbox.getTopEdge() == bottomEdge
                                    && hitbox.getRightEdge() > leftEdge && hitbox.getLeftEdge() < rightEdge) {
                                if (!(hitbox.getObject() instanceof MobileObject && areRelated(object, (MobileObject)hitbox.getObject()))) {
                                    if (pressingAgainst == null) {
                                        pressingAgainst = new HashMap<>();
                                    }
                                    pressingAgainst.put(hitbox.getObject(), Direction.DOWN);
                                }
                            }
                        }
                    }
                }
                for (Hitbox scannedHitbox : scanned) {
                    scannedHitbox.scanned = false;
                }
                if (pressingAgainst != null) {
                    //Object is pressing against things; make it collide with them
                    EnumSet<Direction> slideDirections = EnumSet.noneOf(Direction.class);
                    boolean stop = false;
                    for (Map.Entry<SpaceObject,Direction> entry : pressingAgainst.entrySet()) {
                        SpaceObject pressingObject = entry.getKey();
                        Direction direction = entry.getValue();
                        CollisionResponse response = object.collide(pressingObject, direction);
                        if (response != CollisionResponse.NONE) {
                            switch (response) {
                                case SLIDE:
                                    slideDirections.add(direction);
                                    break;
                                case STOP:
                                    stop = true;
                                    break;
                            }
                            object.addCollision(pressingObject, direction);
                        }
                    }
                    //Change object's velocity appropriately based on its collisions
                    if (stop) {
                        object.setVelocity(0, 0);
                    } else {
                        if (object.getVelocityX() < 0) {
                            if (slideDirections.contains(Direction.LEFT)) {
                                object.setVelocityX(0);
                            }
                        } else if (object.getVelocityX() > 0) {
                            if (slideDirections.contains(Direction.RIGHT)) {
                                object.setVelocityX(0);
                            }
                        }
                        if (object.getVelocityY() < 0) {
                            if (slideDirections.contains(Direction.UP)) {
                                object.setVelocityY(0);
                            }
                        } else if (object.getVelocityY() > 0) {
                            if (slideDirections.contains(Direction.DOWN)) {
                                object.setVelocityY(0);
                            }
                        }
                    }
                }
            }
            return new CellVector(); //Object was not displaced
        }
        CellVector nextMovement = null; //Object might need to move again due to sliding or something
        long left, right, top, bottom;
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
        SortedSet<MoveEvent> moveEvents = new TreeSet<>(moveComparator); //Record encounters that object needs to have as it moves
        if (object.hasCollision() && object.getCollisionHitbox() != null) {
            //Object can collide; check for solid objects in the path of its movement
            Hitbox collisionHitbox = object.getCollisionHitbox();
            long leftEdge = collisionHitbox.getLeftEdge();
            long rightEdge = collisionHitbox.getRightEdge();
            long topEdge = collisionHitbox.getTopEdge();
            long bottomEdge = collisionHitbox.getBottomEdge();
            boolean pressingLeft = false;
            boolean pressingRight = false;
            boolean pressingUp = false;
            boolean pressingDown = false;
            Double pressingAngle = object.getAbsPressingAngle();
            if (pressingAngle != null) {
                pressingLeft = pressingAngle > 90 && pressingAngle < 270;
                pressingRight = pressingAngle < 90 || pressingAngle > 270;
                pressingUp = pressingAngle > 0 && pressingAngle < 180;
                pressingDown = pressingAngle > 180;
            }
            List<Hitbox> scanned = new ArrayList<>();
            Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(leftEdge + left, topEdge + top, rightEdge + right, bottomEdge + bottom));
            if (changeX > 0) {
                if (changeY > 0) { //Object is moving diagonally down-right
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxLeft = hitbox.getLeftEdge();
                                long hitboxTop = hitbox.getTopEdge();
                                long verticalDiff = Frac.div(Frac.mul(hitboxLeft - rightEdge, changeY), changeX);
                                long horizontalDiff = Frac.div(Frac.mul(hitboxTop - bottomEdge, changeX), changeY);
                                if (hitbox.surfaceIsSolid(Direction.LEFT) && hitboxLeft >= rightEdge
                                        && hitbox.getTopEdge() <= bottomEdge + verticalDiff && hitbox.getBottomEdge() > topEdge + verticalDiff
                                        && hitboxLeft < rightEdge + changeX) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.RIGHT, hitboxLeft - rightEdge, hitboxLeft - rightEdge, verticalDiff));
                                    }
                                } else if (hitbox.surfaceIsSolid(Direction.UP) && hitboxTop >= bottomEdge
                                        && hitbox.getLeftEdge() < rightEdge + horizontalDiff && hitbox.getRightEdge() > leftEdge + horizontalDiff
                                        && hitboxTop < bottomEdge + changeY) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.DOWN, horizontalDiff, horizontalDiff, hitboxTop - bottomEdge));
                                    }
                                }
                            }
                        }
                    }
                } else if (changeY < 0) { //Object is moving diagonally up-right
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxLeft = hitbox.getLeftEdge();
                                long hitboxBottom = hitbox.getBottomEdge();
                                long verticalDiff = Frac.div(Frac.mul(hitboxLeft - rightEdge, changeY), changeX);
                                long horizontalDiff = Frac.div(Frac.mul(hitboxBottom - topEdge, changeX), changeY);
                                if (hitbox.surfaceIsSolid(Direction.LEFT) && hitboxLeft >= rightEdge
                                        && hitbox.getTopEdge() < bottomEdge + verticalDiff && hitbox.getBottomEdge() >= topEdge + verticalDiff
                                        && hitboxLeft < rightEdge + changeX) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.RIGHT, hitboxLeft - rightEdge, hitboxLeft - rightEdge, verticalDiff));
                                    }
                                } else if (hitbox.surfaceIsSolid(Direction.DOWN) && hitboxBottom <= topEdge
                                        && hitbox.getLeftEdge() < rightEdge + horizontalDiff && hitbox.getRightEdge() > leftEdge + horizontalDiff
                                        && hitboxBottom > topEdge + changeY) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.UP, horizontalDiff, horizontalDiff, hitboxBottom - topEdge));
                                    }
                                }
                            }
                        }
                    }
                } else { //Object is moving right
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxLeft = hitbox.getLeftEdge();
                                if (hitbox.surfaceIsSolid(Direction.LEFT) && hitboxLeft >= rightEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitbox.getBottomEdge() > topEdge
                                        && (hitboxLeft < rightEdge + changeX || (pressingRight && hitboxLeft == rightEdge + changeX))) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.RIGHT, hitboxLeft - rightEdge, hitboxLeft - rightEdge, 0));
                                    }
                                } else if (pressingUp && hitbox.surfaceIsSolid(Direction.DOWN)
                                        && hitbox.getBottomEdge() == topEdge
                                        && hitbox.getRightEdge() > leftEdge && hitboxLeft < rightEdge + changeX) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        long distance = Math.max(hitboxLeft - rightEdge, -1);
                                        moveEvents.add(new MoveEvent(1, hitboxObject, Direction.UP, distance, distance, 0));
                                    }
                                } else if (pressingDown && hitbox.surfaceIsSolid(Direction.UP)
                                        && hitbox.getTopEdge() == bottomEdge
                                        && hitbox.getRightEdge() > leftEdge && hitboxLeft < rightEdge + changeX) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        long distance = Math.max(hitboxLeft - rightEdge, -1);
                                        moveEvents.add(new MoveEvent(1, hitboxObject, Direction.DOWN, distance, distance, 0));
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (changeX < 0) {
                if (changeY > 0) { //Object is moving diagonally down-left
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxRight = hitbox.getRightEdge();
                                long hitboxTop = hitbox.getTopEdge();
                                long verticalDiff = Frac.div(Frac.mul(hitboxRight - leftEdge, changeY), changeX);
                                long horizontalDiff = Frac.div(Frac.mul(hitboxTop - bottomEdge, changeX), changeY);
                                if (hitbox.surfaceIsSolid(Direction.RIGHT) && hitboxRight <= leftEdge
                                        && hitbox.getTopEdge() <= bottomEdge + verticalDiff && hitbox.getBottomEdge() > topEdge + verticalDiff
                                        && hitboxRight > leftEdge + changeX) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.LEFT, leftEdge - hitboxRight, hitboxRight - leftEdge, verticalDiff));
                                    }
                                } else if (hitbox.surfaceIsSolid(Direction.UP) && hitboxTop >= bottomEdge
                                        && hitbox.getLeftEdge() < rightEdge + horizontalDiff && hitbox.getRightEdge() > leftEdge + horizontalDiff
                                        && hitboxTop < bottomEdge + changeY) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.DOWN, -horizontalDiff, horizontalDiff, hitboxTop - bottomEdge));
                                    }
                                }
                            }
                        }
                    }
                } else if (changeY < 0) { //Object is moving diagonally up-left
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxRight = hitbox.getRightEdge();
                                long hitboxBottom = hitbox.getBottomEdge();
                                long verticalDiff = Frac.div(Frac.mul(hitboxRight - leftEdge, changeY), changeX);
                                long horizontalDiff = Frac.div(Frac.mul(hitboxBottom - topEdge, changeX), changeY);
                                if (hitbox.surfaceIsSolid(Direction.RIGHT) && hitboxRight <= leftEdge
                                        && hitbox.getTopEdge() < bottomEdge + verticalDiff && hitbox.getBottomEdge() >= topEdge + verticalDiff
                                        && hitboxRight > leftEdge + changeX) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.LEFT, leftEdge - hitboxRight, hitboxRight - leftEdge, verticalDiff));
                                    }
                                } else if (hitbox.surfaceIsSolid(Direction.DOWN) && hitboxBottom <= topEdge
                                        && hitbox.getLeftEdge() < rightEdge + horizontalDiff && hitbox.getRightEdge() > leftEdge + horizontalDiff
                                        && hitboxBottom > topEdge + changeY) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.UP, -horizontalDiff, horizontalDiff, hitboxBottom - topEdge));
                                    }
                                }
                            }
                        }
                    }
                } else { //Object is moving left
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxRight = hitbox.getRightEdge();
                                if (hitbox.surfaceIsSolid(Direction.RIGHT) && hitboxRight <= leftEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitbox.getBottomEdge() > topEdge
                                        && (hitboxRight > leftEdge + changeX || (pressingLeft && hitboxRight == leftEdge + changeX))) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.LEFT, leftEdge - hitboxRight, hitboxRight - leftEdge, 0));
                                    }
                                } else if (pressingUp && hitbox.surfaceIsSolid(Direction.DOWN)
                                        && hitbox.getBottomEdge() == topEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitboxRight > leftEdge + changeX) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        long distance = Math.max(leftEdge - hitboxRight, -1);
                                        moveEvents.add(new MoveEvent(1, hitboxObject, Direction.UP, distance, -distance, 0));
                                    }
                                } else if (pressingDown && hitbox.surfaceIsSolid(Direction.UP)
                                        && hitbox.getTopEdge() == bottomEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitboxRight > leftEdge + changeX) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        long distance = Math.max(leftEdge - hitboxRight, -1);
                                        moveEvents.add(new MoveEvent(1, hitboxObject, Direction.DOWN, distance, -distance, 0));
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (changeY > 0) { //Object is moving down
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxTop = hitbox.getTopEdge();
                                if (hitbox.surfaceIsSolid(Direction.UP) && hitboxTop >= bottomEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitbox.getRightEdge() > leftEdge
                                        && (hitboxTop < bottomEdge + changeY || (pressingDown && hitboxTop == bottomEdge + changeY))) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.DOWN, hitboxTop - bottomEdge, 0, hitboxTop - bottomEdge));
                                    }
                                } else if (pressingLeft && hitbox.surfaceIsSolid(Direction.RIGHT)
                                        && hitbox.getRightEdge() == leftEdge
                                        && hitbox.getBottomEdge() > topEdge && hitboxTop < bottomEdge + changeY) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        long distance = Math.max(hitboxTop - bottomEdge, -1);
                                        moveEvents.add(new MoveEvent(1, hitboxObject, Direction.LEFT, distance, 0, distance));
                                    }
                                } else if (pressingRight && hitbox.surfaceIsSolid(Direction.LEFT)
                                        && hitbox.getLeftEdge() == rightEdge
                                        && hitbox.getBottomEdge() > topEdge && hitboxTop < bottomEdge + changeY) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        long distance = Math.max(hitboxTop - bottomEdge, -1);
                                        moveEvents.add(new MoveEvent(1, hitboxObject, Direction.RIGHT, distance, 0, distance));
                                    }
                                }
                            }
                        }
                    }
                } else { //Object is moving up
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.SOLID)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxBottom = hitbox.getBottomEdge();
                                if (hitbox.surfaceIsSolid(Direction.DOWN) && hitboxBottom <= topEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitbox.getRightEdge() > leftEdge
                                        && (hitboxBottom > topEdge + changeY || (pressingUp && hitboxBottom == topEdge + changeY))) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        moveEvents.add(new MoveEvent(0, hitboxObject, Direction.UP, topEdge - hitboxBottom, 0, hitboxBottom - topEdge));
                                    }
                                } else if (pressingLeft && hitbox.surfaceIsSolid(Direction.RIGHT)
                                        && hitbox.getRightEdge() == leftEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitboxBottom > topEdge + changeY) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        long distance = Math.max(topEdge - hitboxBottom, -1);
                                        moveEvents.add(new MoveEvent(1, hitboxObject, Direction.LEFT, distance, 0, -distance));
                                    }
                                } else if (pressingRight && hitbox.surfaceIsSolid(Direction.LEFT)
                                        && hitbox.getLeftEdge() == rightEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitboxBottom > topEdge + changeY) {
                                    SpaceObject hitboxObject = hitbox.getObject();
                                    if (!(hitboxObject instanceof MobileObject && areRelated(object, (MobileObject)hitboxObject))) {
                                        hitboxObject.solidEvent = true;
                                        long distance = Math.max(topEdge - hitboxBottom, -1);
                                        moveEvents.add(new MoveEvent(1, hitboxObject, Direction.RIGHT, distance, 0, -distance));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (Hitbox scannedHitbox : scanned) {
                scannedHitbox.scanned = false;
            }
        }
        if (object.isSolid()) {
            //Object has solid surfaces; check for colliding objects to move along with it
            Hitbox solidHitbox = object.getSolidHitbox();
            boolean solidLeft = solidHitbox.surfaceIsSolid(Direction.LEFT);
            boolean solidRight = solidHitbox.surfaceIsSolid(Direction.RIGHT);
            boolean solidTop = solidHitbox.surfaceIsSolid(Direction.UP);
            boolean solidBottom = solidHitbox.surfaceIsSolid(Direction.DOWN);
            long leftEdge = solidHitbox.getLeftEdge();
            long rightEdge = solidHitbox.getRightEdge();
            long topEdge = solidHitbox.getTopEdge();
            long bottomEdge = solidHitbox.getBottomEdge();
            List<Hitbox> scanned = new ArrayList<>();
            Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(leftEdge + left, topEdge + top, rightEdge + right, bottomEdge + bottom));
            if (changeX > 0) {
                if (changeY > 0) { //Object is moving diagonally down-right
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.COLLISION)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxLeft = hitbox.getLeftEdge();
                                long hitboxTop = hitbox.getTopEdge();
                                long verticalDiff = Frac.div(Frac.mul(hitboxLeft - rightEdge, changeY), changeX);
                                long horizontalDiff = Frac.div(Frac.mul(hitboxTop - bottomEdge, changeX), changeY);
                                MobileObject hitboxObject = (MobileObject)hitbox.getObject();
                                if (solidRight && hitboxLeft >= rightEdge && hitboxLeft < rightEdge + changeX
                                        && hitbox.getTopEdge() <= bottomEdge + verticalDiff && hitbox.getBottomEdge() > topEdge + verticalDiff) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.LEFT, hitboxLeft - rightEdge, hitboxLeft - rightEdge, verticalDiff));
                                    }
                                } else if (solidBottom && hitboxTop >= bottomEdge && hitboxTop < bottomEdge + changeY
                                        && hitbox.getLeftEdge() < rightEdge + horizontalDiff && hitbox.getRightEdge() > leftEdge + horizontalDiff) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.UP, horizontalDiff, horizontalDiff, hitboxTop - bottomEdge));
                                    }
                                } else if (solidLeft && hitboxObject.isPressingIn(Direction.RIGHT) && hitbox.getRightEdge() == leftEdge
                                        && hitboxTop < bottomEdge && hitbox.getBottomEdge() > topEdge && hitboxObject.getVelocityX() + hitboxObject.getStepX() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.RIGHT, 0, 0, 0));
                                    }
                                } else if (solidTop && hitboxObject.isPressingIn(Direction.DOWN) && hitbox.getBottomEdge() == topEdge
                                        && hitboxLeft < rightEdge && hitbox.getRightEdge() > leftEdge && hitboxObject.getVelocityY() + hitboxObject.getStepY() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.DOWN, 0, 0, 0));
                                    }
                                }
                            }
                        }
                    }
                } else if (changeY < 0) { //Object is moving diagonally up-right
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.COLLISION)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxLeft = hitbox.getLeftEdge();
                                long hitboxBottom = hitbox.getBottomEdge();
                                long verticalDiff = Frac.div(Frac.mul(hitboxLeft - rightEdge, changeY), changeX);
                                long horizontalDiff = Frac.div(Frac.mul(hitboxBottom - topEdge, changeX), changeY);
                                MobileObject hitboxObject = (MobileObject)hitbox.getObject();
                                if (solidRight && hitboxLeft >= rightEdge && hitboxLeft < rightEdge + changeX
                                        && hitbox.getTopEdge() < bottomEdge + verticalDiff && hitbox.getBottomEdge() >= topEdge + verticalDiff) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.LEFT, hitboxLeft - rightEdge, hitboxLeft - rightEdge, verticalDiff));
                                    }
                                } else if (solidTop && hitboxBottom <= topEdge && hitboxBottom > topEdge + changeY
                                        && hitbox.getLeftEdge() < rightEdge + horizontalDiff && hitbox.getRightEdge() > leftEdge + horizontalDiff) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.DOWN, horizontalDiff, horizontalDiff, hitboxBottom - topEdge));
                                    }
                                } else if (solidLeft && hitboxObject.isPressingIn(Direction.RIGHT) && hitbox.getRightEdge() == leftEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitboxBottom > topEdge && hitboxObject.getVelocityX() + hitboxObject.getStepX() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.RIGHT, 0, 0, 0));
                                    }
                                } else if (solidBottom && hitboxObject.isPressingIn(Direction.UP) && hitbox.getTopEdge() == bottomEdge
                                        && hitboxLeft < rightEdge && hitbox.getRightEdge() > leftEdge && hitboxObject.getVelocityY() + hitboxObject.getStepY() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.UP, 0, 0, 0));
                                    }
                                }
                            }
                        }
                    }
                } else { //Object is moving right
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.COLLISION)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxLeft = hitbox.getLeftEdge();
                                MobileObject hitboxObject = (MobileObject)hitbox.getObject();
                                if (solidRight && hitboxLeft >= rightEdge && hitboxLeft < rightEdge + changeX
                                        && hitbox.getTopEdge() < bottomEdge && hitbox.getBottomEdge() > topEdge) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.LEFT, hitboxLeft - rightEdge, hitboxLeft - rightEdge, 0));
                                    }
                                } else if (solidTop && hitboxObject.isPressingIn(Direction.DOWN) && hitbox.getBottomEdge() == topEdge
                                        && hitbox.getRightEdge() > leftEdge && hitboxLeft < rightEdge + changeX && hitboxObject.getVelocityY() + hitboxObject.getStepY() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        long distance = Math.max(hitboxLeft - rightEdge, -1);
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.DOWN, distance, distance, 0));
                                    }
                                } else if (solidBottom && hitboxObject.isPressingIn(Direction.UP) && hitbox.getTopEdge() == bottomEdge
                                        && hitbox.getRightEdge() > leftEdge && hitboxLeft < rightEdge + changeX && hitboxObject.getVelocityY() + hitboxObject.getStepY() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        long distance = Math.max(hitboxLeft - rightEdge, -1);
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.UP, distance, distance, 0));
                                    }
                                } else if (solidLeft && hitboxObject.isPressingIn(Direction.RIGHT) && hitbox.getRightEdge() == leftEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitbox.getBottomEdge() > topEdge && hitboxObject.getVelocityX() + hitboxObject.getStepX() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.RIGHT, 0, 0, 0));
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (changeX < 0) {
                if (changeY > 0) { //Object is moving diagonally down-left
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.COLLISION)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxRight = hitbox.getRightEdge();
                                long hitboxTop = hitbox.getTopEdge();
                                long verticalDiff = Frac.div(Frac.mul(hitboxRight - leftEdge, changeY), changeX);
                                long horizontalDiff = Frac.div(Frac.mul(hitboxTop - bottomEdge, changeX), changeY);
                                MobileObject hitboxObject = (MobileObject)hitbox.getObject();
                                if (solidLeft && hitboxRight <= leftEdge && hitboxRight > leftEdge + changeX
                                        && hitbox.getTopEdge() <= bottomEdge + verticalDiff && hitbox.getBottomEdge() > topEdge + verticalDiff) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.RIGHT, leftEdge - hitboxRight, hitboxRight - leftEdge, verticalDiff));
                                    }
                                } else if (solidBottom && hitboxTop >= bottomEdge && hitboxTop < bottomEdge + changeY
                                        && hitbox.getLeftEdge() < rightEdge + horizontalDiff && hitbox.getRightEdge() > leftEdge + horizontalDiff) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.UP, -horizontalDiff, horizontalDiff, hitboxTop - bottomEdge));
                                    }
                                } else if (solidRight && hitboxObject.isPressingIn(Direction.LEFT) && hitbox.getLeftEdge() == rightEdge
                                        && hitboxTop < bottomEdge && hitbox.getBottomEdge() > topEdge && hitboxObject.getVelocityX() + hitboxObject.getStepX() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.LEFT, 0, 0, 0));
                                    }
                                } else if (solidTop && hitboxObject.isPressingIn(Direction.DOWN) && hitbox.getBottomEdge() == topEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitboxRight > leftEdge && hitboxObject.getVelocityY() + hitboxObject.getStepY() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.DOWN, 0, 0, 0));
                                    }
                                }
                            }
                        }
                    }
                } else if (changeY < 0) { //Object is moving diagonally up-left
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.COLLISION)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxRight = hitbox.getRightEdge();
                                long hitboxBottom = hitbox.getBottomEdge();
                                long verticalDiff = Frac.div(Frac.mul(hitboxRight - leftEdge, changeY), changeX);
                                long horizontalDiff = Frac.div(Frac.mul(hitboxBottom - topEdge, changeX), changeY);
                                MobileObject hitboxObject = (MobileObject)hitbox.getObject();
                                if (solidLeft && hitboxRight <= leftEdge && hitboxRight > leftEdge + changeX
                                        && hitbox.getTopEdge() < bottomEdge + verticalDiff && hitbox.getBottomEdge() >= topEdge + verticalDiff) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.RIGHT, leftEdge - hitboxRight, hitboxRight - leftEdge, verticalDiff));
                                    }
                                } else if (solidTop && hitboxBottom <= topEdge && hitboxBottom > topEdge + changeY
                                        && hitbox.getLeftEdge() < rightEdge + horizontalDiff && hitbox.getRightEdge() > leftEdge + horizontalDiff) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.DOWN, -horizontalDiff, horizontalDiff, hitboxBottom - topEdge));
                                    }
                                } else if (solidRight && hitboxObject.isPressingIn(Direction.LEFT) && hitbox.getLeftEdge() == rightEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitboxBottom > topEdge && hitboxObject.getVelocityX() + hitboxObject.getStepX() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.LEFT, 0, 0, 0));
                                    }
                                } else if (solidBottom && hitboxObject.isPressingIn(Direction.UP) && hitbox.getTopEdge() == bottomEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitboxRight > leftEdge && hitboxObject.getVelocityY() + hitboxObject.getStepY() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.UP, 0, 0, 0));
                                    }
                                }
                            }
                        }
                    }
                } else { //Object is moving left
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.COLLISION)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxRight = hitbox.getRightEdge();
                                MobileObject hitboxObject = (MobileObject)hitbox.getObject();
                                if (solidLeft && hitboxRight <= leftEdge && hitboxRight > leftEdge + changeX
                                        && hitbox.getTopEdge() < bottomEdge && hitbox.getBottomEdge() > topEdge) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.RIGHT, leftEdge - hitboxRight, hitboxRight - leftEdge, 0));
                                    }
                                } else if (solidTop && hitboxObject.isPressingIn(Direction.DOWN) && hitbox.getBottomEdge() == topEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitboxRight > leftEdge + changeX && hitboxObject.getVelocityY() + hitboxObject.getStepY() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        long distance = Math.max(leftEdge - hitboxRight, -1);
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.DOWN, distance, -distance, 0));
                                    }
                                } else if (solidBottom && hitboxObject.isPressingIn(Direction.UP) && hitbox.getTopEdge() == bottomEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitboxRight > leftEdge + changeX && hitboxObject.getVelocityY() + hitboxObject.getStepY() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        long distance = Math.max(leftEdge - hitboxRight, -1);
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.UP, distance, -distance, 0));
                                    }
                                } else if (solidRight && hitboxObject.isPressingIn(Direction.LEFT) && hitbox.getLeftEdge() == rightEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitbox.getBottomEdge() > topEdge && hitboxObject.getVelocityX() + hitboxObject.getStepX() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.LEFT, 0, 0, 0));
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (changeY > 0) { //Object is moving down
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.COLLISION)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxTop = hitbox.getTopEdge();
                                MobileObject hitboxObject = (MobileObject)hitbox.getObject();
                                if (solidBottom && hitboxTop >= bottomEdge && hitboxTop < bottomEdge + changeY
                                        && hitbox.getLeftEdge() < rightEdge && hitbox.getRightEdge() > leftEdge) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.UP, hitboxTop - bottomEdge, 0, hitboxTop - bottomEdge));
                                    }
                                } else if (solidLeft && hitboxObject.isPressingIn(Direction.RIGHT) && hitbox.getRightEdge() == leftEdge
                                        && hitbox.getBottomEdge() > topEdge && hitboxTop < bottomEdge + changeY && hitboxObject.getVelocityX() + hitboxObject.getStepX() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        long distance = Math.max(hitboxTop - bottomEdge, -1);
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.RIGHT, distance, distance, 0));
                                    }
                                } else if (solidRight && hitboxObject.isPressingIn(Direction.LEFT) && hitbox.getLeftEdge() == rightEdge
                                        && hitbox.getBottomEdge() > topEdge && hitboxTop < bottomEdge + changeY && hitboxObject.getVelocityX() + hitboxObject.getStepX() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        long distance = Math.max(hitboxTop - bottomEdge, -1);
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.LEFT, distance, distance, 0));
                                    }
                                } else if (solidTop && hitboxObject.isPressingIn(Direction.DOWN) && hitbox.getBottomEdge() == topEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitbox.getRightEdge() > leftEdge && hitboxObject.getVelocityY() + hitboxObject.getStepY() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.DOWN, 0, 0, 0));
                                    }
                                }
                            }
                        }
                    }
                } else { //Object is moving up
                    while (iterator.hasNext()) {
                        Cell cell = iterator.next();
                        for (Hitbox hitbox : cell.hitboxes.get(HitboxRole.COLLISION)) {
                            if (!hitbox.scanned) {
                                hitbox.scanned = true;
                                scanned.add(hitbox);
                                long hitboxBottom = hitbox.getBottomEdge();
                                MobileObject hitboxObject = (MobileObject)hitbox.getObject();
                                if (solidTop && hitboxBottom <= topEdge && hitboxBottom > topEdge + changeY
                                        && hitbox.getLeftEdge() < rightEdge && hitbox.getRightEdge() > leftEdge) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.DOWN, topEdge - hitboxBottom, 0, hitboxBottom - topEdge));
                                    }
                                } else if (solidLeft && hitboxObject.isPressingIn(Direction.RIGHT) && hitbox.getRightEdge() == leftEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitboxBottom > topEdge + changeY && hitboxObject.getVelocityX() + hitboxObject.getStepX() >= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        long distance = Math.max(topEdge - hitboxBottom, -1);
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.RIGHT, distance, -distance, 0));
                                    }
                                } else if (solidRight && hitboxObject.isPressingIn(Direction.LEFT) && hitbox.getLeftEdge() == rightEdge
                                        && hitbox.getTopEdge() < bottomEdge && hitboxBottom > topEdge + changeY && hitboxObject.getVelocityX() + hitboxObject.getStepX() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        long distance = Math.max(topEdge - hitboxBottom, -1);
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.LEFT, distance, -distance, 0));
                                    }
                                } else if (solidBottom && hitboxObject.isPressingIn(Direction.UP) && hitbox.getTopEdge() == bottomEdge
                                        && hitbox.getLeftEdge() < rightEdge && hitbox.getRightEdge() > leftEdge && hitboxObject.getVelocityY() + hitboxObject.getStepY() <= 0) {
                                    if (!areRelated(object, hitboxObject)
                                            && (!hitboxObject.solidEvent || movementPriorityComparator.compare(object, hitboxObject) > 0)) {
                                        moveEvents.add(new MoveEvent(2, hitboxObject, Direction.UP, 0, 0, 0));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (Hitbox scannedHitbox : scanned) {
                scannedHitbox.scanned = false;
            }
        }
        List<MoveData> moveData = null; //Record objects that need to move along with this object
        if (!moveEvents.isEmpty()) { //Does object need to collide with anything?
            boolean blocked = false;
            long blockedMetric = 0;
            int blockedType = 0;
            long realChangeX = 0;
            long realChangeY = 0;
            EnumSet<Direction> slideDirections = EnumSet.noneOf(Direction.class);
            boolean stop = false;
            //Make object collide with things
            for (MoveEvent event : moveEvents) {
                if (blocked && (event.metric > blockedMetric || event.type > blockedType)) {
                    break;
                }
                Direction direction = event.direction;
                if (event.type == 2) { //Colliding object that will collide with this object
                    MobileObject objectToMove = (MobileObject)event.object;
                    CollisionResponse response = objectToMove.collide(object, direction);
                    if (response != CollisionResponse.NONE) {
                        switch (response) {
                            case SLIDE:
                                switch (direction) {
                                    case LEFT:
                                        if (objectToMove.getVelocityX() < 0) {
                                            objectToMove.setVelocityX(0);
                                        }
                                        break;
                                    case RIGHT:
                                        if (objectToMove.getVelocityX() > 0) {
                                            objectToMove.setVelocityX(0);
                                        }
                                        break;
                                    case UP:
                                        if (objectToMove.getVelocityY() < 0) {
                                            objectToMove.setVelocityY(0);
                                        }
                                        break;
                                    case DOWN:
                                        if (objectToMove.getVelocityY() > 0) {
                                            objectToMove.setVelocityY(0);
                                        }
                                        break;
                                }
                                break;
                            case STOP:
                                objectToMove.setVelocity(0, 0);
                                break;
                        }
                        objectToMove.addCollision(object, direction);
                        objectToMove.moved = true;
                        objectToMove.effLeader = object;
                        if (moveData == null) {
                            moveData = new ArrayList<>();
                        }
                        boolean objectPressing = objectToMove.isPressingIn(direction);
                        moveData.add(new MoveData(objectToMove, objectPressing || direction == Direction.LEFT || direction == Direction.RIGHT,
                                objectPressing || direction == Direction.UP || direction == Direction.DOWN, event.diffX, event.diffY));
                    }
                } else { //Solid object that this object will collide with
                    SpaceObject solidObject = event.object;
                    if (solidObject.moved) {
                        continue;
                    }
                    CollisionResponse response = object.collide(solidObject, direction);
                    if (response != CollisionResponse.NONE) {
                        switch (response) {
                            case SLIDE:
                                slideDirections.add(direction);
                                break;
                            case STOP:
                                stop = true;
                                break;
                        }
                        object.addCollision(solidObject, direction);
                        if (!blocked && (event.type == 0 || stop)) {
                            blocked = true;
                            blockedMetric = event.metric;
                            blockedType = event.type;
                            realChangeX = event.diffX;
                            realChangeY = event.diffY;
                        }
                    }
                }
            }
            //Change object's velocity appropriately based on its collisions
            if (blocked) {
                if (stop) {
                    object.setVelocity(0, 0);
                } else {
                    nextMovement = new CellVector(changeX - realChangeX, changeY - realChangeY);
                    if (slideDirections.contains(Direction.LEFT)) {
                        if (object.getVelocityX() < 0) {
                            object.setVelocityX(0);
                        }
                        nextMovement.setX(0);
                    } else if (slideDirections.contains(Direction.RIGHT)) {
                        if (object.getVelocityX() > 0) {
                            object.setVelocityX(0);
                        }
                        nextMovement.setX(0);
                    }
                    if (slideDirections.contains(Direction.UP)) {
                        if (object.getVelocityY() < 0) {
                            object.setVelocityY(0);
                        }
                        nextMovement.setY(0);
                    } else if (slideDirections.contains(Direction.DOWN)) {
                        if (object.getVelocityY() > 0) {
                            object.setVelocityY(0);
                        }
                        nextMovement.setY(0);
                    }
                }
                changeX = realChangeX;
                changeY = realChangeY;
            }
            for (MoveEvent event : moveEvents) {
                event.object.solidEvent = false;
                event.object.moved = false;
            }
        }
        CellVector displacement = new CellVector(changeX, changeY); //How far was object displaced in total?
        object.setPosition(object.getX() + changeX, object.getY() + changeY); //Move object
        if (!object.followers.isEmpty()) {
            //Object has followers; move them along with it
            for (MobileObject follower : object.followers) {
                move(follower, changeX, changeY);
            }
        }
        if (moveData != null) {
            //Object needs to move certain colliding objects along with it; do so
            for (MoveData data : moveData) {
                move(data.object, (data.moveX ? changeX - data.diffX : 0), (data.moveY ? changeY - data.diffY : 0));
            }
            for (MoveData data : moveData) {
                data.object.effLeader = data.object.getLeader();
            }
        }
        if (nextMovement != null && (nextMovement.getX() != 0 || nextMovement.getY() != 0)) {
            //Object needs to move again immediately; do so
            displacement.add(move(object, nextMovement.getX(), nextMovement.getY()));
        }
        return displacement;
    }
    
    @Override
    public void frameActions(T game, U state) {
        beforeMovementEvents.perform(state);
        for (MobileObject object : mobileObjects) {
            object.collisions.clear();
            object.collisionDirections.clear();
            object.displacement.clear();
        }
        Iterator<MobileObject> iterator = mobileObjectIterator();
        while (iterator.hasNext()) {
            MobileObject object = iterator.next();
            long objectTimeFactor = object.getEffectiveTimeFactor();
            long changeX = Frac.mul(objectTimeFactor, object.getVelocityX() + object.getStepX());
            long changeY = Frac.mul(objectTimeFactor, object.getVelocityY() + object.getStepY());
            object.displacement.setCoordinates(move(object, changeX, changeY));
            object.setStep(0, 0);
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
                int sx1 = Math.max(vx1, x1);
                int sy1 = Math.max(vy1, y1);
                int sx2 = Math.min(vx2, x2);
                int sy2 = Math.min(vy2, y2);
                g.setWorldClip(sx1, sy1, sx2 - sx1, sy2 - sy1);
                if (viewport.getCamera() != null && viewport.getCamera().newState == this) {
                    long cx = viewport.getCamera().getCenterX();
                    long cy = viewport.getCamera().getCenterY();
                    long leftEdge = viewport.getLeftEdge();
                    long rightEdge = viewport.getRightEdge();
                    long topEdge = viewport.getTopEdge();
                    long bottomEdge = viewport.getBottomEdge();
                    int scx = vx1 + Frac.toInt(cx - leftEdge);
                    int scy = vy1 + Frac.toInt(cy - topEdge);
                    for (SpaceLayer layer : spaceLayers.headMap(0).values()) {
                        layer.renderActions(g, cx, cy, scx, scy, vx1, vy1, vx2, vy2);
                    }
                    int[] cellRange = getCellRangeExclusive(leftEdge, topEdge, rightEdge, bottomEdge);
                    if (drawMode == DrawMode.FLAT) {
                        if (cellRange[0] == cellRange[2] && cellRange[1] == cellRange[3]) {
                            for (Hitbox locatorHitbox : getCell(new Point(cellRange[0], cellRange[1])).hitboxes.get(HitboxRole.LOCATOR)) {
                                if (locatorHitbox.getLeftEdge() < rightEdge
                                        && locatorHitbox.getRightEdge() > leftEdge
                                        && locatorHitbox.getTopEdge() < bottomEdge
                                        && locatorHitbox.getBottomEdge() > topEdge) {
                                    locatorHitbox.getObject().draw(g,
                                            vx1 + Frac.toInt(locatorHitbox.getAbsX() - leftEdge),
                                            vy1 + Frac.toInt(locatorHitbox.getAbsY() - topEdge),
                                            vx1, vy1, vx2, vy2);
                                }
                            }
                        } else {
                            List<Set<Hitbox>> hitboxesList = new ArrayList<>((cellRange[2] - cellRange[0] + 1)*(cellRange[3] - cellRange[1] + 1));
                            Iterator<Cell> iterator = new ReadCellRangeIterator(cellRange);
                            while (iterator.hasNext()) {
                                hitboxesList.add(iterator.next().hitboxes.get(HitboxRole.LOCATOR));
                            }
                            if (hitboxesList.size() == 2) {
                                Iterator<Hitbox> iterator1 = hitboxesList.get(0).iterator();
                                Hitbox hitbox1 = (iterator1.hasNext() ? iterator1.next() : null);
                                Iterator<Hitbox> iterator2 = hitboxesList.get(1).iterator();
                                Hitbox hitbox2 = (iterator2.hasNext() ? iterator2.next() : null); 
                                Hitbox lastHitbox = null;
                                while (hitbox1 != null || hitbox2 != null) {
                                    if (hitbox1 == null) {
                                        do {
                                            if (hitbox2.getLeftEdge() < rightEdge
                                                    && hitbox2.getRightEdge() > leftEdge
                                                    && hitbox2.getTopEdge() < bottomEdge
                                                    && hitbox2.getBottomEdge() > topEdge) {
                                                hitbox2.getObject().draw(g,
                                                        vx1 + Frac.toInt(hitbox2.getAbsX() - leftEdge),
                                                        vy1 + Frac.toInt(hitbox2.getAbsY() - topEdge),
                                                        vx1, vy1, vx2, vy2);
                                            }
                                            hitbox2 = (iterator2.hasNext() ? iterator2.next() : null); 
                                        } while (hitbox2 != null);
                                        break;
                                    } else if (hitbox2 == null) {
                                        do {
                                            if (hitbox1.getLeftEdge() < rightEdge
                                                    && hitbox1.getRightEdge() > leftEdge
                                                    && hitbox1.getTopEdge() < bottomEdge
                                                    && hitbox1.getBottomEdge() > topEdge) {
                                                hitbox1.getObject().draw(g,
                                                        vx1 + Frac.toInt(hitbox1.getAbsX() - leftEdge),
                                                        vy1 + Frac.toInt(hitbox1.getAbsY() - topEdge),
                                                        vx1, vy1, vx2, vy2);
                                            }
                                            hitbox1 = (iterator1.hasNext() ? iterator1.next() : null); 
                                        } while (hitbox1 != null);
                                        break;
                                    } else {
                                        if (drawPriorityComparator.compare(hitbox1, hitbox2) > 0) {
                                            if (hitbox1 != lastHitbox) {
                                                if (hitbox1.getLeftEdge() < rightEdge
                                                        && hitbox1.getRightEdge() > leftEdge
                                                        && hitbox1.getTopEdge() < bottomEdge
                                                        && hitbox1.getBottomEdge() > topEdge) {
                                                    hitbox1.getObject().draw(g,
                                                            vx1 + Frac.toInt(hitbox1.getAbsX() - leftEdge),
                                                            vy1 + Frac.toInt(hitbox1.getAbsY() - topEdge),
                                                            vx1, vy1, vx2, vy2);
                                                }
                                                lastHitbox = hitbox1;
                                            }
                                            hitbox1 = (iterator1.hasNext() ? iterator1.next() : null);
                                        } else {
                                            if (hitbox2 != lastHitbox) {
                                                if (hitbox2.getLeftEdge() < rightEdge
                                                        && hitbox2.getRightEdge() > leftEdge
                                                        && hitbox2.getTopEdge() < bottomEdge
                                                        && hitbox2.getBottomEdge() > topEdge) {
                                                    hitbox2.getObject().draw(g,
                                                            vx1 + Frac.toInt(hitbox2.getAbsX() - leftEdge),
                                                            vy1 + Frac.toInt(hitbox2.getAbsY() - topEdge),
                                                            vx1, vy1, vx2, vy2);
                                                }
                                                lastHitbox = hitbox2;
                                            }
                                            hitbox2 = (iterator2.hasNext() ? iterator2.next() : null);
                                        }
                                    }
                                }
                            } else {
                                PriorityQueue<Pair<Hitbox,Iterator<Hitbox>>> queue = new PriorityQueue<>(drawPriorityIteratorComparator);
                                for (Set<Hitbox> locatorHitboxes : hitboxesList) {
                                    if (!locatorHitboxes.isEmpty()) {
                                        Iterator<Hitbox> hitboxIterator = locatorHitboxes.iterator();
                                        queue.add(new Pair<>(hitboxIterator.next(), hitboxIterator));
                                    }
                                }
                                Hitbox lastHitbox = null;
                                while (!queue.isEmpty()) {
                                    Pair<Hitbox,Iterator<Hitbox>> pair = queue.poll();
                                    Hitbox locatorHitbox = pair.getKey();
                                    if (locatorHitbox != lastHitbox) {
                                        if (locatorHitbox.getLeftEdge() < rightEdge
                                                && locatorHitbox.getRightEdge() > leftEdge
                                                && locatorHitbox.getTopEdge() < bottomEdge
                                                && locatorHitbox.getBottomEdge() > topEdge) {
                                            locatorHitbox.getObject().draw(g,
                                                    vx1 + Frac.toInt(locatorHitbox.getAbsX() - leftEdge),
                                                    vy1 + Frac.toInt(locatorHitbox.getAbsY() - topEdge),
                                                    vx1, vy1, vx2, vy2);
                                        }
                                        lastHitbox = locatorHitbox;
                                    }
                                    Iterator<Hitbox> hitboxIterator = pair.getValue();
                                    if (hitboxIterator.hasNext()) {
                                        queue.add(new Pair<>(hitboxIterator.next(), hitboxIterator));
                                    }
                                }
                            }
                        }
                    } else {
                        SortedSet<Hitbox> toDraw = new TreeSet<>(drawMode == DrawMode.OVER ? overModeComparator : underModeComparator);
                        Iterator<Cell> iterator = new ReadCellRangeIterator(cellRange);
                        while (iterator.hasNext()) {
                            for (Hitbox locatorHitbox : iterator.next().hitboxes.get(HitboxRole.LOCATOR)) {
                                toDraw.add(locatorHitbox);
                            }
                        }
                        for (Hitbox locatorHitbox : toDraw) {
                            if (locatorHitbox.getLeftEdge() < rightEdge
                                    && locatorHitbox.getRightEdge() > leftEdge
                                    && locatorHitbox.getTopEdge() < bottomEdge
                                    && locatorHitbox.getBottomEdge() > topEdge) {
                                locatorHitbox.getObject().draw(g,
                                        vx1 + Frac.toInt(locatorHitbox.getAbsX() - leftEdge),
                                        vy1 + Frac.toInt(locatorHitbox.getAbsY() - topEdge),
                                        vx1, vy1, vx2, vy2);
                            }
                        }
                    }
                    for (SpaceLayer layer : spaceLayers.tailMap(0).values()) {
                        layer.renderActions(g, cx, cy, scx, scy, vx1, vy1, vx2, vy2);
                    }
                }
                if (viewport.getHUD() != null) {
                    viewport.getHUD().renderActions(g, vx1, vy1, vx2, vy2);
                }
                g.clearWorldClip();
            }
        }
        g.setWorldClip(x1, y1, x2 - x1, y2 - y1);
        if (hud != null) {
            hud.renderActions(g, x1, y1, x2, y2);
        }
    }
    
}
