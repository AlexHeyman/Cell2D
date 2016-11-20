package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.IroncladGameState;
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

public class LevelState extends IroncladGameState<LevelState,LevelThinker,LevelThinkerState> {
    
    private static abstract class LevelComparator<T> implements Comparator<T>, Serializable {}
    
    private static final Comparator<ThinkerObject> movementPriorityComparator = new LevelComparator<ThinkerObject>() {
        
        @Override
        public final int compare(ThinkerObject object1, ThinkerObject object2) {
            int priorityDifference = object1.movementPriority - object2.movementPriority;
            return (priorityDifference == 0 ? Long.signum(object1.id - object2.id) : priorityDifference);
        }
        
    };
    private static final Comparator<Hitbox> drawLayerComparator = new LevelComparator<Hitbox>() {
        
        @Override
        public final int compare(Hitbox hitbox1, Hitbox hitbox2) {
            int drawLayerDifference = hitbox1.drawLayer - hitbox2.drawLayer;
            return (drawLayerDifference == 0 ? Long.signum(hitbox1.id - hitbox2.id) : drawLayerDifference);
        }
        
    };
    private static final Comparator<Pair<Hitbox,Iterator<Hitbox>>> drawLayerIteratorComparator = new LevelComparator<Pair<Hitbox,Iterator<Hitbox>>>() {
        
        @Override
        public final int compare(Pair<Hitbox, Iterator<Hitbox>> pair1, Pair<Hitbox, Iterator<Hitbox>> pair2) {
            return drawLayerComparator.compare(pair1.getKey(), pair2.getKey());
        }
        
    };
    
    private boolean movementHasOccurred = false;
    private final Set<LevelObject> levelObjects = new HashSet<>();
    private int objectIterators = 0;
    private final SortedSet<ThinkerObject> thinkerObjects = new TreeSet<>(movementPriorityComparator);
    private double chunkWidth, chunkHeight;
    private final Map<Point,Chunk> chunks = new HashMap<>();
    private final Queue<ObjectChangeData> objectsToChange = new LinkedList<>();
    private boolean changingObjects = false;
    private final SortedMap<Integer,LevelLayer> levelLayers = new TreeMap<>();
    private HUD hud = null;
    private final Map<Integer,Viewport> viewports = new HashMap<>();
    
    public LevelState(IroncladGame game, int id, double chunkWidth, double chunkHeight) {
        super(game, id);
        setChunkDimensions(chunkWidth, chunkHeight);
    }
    
    public LevelState(IroncladGame game, int id) {
        this(game, id, 256, 256);
    }
    
    @Override
    public final LevelState getThis() {
        return this;
    }
    
    private class Chunk {
        
        private final SortedSet<Hitbox> locatorHitboxes = new TreeSet<>(drawLayerComparator);
        private final Set<Hitbox> overlapHitboxes = new HashSet<>();
        private final Map<Direction,Set<Hitbox>> solidHitboxes = new EnumMap<>(Direction.class);
        private final Set<Hitbox> collisionHitboxes = new HashSet<>();
        
        private Chunk() {}
        
        private Set<Hitbox> getSolidHitboxes(Direction direction) {
            Set<Hitbox> hitboxes = solidHitboxes.get(direction);
            if (hitboxes == null) {
                hitboxes = new HashSet<>();
                solidHitboxes.put(direction, hitboxes);
            }
            return hitboxes;
        }
        
    }
    
    private Chunk getChunk(Point point) {
        Chunk chunk = chunks.get(point);
        if (chunk == null) {
            chunk = new Chunk();
            chunks.put(point, chunk);
        }
        return chunk;
    }
    
    private int[] getChunkRangeInclusive(double x1, double y1, double x2, double y2) {
        int[] chunkRange = {(int)Math.ceil(x1/chunkWidth) - 1, (int)Math.ceil(y1/chunkHeight) - 1, (int)Math.floor(x2/chunkWidth), (int)Math.floor(y2/chunkHeight)};
        return chunkRange;
    }
    
    private int[] getChunkRangeExclusive(double x1, double y1, double x2, double y2) {
        int[] chunkRange = {(int)Math.floor(x1/chunkWidth), (int)Math.floor(y1/chunkHeight), (int)Math.ceil(x2/chunkWidth) - 1, (int)Math.ceil(y2/chunkHeight) - 1};
        if (chunkRange[0] > chunkRange[2]) {
            chunkRange[0]--;
            chunkRange[2]++;
        }
        if (chunkRange[1] > chunkRange[3]) {
            chunkRange[1]--;
            chunkRange[3]++;
        }
        return chunkRange;
    }
    
    private void updateChunkRange(Hitbox hitbox) {
        hitbox.chunkRange = getChunkRangeInclusive(hitbox.getLeftEdge(), hitbox.getTopEdge(), hitbox.getRightEdge(), hitbox.getBottomEdge());
    }
    
    private class ChunkRangeIterator implements Iterator<Chunk> {
        
        private final int[] chunkRange;
        private int xPos, yPos;
        
        private ChunkRangeIterator(int[] chunkRange) {
            this.chunkRange = chunkRange;
            xPos = chunkRange[0];
            yPos = chunkRange[1];
        }
        
        @Override
        public boolean hasNext() {
            return yPos <= chunkRange[3];
        }
        
        @Override
        public Chunk next() {
            Chunk next = getChunk(new Point(xPos, yPos));
            if (xPos == chunkRange[2]) {
                xPos = chunkRange[0];
                yPos++;
            } else {
                xPos++;
            }
            return next;
        }
        
    }
    
    private Chunk[] getChunks(int[] chunkRange) {
        Chunk[] chunkArray = new Chunk[(chunkRange[2] - chunkRange[0] + 1)*(chunkRange[3] - chunkRange[1] + 1)];
        int i = 0;
        Iterator<Chunk> iterator = new ChunkRangeIterator(chunkRange);
        while (iterator.hasNext()) {
            chunkArray[i] = iterator.next();
            i++;
        }
        return chunkArray;
    }
    
    final void updateChunks(Hitbox hitbox) {
        int[] oldRange = hitbox.chunkRange;
        updateChunkRange(hitbox);
        int[] newRange = hitbox.chunkRange;
        if (oldRange == null || oldRange[0] != newRange[0] || oldRange[1] != newRange[1]
                || oldRange[2] != newRange[2] || oldRange[3] != newRange[3]) {
            int[] addRange;
            if (oldRange == null) {
                addRange = newRange;
            } else {
                int[] removeRange = oldRange;
                Iterator<Chunk> iterator = new ChunkRangeIterator(removeRange);
                while (iterator.hasNext()) {
                    Chunk chunk = iterator.next();
                    if (hitbox.roles[0]) {
                        chunk.locatorHitboxes.remove(hitbox);
                    }
                    if (hitbox.roles[1]) {
                        chunk.overlapHitboxes.remove(hitbox);
                    }
                    if (hitbox.roles[2]) {
                        for (Direction direction : hitbox.solidSurfaces) {
                            chunk.getSolidHitboxes(direction).remove(hitbox);
                        }
                    }
                    if (hitbox.roles[3]) {
                        chunk.collisionHitboxes.remove(hitbox);
                    }
                }
                addRange = newRange;
            }
            Iterator<Chunk> iterator = new ChunkRangeIterator(addRange);
            while (iterator.hasNext()) {
                Chunk chunk = iterator.next();
                if (hitbox.roles[0]) {
                    chunk.locatorHitboxes.add(hitbox);
                }
                if (hitbox.roles[1]) {
                    chunk.overlapHitboxes.add(hitbox);
                }
                if (hitbox.roles[2]) {
                    for (Direction direction : hitbox.solidSurfaces) {
                        chunk.getSolidHitboxes(direction).add(hitbox);
                    }
                }
                if (hitbox.roles[3]) {
                    chunk.collisionHitboxes.add(hitbox);
                }
            }
        }
    }
    
    final void addLocatorHitbox(Hitbox hitbox) {
        if (hitbox.numChunkRoles == 0) {
            updateChunkRange(hitbox);
        }
        hitbox.numChunkRoles++;
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            iterator.next().locatorHitboxes.add(hitbox);
        }
    }
    
    final void removeLocatorHitbox(Hitbox hitbox) {
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            iterator.next().locatorHitboxes.remove(hitbox);
        }
        hitbox.numChunkRoles--;
        if (hitbox.numChunkRoles == 0) {
            hitbox.chunkRange = null;
        }
    }
    
    final void changeLocatorHitboxDrawLayer(Hitbox hitbox, int drawLayer) {
        Chunk[] chunkArray = getChunks(hitbox.chunkRange);
        for (Chunk chunk : chunkArray) {
            chunk.locatorHitboxes.remove(hitbox);
        }
        hitbox.drawLayer = drawLayer;
        for (Chunk chunk : chunkArray) {
            chunk.locatorHitboxes.add(hitbox);
        }
    }
    
    final void addOverlapHitbox(Hitbox hitbox) {
        if (hitbox.numChunkRoles == 0) {
            updateChunkRange(hitbox);
        }
        hitbox.numChunkRoles++;
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            iterator.next().overlapHitboxes.add(hitbox);
        }
    }
    
    final void removeOverlapHitbox(Hitbox hitbox) {
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            iterator.next().overlapHitboxes.remove(hitbox);
        }
        hitbox.numChunkRoles--;
        if (hitbox.numChunkRoles == 0) {
            hitbox.chunkRange = null;
        }
    }
    
    final void addSolidHitbox(Hitbox hitbox, Direction direction) {
        if (hitbox.numChunkRoles == 0) {
            updateChunkRange(hitbox);
        }
        hitbox.numChunkRoles++;
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            iterator.next().getSolidHitboxes(direction).add(hitbox);
        }
    }
    
    final void addSolidHitbox(Hitbox hitbox) {
        if (hitbox.numChunkRoles == 0) {
            updateChunkRange(hitbox);
        }
        hitbox.numChunkRoles += hitbox.solidSurfaces.size();
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            for (Direction direction : hitbox.solidSurfaces) {
                chunk.getSolidHitboxes(direction).add(hitbox);
            }
        }
    }
    
    final void removeSolidHitbox(Hitbox hitbox, Direction direction) {
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            iterator.next().getSolidHitboxes(direction).remove(hitbox);
        }
        hitbox.numChunkRoles--;
        if (hitbox.numChunkRoles == 0) {
            hitbox.chunkRange = null;
        }
    }
    
    final void removeSolidHitbox(Hitbox hitbox) {
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            for (Direction direction : hitbox.solidSurfaces) {
                chunk.getSolidHitboxes(direction).remove(hitbox);
            }
        }
        hitbox.numChunkRoles -= hitbox.solidSurfaces.size();
        if (hitbox.numChunkRoles == 0) {
            hitbox.chunkRange = null;
        }
    }
    
    final void completeSolidHitbox(Hitbox hitbox) {
        Set<Direction> directionsToAdd = EnumSet.complementOf(hitbox.solidSurfaces);
        if (hitbox.numChunkRoles == 0) {
            updateChunkRange(hitbox);
        }
        hitbox.numChunkRoles += directionsToAdd.size();
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            for (Direction direction : directionsToAdd) {
                chunk.getSolidHitboxes(direction).add(hitbox);
            }
        }
    }
    
    final void addCollisionHitbox(Hitbox hitbox) {
        if (hitbox.numChunkRoles == 0) {
            updateChunkRange(hitbox);
        }
        hitbox.numChunkRoles++;
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            iterator.next().collisionHitboxes.add(hitbox);
        }
    }
    
    final void removeCollisionHitbox(Hitbox hitbox) {
        Iterator<Chunk> iterator = new ChunkRangeIterator(hitbox.chunkRange);
        while (iterator.hasNext()) {
            iterator.next().collisionHitboxes.remove(hitbox);
        }
        hitbox.numChunkRoles--;
        if (hitbox.numChunkRoles == 0) {
            hitbox.chunkRange = null;
        }
    }
    
    final void addThinkerObject(ThinkerObject object) {
        thinkerObjects.add(object);
    }
    
    final void removeThinkerObject(ThinkerObject object) {
        thinkerObjects.remove(object);
    }
    
    final void changeThinkerObjectMovementPriority(ThinkerObject object, int movementPriority) {
        thinkerObjects.remove(object);
        object.movementPriority = movementPriority;
        thinkerObjects.add(object);
    }
    
    public final double getChunkWidth() {
        return chunkWidth;
    }
    
    public final double getChunkHeight() {
        return chunkHeight;
    }
    
    public final void setChunkDimensions(double chunkWidth, double chunkHeight) {
        if (chunkWidth <= 0) {
            throw new RuntimeException("Attempted to give a level state a non-positive chunk width");
        }
        if (chunkHeight <= 0) {
            throw new RuntimeException("Attempted to give a level state a non-positive chunk height");
        }
        chunks.clear();
        this.chunkWidth = chunkWidth;
        this.chunkHeight = chunkHeight;
        if (!levelObjects.isEmpty()) {
            for (LevelObject object : levelObjects) {
                object.addChunkData();
            }
        }
    }
    
    public final void loadArea(Area area) {
        
    }
    
    public class ObjectIterator implements Iterator<LevelObject> {
        
        private boolean disposed = false;
        private final Iterator<LevelObject> iterator = levelObjects.iterator();
        private LevelObject lastObject = null;
        
        private ObjectIterator() {
            objectIterators++;
        }
        
        @Override
        public final boolean hasNext() {
            if (disposed) {
                return false;
            }
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                dispose();
            }
            return hasNext;
            
        }
        
        @Override
        public final LevelObject next() {
            if (disposed) {
                return null;
            }
            lastObject = iterator.next();
            return lastObject;
        }
        
        @Override
        public final void remove() {
            if (!disposed && lastObject != null) {
                removeObject(lastObject);
                lastObject = null;
            }
        }
        
        public final boolean hasBeenDisposed() {
            return disposed;
        }
        
        public final void dispose() {
            if (!disposed) {
                disposed = true;
                objectIterators--;
                changeObjects();
            }
        }
        
    }
    
    public final ObjectIterator objectIterator() {
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
    
    private void addActions(LevelObject object) {
        levelObjects.add(object);
        object.state = this;
        object.addActions();
        object.addChunkData();
    }
    
    public final boolean removeObject(LevelObject object) {
        if (object.newState == this) {
            addObjectChangeData(object, null);
            return true;
        }
        return false;
    }
    
    private void removeActions(LevelObject object) {
        object.removeActions();
        levelObjects.remove(object);
        object.state = null;
    }
    
    @Override
    public void addThinkerActions(IroncladGame game, LevelThinker thinker) {
        
    }
    
    @Override
    public void removeThinkerActions(IroncladGame game, LevelThinker thinker) {
        
    }
    
    private void addObjectChangeData(LevelObject object, LevelState newState) {
        object.newState = newState;
        ObjectChangeData data = new ObjectChangeData(object, newState);
        if (object.state != null) {
            object.state.objectsToChange.add(data);
            object.state.changeObjects();
        }
        if (newState != null) {
            newState.objectsToChange.add(data);
            newState.changeObjects();
        }
    }
    
    private void changeObjects() {
        if (objectIterators == 0 && !changingObjects) {
            changingObjects = true;
            while (!objectsToChange.isEmpty()) {
                ObjectChangeData data = objectsToChange.remove();
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
            changingObjects = false;
        }
    }
    
    @Override
    public final void stepActions(IroncladGame game) {
        double timeFactor = getTimeFactor();
        if (timeFactor > 0) {
            Iterator<LevelThinker> iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().beforeMovement(game, this);
            }
            for (ThinkerObject object : thinkerObjects) {
                double objectTimeFactor = timeFactor*object.getTimeFactor();
                double dx = objectTimeFactor*(object.getVelocityX() + object.getDisplacementX());
                double dy = objectTimeFactor*(object.getVelocityY() + object.getDisplacementY());
                if (dx != 0 || dy != 0) {
                    if (object.hasCollision() && object.getCollisionHitbox() != null) {
                        object.setPosition(object.getX() + dx, object.getY() + dy);
                    } else {
                        object.setPosition(object.getX() + dx, object.getY() + dy);
                    }
                }
                object.setDisplacement(0, 0);
            }
            movementHasOccurred = true;
            iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().afterMovement(game, this);
            }
            movementHasOccurred = false;
        }
    }
    
    public final LevelLayer getLayer(int id) {
        return levelLayers.get(id);
    }
    
    public final boolean setLayer(int id, LevelLayer layer) {
        if (id == 0) {
            throw new RuntimeException("Attempted to set a level layer with an ID of 0");
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
    public final void renderActions(IroncladGame game, Graphics g, int x1, int y1, int x2, int y2) {
        g.clearWorldClip();
        for (Viewport viewport : viewports.values()) {
            if (viewport.roundX1 != viewport.roundX2 && viewport.roundY1 != viewport.roundY2) {
                int vx1 = x1 + viewport.roundX1;
                int vy1 = y1 + viewport.roundY1;
                int vx2 = x1 + viewport.roundX2;
                int vy2 = y1 + viewport.roundY2;
                g.setWorldClip(vx1, vy1, vx2 - vx1, vy2 - vy1);
                if (viewport.camera != null && viewport.camera.state == this) {
                    double cx = viewport.camera.getCenterX();
                    double cy = viewport.camera.getCenterY();
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
                    int[] chunkRange = getChunkRangeExclusive(left, top, right, bottom);
                    if (chunkRange[0] == chunkRange[2] && chunkRange[1] == chunkRange[3]) {
                        for (Hitbox locatorHitbox : getChunk(new Point(chunkRange[0], chunkRange[1])).locatorHitboxes) {
                            draw(g, locatorHitbox, left, right, top, bottom, xOffset, yOffset);
                        }
                    } else {
                        List<Set<Hitbox>> hitboxesList = new ArrayList<>((chunkRange[2] - chunkRange[0] + 1)*(chunkRange[3] - chunkRange[1] + 1));
                        Iterator<Chunk> iterator = new ChunkRangeIterator(chunkRange);
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
                                       if (hitbox2 != lastHitbox) {
                                            draw(g, hitbox2, left, right, top, bottom, xOffset, yOffset);
                                            lastHitbox = hitbox2;
                                        }
                                        hitbox2 = (iterator2.hasNext() ? iterator2.next() : null); 
                                    } while (hitbox2 != null);
                                    break;
                                } else if (hitbox2 == null) {
                                    do {
                                       if (hitbox1 != lastHitbox) {
                                            draw(g, hitbox1, left, right, top, bottom, xOffset, yOffset);
                                            lastHitbox = hitbox1;
                                        }
                                        hitbox1 = (iterator1.hasNext() ? iterator1.next() : null); 
                                    } while (hitbox1 != null);
                                    break;
                                } else {
                                    int comparison = drawLayerComparator.compare(hitbox1, hitbox2);
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
                            PriorityQueue<Pair<Hitbox,Iterator<Hitbox>>> queue = new PriorityQueue<>(drawLayerIteratorComparator);
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
                    for (LevelLayer layer : levelLayers.tailMap(1).values()) {
                        layer.renderActions(game, this, g, cx, cy, vx1, vy1, vx2, vy2);
                    }
                }
                if (viewport.hud != null) {
                    viewport.hud.renderActions(game, this, g, vx1, vy1, vx2, vy2);
                }
                g.clearWorldClip();
            }
        }
        g.setWorldClip(x1, y1, x2 - x1, y2 - y1);
        if (hud != null) {
            hud.renderActions(game, this, g, x1, y1, x2, y2);
        }
    }
    
}
