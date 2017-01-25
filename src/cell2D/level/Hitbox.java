package cell2D.level;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.newdawn.slick.util.FastTrig;

public abstract class Hitbox {
    
    static final int HITBOXES_PER_OBJECT = 4;
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    private Hitbox parent = null;
    private final Set<Hitbox> children = new HashSet<>();
    CompositeHitbox componentOf = null;
    EnumSet<Direction> solidSurfaces = EnumSet.noneOf(Direction.class);
    private LevelObject object = null;
    final boolean[] roles = new boolean[HITBOXES_PER_OBJECT];
    private int numRoles = 0;
    LevelState state = null;
    int[] cellRange = null;
    boolean scanned = false;
    int drawPriority = 0;
    int numCellRoles = 0;
    private final LevelVector relPosition, absPosition;
    private boolean relXFlip = false;
    private boolean absXFlip = false;
    private boolean relYFlip = false;
    private boolean absYFlip = false;
    private double relAngle = 0;
    private double relAngleX = 1;
    private double relAngleY = 0;
    private double absAngle = 0;
    private double absAngleX = 1;
    private double absAngleY = 0;
    
    public Hitbox(LevelVector relPosition) {
        id = getNextID();
        this.relPosition = new LevelVector(relPosition);
        absPosition = new LevelVector(relPosition);
    }
    
    public Hitbox(double relX, double relY) {
        this(new LevelVector(relX, relY));
    }
    
    public abstract Hitbox getCopy();
    
    public final void copyProperties(Hitbox hitbox) {
        setRelXFlip(hitbox.relXFlip);
        setRelYFlip(hitbox.relYFlip);
        setRelAngle(hitbox.relAngle);
    }
    
    private static long getNextID() {
        return idCounter.getAndIncrement();
    }
    
    final Hitbox getParent() {
        return parent;
    }
    
    final boolean addChild(Hitbox hitbox) {
        if (hitbox != null && hitbox != this
                && hitbox.parent == null && hitbox.object == null) {
            Hitbox ancestor = parent;
            while (ancestor != null) {
                if (ancestor == hitbox) {
                    return false;
                }
                ancestor = ancestor.parent;
            }
            children.add(hitbox);
            hitbox.parent = this;
            hitbox.recursivelyUpdateData();
            return true;
        }
        return false;
    }
    
    final boolean removeChild(Hitbox hitbox) {
        if (hitbox != null && hitbox.parent == this) {
            children.remove(hitbox);
            hitbox.parent = null;
            hitbox.recursivelyUpdateData();
            return true;
        }
        return false;
    }
    
    private void recursivelyUpdateData() {
        if (parent == null) {
            object = null;
            state = null;
            absXFlip = relXFlip;
            absYFlip = relYFlip;
        } else {
            object = parent.object;
            state = parent.state;
            absXFlip = parent.absXFlip ^ relXFlip;
            absYFlip = parent.absYFlip ^ relYFlip;
        }
        updateAbsAngle();
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateData();
            }
        }
        updateAbsXFlipActions();
        updateAbsYFlipActions();
        updateAbsAngleActions();
    }
    
    public final CompositeHitbox getComponentOf() {
        return componentOf;
    }
    
    public final boolean surfaceIsSolid(Direction direction) {
        return solidSurfaces.contains(direction);
    }
    
    public final void setSurfaceSolid(Direction direction, boolean solid) {
        if (solid) {
            if (solidSurfaces.add(direction) && roles[2] && state != null) {
                state.addSolidSurface(this, direction);
            }
        } else {
            if (solidSurfaces.remove(direction) && roles[2] && state != null) {
                state.removeSolidSurface(this, direction);
            }
        }
    }
    
    public final void setSolid(boolean solid) {
        if (solid) {
            if (roles[2] && state != null) {
                state.completeSolidSurfaces(this);
            }
            solidSurfaces = EnumSet.allOf(Direction.class);
        } else {
            if (roles[2] && state != null) {
                state.removeAllSolidSurfaces(this);
            }
            solidSurfaces.clear();
        }
    }
    
    public final LevelObject getObject() {
        return object;
    }
    
    final void setObject(LevelObject object) {
        if (object != this.object) {
            recursivelySetObject(object);
        }
    }
    
    private void recursivelySetObject(LevelObject object) {
        this.object = object;
        state = (object == null ? null : object.state);
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelySetObject(object);
            }
        }
    }
    
    final void updateBoundaries() {
        if (componentOf != null) {
            componentOf.updateShape();
        }
        if (state != null && cellRange != null) {
            state.updateCells(this);
        }
    }
    
    final void addAsLocatorHitbox(int drawPriority) {
        this.drawPriority = drawPriority;
        if (state != null) {
            state.addLocatorHitbox(this);
        }
        roles[0] = true;
        numRoles++;
    }
    
    final void removeAsLocatorHitbox() {
        if (state != null) {
            state.removeLocatorHitbox(this);
        }
        drawPriority = 0;
        roles[0] = false;
        numRoles--;
        if (numRoles == 0) {
            setObject(null);
            if (parent != null) {
                parent.removeChild(this);
            }
        }
    }
    
    final void changeDrawPriority(int drawPriority) {
        if (state == null) {
            this.drawPriority = drawPriority;
        } else {
            state.changeLocatorHitboxDrawPriority(this, drawPriority);
        }
    }
    
    final void addAsOverlapHitbox() {
        if (state != null) {
            state.addOverlapHitbox(this);
        }
        roles[1] = true;
        numRoles++;
    }
    
    final void removeAsOverlapHitbox() {
        if (state != null) {
            state.removeOverlapHitbox(this);
        }
        roles[1] = false;
        numRoles--;
        if (numRoles == 0) {
            setObject(null);
            if (parent != null) {
                parent.removeChild(this);
            }
        }
    }
    
    final void addAsSolidHitbox() {
        if (state != null) {
            state.addAllSolidSurfaces(this);
        }
        roles[2] = true;
        numRoles++;
    }
    
    final void removeAsSolidHitbox() {
        if (state != null) {
            state.removeAllSolidSurfaces(this);
        }
        roles[2] = false;
        numRoles--;
        if (numRoles == 0) {
            setObject(null);
            if (parent != null) {
                parent.removeChild(this);
            }
        }
    }
    
    final void addAsCollisionHitbox(CollisionMode collisionMode) {
        if (state != null && collisionMode != CollisionMode.NONE) {
            state.addCollisionHitbox(this);
        }
        roles[3] = true;
        numRoles++;
    }
    
    final void removeAsCollisionHitbox(CollisionMode collisionMode) {
        if (state != null && collisionMode != CollisionMode.NONE) {
            state.removeCollisionHitbox(this);
        }
        roles[3] = false;
        numRoles--;
        if (numRoles == 0) {
            setObject(null);
            if (parent != null) {
                parent.removeChild(this);
            }
        }
    }
    
    public final LevelState getGameState() {
        return state;
    }
    
    final void setGameState(LevelState state) {
        this.state = state;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.setGameState(state);
            }
        }
    }
    
    public final LevelVector getRelPosition() {
        return new LevelVector(relPosition);
    }
    
    public final double getRelX() {
        return relPosition.getX();
    }
    
    public final double getRelY() {
        return relPosition.getY();
    }
    
    public final void setRelPosition(LevelVector relPosition) {
        this.relPosition.copy(relPosition);
        recursivelyUpdateAbsPosition();
    }
    
    public final void setRelPosition(double relX, double relY) {
        relPosition.setCoordinates(relX, relY);
        recursivelyUpdateAbsPosition();
    }
    
    public final void setRelX(double relX) {
        relPosition.setX(relX);
        recursivelyUpdateAbsPosition();
    }
    
    public final void setRelY(double relY) {
        relPosition.setY(relY);
        recursivelyUpdateAbsPosition();
    }
    
    public final LevelVector getAbsPosition() {
        return new LevelVector(absPosition);
    }
    
    public final double getAbsX() {
        return absPosition.getX();
    }
    
    public final double getAbsY() {
        return absPosition.getY();
    }
    
    private void updateAbsPosition() {
        if (parent == null) {
            absPosition.copy(relPosition);
        } else {
            absPosition.copy(parent.absPosition).add(new LevelVector(relPosition).relativeTo(parent));
        }
        updateBoundaries();
    }
    
    private void recursivelyUpdateAbsPosition() {
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsPosition();
            }
        }
    }
    
    public final boolean getRelXFlip() {
        return relXFlip;
    }
    
    public final int getRelXSign() {
        return (relXFlip ? -1 : 1);
    }
    
    public final void setRelXFlip(boolean relXFlip) {
        this.relXFlip = relXFlip;
        absXFlip = (parent == null ? false : parent.absXFlip) ^ relXFlip;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsXFlip();
            }
        }
        updateAbsXFlipActions();
    }
    
    public final boolean getAbsXFlip() {
        return absXFlip;
    }
    
    public final int getAbsXSign() {
        return (absXFlip ? -1 : 1);
    }
    
    void updateAbsXFlipActions() {}
    
    private void recursivelyUpdateAbsXFlip() {
        absXFlip = parent.absXFlip ^ relXFlip;
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsXFlip();
            }
        }
        updateAbsXFlipActions();
    }
    
    public final boolean getRelYFlip() {
        return relYFlip;
    }
    
    public final int getRelYSign() {
        return (relYFlip ? -1 : 1);
    }
    
    public final void setRelYFlip(boolean relYFlip) {
        this.relYFlip = relYFlip;
        absYFlip = (parent == null ? false : parent.absYFlip) ^ relYFlip;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsYFlip();
            }
        }
        updateAbsYFlipActions();
    }
    
    public final boolean getAbsYFlip() {
        return absYFlip;
    }
    
    public final int getAbsYSign() {
        return (absYFlip ? -1 : 1);
    }
    
    void updateAbsYFlipActions() {}
    
    private void recursivelyUpdateAbsYFlip() {
        absYFlip = parent.absYFlip ^ relYFlip;
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsYFlip();
            }
        }
        updateAbsYFlipActions();
    }
    
    private double modAngle(double angle) {
        angle %= 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    public final double getRelAngle() {
        return relAngle;
    }
    
    public final double getRelAngleX() {
        return relAngleX;
    }
    
    public final double getRelAngleY() {
        return relAngleY;
    }
    
    public final void setRelAngle(double relAngle) {
        this.relAngle = modAngle(relAngle);
        double radians = Math.toRadians(relAngle);
        relAngleX = FastTrig.cos(radians);
        relAngleY = -FastTrig.sin(radians);
        updateAbsAngle();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsAngle();
            }
        }
        updateAbsAngleActions();
    }
    
    public final void changeRelAngle(double relAngle) {
        setRelAngle(this.relAngle + relAngle);
    }
    
    public final double getAbsAngle() {
        return absAngle;
    }
    
    public final double getAbsAngleX() {
        return absAngleX;
    }
    
    public final double getAbsAngleY() {
        return absAngleY;
    }
    
    private void updateAbsAngle() {
        if (parent == null) {
            absAngle = relAngle;
        } else {
            double angle = relAngle;
            if (parent.absXFlip) {
                angle = 180 - angle;
            }
            if (parent.absYFlip) {
                angle = 360 - angle;
            }
            absAngle = modAngle(parent.absAngle + angle);
        }
        double radians = Math.toRadians(absAngle);
        absAngleX = FastTrig.cos(radians);
        absAngleY = -FastTrig.sin(radians);
    }
    
    void updateAbsAngleActions() {}
    
    private void recursivelyUpdateAbsAngle() {
        updateAbsAngle();
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsAngle();
            }
        }
        updateAbsAngleActions();
    }
    
    public abstract double getLeftEdge();
    
    public abstract double getRightEdge();
    
    public abstract double getTopEdge();
    
    public abstract double getBottomEdge();
    
    public final double distanceTo(Hitbox hitbox) {
        return LevelVector.distanceBetween(getAbsX(), getAbsY(), hitbox.getAbsX(), hitbox.getAbsY());
    }
    
    public final double angleTo(Hitbox hitbox) {
        return LevelVector.angleBetween(getAbsX(), getAbsY(), hitbox.getAbsX(), hitbox.getAbsY());
    }
    
    private static boolean circleIntersectsLineSegment(LevelVector center, double radius, LevelVector start, LevelVector diff) {
        //Credit to bobobobo of StackOverflow for the algorithm.
        LevelVector f = LevelVector.sub(start, center);
        double a = diff.dot(diff);
        double b = 2*f.dot(diff);
        double c = f.dot(f) - radius*radius;
        double disc = b*b - 4*a*c;
        if (disc < 0) {
            return false;
        }
        disc = Math.sqrt(disc);
        double t1 = (-b - disc)/(2*a);
        double t2 = (-b + disc)/(2*a);
        return (t1 > 0 && t1 < 1) || (t2 > 0 && t2 < 1);
    }
    
    private static boolean circleIntersectsPolygon(LevelVector center, double radius, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            return center.distanceTo(polygon.getAbsPosition()) < radius;
        } else if (numVertices == 1) {
            return center.distanceTo(polygon.getAbsVertex(0)) < radius;
        }
        LevelVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) {
            return circleIntersectsLineSegment(center, radius, firstVertex, polygon.getAbsVertex(1).sub(firstVertex));
        }
        LevelVector[] vertices = new LevelVector[numVertices];
        vertices[0] = firstVertex;
        LevelVector[] diffs = new LevelVector[numVertices];
        for (int i = 0; i < numVertices - 1; i++) {
            vertices[i + 1] = polygon.getAbsVertex(i + 1);
            diffs[i] = LevelVector.sub(vertices[i + 1], vertices[i]);
            if (circleIntersectsLineSegment(center, radius, vertices[i], diffs[i])) {
                return true;
            }
        }
        diffs[numVertices - 1] = LevelVector.sub(firstVertex, vertices[numVertices - 1]);
        if (circleIntersectsLineSegment(center, radius, vertices[numVertices - 1], diffs[numVertices - 1])) {
            return true;
        }
        return pointIntersectsPolygon(center, polygon.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean circleIntersectsRectangle(LevelVector center, double radius, double x1, double y1, double x2, double y2) {
        if (center.getX() > x1 && center.getX() < x2 && center.getY() > y1 && center.getY() < y2) {
            return true;
        }
        LevelVector horizontalDiff = new LevelVector(x2 - x1, 0);
        LevelVector verticalDiff = new LevelVector(0, y2 - y1);
        LevelVector topLeft = new LevelVector(x1, y1);
        return circleIntersectsLineSegment(center, radius, topLeft, horizontalDiff)
                || circleIntersectsLineSegment(center, radius, new LevelVector(x1, y2), horizontalDiff)
                || circleIntersectsLineSegment(center, radius, topLeft, verticalDiff)
                || circleIntersectsLineSegment(center, radius, new LevelVector(x2, y1), verticalDiff);
    }
    
    private static boolean circleIntersectsSlope(LevelVector center, double radius, SlopeHitbox slope) {
        if (!slope.isSloping()) {
            return circleIntersectsRectangle(center, radius, slope.getLeftEdge(), slope.getTopEdge(), slope.getRightEdge(), slope.getBottomEdge());
        }
        LevelVector[] vertices = new LevelVector[3];
        vertices[0] = slope.getAbsPosition();
        LevelVector[] diffs = new LevelVector[3];
        diffs[0] = slope.getAbsDifference();
        if (circleIntersectsLineSegment(center, radius, vertices[0], diffs[0])) {
            return true;
        } else if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return false;
        }
        vertices[1] = slope.getPosition2();
        diffs[1] = new LevelVector(-slope.getAbsDX(), 0);
        if (circleIntersectsLineSegment(center, radius, vertices[1], diffs[1])) {
            return true;
        }
        vertices[2] = LevelVector.add(vertices[1], diffs[1]);
        diffs[2] = new LevelVector(0, -slope.getAbsDY());
        if (circleIntersectsLineSegment(center, radius, vertices[2], diffs[2])) {
            return true;
        }
        return pointIntersectsPolygon(center, slope.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean lineSegmentIntersectsPoint(LevelVector start, LevelVector diff, LevelVector point) {
        LevelVector relPoint = LevelVector.sub(point, start);
        if (diff.getX() == 0) {
            return relPoint.getX() == 0 && Math.signum(relPoint.getY()) == Math.signum(diff.getY()) && Math.abs(relPoint.getY()) < Math.abs(diff.getY());
        }
        return LevelVector.cross(relPoint, diff) == 0 && Math.signum(relPoint.getX()) == Math.signum(diff.getX()) && Math.abs(relPoint.getX()) < Math.abs(diff.getX());
    }
    
    private static boolean lineSegmentIntersectsPolygon(LevelVector start, LevelVector diff, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            return lineSegmentIntersectsPoint(start, diff, polygon.getAbsPosition());
        } else if (numVertices == 1) {
            return lineSegmentIntersectsPoint(start, diff, polygon.getAbsVertex(0));
        }
        LevelVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) {
            return LevelVector.lineSegmentsIntersect(start, diff, firstVertex, polygon.getAbsVertex(1).sub(firstVertex));
        }
        LevelVector[] vertices = new LevelVector[numVertices];
        vertices[0] = firstVertex;
        LevelVector[] diffs = new LevelVector[numVertices];
        for (int i = 0; i < numVertices - 1; i++) {
            vertices[i + 1] = polygon.getAbsVertex(i + 1);
            diffs[i] = LevelVector.sub(vertices[i + 1], vertices[i]);
            if (LevelVector.lineSegmentsIntersect(start, diff, vertices[i], diffs[i])) {
                return true;
            }
        }
        diffs[numVertices - 1] = LevelVector.sub(firstVertex, vertices[numVertices - 1]);
        if (LevelVector.lineSegmentsIntersect(start, diff, vertices[numVertices - 1], diffs[numVertices - 1])) {
            return true;
        }
        return pointIntersectsPolygon(start, polygon.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean lineSegmentIntersectsRectangle(LevelVector start, LevelVector diff, double x1, double y1, double x2, double y2) {
        if (start.getX() > x1 && start.getX() < x2 && start.getY() > y1 && start.getY() < y2) {
            return true;
        }
        double lineX2 = start.getX() + diff.getX();
        double lineY2 = start.getY() + diff.getY();
        if (lineX2 > x1 && lineX2 < x2 && lineY2 > y1 && lineY2 < y2) {
            return true;
        }
        LevelVector horizontalDiff = new LevelVector(x2 - x1, 0);
        LevelVector verticalDiff = new LevelVector(0, y2 - y1);
        LevelVector topLeft = new LevelVector(x1, y1);
        return LevelVector.lineSegmentsIntersect(start, diff, topLeft, horizontalDiff)
                || LevelVector.lineSegmentsIntersect(start, diff, new LevelVector(x1, y2), horizontalDiff)
                || LevelVector.lineSegmentsIntersect(start, diff, topLeft, verticalDiff)
                || LevelVector.lineSegmentsIntersect(start, diff, new LevelVector(x2, y1), verticalDiff);
    }
    
    private static boolean lineSegmentIntersectsSlope(LevelVector start, LevelVector diff, SlopeHitbox slope) {
        if (!slope.isSloping()) {
            return lineSegmentIntersectsRectangle(start, diff, slope.getLeftEdge(), slope.getTopEdge(), slope.getRightEdge(), slope.getBottomEdge());
        }
        LevelVector[] vertices = new LevelVector[3];
        vertices[0] = slope.getAbsPosition();
        LevelVector[] diffs = new LevelVector[3];
        diffs[0] = slope.getAbsDifference();
        if (LevelVector.lineSegmentsIntersect(start, diff, vertices[0], diffs[0])) {
            return true;
        } else if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return false;
        }
        vertices[1] = slope.getPosition2();
        diffs[1] = new LevelVector(-slope.getAbsDX(), 0);
        if (LevelVector.lineSegmentsIntersect(start, diff, vertices[1], diffs[1])) {
            return true;
        }
        vertices[2] = LevelVector.add(vertices[1], diffs[1]);
        diffs[2] = new LevelVector(0, -slope.getAbsDY());
        if (LevelVector.lineSegmentsIntersect(start, diff, vertices[2], diffs[2])) {
            return true;
        }
        return pointIntersectsPolygon(start, slope.getLeftEdge() - 1, vertices, diffs);
    }
    
    //Credit to Mecki of StackOverflow for the point-polygon intersection algorithm.
    
    private static boolean pointIntersectsPolygon(LevelVector point, double startX, LevelVector[] vertices, LevelVector[] diffs) {
        LevelVector start = new LevelVector(startX, point.getY());
        LevelVector diff = new LevelVector(point.getX() - startX, 0);
        boolean intersects = false;
        for (int i = 0; i < vertices.length; i++) {
            if (LevelVector.lineSegmentsIntersect(start, diff, vertices[i], diffs[i])) {
                intersects = !intersects;
            }
        }
        return intersects;
    }
    
    private static boolean pointIntersectsPolygon(LevelVector point, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            LevelVector position = polygon.getAbsPosition();
            return point.getX() == position.getX() && point.getY() == position.getY();
        } else if (numVertices == 1) {
            LevelVector position = polygon.getAbsVertex(0);
            return point.getX() == position.getX() && point.getY() == position.getY();
        }
        LevelVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) {
            return lineSegmentIntersectsPoint(firstVertex, polygon.getAbsVertex(1).sub(firstVertex), point);
        }
        double startX = polygon.getLeftEdge() - 1;
        LevelVector start = new LevelVector(startX, point.getY());
        LevelVector diff = new LevelVector(point.getX() - startX, 0);
        LevelVector lastVertex = firstVertex;
        boolean intersects = false;
        for (int i = 1; i < numVertices; i++) {
            LevelVector vertex = polygon.getAbsVertex(i);
            if (LevelVector.lineSegmentsIntersect(start, diff, lastVertex, LevelVector.sub(vertex, lastVertex))) {
                intersects = !intersects;
            }
            lastVertex = vertex;
        }
        if (LevelVector.lineSegmentsIntersect(start, diff, lastVertex, LevelVector.sub(firstVertex, lastVertex))) {
            intersects = !intersects;
        }
        return intersects;
    }
    
    private static boolean pointIntersectsRectangle(LevelVector point, double x1, double y1, double x2, double y2) {
        return point.getX() > x1 && point.getX() < x2
                && point.getY() > y1 && point.getY() < y2;
    }
    
    private static boolean pointIntersectsSlope(LevelVector point, SlopeHitbox slope) {
        if (!slope.isSloping()) {
            return true;
        } else if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return lineSegmentIntersectsPoint(slope.getAbsPosition(), slope.getAbsDifference(), point);
        }
        LevelVector[] vertices = {slope.getAbsPosition(), slope.getPosition2(), null};
        LevelVector[] diffs = {slope.getAbsDifference(), new LevelVector(-slope.getAbsDX(), 0), new LevelVector(0, -slope.getAbsDY())};
        vertices[2] = LevelVector.add(vertices[1], diffs[1]);
        return pointIntersectsPolygon(point, slope.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean polygonsIntersect(PolygonHitbox polygon1, PolygonHitbox polygon2) {
        int numVertices1 = polygon1.getNumVertices();
        int numVertices2 = polygon2.getNumVertices();
        if (numVertices1 == 0) {
            return pointIntersectsPolygon(polygon1.getAbsPosition(), polygon2);
        } else if (numVertices2 == 0) {
            return pointIntersectsPolygon(polygon2.getAbsPosition(), polygon1);
        } else if (numVertices1 == 1) {
            return pointIntersectsPolygon(polygon1.getAbsVertex(0), polygon2);
        } else if (numVertices2 == 1) {
            return pointIntersectsPolygon(polygon2.getAbsVertex(0), polygon1);
        }
        LevelVector firstVertex1 = polygon1.getAbsVertex(0);
        if (numVertices1 == 2) {
            return lineSegmentIntersectsPolygon(firstVertex1, polygon1.getAbsVertex(1).sub(firstVertex1), polygon2);
        }
        LevelVector firstVertex2 = polygon2.getAbsVertex(0);
        if (numVertices2 == 2) {
            return lineSegmentIntersectsPolygon(firstVertex2, polygon2.getAbsVertex(1).sub(firstVertex2), polygon1);
        }
        LevelVector secondVertex2 = polygon2.getAbsVertex(1);
        LevelVector firstDiff2 = LevelVector.sub(secondVertex2, firstVertex2);
        LevelVector[] vertices1 = new LevelVector[numVertices1];
        vertices1[0] = firstVertex1;
        LevelVector[] diffs1 = new LevelVector[numVertices1];
        for (int i = 0; i < numVertices1 - 1; i++) {
            vertices1[i + 1] = polygon1.getAbsVertex(i + 1);
            diffs1[i] = LevelVector.sub(vertices1[i + 1], vertices1[i]);
            if (LevelVector.lineSegmentsIntersect(firstVertex2, firstDiff2, vertices1[i], diffs1[i])) {
                return true;
            }
        }
        diffs1[numVertices1 - 1] = LevelVector.sub(firstVertex1, vertices1[numVertices1 - 1]);
        if (LevelVector.lineSegmentsIntersect(firstVertex2, firstDiff2, vertices1[numVertices1 - 1], diffs1[numVertices1 - 1])) {
            return true;
        }
        LevelVector[] vertices2 = new LevelVector[numVertices2];
        vertices2[0] = firstVertex2;
        vertices2[1] = secondVertex2;
        LevelVector[] diffs2 = new LevelVector[numVertices2];
        diffs2[0] = firstDiff2;
        for (int i = 1; i < numVertices2 - 1; i++) {
            vertices2[i + 1] = polygon2.getAbsVertex(i);
            diffs2[i] = LevelVector.sub(vertices2[i + 1], vertices2[i]);
            for (int j = 0; j < numVertices1; j++) {
                if (LevelVector.lineSegmentsIntersect(vertices2[i], diffs2[i], vertices1[j], diffs1[j])) {
                    return true;
                }
            }
        }
        diffs2[numVertices2 - 1] = LevelVector.sub(firstVertex2, vertices2[numVertices2 - 1]);
        for (int j = 0; j < numVertices1; j++) {
            if (LevelVector.lineSegmentsIntersect(vertices2[numVertices2 - 1], diffs2[numVertices2 - 1], vertices1[j], diffs1[j])) {
                return true;
            }
        }
        return pointIntersectsPolygon(firstVertex1, polygon2.getLeftEdge() - 1, vertices2, diffs2)
                || pointIntersectsPolygon(firstVertex2, polygon1.getLeftEdge() - 1, vertices1, diffs1);
    }
    
    private static boolean polygonIntersectsRectangle(PolygonHitbox polygon, double x1, double y1, double x2, double y2) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            return pointIntersectsRectangle(polygon.getAbsPosition(), x1, y1, x2, y2);
        } else if (numVertices == 1) {
            return pointIntersectsRectangle(polygon.getAbsVertex(0), x1, y1, x2, y2);
        }
        if (numVertices == 2) {
            LevelVector firstVertex = polygon.getAbsVertex(0);
            return lineSegmentIntersectsRectangle(firstVertex, polygon.getAbsVertex(1).sub(firstVertex), x1, y1, x2, y2);
        }
        LevelVector[] vertices = new LevelVector[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = polygon.getAbsVertex(i);
            if (pointIntersectsRectangle(vertices[i], x1, y1, x2, y2)) {
                return true;
            }
        }
        LevelVector horizontalDiff = new LevelVector(x2 - x1, 0);
        LevelVector verticalDiff = new LevelVector(0, y2 - y1);
        LevelVector topLeft = new LevelVector(x1, y1);
        LevelVector bottomLeft = new LevelVector(x1, y2);
        LevelVector topRight = new LevelVector(x2, y1);
        for (int i = 0; i < numVertices - 1; i++) {
            LevelVector diff = LevelVector.sub(vertices[i + 1], vertices[i]);
            if (LevelVector.lineSegmentsIntersect(vertices[i], diff, topLeft, horizontalDiff)
                    || LevelVector.lineSegmentsIntersect(vertices[i], diff, bottomLeft, horizontalDiff)
                    || LevelVector.lineSegmentsIntersect(vertices[i], diff, topLeft, verticalDiff)
                    || LevelVector.lineSegmentsIntersect(vertices[i], diff, topRight, verticalDiff)) {
                return true;
            }
        }
        LevelVector start = vertices[numVertices - 1];
        LevelVector diff = LevelVector.sub(vertices[0], start);
        return LevelVector.lineSegmentsIntersect(start, diff, topLeft, horizontalDiff)
                || LevelVector.lineSegmentsIntersect(start, diff, bottomLeft, horizontalDiff)
                || LevelVector.lineSegmentsIntersect(start, diff, topLeft, verticalDiff)
                || LevelVector.lineSegmentsIntersect(start, diff, topRight, verticalDiff);
    }
    
    private static boolean polygonIntersectsSlope(PolygonHitbox polygon, SlopeHitbox slope) {
        if (!slope.isSloping()) {
            return polygonIntersectsRectangle(polygon, slope.getLeftEdge(), slope.getTopEdge(), slope.getRightEdge(), slope.getBottomEdge());
        } else if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return lineSegmentIntersectsPolygon(slope.getAbsPosition(), slope.getAbsDifference(), polygon);
        }
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            return pointIntersectsSlope(polygon.getAbsPosition(), slope);
        } else if (numVertices == 1) {
            return pointIntersectsSlope(polygon.getAbsVertex(0), slope);
        }
        LevelVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) {
            return lineSegmentIntersectsSlope(firstVertex, polygon.getAbsVertex(1).sub(firstVertex), slope);
        }
        LevelVector[] vertices = new LevelVector[numVertices];
        vertices[0] = firstVertex;
        LevelVector[] diffs = new LevelVector[numVertices];
        LevelVector[] slopeVertices = new LevelVector[3];
        slopeVertices[0] = slope.getAbsPosition();
        LevelVector[] slopeDiffs = new LevelVector[3];
        slopeDiffs[0] = slope.getAbsDifference();
        for (int i = 0; i < numVertices - 1; i++) {
            vertices[i + 1] = polygon.getAbsVertex(i + 1);
            diffs[i] = LevelVector.sub(vertices[i + 1], vertices[i]);
            if (LevelVector.lineSegmentsIntersect(vertices[i], diffs[i], slopeVertices[0], slopeDiffs[0])) {
                return true;
            }
        }
        diffs[numVertices - 1] = LevelVector.sub(firstVertex, vertices[numVertices - 1]);
        if (LevelVector.lineSegmentsIntersect(vertices[numVertices - 1], diffs[numVertices - 1], slopeVertices[0], slopeDiffs[0])) {
            return true;
        }
        slopeVertices[1] = slope.getPosition2();
        slopeDiffs[1] = new LevelVector(-slope.getAbsDX(), 0);
        for (int i = 0; i < numVertices; i++) {
            if (LevelVector.lineSegmentsIntersect(vertices[i], diffs[i], slopeVertices[1], slopeDiffs[1])) {
                return true;
            }
        }
        slopeVertices[2] = LevelVector.add(slopeVertices[1], slopeDiffs[1]);
        slopeDiffs[2] = new LevelVector(0, -slope.getAbsDY());
        for (int i = 0; i < numVertices; i++) {
            if (LevelVector.lineSegmentsIntersect(vertices[i], diffs[i], slopeVertices[2], slopeDiffs[2])) {
                return true;
            }
        }
        return pointIntersectsPolygon(slopeVertices[0], polygon.getLeftEdge() - 1, vertices, diffs)
                || pointIntersectsPolygon(firstVertex, slope.getLeftEdge() - 1, slopeVertices, slopeDiffs);
    }
    
    private static boolean rectanglesIntersect(double x1_1, double y1_1, double x2_1, double y2_1, double x1_2, double y1_2, double x2_2, double y2_2) {
        return x2_1 > x1_2 && x1_1 < x2_2 && y2_1 > y1_2 && y1_1 < y2_2;
    }
    
    private static boolean rectangleIntersectsSlope(double x1, double y1, double x2, double y2, SlopeHitbox slope) {
        if (!slope.isSloping()) {
            return rectanglesIntersect(x1, y1, x2, y2, slope.getLeftEdge(), slope.getTopEdge(), slope.getRightEdge(), slope.getBottomEdge());
        } else if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return lineSegmentIntersectsRectangle(slope.getAbsPosition(), slope.getAbsDifference(), x1, y1, x2, y2);
        }
        LevelVector[] vertices = new LevelVector[3];
        vertices[0] = slope.getAbsPosition();
        if (pointIntersectsRectangle(vertices[0], x1, y1, x2, y2)) {
            return true;
        }
        LevelVector[] diffs = new LevelVector[3];
        diffs[0] = slope.getAbsDifference();
        vertices[1] = slope.getPosition2();
        if (pointIntersectsRectangle(vertices[1], x1, y1, x2, y2)) {
            return true;
        }
        diffs[1] = new LevelVector(-slope.getAbsDX(), 0);
        vertices[2] = LevelVector.add(vertices[1], diffs[1]);
        if (pointIntersectsRectangle(vertices[2], x1, y1, x2, y2)) {
            return true;
        }
        diffs[2] = new LevelVector(0, -slope.getAbsDY());
        LevelVector horizontalDiff = new LevelVector(x2 - x1, 0);
        LevelVector verticalDiff = new LevelVector(0, y2 - y1);
        LevelVector topLeft = new LevelVector(x1, y1);
        LevelVector bottomLeft = new LevelVector(x1, y2);
        LevelVector topRight = new LevelVector(x2, y1);
        for (int i = 0; i < 3; i++) {
            if (LevelVector.lineSegmentsIntersect(topLeft, horizontalDiff, vertices[i], diffs[i])
                    || LevelVector.lineSegmentsIntersect(bottomLeft, horizontalDiff, vertices[i], diffs[i])
                    || LevelVector.lineSegmentsIntersect(topLeft, verticalDiff, vertices[i], diffs[i])
                    || LevelVector.lineSegmentsIntersect(topRight, verticalDiff, vertices[i], diffs[i])) {
                return true;
            }
        }
        return pointIntersectsPolygon(new LevelVector(x1, y1), slope.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean slopesIntersect(SlopeHitbox slope1, SlopeHitbox slope2) {
        if (!slope1.isSloping()) {
            return rectangleIntersectsSlope(slope1.getLeftEdge(), slope1.getTopEdge(), slope1.getRightEdge(), slope1.getBottomEdge(), slope2);
        } else if (!slope2.isSloping()) {
            return rectangleIntersectsSlope(slope2.getLeftEdge(), slope2.getTopEdge(), slope2.getRightEdge(), slope2.getBottomEdge(), slope1);
        } else if (!slope1.isPresentAbove() && !slope1.isPresentBelow()) {
            return lineSegmentIntersectsSlope(slope1.getAbsPosition(), slope1.getAbsDifference(), slope2);
        } else if (!slope2.isPresentAbove() && !slope2.isPresentBelow()) {
            return lineSegmentIntersectsSlope(slope2.getAbsPosition(), slope2.getAbsDifference(), slope1);
        }
        LevelVector[] vertices1 = {slope1.getAbsPosition(), slope1.getPosition2(), null};
        LevelVector[] diffs1 = {slope1.getAbsDifference(), new LevelVector(-slope1.getAbsDX(), 0), new LevelVector(0, -slope1.getAbsDY())};
        vertices1[2] = LevelVector.add(vertices1[1], diffs1[1]);
        LevelVector[] vertices2 = new LevelVector[3];
        vertices2[0] = slope2.getAbsPosition();
        LevelVector[] diffs2 = new LevelVector[3];
        diffs2[0] = slope2.getAbsDifference();
        for (int i = 0; i < 3; i++) {
            if (LevelVector.lineSegmentsIntersect(vertices1[i], diffs1[i], vertices2[0], diffs2[0])) {
                return true;
            }
        }
        vertices2[1] = slope2.getPosition2();
        diffs2[1] = new LevelVector(-slope2.getAbsDX(), 0);
        for (int i = 0; i < 3; i++) {
            if (LevelVector.lineSegmentsIntersect(vertices1[i], diffs1[i], vertices2[1], diffs2[1])) {
                return true;
            }
        }
        vertices2[2] = LevelVector.add(vertices2[1], diffs2[1]);
        diffs2[2] = new LevelVector(0, -slope2.getAbsDY());
        for (int i = 0; i < 3; i++) {
            if (LevelVector.lineSegmentsIntersect(vertices1[i], diffs1[i], vertices2[2], diffs2[2])) {
                return true;
            }
        }
        return pointIntersectsPolygon(vertices1[0], slope2.getLeftEdge() - 1, vertices2, diffs2)
                || pointIntersectsPolygon(vertices2[0], slope1.getLeftEdge() - 1, vertices1, diffs1);
    }
    
    public final boolean overlaps(Hitbox hitbox) {
        return overlap(this, hitbox);
    }
    
    public static final boolean overlap(Hitbox hitbox1, Hitbox hitbox2) {
        if (hitbox1 != hitbox2
                && (hitbox1.state == hitbox2.state || hitbox1.state == null || hitbox2.state == null)
                && hitbox1.getLeftEdge() <= hitbox2.getRightEdge()
                && hitbox1.getRightEdge() >= hitbox2.getLeftEdge()
                && hitbox1.getTopEdge() <= hitbox2.getBottomEdge()
                && hitbox1.getBottomEdge() >= hitbox2.getTopEdge()) {
            if (hitbox1 instanceof CompositeHitbox) {
                for (Hitbox component : ((CompositeHitbox)hitbox1).components.values()) {
                    if (overlap(component, hitbox2)) {
                        return true;
                    }
                }
                return false;
            } else if (hitbox2 instanceof CompositeHitbox) {
                for (Hitbox component : ((CompositeHitbox)hitbox2).components.values()) {
                    if (overlap(hitbox1, component)) {
                        return true;
                    }
                }
                return false;
            } else if (hitbox1 instanceof CircleHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return hitbox1.distanceTo(hitbox2) < ((CircleHitbox)hitbox1).getAbsRadius() + ((CircleHitbox)hitbox2).getAbsRadius();
                } else if (hitbox2 instanceof LineHitbox) {
                    return circleIntersectsLineSegment(hitbox1.absPosition, ((CircleHitbox)hitbox1).getAbsRadius(), hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference());
                } else if (hitbox2 instanceof PointHitbox) {
                    return hitbox1.distanceTo(hitbox2) < ((CircleHitbox)hitbox1).getAbsRadius();
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return circleIntersectsPolygon(hitbox1.absPosition, ((CircleHitbox)hitbox1).getAbsRadius(), (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return circleIntersectsRectangle(hitbox1.absPosition, ((CircleHitbox)hitbox1).getAbsRadius(), hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                } else if (hitbox2 instanceof SlopeHitbox) {
                    return circleIntersectsSlope(hitbox1.absPosition, ((CircleHitbox)hitbox1).getAbsRadius(), (SlopeHitbox)hitbox2);
                }
            } else if (hitbox1 instanceof LineHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsLineSegment(hitbox2.absPosition, ((CircleHitbox)hitbox2).getAbsRadius(), hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference());
                } else if (hitbox2 instanceof LineHitbox) {
                    return LevelVector.directSegsIntersect(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference());
                } else if (hitbox2 instanceof PointHitbox) {
                    return lineSegmentIntersectsPoint(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), hitbox2.absPosition);
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return lineSegmentIntersectsPolygon(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return lineSegmentIntersectsRectangle(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                } else if (hitbox2 instanceof SlopeHitbox) {
                    return lineSegmentIntersectsSlope(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), (SlopeHitbox)hitbox2);
                }
            } else if (hitbox1 instanceof PointHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return hitbox1.distanceTo(hitbox2) < ((CircleHitbox)hitbox2).getAbsRadius();
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsPoint(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), hitbox1.absPosition);
                } else if (hitbox2 instanceof PointHitbox) {
                    return true;
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return pointIntersectsPolygon(hitbox1.absPosition, (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return pointIntersectsRectangle(hitbox1.absPosition, hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                } else if (hitbox2 instanceof SlopeHitbox) {
                    return pointIntersectsSlope(hitbox1.absPosition, (SlopeHitbox)hitbox2);
                }
            } else if (hitbox1 instanceof PolygonHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsPolygon(hitbox2.absPosition, ((CircleHitbox)hitbox2).getAbsRadius(), (PolygonHitbox)hitbox1);
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsPolygon(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), (PolygonHitbox)hitbox1);
                } else if (hitbox2 instanceof PointHitbox) {
                    return pointIntersectsPolygon(hitbox2.absPosition, (PolygonHitbox)hitbox1);
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return polygonsIntersect((PolygonHitbox)hitbox1, (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return polygonIntersectsRectangle((PolygonHitbox)hitbox1, hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                } else if (hitbox2 instanceof SlopeHitbox) {
                    return polygonIntersectsSlope((PolygonHitbox)hitbox1, (SlopeHitbox)hitbox2);
                }
            } else if (hitbox1 instanceof RectangleHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsRectangle(hitbox2.absPosition, ((CircleHitbox)hitbox2).getAbsRadius(), hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsRectangle(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof PointHitbox) {
                    return pointIntersectsRectangle(hitbox2.absPosition, hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return polygonIntersectsRectangle((PolygonHitbox)hitbox2, hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return rectanglesIntersect(hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge(),
                            hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                } else if (hitbox2 instanceof SlopeHitbox) {
                    return rectangleIntersectsSlope(hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge(), (SlopeHitbox)hitbox2);
                }
            } else if (hitbox1 instanceof SlopeHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsSlope(hitbox2.absPosition, ((CircleHitbox)hitbox2).getAbsRadius(), (SlopeHitbox)hitbox1);
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsSlope(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), (SlopeHitbox)hitbox1);
                } else if (hitbox2 instanceof PointHitbox) {
                    return pointIntersectsSlope(hitbox2.absPosition, (SlopeHitbox)hitbox1);
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return polygonIntersectsSlope((PolygonHitbox)hitbox2, (SlopeHitbox)hitbox1);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return rectangleIntersectsSlope(hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge(), (SlopeHitbox)hitbox1);
                } else if (hitbox2 instanceof SlopeHitbox) {
                    return slopesIntersect((SlopeHitbox)hitbox1, (SlopeHitbox)hitbox2);
                }
            }
        }
        return false;
    }
    
    public final boolean intersectsSolidHitbox(Hitbox solidHitbox) {
        return intersectsSolidHitbox(this, solidHitbox);
    }
    
    public static final boolean intersectsSolidHitbox(Hitbox collisionHitbox, Hitbox solidHitbox) {
        if (collisionHitbox.getObject() != solidHitbox.getObject()
                && collisionHitbox.state != null && collisionHitbox.state == solidHitbox.state
                && collisionHitbox.getLeftEdge() <= solidHitbox.getRightEdge()
                && collisionHitbox.getRightEdge() >= solidHitbox.getLeftEdge()
                && collisionHitbox.getTopEdge() <= solidHitbox.getBottomEdge()
                && collisionHitbox.getBottomEdge() >= solidHitbox.getTopEdge()) {
            if (collisionHitbox instanceof CompositeHitbox) {
                for (Hitbox component : ((CompositeHitbox)collisionHitbox).components.values()) {
                    if (intersectsSolidHitbox(component, solidHitbox)) {
                        return true;
                    }
                }
                return false;
            } else if (solidHitbox instanceof CompositeHitbox) {
                for (Hitbox component : ((CompositeHitbox)solidHitbox).components.values()) {
                    if (intersectsSolidHitbox(collisionHitbox, component)) {
                        return true;
                    }
                }
                return false;
            } else if (collisionHitbox instanceof CircleHitbox) {
                if (solidHitbox instanceof SlopeHitbox) {
                    return circleIntersectsSlope(collisionHitbox.absPosition, ((CircleHitbox)collisionHitbox).getAbsRadius(), (SlopeHitbox)solidHitbox);
                }
                return circleIntersectsRectangle(collisionHitbox.absPosition, ((CircleHitbox)collisionHitbox).getAbsRadius(), solidHitbox.getLeftEdge(), solidHitbox.getTopEdge(), solidHitbox.getRightEdge(), solidHitbox.getBottomEdge());
            } else if (collisionHitbox instanceof LineHitbox) {
                if (solidHitbox instanceof SlopeHitbox) {
                    return lineSegmentIntersectsSlope(collisionHitbox.absPosition, ((LineHitbox)collisionHitbox).getAbsDifference(), (SlopeHitbox)solidHitbox);
                }
                return lineSegmentIntersectsRectangle(collisionHitbox.absPosition, ((LineHitbox)collisionHitbox).getAbsDifference(), solidHitbox.getLeftEdge(), solidHitbox.getTopEdge(), solidHitbox.getRightEdge(), solidHitbox.getBottomEdge());
            } else if (collisionHitbox instanceof PointHitbox) {
                if (solidHitbox instanceof SlopeHitbox) {
                    return pointIntersectsSlope(collisionHitbox.absPosition, (SlopeHitbox)solidHitbox);
                }
                return pointIntersectsRectangle(collisionHitbox.absPosition, solidHitbox.getLeftEdge(), solidHitbox.getTopEdge(), solidHitbox.getRightEdge(), solidHitbox.getBottomEdge());
            } else if (collisionHitbox instanceof PolygonHitbox) {
                if (solidHitbox instanceof SlopeHitbox) {
                    return polygonIntersectsSlope((PolygonHitbox)collisionHitbox, (SlopeHitbox)solidHitbox);
                }
                return polygonIntersectsRectangle((PolygonHitbox)collisionHitbox, solidHitbox.getLeftEdge(), solidHitbox.getTopEdge(), solidHitbox.getRightEdge(), solidHitbox.getBottomEdge());
            } else if (collisionHitbox instanceof RectangleHitbox) {
                if (solidHitbox instanceof SlopeHitbox) {
                    return rectangleIntersectsSlope(collisionHitbox.getLeftEdge(), collisionHitbox.getTopEdge(), collisionHitbox.getRightEdge(), collisionHitbox.getBottomEdge(), (SlopeHitbox)solidHitbox);
                }
                return rectanglesIntersect(collisionHitbox.getLeftEdge(), collisionHitbox.getTopEdge(), collisionHitbox.getRightEdge(), collisionHitbox.getBottomEdge(),
                        solidHitbox.getLeftEdge(), solidHitbox.getTopEdge(), solidHitbox.getRightEdge(), solidHitbox.getBottomEdge());
            } else if (collisionHitbox instanceof SlopeHitbox) {
                if (solidHitbox instanceof SlopeHitbox) {
                    return slopesIntersect((SlopeHitbox)collisionHitbox, (SlopeHitbox)solidHitbox);
                }
                return rectangleIntersectsSlope(solidHitbox.getLeftEdge(), solidHitbox.getTopEdge(), solidHitbox.getRightEdge(), solidHitbox.getBottomEdge(), (SlopeHitbox)collisionHitbox);
            }
        }
        return false;
    }
    
}
