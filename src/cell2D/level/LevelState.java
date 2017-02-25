package cell2D.level;

import cell2D.CellGame;
import cell2D.CellGameState;
import cell2D.SafeIterator;
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

public class LevelState extends CellGameState<LevelState,LevelThinker,LevelThinkerState> {
    
    private static abstract class LevelComparator<T> implements Comparator<T>, Serializable {}
    
    private static final Comparator<Hitbox> drawPriorityComparator = new LevelComparator<Hitbox>() {
        
        @Override
        public final int compare(Hitbox hitbox1, Hitbox hitbox2) {
            int drawPriorityDifference = hitbox1.drawPriority - hitbox2.drawPriority;
            return (drawPriorityDifference == 0 ? Long.signum(hitbox1.id - hitbox2.id) : drawPriorityDifference);
        }
        
    };
    private static final Comparator<Pair<Hitbox,Iterator<Hitbox>>> drawPriorityIteratorComparator = new LevelComparator<Pair<Hitbox,Iterator<Hitbox>>>() {
        
        @Override
        public final int compare(Pair<Hitbox, Iterator<Hitbox>> pair1, Pair<Hitbox, Iterator<Hitbox>> pair2) {
            return drawPriorityComparator.compare(pair1.getKey(), pair2.getKey());
        }
        
    };
    private static final Comparator<Hitbox> overModeComparator = new LevelComparator<Hitbox>() {
        
        @Override
        public final int compare(Hitbox hitbox1, Hitbox hitbox2) {
            int drawPriorityDifference = hitbox1.drawPriority - hitbox2.drawPriority;
            if (drawPriorityDifference == 0) {
                int yDiffSignum = (int)Math.signum(hitbox1.getRelY() - hitbox2.getRelY());
                return (yDiffSignum == 0 ? Long.signum(hitbox1.id - hitbox2.id) : yDiffSignum);
            }
            return drawPriorityDifference;
        }
        
    };
    private static final Comparator<Hitbox> underModeComparator = new LevelComparator<Hitbox>() {
        
        @Override
        public final int compare(Hitbox hitbox1, Hitbox hitbox2) {
            int drawPriorityDifference = hitbox1.drawPriority - hitbox2.drawPriority;
            if (drawPriorityDifference == 0) {
                int yDiffSignum = (int)Math.signum(hitbox2.getRelY() - hitbox1.getRelY());
                return (yDiffSignum == 0 ? Long.signum(hitbox1.id - hitbox2.id) : yDiffSignum);
            }
            return drawPriorityDifference;
        }
        
    };
    private static final Comparator<ThinkerObject> movementPriorityComparator = new LevelComparator<ThinkerObject>() {
        
        @Override
        public final int compare(ThinkerObject object1, ThinkerObject object2) {
            int priorityDifference = object1.movementPriority - object2.movementPriority;
            return (priorityDifference == 0 ? Long.signum(object1.id - object2.id) : priorityDifference);
        }
        
    };
    
    private int stepState = 0;
    private final Set<LevelObject> levelObjects = new HashSet<>();
    private int objectIterators = 0;
    private final Queue<ObjectChangeData> objectChanges = new LinkedList<>();
    private boolean updatingObjectList = false;
    private final SortedSet<ThinkerObject> thinkerObjects = new TreeSet<>(movementPriorityComparator);
    private int thinkerObjectIterators = 0;
    private final Queue<ThinkerObjectChangeData> thinkerObjectChanges = new LinkedList<>();
    private double cellWidth, cellHeight;
    private final Map<Point,Cell> cells = new HashMap<>();
    private int cellLeft = 0;
    private int cellRight = 0;
    private int cellTop = 0;
    private int cellBottom = 0;
    private DrawMode drawMode;
    private final SortedMap<Integer,LevelLayer> levelLayers = new TreeMap<>();
    private HUD hud = null;
    private final Map<Integer,Viewport> viewports = new HashMap<>();
    
    public LevelState(CellGame game, int id, double cellWidth, double cellHeight, DrawMode drawMode) {
        super(game, id);
        setCellDimensions(cellWidth, cellHeight);
        setDrawMode(drawMode);
    }
    
    @Override
    public final LevelState getThis() {
        return this;
    }
    
    private class Cell {
        
        private double left, right, top, bottom;
        private Set<Hitbox> locatorHitboxes;
        private final Set<Hitbox> centerHitboxes = new HashSet<>();
        private final Set<Hitbox> overlapHitboxes = new HashSet<>();
        private final Set<Hitbox> solidHitboxes = new HashSet<>();
        private final Map<Direction,Set<Hitbox>> solidSurfaces = new EnumMap<>(Direction.class);
        private final Set<Hitbox> collisionHitboxes = new HashSet<>();
        
        private Cell(int x, int y) {
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
        
        private Set<Hitbox> getSolidSurfaces(Direction direction) {
            Set<Hitbox> hitboxes = solidSurfaces.get(direction);
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
            int pointX = (int)point.getX();
            int pointY = (int)point.getY();
            if (cells.isEmpty()) {
                cellLeft = pointX;
                cellRight = pointX;
                cellTop = pointY;
                cellBottom = pointY;
            } else {
                if (pointX < cellLeft) {
                    cellLeft = pointX;
                } else if (pointX > cellRight) {
                    cellRight = pointX;
                }
                if (pointY < cellTop) {
                    cellTop = pointY;
                } else if (pointY > cellBottom) {
                    cellBottom = pointY;
                }
            }
            cell = new Cell(pointX, pointY);
            cells.put(point, cell);
        }
        return cell;
    }
    
    private int[] getCellRangeInclusive(double x1, double y1, double x2, double y2) {
        int[] cellRange = {(int)Math.ceil(x1/cellWidth) - 1, (int)Math.ceil(y1/cellHeight) - 1, (int)Math.floor(x2/cellWidth), (int)Math.floor(y2/cellHeight)};
        return cellRange;
    }
    
    private int[] getCellRangeInclusive(Hitbox hitbox) {
        return getCellRangeInclusive(hitbox.getLeftEdge(), hitbox.getTopEdge(), hitbox.getRightEdge(), hitbox.getBottomEdge());
    }
    
    private int[] getCellRangeExclusive(double x1, double y1, double x2, double y2) {
        int[] cellRange = {(int)Math.floor(x1/cellWidth), (int)Math.floor(y1/cellHeight), (int)Math.ceil(x2/cellWidth) - 1, (int)Math.ceil(y2/cellHeight) - 1};
        if (cellRange[0] > cellRange[2]) {
            cellRange[0]--;
            cellRange[2]++;
        }
        if (cellRange[1] > cellRange[3]) {
            cellRange[1]--;
            cellRange[3]++;
        }
        return cellRange;
    }
    
    private int[] getCellRangeExclusive(Hitbox hitbox) {
        return getCellRangeExclusive(hitbox.getLeftEdge(), hitbox.getTopEdge(), hitbox.getRightEdge(), hitbox.getBottomEdge());
    }
    
    private void updateCellRange(Hitbox hitbox) {
        hitbox.cellRange = getCellRangeInclusive(hitbox);
    }
    
    private class ReadCellRangeIterator implements Iterator<Cell> {
        
        private final int left, right, top, bottom;
        private int xPos, yPos;
        private Cell nextCell;
        
        private ReadCellRangeIterator(int[] cellRange) {
            left = Math.max(cellRange[0], cellLeft);
            right = Math.min(cellRange[2], cellRight);
            top = Math.max(cellRange[1], cellTop);
            bottom = Math.min(cellRange[3], cellBottom);
            xPos = left;
            yPos = (left > right || top > bottom ? bottom + 1 : top);
            advance();
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
    
    private Cell[] getCells(int[] cellRange) {
        Cell[] cellArray = new Cell[(cellRange[2] - cellRange[0] + 1)*(cellRange[3] - cellRange[1] + 1)];
        int i = 0;
        Iterator<Cell> iterator = new WriteCellRangeIterator(cellRange);
        while (iterator.hasNext()) {
            cellArray[i] = iterator.next();
            i++;
        }
        return cellArray;
    }
    
    public final double getCellWidth() {
        return cellWidth;
    }
    
    public final double getCellHeight() {
        return cellHeight;
    }
    
    public final void setCellDimensions(double cellWidth, double cellHeight) {
        if (cellWidth <= 0) {
            throw new RuntimeException("Attempted to give a LevelState a non-positive cell width");
        }
        if (cellHeight <= 0) {
            throw new RuntimeException("Attempted to give a LevelState a non-positive cell height");
        }
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        rebuildCells();
    }
    
    public final void rebuildCells() {
        cells.clear();
        if (!levelObjects.isEmpty()) {
            for (LevelObject object : levelObjects) {
                object.addCellData();
            }
        }
    }
    
    public final DrawMode getDrawMode() {
        return drawMode;
    }
    
    public final void setDrawMode(DrawMode drawMode) {
        this.drawMode = drawMode;
        if (!cells.isEmpty()) {
            for (Cell cell : cells.values()) {
                Set<Hitbox> oldLocatorHitboxes = cell.locatorHitboxes;
                cell.initializeLocatorHitboxes();
                for (Hitbox hitbox : oldLocatorHitboxes) {
                    cell.locatorHitboxes.add(hitbox);
                }
            }
        }
    }
    
    public final void loadArea(LevelVector origin, Area area) {
        loadArea(origin.getX(), origin.getY(), area);
    }
    
    public final void loadArea(double originX, double originY, Area area) {
        for (LevelObject object : area.load(getGame(), this)) {
            if (object.state == null && object.newState == null) {
                object.setPosition(object.getPosition().add(originX, originY));
                object.newState = this;
                objectChanges.add(new ObjectChangeData(object, this));  
            }
        }
        updateObjectList();
    }
    
    final void updateCells(Hitbox hitbox) {
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
    
    final void addLocatorHitbox(Hitbox hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().locatorHitboxes.add(hitbox);
        }
    }
    
    final void removeLocatorHitbox(Hitbox hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().locatorHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    final void changeLocatorHitboxDrawPriority(Hitbox hitbox, int drawPriority) {
        Cell[] cellArray = getCells(hitbox.cellRange);
        for (Cell cell : cellArray) {
            cell.locatorHitboxes.remove(hitbox);
        }
        hitbox.drawPriority = drawPriority;
        for (Cell cell : cellArray) {
            cell.locatorHitboxes.add(hitbox);
        }
    }
    
    final void addCenterHitbox(Hitbox hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().centerHitboxes.add(hitbox);
        }
    }
    
    final void removeCenterHitbox(Hitbox hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().centerHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    final void addOverlapHitbox(Hitbox hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().overlapHitboxes.add(hitbox);
        }
    }
    
    final void removeOverlapHitbox(Hitbox hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().overlapHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    private void addSolidHitbox(Hitbox hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().solidHitboxes.add(hitbox);
        }
    }
    
    private void removeSolidHitbox(Hitbox hitbox) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().solidHitboxes.remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.numCellRoles == 0) {
            hitbox.cellRange = null;
        }
    }
    
    final void addSolidSurface(Hitbox hitbox, Direction direction) {
        if (hitbox.solidSurfaces.size() == 1) {
            addSolidHitbox(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().getSolidSurfaces(direction).add(hitbox);
        }
    }
    
    final void removeSolidSurface(Hitbox hitbox, Direction direction) {
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().getSolidSurfaces(direction).remove(hitbox);
        }
        hitbox.numCellRoles--;
        if (hitbox.solidSurfaces.isEmpty()) {
            removeSolidHitbox(hitbox);
        }
    }
    
    final void addAllSolidSurfaces(Hitbox hitbox) {
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
    
    final void removeAllSolidSurfaces(Hitbox hitbox) {
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
    
    final void completeSolidSurfaces(Hitbox hitbox) {
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
    
    final void addCollisionHitbox(Hitbox hitbox) {
        if (hitbox.numCellRoles == 0) {
            updateCellRange(hitbox);
        }
        hitbox.numCellRoles++;
        Iterator<Cell> iterator = new WriteCellRangeIterator(hitbox.cellRange);
        while (iterator.hasNext()) {
            iterator.next().collisionHitboxes.add(hitbox);
        }
    }
    
    final void removeCollisionHitbox(Hitbox hitbox) {
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
    public final void addThinkerActions(CellGame game, LevelThinker thinker) {
        if (stepState == 2) {
            thinker.afterMovement(game, this);
        } else if (stepState == 1) {
            thinker.beforeMovement(game, this);
        }
    }
    
    @Override
    public final void updateThinkerListActions(CellGame game) {
        updateObjectList();
    }
    
    private class ObjectIterator implements SafeIterator<LevelObject> {
        
        private boolean finished = false;
        private final Iterator<LevelObject> iterator = levelObjects.iterator();
        private LevelObject lastObject = null;
        
        private ObjectIterator() {
            objectIterators++;
        }
        
        @Override
        public final boolean hasNext() {
            if (finished) {
                return false;
            }
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                finish();
            }
            return hasNext;
        }
        
        @Override
        public final LevelObject next() {
            if (finished) {
                return null;
            }
            lastObject = iterator.next();
            return lastObject;
        }
        
        @Override
        public final void remove() {
            if (!finished && lastObject != null) {
                removeObject(lastObject);
                lastObject = null;
            }
        }
        
        @Override
        public final boolean isFinished() {
            return finished;
        }
        
        @Override
        public final void finish() {
            if (!finished) {
                finished = true;
                objectIterators--;
                updateObjectList();
            }
        }
        
    }
    
    public final boolean iteratingThroughObjects() {
        return objectIterators > 0;
    }
    
    public final SafeIterator<LevelObject> objectIterator() {
        return new ObjectIterator();
    }
    
    private static class ObjectChangeData {
        
        private boolean used = false;
        private final LevelObject object;
        private final LevelState newState;
        
        private ObjectChangeData(LevelObject object, LevelState newState) {
            this.object = object;
            this.newState = newState;
        }
        
    }
    
    public final boolean addObject(LevelObject object) {
        if (object.newState == null) {
            addObjectChangeData(object, this);
            return true;
        }
        return false;
    }
    
    public final boolean removeObject(LevelObject object) {
        if (object.newState == this) {
            addObjectChangeData(object, null);
            return true;
        }
        return false;
    }
    
    private void addObjectChangeData(LevelObject object, LevelState newState) {
        object.newState = newState;
        ObjectChangeData data = new ObjectChangeData(object, newState);
        if (object.state != null) {
            object.state.objectChanges.add(data);
            object.state.updateObjectList();
        }
        if (newState != null) {
            newState.objectChanges.add(data);
            newState.updateObjectList();
        }
    }
    
    private void addActions(LevelObject object) {
        levelObjects.add(object);
        object.state = this;
        object.addCellData();
        object.addActions();
    }
    
    private void removeActions(LevelObject object) {
        object.removeActions();
        levelObjects.remove(object);
        object.state = null;
    }
    
    private void updateObjectList() {
        if (objectIterators == 0 && thinkerObjectIterators == 0
                && !iteratingThroughThinkers() && !updatingObjectList) {
            updatingObjectList = true;
            while (!objectChanges.isEmpty()) {
                ObjectChangeData data = objectChanges.remove();
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
    
    private class ThinkerObjectIterator implements SafeIterator<ThinkerObject> {
        
        private boolean finished = false;
        private final Iterator<ThinkerObject> iterator = thinkerObjects.iterator();
        private ThinkerObject lastObject = null;
        
        private ThinkerObjectIterator() {
            thinkerObjectIterators++;
        }
        
        @Override
        public final boolean hasNext() {
            if (finished) {
                return false;
            }
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                finish();
            }
            return hasNext;
        }
        
        @Override
        public final ThinkerObject next() {
            if (finished) {
                return null;
            }
            lastObject = iterator.next();
            return lastObject;
        }
        
        @Override
        public final void remove() {
            if (!finished && lastObject != null) {
                removeObject(lastObject);
                lastObject = null;
            }
        }
        
        @Override
        public final boolean isFinished() {
            return finished;
        }
        
        @Override
        public final void finish() {
            if (!finished) {
                finished = true;
                thinkerObjectIterators--;
                updateThinkerObjectList();
            }
        }
        
    }
    
    public final boolean iteratingThroughThinkerObjects() {
        return thinkerObjectIterators > 0;
    }
    
    public final SafeIterator<ThinkerObject> thinkerObjectIterator() {
        return new ThinkerObjectIterator();
    }
    
    private static class ThinkerObjectChangeData {
        
        private boolean used = false;
        private final boolean changePriority;
        private final ThinkerObject object;
        private final boolean add;
        private final int movementPriority;
        
        private ThinkerObjectChangeData(ThinkerObject object, boolean add) {
            changePriority = false;
            this.object = object;
            this.add = add;
            movementPriority = 0;
        }
        
        private ThinkerObjectChangeData(ThinkerObject object, int movementPriority) {
            changePriority = true;
            this.object = object;
            add = false;
            this.movementPriority = movementPriority;
        }
        
    }
    
    final void addThinkerObject(ThinkerObject object) {
        thinkerObjectChanges.add(new ThinkerObjectChangeData(object, true));
        updateThinkerObjectList();
    }
    
    final void removeThinkerObject(ThinkerObject object) {
        thinkerObjectChanges.add(new ThinkerObjectChangeData(object, false));
        updateThinkerObjectList();
    }
    
    final void changeThinkerObjectMovementPriority(ThinkerObject object, int movementPriority) {
        thinkerObjectChanges.add(new ThinkerObjectChangeData(object, movementPriority));
        updateThinkerObjectList();
    }
    
    private void updateThinkerObjectList() {
        if (thinkerObjectIterators == 0) {
            while (!thinkerObjectChanges.isEmpty()) {
                ThinkerObjectChangeData data = thinkerObjectChanges.remove();
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
    
    public final <T extends LevelObject> T nearestObject(LevelVector point, Class<T> cls) {
        return nearestObject(point.getX(), point.getY(), cls);
    }
    
    public final <T extends LevelObject> T nearestObject(double pointX, double pointY, Class<T> cls) {
        T nearest = null;
        double nearestDistance = -1;
        for (LevelObject object : (ThinkerObject.class.isAssignableFrom(cls) ? thinkerObjects : levelObjects)) {
            if (cls.isAssignableFrom(object.getClass())) {
                double distance = LevelVector.distanceBetween(pointX, pointY, object.getX(), object.getY());
                if (nearestDistance < 0 || distance < nearestDistance) {
                    nearest = cls.cast(object);
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }
    
    public final <T extends LevelObject> boolean objectIsWithinRectangle(double x1, double y1, double x2, double y2, Class<T> cls) {
        return objectWithinRectangle(x1, y1, x2, y2, cls) != null;
    }
    
    public final <T extends LevelObject> T objectWithinRectangle(double x1, double y1, double x2, double y2, Class<T> cls) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox centerHitbox : iterator.next().centerHitboxes) {
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
    
    public final <T extends LevelObject> List<T> objectsWithinRectangle(double x1, double y1, double x2, double y2, Class<T> cls) {
        List<T> within = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox centerHitbox : iterator.next().centerHitboxes) {
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
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return within;
    }
    
    public final <T extends LevelObject> T nearestObjectWithinRectangle(LevelVector point, double x1, double y1, double x2, double y2, Class<T> cls) {
        return nearestObjectWithinRectangle(point.getX(), point.getY(), x1, y1, x2, y2, cls);
    }
    
    public final <T extends LevelObject> T nearestObjectWithinRectangle(double pointX, double pointY, double x1, double y1, double x2, double y2, Class<T> cls) {
        T nearest = null;
        double nearestDistance = -1;
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(x1, y1, x2, y2));
        while (iterator.hasNext()) {
            for (Hitbox centerHitbox : iterator.next().centerHitboxes) {
                if (!centerHitbox.scanned) {
                    LevelObject object = centerHitbox.getObject();
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
        for (Hitbox hitbox : scanned) {
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
    
    public final <T extends LevelObject> boolean objectIsWithinCircle(LevelVector center, double radius, Class<T> cls) {
        return objectWithinCircle(center.getX(), center.getY(), radius, cls) != null;
    }
    
    public final <T extends LevelObject> boolean objectIsWithinCircle(double centerX, double centerY, double radius, Class<T> cls) {
        return objectWithinCircle(centerX, centerY, radius, cls) != null;
    }
    
    public final <T extends LevelObject> T objectWithinCircle(LevelVector center, double radius, Class<T> cls) {
        return objectWithinCircle(center.getX(), center.getY(), radius, cls);
    }
    
    public final <T extends LevelObject> T objectWithinCircle(double centerX, double centerY, double radius, Class<T> cls) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox centerHitbox : cell.centerHitboxes) {
                    if (!centerHitbox.scanned) {
                        if (cls.isAssignableFrom(centerHitbox.getObject().getClass())
                                && LevelVector.distanceBetween(centerX, centerY, centerHitbox.getAbsX(), centerHitbox.getAbsY()) <= radius) {
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
    
    public final <T extends LevelObject> List<T> objectsWithinCircle(LevelVector center, double radius, Class<T> cls) {
        return objectsWithinCircle(center.getX(), center.getY(), radius, cls);
    }
    
    public final <T extends LevelObject> List<T> objectsWithinCircle(double centerX, double centerY, double radius, Class<T> cls) {
        List<T> within = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox centerHitbox : cell.centerHitboxes) {
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
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return within;
    }
    
    public final <T extends LevelObject> T nearestObjectWithinCircle(LevelVector point, LevelVector center, double radius, Class<T> cls) {
        return nearestObjectWithinCircle(point.getX(), point.getY(), center.getX(), center.getY(), radius, cls);
    }
    
    public final <T extends LevelObject> T nearestObjectWithinCircle(double pointX, double pointY, double centerX, double centerY, double radius, Class<T> cls) {
        T nearest = null;
        double nearestDistance = -1;
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(centerX - radius, centerY - radius, centerX + radius, centerY + radius));
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (circleMeetsRectangle(centerX, centerY, radius, cell.left, cell.top, cell.right, cell.bottom)) {
                for (Hitbox centerHitbox : cell.centerHitboxes) {
                    if (!centerHitbox.scanned) {
                        LevelObject object = centerHitbox.getObject();
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
        for (Hitbox hitbox : scanned) {
            hitbox.scanned = false;
        }
        return nearest;
    }
    
    public final <T extends LevelObject> boolean isOverlappingObject(Hitbox hitbox, Class<T> cls) {
        return overlappingObject(hitbox, cls) != null;
    }
    
    public final <T extends LevelObject> T overlappingObject(Hitbox hitbox, Class<T> cls) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox overlapHitbox : iterator.next().overlapHitboxes) {
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
    
    public final <T extends LevelObject> List<T> overlappingObjects(Hitbox hitbox, Class<T> cls) {
        List<T> overlapping = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox overlapHitbox : iterator.next().overlapHitboxes) {
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
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return overlapping;
    }
    
    public final <T extends LevelObject> T nearestOverlappingObject(LevelVector point, Hitbox hitbox, Class<T> cls) {
        return nearestOverlappingObject(point.getX(), point.getY(), hitbox, cls);
    }
    
    public final <T extends LevelObject> T nearestOverlappingObject(double pointX, double pointY, Hitbox hitbox, Class<T> cls) {
        T nearest = null;
        double nearestDistance = -1;
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox overlapHitbox : iterator.next().overlapHitboxes) {
                if (!overlapHitbox.scanned) {
                    LevelObject object = overlapHitbox.getObject();
                    if (cls.isAssignableFrom(object.getClass())
                            && Hitbox.overlap(hitbox, overlapHitbox)) {
                        double distance = LevelVector.distanceBetween(pointX, pointY, overlapHitbox.getAbsX(), overlapHitbox.getAbsY());
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
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return nearest;
    }
    
    public final <T extends LevelObject> List<T> boundingBoxesMeet(Hitbox hitbox, Class<T> cls) {
        List<T> meeting = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox overlapHitbox : iterator.next().overlapHitboxes) {
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
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return meeting;
    }
    
    public final <T extends LevelObject> boolean isIntersectingSolidObject(Hitbox hitbox, Class<T> cls) {
        return intersectingSolidObject(hitbox, cls) != null;
    }
    
    public final <T extends LevelObject> T intersectingSolidObject(Hitbox hitbox, Class<T> cls) {
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox solidHitbox : iterator.next().solidHitboxes) {
                if (!solidHitbox.scanned) {
                    if (cls.isAssignableFrom(solidHitbox.getObject().getClass())
                            && Hitbox.intersectsSolidHitbox(hitbox, solidHitbox)) {
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
    
    public final <T extends LevelObject> List<T> intersectingSolidObjects(Hitbox hitbox, Class<T> cls) {
        List<T> intersecting = new ArrayList<>();
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox solidHitbox : iterator.next().solidHitboxes) {
                if (!solidHitbox.scanned) {
                    if (cls.isAssignableFrom(solidHitbox.getObject().getClass())
                            && Hitbox.intersectsSolidHitbox(hitbox, solidHitbox)) {
                        intersecting.add(cls.cast(solidHitbox.getObject()));
                    }
                    solidHitbox.scanned = true;
                    scanned.add(solidHitbox);
                }
            }
        }
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return intersecting;
    }
    
    public final <T extends LevelObject> T nearestIntersectingSolidObject(LevelVector point, Hitbox hitbox, Class<T> cls) {
        return nearestIntersectingSolidObject(point.getX(), point.getY(), hitbox, cls);
    }
    
    public final <T extends LevelObject> T nearestIntersectingSolidObject(double pointX, double pointY, Hitbox hitbox, Class<T> cls) {
        T nearest = null;
        double nearestDistance = -1;
        List<Hitbox> scanned = new ArrayList<>();
        Iterator<Cell> iterator = new ReadCellRangeIterator(getCellRangeExclusive(hitbox));
        while (iterator.hasNext()) {
            for (Hitbox solidHitbox : iterator.next().solidHitboxes) {
                if (!solidHitbox.scanned) {
                    LevelObject object = solidHitbox.getObject();
                    if (cls.isAssignableFrom(object.getClass())
                            && Hitbox.intersectsSolidHitbox(hitbox, solidHitbox)) {
                        double distance = LevelVector.distanceBetween(pointX, pointY, solidHitbox.getAbsX(), solidHitbox.getAbsY());
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
        for (Hitbox scannedHitbox : scanned) {
            scannedHitbox.scanned = false;
        }
        return nearest;
    }
    
    public final LevelLayer getLayer(int id) {
        return levelLayers.get(id);
    }
    
    public final boolean setLayer(int id, LevelLayer layer) {
        if (id == 0) {
            throw new RuntimeException("Attempted to set a LevelLayer with an ID of 0");
        }
        if (layer == null) {
            return removeLayer(id);
        }
        if (addThinker(layer)) {
            LevelLayer oldLayer = levelLayers.get(id);
            if (oldLayer != null) {
                removeThinker(oldLayer);
            }
            levelLayers.put(id, layer);
            return true;
        }
        return false;
    }
    
    public final boolean removeLayer(int id) {
        LevelLayer oldLayer = levelLayers.get(id);
        if (oldLayer != null) {
            removeThinker(oldLayer);
            levelLayers.remove(id);
            return true;
        }
        return false;
    }
    
    public final void clearLayers() {
        for (LevelLayer layer : levelLayers.values()) {
            removeThinker(layer);
        }
        levelLayers.clear();
    }
    
    public final HUD getHUD() {
        return hud;
    }
    
    public final boolean setHUD(HUD hud) {
        if (hud == null || addThinker(hud)) {
            if (this.hud != null) {
                removeThinker(this.hud);
            }
            this.hud = hud;
            return true;
        }
        return false;
    }
    
    public final Viewport getViewport(int id) {
        return viewports.get(id);
    }
    
    public final boolean setViewport(int id, Viewport viewport) {
        if (viewport == null) {
            return removeViewport(id);
        }
        if (addThinker(viewport)) {
            Viewport oldViewport = viewports.get(id);
            if (oldViewport != null) {
                removeThinker(oldViewport);
            }
            viewports.put(id, viewport);
            return true;
        }
        return false;
    }
    
    public final boolean removeViewport(int id) {
        Viewport oldViewport = viewports.get(id);
        if (oldViewport != null) {
            removeThinker(oldViewport);
            viewports.remove(id);
            return true;
        }
        return false;
    }
    
    public final void clearViewports() {
        for (Viewport viewport : viewports.values()) {
            removeThinker(viewport);
        }
        viewports.clear();
    }
    
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
    
    final void move(ThinkerObject object, double dx, double dy) {
        object.clearCollisions();
        if (object.getCollisionHitbox() != null) {
            if (object.getCollisionMode() == CollisionMode.CONTINUOUS) {
                if (dx != 0 || dy != 0) {
                    object.setPosition(object.getX() + dx, object.getY() + dy);
                }
                return;
            } else if (object.getCollisionMode() == CollisionMode.DISCRETE) {
                if (dx != 0 || dy != 0) {
                    object.setPosition(object.getX() + dx, object.getY() + dy);
                }
                if (object.getCollisionHitbox() != null) {
                    for (LevelObject levelObject : intersectingSolidObjects(object.getCollisionHitbox(), LevelObject.class)) {
                        if (object.checkCollision(levelObject, CollisionType.INTERSECTING)) {
                            object.addCollision(levelObject, CollisionType.INTERSECTING);
                        }
                    }
                }
                return;
            }
        }
        if (dx != 0 || dy != 0) {
            object.setPosition(object.getX() + dx, object.getY() + dy);
        }
    }
    
    @Override
    public final void stepActions(CellGame game) {
        double timeFactor = getTimeFactor();
        if (timeFactor > 0) {
            stepState = 1;
            Iterator<LevelThinker> iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().beforeMovement(game, this);
            }
            Iterator<ThinkerObject> thinkerObjectIterator = thinkerObjectIterator();
            while (thinkerObjectIterator.hasNext()) {
                ThinkerObject object = thinkerObjectIterator.next();
                double objectTimeFactor = object.getEffectiveTimeFactor();
                double dx = objectTimeFactor*(object.getVelocityX() + object.getDisplacementX());
                double dy = objectTimeFactor*(object.getVelocityY() + object.getDisplacementY());
                move(object, dx, dy);
                object.setDisplacement(0, 0);
            }
            stepState = 2;
            iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().afterMovement(game, this);
            }
            stepState = 0;
        }
    }
    
    private void draw(Graphics g, Hitbox locatorHitbox,
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
    public final void renderActions(CellGame game, Graphics g, int x1, int y1, int x2, int y2) {
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
                            for (Hitbox locatorHitbox : getCell(new Point(cellRange[0], cellRange[1])).locatorHitboxes) {
                                draw(g, locatorHitbox, left, right, top, bottom, xOffset, yOffset);
                            }
                        } else {
                            List<Set<Hitbox>> hitboxesList = new ArrayList<>((cellRange[2] - cellRange[0] + 1)*(cellRange[3] - cellRange[1] + 1));
                            Iterator<Cell> iterator = new ReadCellRangeIterator(cellRange);
                            while (iterator.hasNext()) {
                                hitboxesList.add(iterator.next().locatorHitboxes);
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
                                        draw(g, locatorHitbox, left, right, top, bottom, xOffset, yOffset);
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
                            for (Hitbox locatorHitbox : iterator.next().locatorHitboxes) {
                                toDraw.add(locatorHitbox);
                            }
                        }
                        for (Hitbox locatorHitbox : toDraw) {
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
