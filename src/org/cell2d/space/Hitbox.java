package org.cell2d.space;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.cell2d.CellVector;
import org.cell2d.Direction;
import org.cell2d.Frac;

/**
 * <p>A Hitbox is a region of space that can be checked for intersection with
 * other regions. A Hitbox has a <i>position</i> at a single point that acts as
 * an origin point for the Hitbox's shape. This point is usually inside the
 * Hitbox, but may not always be. Hitboxes can be rotated around their positions
 * and flipped across horizontal and vertical axes through their positions.
 * Rotating a Hitbox will also rotate the axes along which it is flipped. As
 * with CellVectors, Hitboxes measure angles in degrees going counterclockwise
 * from directly right and normalize them to be between 0 and 360.</p>
 * 
 * <p>If a Hitbox is a component of a CompositeHitbox, its position, flipped
 * status, and angle of rotation are all relative to those of the
 * CompositeHitbox. The same is true of a Hitbox being used by a SpaceObject,
 * but not as its locator Hitbox, in relation to that SpaceObject's locator
 * Hitbox. To avoid confusion, all spatial information about a Hitbox is
 * specified as relative or absolute. For Hitboxes which are not located
 * relative to other Hitboxes in this way, the two types of information are
 * identical.</p>
 * 
 * <p>A Hitbox stores information on whether its surfaces in each Direction are
 * solid, but this only affects its behavior when being used as a SpaceObject's
 * solid Hitbox.</p>
 * @see SpaceObject
 * @see CompositeHitbox
 * @author Andrew Heyman
 */
public abstract class Hitbox {
    
    private Hitbox parent = null;
    private final Set<Hitbox> children = new HashSet<>();
    CompositeHitbox componentOf = null;
    EnumSet<Direction> solidSurfaces = EnumSet.noneOf(Direction.class);
    private SpaceObject object = null;
    final Set<HitboxRole> roles = EnumSet.noneOf(HitboxRole.class);
    SpaceState state = null;
    int[] cellRange = null;
    boolean scanned = false;
    int drawPriority = 0;
    int numCellRoles = 0;
    private final CellVector relPosition, absPosition;
    private boolean relXFlip = false;
    private boolean absXFlip = false;
    private boolean relYFlip = false;
    private boolean absYFlip = false;
    private double relAngle = 0;
    private long relAngleX = Frac.UNIT;
    private long relAngleY = 0;
    private double absAngle = 0;
    private long absAngleX = Frac.UNIT;
    private long absAngleY = 0;
    
    /**
     * Constructs a Hitbox with the specified relative position.
     * @param relPosition This Hitbox's relative position
     */
    public Hitbox(CellVector relPosition) {
        this.relPosition = new CellVector(relPosition);
        absPosition = new CellVector(relPosition);
    }
    
    /**
     * Constructs a Hitbox with the specified relative position.
     * @param relX The x-coordinate of this Hitbox's relative position
     * @param relY The y-coordinate of this Hitbox's relative position
     */
    public Hitbox(long relX, long relY) {
        this.relPosition = new CellVector(relX, relY);
        absPosition = new CellVector(relPosition);
    }
    
    /**
     * Returns a copy of this Hitbox with its relative position at the origin
     * that is not flipped or rotated.
     * @return A copy of this Hitbox
     */
    public abstract Hitbox getCopy();
    
    final Hitbox getParent() {
        return parent;
    }
    
    final boolean addChild(Hitbox child) {
        if (child.parent == null && child.object == null) {
            Hitbox ancestor = this;
            do {
                if (ancestor == child) {
                    return false;
                }
                ancestor = ancestor.parent;
            } while (ancestor != null);
            children.add(child);
            child.parent = this;
            child.recursivelyUpdateData();
            return true;
        }
        return false;
    }
    
    final boolean removeChild(Hitbox child) {
        if (child.parent == this) {
            children.remove(child);
            child.parent = null;
            child.recursivelyUpdateData();
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
    
    /**
     * Returns the CompositeHitbox that this Hitbox is a component of, or null
     * if it is not a component of one.
     * @return The CompositeHitbox that this Hitbox is a component of
     */
    public final CompositeHitbox getComponentOf() {
        return componentOf;
    }
    
    /**
     * Returns whether any of this Hitbox's surfaces are solid.
     * @return Whether any of this Hitbox's surfaces are solid
     */
    public final boolean isSolid() {
        return !solidSurfaces.isEmpty();
    }
    
    /**
     * Returns whether this Hitbox's surface in the specified Direction is
     * solid.
     * @param direction The Direction of the surface to be examined
     * @return Whether the surface in the specified Direction is solid
     */
    public final boolean surfaceIsSolid(Direction direction) {
        return solidSurfaces.contains(direction);
    }
    
    /**
     * Sets whether this Hitbox's surface in the specified Direction is solid.
     * @param direction The Direction of the surface whose solidity is to be set
     * @param solid Whether the surface in the specified Direction should be
     * solid
     */
    public final void setSurfaceSolid(Direction direction, boolean solid) {
        if (solid) {
            if (solidSurfaces.add(direction) && solidSurfaces.size() == 1
                    && roles.contains(HitboxRole.SOLID) && state != null) {
                state.addHitbox(this, HitboxRole.SOLID);
            }
        } else {
            if (solidSurfaces.remove(direction) && solidSurfaces.isEmpty()
                    && roles.contains(HitboxRole.SOLID) && state != null) {
                state.removeHitbox(this, HitboxRole.SOLID);
            }
        }
    }
    
    /**
     * Sets whether this Hitbox's surfaces in every direction are solid.
     * @param solid Whether this Hitbox's surfaces in every direction should be
     * solid
     */
    public final void setSolid(boolean solid) {
        if (solid) {
            if (solidSurfaces.isEmpty() && roles.contains(HitboxRole.SOLID) && state != null) {
                state.addHitbox(this, HitboxRole.SOLID);
            }
            solidSurfaces = EnumSet.allOf(Direction.class);
        } else {
            if (!solidSurfaces.isEmpty() && roles.contains(HitboxRole.SOLID) && state != null) {
                state.removeHitbox(this, HitboxRole.SOLID);
            }
            solidSurfaces.clear();
        }
    }
    
    /**
     * Returns the SpaceObject that is using this Hitbox, directly or indirectly
     * as part of a CompositeHitbox, or null if it is not being used by a
     * SpaceObject.
     * @return This Hitbox's SpaceObject
     */
    public final SpaceObject getObject() {
        return object;
    }
    
    final void setObject(SpaceObject object) {
        if (object != this.object) {
            recursivelySetObject(object);
        }
    }
    
    private void recursivelySetObject(SpaceObject object) {
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
    
    final void add(HitboxRole role) {
        if (state != null) {
            state.addHitbox(this, role);
        }
        roles.add(role);
    }
    
    final void remove(HitboxRole role) {
        if (state != null) {
            state.removeHitbox(this, role);
        }
        roles.remove(role);
        if (roles.isEmpty()) {
            setObject(null);
            if (parent != null) {
                parent.removeChild(this);
            }
        }
    }
    
    final void addAsCollisionHitbox(boolean hasCollision) {
        if (state != null && hasCollision) {
            state.addHitbox(this, HitboxRole.COLLISION);
        }
        roles.add(HitboxRole.COLLISION);
    }
    
    final void removeAsCollisionHitbox(boolean hasCollision) {
        if (state != null && hasCollision) {
            state.removeHitbox(this, HitboxRole.COLLISION);
        }
        roles.remove(HitboxRole.COLLISION);
        if (roles.isEmpty()) {
            setObject(null);
            if (parent != null) {
                parent.removeChild(this);
            }
        }
    }
    
    final void setDrawPriority(int drawPriority) {
        if (state == null) {
            this.drawPriority = drawPriority;
        } else {
            state.setLocatorHitboxDrawPriority(this, drawPriority);
        }
    }
    
    /**
     * Returns the SpaceState of the SpaceObject that is using this Hitbox, or
     * null if either the SpaceObject is not assigned to a SpaceState or this
     * Hitbox is not being used by a SpaceObject.
     * @return This Hitbox's SpaceObject's SpaceState
     */
    public final SpaceState getGameState() {
        return state;
    }
    
    final void setGameState(SpaceState state) {
        this.state = state;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.setGameState(state);
            }
        }
    }
    
    /**
     * Returns this Hitbox's relative position.
     * @return This Hitbox's relative position
     */
    public final CellVector getRelPosition() {
        return new CellVector(relPosition);
    }
    
    /**
     * Returns the x-coordinate of this Hitbox's relative position.
     * @return The x-coordinate of this Hitbox's relative position
     */
    public final long getRelX() {
        return relPosition.getX();
    }
    
    /**
     * Returns the y-coordinate of this Hitbox's relative position.
     * @return The y-coordinate of this Hitbox's relative position
     */
    public final long getRelY() {
        return relPosition.getY();
    }
    
    /**
     * Sets this Hitbox's relative position to the specified value.
     * @param relPosition The new relative position
     */
    public final void setRelPosition(CellVector relPosition) {
        this.relPosition.setCoordinates(relPosition);
        recursivelyUpdateAbsPosition();
    }
    
    /**
     * Sets this Hitbox's relative position to the specified coordinates.
     * @param relX The x-coordinate of the new relative position
     * @param relY The y-coordinate of the new relative position
     */
    public final void setRelPosition(long relX, long relY) {
        relPosition.setCoordinates(relX, relY);
        recursivelyUpdateAbsPosition();
    }
    
    /**
     * Sets the x-coordinate of this Hitbox's relative position to the specified
     * value.
     * @param relX The x-coordinate of the new relative position
     */
    public final void setRelX(long relX) {
        relPosition.setX(relX);
        recursivelyUpdateAbsPosition();
    }
    
    /**
     * Sets the y-coordinate of this Hitbox's relative position to the specified
     * value.
     * @param relY The y-coordinate of the new relative position
     */
    public final void setRelY(long relY) {
        relPosition.setY(relY);
        recursivelyUpdateAbsPosition();
    }
    
    /**
     * Changes this Hitbox's relative position by the specified amount.
     * @param change The amount to change the relative position by
     */
    public final void changeRelPosition(CellVector change) {
        relPosition.add(change);
        recursivelyUpdateAbsPosition();
    }
    
    /**
     * Changes the coordinates of this Hitbox's relative position by the
     * specified amounts.
     * @param changeX The amount to change the relative position's x-coordinate
     * by
     * @param changeY The amount to change the relative position's y-coordinate
     * by
     */
    public final void changeRelPosition(long changeX, long changeY) {
        relPosition.add(changeX, changeY);
        recursivelyUpdateAbsPosition();
    }
    
    /**
     * Changes the x-coordinate of this Hitbox's relative position by the
     * specified amount.
     * @param changeX The amount to change the relative position's x-coordinate
     * by
     */
    public final void changeRelX(long changeX) {
        relPosition.setX(relPosition.getX() + changeX);
        recursivelyUpdateAbsPosition();
    }
    
    /**
     * Changes the y-coordinate of this Hitbox's relative position by the
     * specified amount.
     * @param changeY The amount to change the relative position's y-coordinate
     * by
     */
    public final void changeRelY(long changeY) {
        relPosition.setY(relPosition.getY() + changeY);
        recursivelyUpdateAbsPosition();
    }
    
    /**
     * Returns this Hitbox's absolute position.
     * @return This Hitbox's absolute position
     */
    public final CellVector getAbsPosition() {
        return new CellVector(absPosition);
    }
    
    /**
     * Returns the x-coordinate of this Hitbox's absolute position.
     * @return The x-coordinate of this Hitbox's absolute position
     */
    public final long getAbsX() {
        return absPosition.getX();
    }
    
    /**
     * Returns the y-coordinate of this Hitbox's absolute position.
     * @return The y-coordinate of this Hitbox's absolute position
     */
    public final long getAbsY() {
        return absPosition.getY();
    }
    
    private void updateAbsPosition() {
        if (parent == null) {
            absPosition.setCoordinates(relPosition);
        } else {
            absPosition.setCoordinates(parent.absPosition).add(new CellVector(relPosition).relativeTo(parent));
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
    
    /**
     * Returns whether this Hitbox is relatively horizontally flipped.
     * @return Whether this Hitbox is relatively horizontally flipped
     */
    public final boolean getRelXFlip() {
        return relXFlip;
    }
    
    /**
     * Returns -1 if this Hitbox is relatively horizontally flipped and 1 if it
     * is not.
     * @return -1 if this Hitbox is relatively horizontally flipped and 1 if it
     * is not
     */
    public final int getRelXSign() {
        return (relXFlip ? -1 : 1);
    }
    
    /**
     * Sets whether this Hitbox is relatively horizontally flipped.
     * @param relXFlip Whether this Hitbox should be relatively horizontally
     * flipped
     */
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
    
    /**
     * Flips this Hitbox relatively horizontally, making it flipped if it was
     * not before and not flipped if it was before.
     */
    public final void relFlipX() {
        relXFlip = !relXFlip;
        absXFlip = !absXFlip;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsXFlip();
            }
        }
        updateAbsXFlipActions();
    }
    
    /**
     * Returns whether this Hitbox is absolutely horizontally flipped.
     * @return Whether this Hitbox is absolutely horizontally flipped
     */
    public final boolean getAbsXFlip() {
        return absXFlip;
    }
    
    /**
     * Returns -1 if this Hitbox is absolutely horizontally flipped and 1 if it
     * is not.
     * @return -1 if this Hitbox is absolutely horizontally flipped and 1 if it
     * is not
     */
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
    
    /**
     * Returns whether this Hitbox is relatively vertically flipped.
     * @return Whether this Hitbox is relatively vertically flipped
     */
    public final boolean getRelYFlip() {
        return relYFlip;
    }
    
    /**
     * Returns -1 if this Hitbox is relatively vertically flipped and 1 if it is
     * not.
     * @return -1 if this Hitbox is relatively vertically flipped and 1 if it is
     * not
     */
    public final int getRelYSign() {
        return (relYFlip ? -1 : 1);
    }
    
    /**
     * Sets whether this Hitbox is relatively vertically flipped.
     * @param relYFlip Whether this Hitbox should be relatively vertically
     * flipped
     */
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
    
    /**
     * Flips this Hitbox relatively vertically, making it flipped if it was not
     * before and not flipped if it was before.
     */
    public final void relFlipY() {
        relYFlip = !relYFlip;
        absYFlip = !absYFlip;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsYFlip();
            }
        }
        updateAbsYFlipActions();
    }
    
    /**
     * Returns whether this Hitbox is absolutely vertically flipped.
     * @return Whether this Hitbox is absolutely vertically flipped
     */
    public final boolean getAbsYFlip() {
        return absYFlip;
    }
    
    /**
     * Returns -1 if this Hitbox is absolutely vertically flipped and 1 if it is
     * not.
     * @return -1 if this Hitbox is absolutely vertically flipped and 1 if it is
     * not
     */
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
    
    /**
     * Returns this Hitbox's relative angle of rotation.
     * @return This Hitbox's relative angle of rotation
     */
    public final double getRelAngle() {
        return relAngle;
    }
    
    /**
     * Returns the x-coordinate of the unit vector that points in the direction
     * of this Hitbox's relative angle of rotation. This is equal to the cosine
     * of the angle.
     * @return The x-coordinate of this Hitbox's relative angle of rotation
     */
    public final long getRelAngleX() {
        return relAngleX;
    }
    
    /**
     * Returns the y-coordinate of the unit vector that points in the direction
     * of this Hitbox's relative angle of rotation. Since y-coordinates increase
     * going downward, this is equal to the negative sine of the angle.
     * @return The y-coordinate of this Hitbox's relative angle of rotation
     */
    public final long getRelAngleY() {
        return relAngleY;
    }
    
    /**
     * Sets this Hitbox's relative angle of rotation to the specified value.
     * @param relAngle The new relative angle of rotation
     */
    public final void setRelAngle(double relAngle) {
        this.relAngle = relAngle % 360;
        if (this.relAngle < 0) {
            this.relAngle += 360;
        }
        double radians = Math.toRadians(relAngle);
        relAngleX = Frac.units(Math.cos(radians));
        relAngleY = Frac.units(-Math.sin(radians));
        updateAbsAngle();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsAngle();
            }
        }
        updateAbsAngleActions();
    }
    
    /**
     * Changes this Hitbox's relative angle of rotation by the specified amount.
     * @param relAngle The amount to change the relative angle of rotation by
     */
    public final void changeRelAngle(double relAngle) {
        setRelAngle(this.relAngle + relAngle);
    }
    
    /**
     * Returns this Hitbox's absolute angle of rotation.
     * @return This Hitbox's absolute angle of rotation
     */
    public final double getAbsAngle() {
        return absAngle;
    }
    
    /**
     * Returns the x-coordinate of the unit vector that points in the direction
     * of this Hitbox's absolute angle of rotation. This is equal to the cosine
     * of the angle.
     * @return The x-coordinate of this Hitbox's absolute angle of rotation
     */
    public final long getAbsAngleX() {
        return absAngleX;
    }
    
    /**
     * Returns the y-coordinate of the unit vector that points in the direction
     * of this Hitbox's absolute angle of rotation. Since y-coordinates increase
     * going downward, this is equal to the negative sine of the angle.
     * @return The y-coordinate of this Hitbox's absolute angle of rotation
     */
    public final long getAbsAngleY() {
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
                angle = -angle;
            }
            absAngle = (parent.absAngle + angle) % 360;
            if (absAngle < 0) {
                absAngle += 360;
            }
        }
        double radians = Math.toRadians(absAngle);
        absAngleX = Frac.units(Math.cos(radians));
        absAngleY = Frac.units(-Math.sin(radians));
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
    
    /**
     * Returns the x-coordinate of this Hitbox's absolute left boundary.
     * @return The x-coordinate of this Hitbox's absolute left boundary
     */
    public abstract long getLeftEdge();
    
    /**
     * Returns the x-coordinate of this Hitbox's absolute right boundary.
     * @return The x-coordinate of this Hitbox's absolute right boundary
     */
    public abstract long getRightEdge();
    
    /**
     * Returns the y-coordinate of this Hitbox's absolute top boundary.
     * @return The y-coordinate of this Hitbox's absolute top boundary
     */
    public abstract long getTopEdge();
    
    /**
     * Returns the y-coordinate of this Hitbox's absolute bottom boundary.
     * @return The y-coordinate of this Hitbox's absolute bottom boundary
     */
    public abstract long getBottomEdge();
    
    /**
     * Returns the absolute distance from this Hitbox's position to the
     * specified Hitbox's position.
     * @param hitbox The Hitbox to return the distance to
     * @return The absolute distance from this Hitbox's position to the
     * specified Hitbox's position
     */
    public final long distanceTo(Hitbox hitbox) {
        return CellVector.distanceBetween(getAbsX(), getAbsY(), hitbox.getAbsX(), hitbox.getAbsY());
    }
    
    /**
     * Returns the absolute angle from this Hitbox's position to the specified
     * Hitbox's position.
     * @param hitbox The Hitbox to return the angle to
     * @return The absolute angle from this Hitbox's position to the specified
     * Hitbox's position
     */
    public final double angleTo(Hitbox hitbox) {
        return CellVector.angleBetween(getAbsX(), getAbsY(), hitbox.getAbsX(), hitbox.getAbsY());
    }
    
    private static boolean circleEdgeIntersectsSeg(
            CellVector center, long radius, CellVector start, CellVector diff) {
        //Credit to bobobobo of StackOverflow for the algorithm.
        CellVector f = CellVector.sub(start, center);
        long a = diff.dot(diff);
        long b = 2*f.dot(diff);
        long c = f.dot(f) - Frac.mul(radius, radius);
        long disc = Frac.mul(b, b) - 4*Frac.mul(a, c);
        if (disc < 0) {
            return false;
        }
        disc = Frac.sqrt(disc);
        long t1 = Frac.div(-b - disc, 2*a);
        long t2 = Frac.div(-b + disc, 2*a);
        return (t1 > 0 && t1 < Frac.UNIT) || (t2 > 0 && t2 < Frac.UNIT);
    }
    
    private static boolean circleIntersectsLineSegment(
            CellVector center, long radius, CellVector start, CellVector diff) {
        return center.distanceTo(start) < radius //Segment's first endpoint is in circle
                || center.distanceTo(CellVector.add(start, diff)) < radius //Segment's second endpoint is in circle
                || circleEdgeIntersectsSeg(center, radius, start, diff); //Segment intersects circle's edge
    }
    
    private static boolean angleImpalesVertex(double angle, CellVector diff1, CellVector diff2) {
        double angle1 = (diff1.getAngle() + 180 - angle) % 360;
        if (angle1 < 0) {
            angle1 += 360;
        }
        double angle2 = diff2.getAngle() - angle;
        if (angle2 < 0) {
            angle2 += 360;
        }
        return (angle1 < 180 && angle2 > 180) || (angle2 < 180 && angle1 > 180);
    }
    
    private static boolean circleImpalesVertex(
            CellVector center, long radius, CellVector diff1, CellVector vertex, CellVector diff2) {
        return center.distanceTo(vertex) == radius
                && angleImpalesVertex((center.angleTo(vertex) + 90) % 360, diff1, diff2);
    }
    
    private static boolean circleIntersectsPolygon(CellVector center, long radius, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) { //Polygon can't overlap
            return false;
        } else if (numVertices == 1) { //Polygon is a point at its first vertex
            return center.distanceTo(polygon.getAbsVertex(0)) < radius; //Point is in circle
        }
        CellVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) { //Polygon is a line segment
            //Circle intersects line segment
            return circleIntersectsLineSegment(center, radius,
                    firstVertex, polygon.getAbsVertex(1).sub(firstVertex));
        }
        //Any of polygon's vertices are in circle
        if (center.distanceTo(firstVertex) < radius) {
            return true;
        }
        CellVector[] vertices = new CellVector[numVertices];
        vertices[0] = firstVertex;
        for (int i = 1; i < numVertices; i++) {
            vertices[i] = polygon.getAbsVertex(i);
            if (center.distanceTo(vertices[i]) < radius) {
                return true;
            }
        }
        CellVector[] diffs = new CellVector[numVertices];
        //Any of polygon's edges intersect circle
        for (int i = 0; i < numVertices - 1; i++) {
            diffs[i] = CellVector.sub(vertices[i + 1], vertices[i]);
            if (circleEdgeIntersectsSeg(center, radius, vertices[i], diffs[i])) {
                return true;
            }
        }
        diffs[numVertices - 1] = CellVector.sub(firstVertex, vertices[numVertices - 1]);
        if (circleEdgeIntersectsSeg(center, radius, vertices[numVertices - 1], diffs[numVertices - 1])) {
            return true;
        }
        //Circle impales any of polygon's vertices
        if (circleImpalesVertex(center, radius, diffs[numVertices - 1], vertices[0], diffs[0])) {
            return true;
        }
        for (int i = 1; i < numVertices; i++) {
            if (circleImpalesVertex(center, radius, diffs[i - 1], vertices[i], diffs[i])) {
                return true;
            }
        }
        //Circle's center is in polygon
        return pointIntersectsPolygon(center, polygon.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean circleIntersectsOrthogonalSeg(
            long cu, long cv, long radius, long u1, long u2, long v) {
        v -= cv;
        if (Math.abs(v) < radius) {
            long rangeRadius = Frac.sqrt(Frac.mul(radius, radius) - Frac.mul(v, v));
            return u1 < cu + rangeRadius && u2 > cu - rangeRadius;
        }
        return false;
    }
    
    private static boolean circleIntersectsRectangle(
            long cx, long cy, long radius, long x1, long y1, long x2, long y2) {
        if (cx > x1 && cx < x2 && cy > y1 && cy < y2) { //Circle's center is in rectangle
            return true;
        }
        //Any of rectangle's edges intersect circle
        return circleIntersectsOrthogonalSeg(cx, cy, radius, x1, x2, y1)
                || circleIntersectsOrthogonalSeg(cx, cy, radius, x1, x2, y2)
                || circleIntersectsOrthogonalSeg(cy, cx, radius, y1, y2, x1)
                || circleIntersectsOrthogonalSeg(cy, cx, radius, y1, y2, x2);
    }
    
    private static boolean lineSegmentIntersectsPoint(CellVector start, CellVector diff, CellVector point) {
        CellVector relPoint = CellVector.sub(point, start);
        if (diff.getX() == 0) { //Segment is vertical
            //Point is on the right portion of the segment's vertical line
            return relPoint.getX() == 0 && Math.signum(relPoint.getY()) == Math.signum(diff.getY())
                    && Math.abs(relPoint.getY()) < Math.abs(diff.getY());
        }
        //Segment is not vertical; point is on the right portion of the segment's line
        return relPoint.cross(diff) == 0 && Math.signum(relPoint.getX()) == Math.signum(diff.getX())
                && Math.abs(relPoint.getX()) < Math.abs(diff.getX());
    }
    
    private static boolean lineSegmentImpalesVertex(
            CellVector start, CellVector diff, CellVector diff1, CellVector vertex, CellVector diff2) {
        return lineSegmentIntersectsPoint(start, diff, vertex)
                && angleImpalesVertex(diff.getAngle(), diff1, diff2);
    }
    
    private static boolean lineSegmentImpalesPolygonVertices(
            CellVector start, CellVector diff, CellVector[] vertices, CellVector[] diffs) {
        if (lineSegmentImpalesVertex(start, diff, diffs[vertices.length - 1], vertices[0], diffs[0])) {
            return true;
        }
        for (int i = 1; i < vertices.length; i++) {
            if (lineSegmentImpalesVertex(start, diff, diffs[i - 1], vertices[i], diffs[i])) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean lineSegmentIntersectsPolygon(
            CellVector start, CellVector diff, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) { //Polygon can't overlap
            return false;
        } else if (numVertices == 1) { //Polygon is a point at its first vertex
            return lineSegmentIntersectsPoint(start, diff, polygon.getAbsVertex(0)); //Point is on segment
        }
        CellVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) { //Polygon is a line segment
            //Segments intersect
            return CellVector.lineSegmentsIntersect(start, diff,
                    firstVertex, polygon.getAbsVertex(1).sub(firstVertex));
        }
        CellVector[] vertices = new CellVector[numVertices];
        vertices[0] = firstVertex;
        CellVector[] diffs = new CellVector[numVertices];
        //Any of polygon's edges intersect segment
        for (int i = 0; i < numVertices - 1; i++) {
            vertices[i + 1] = polygon.getAbsVertex(i + 1);
            diffs[i] = CellVector.sub(vertices[i + 1], vertices[i]);
            if (CellVector.lineSegmentsIntersect(start, diff, vertices[i], diffs[i])) {
                return true;
            }
        }
        diffs[numVertices - 1] = CellVector.sub(firstVertex, vertices[numVertices - 1]);
        if (CellVector.lineSegmentsIntersect(start, diff, vertices[numVertices - 1], diffs[numVertices - 1])) {
            return true;
        }
        //Segment impales any of polygon's vertices
        if (lineSegmentImpalesPolygonVertices(start, diff, vertices, diffs)) {
            return true;
        }
        //One of segment's endpoints is in polygon
        return pointIntersectsPolygon(start, polygon.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean lineSegmentIntersectsRectangle(
            CellVector start, CellVector diff, long x1, long y1, long x2, long y2) {
        //Segment's first endpoint is in rectangle
        if (start.getX() > x1 && start.getX() < x2 && start.getY() > y1 && start.getY() < y2) {
            return true;
        }
        //Segment's second endpoint is in rectangle
        long lineX2 = start.getX() + diff.getX();
        long lineY2 = start.getY() + diff.getY();
        if (lineX2 > x1 && lineX2 < x2 && lineY2 > y1 && lineY2 < y2) {
            return true;
        }
        CellVector topLeft = new CellVector(x1, y1);
        CellVector horizontalDiff = new CellVector(x2 - x1, 0);
        CellVector verticalDiff = new CellVector(0, y2 - y1);
        //Any of rectangle's edges intersect segment
        if (CellVector.lineSegmentsIntersect(start, diff, topLeft, horizontalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, new CellVector(x1, y2), horizontalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, topLeft, verticalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, new CellVector(x2, y1), verticalDiff)) {
            return true;
        }
        //Segment impales any of rectangle's vertices
        if (lineSegmentImpalesVertex(start, diff, horizontalDiff, new CellVector(x2, y1), verticalDiff)) {
            return true;
        }
        CellVector leftEdge = new CellVector(0, y1 - y2);
        if (lineSegmentImpalesVertex(start, diff, leftEdge, topLeft, horizontalDiff)) {
            return true;
        }
        CellVector bottomEdge = new CellVector(x1 - x2, 0);
        return lineSegmentImpalesVertex(start, diff, verticalDiff, new CellVector(x2, y2), bottomEdge)
                || lineSegmentImpalesVertex(start, diff, bottomEdge, new CellVector(x1, y2), leftEdge);
    }
    
    private static boolean segIntersectsHorizontalSeg(
            CellVector start, CellVector diff, long x1, long x2, long y, boolean closed) {
        //Segment is half-closed at start; horizontal segment may be half-closed at x2
        if (diff.getY() == 0) { //Segment is horizontal
            //Segment is on same line as horizontal segment and intersects it horizontally
            if (start.getY() != y) {
                return false;
            }
            if (diff.getX() > 0) {
                return (closed ? start.getX() <= x2 : start.getX() < x2)
                        && start.getX() + diff.getX() > x1;
            }
            return start.getX() > x1 && start.getX() + diff.getX() < x2;
        } else if (diff.getY() > 0) { //Segment goes downward
            //Segment overlaps with horizontal segment vertically
            if (start.getY() > y || start.getY() + diff.getY() <= y) {
                return false;
            }
        } else if (diff.getY() < 0) { //Segment goes upward
            //Segment overlaps with horizontal segment vertically
            if (start.getY() < y || start.getY() + diff.getY() >= y) {
                return false;
            }
        }
        long x = start.getX() + Frac.div(Frac.mul(y - start.getY(), diff.getX()), diff.getY());
        //Segment's point at horizontal segment's y is on horizontal segment
        return x > x1 && (closed ? x <= x2 : x < x2);
    }
    
    //Credit to Mecki of StackOverflow for the point-polygon intersection algorithm.
    
    private static boolean pointIntersectsPolygon(CellVector point,
            long startX, CellVector[] vertices, CellVector[] diffs) {
        //Line segment entering polygon to point crosses polygon's edges an odd number of times
        boolean intersects = false;
        for (int i = 0; i < vertices.length; i++) {
            if (segIntersectsHorizontalSeg(vertices[i], diffs[i],
                    startX, point.getX(), point.getY(), intersects)) {
                intersects = !intersects;
            }
        }
        return intersects;
    }
    
    private static boolean pointIntersectsPolygon(CellVector point, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices <= 1) { //Polygon can't overlap points
            return false;
        }
        CellVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) { //Polygon is a line segment
            //Point is on segment
            return lineSegmentIntersectsPoint(firstVertex, polygon.getAbsVertex(1).sub(firstVertex), point);
        }
        long startX = polygon.getLeftEdge() - 1;
        CellVector lastVertex = firstVertex;
        //Line segment entering polygon to point crosses polygon's edges an odd number of times
        boolean intersects = false;
        for (int i = 1; i < numVertices; i++) {
            CellVector vertex = polygon.getAbsVertex(i);
            if (segIntersectsHorizontalSeg(lastVertex, CellVector.sub(vertex, lastVertex),
                    startX, point.getX(), point.getY(), intersects)) {
                intersects = !intersects;
            }
            lastVertex = vertex;
        }
        if (segIntersectsHorizontalSeg(lastVertex, CellVector.sub(firstVertex, lastVertex),
                startX, point.getX(), point.getY(), intersects)) {
            intersects = !intersects;
        }
        return intersects;
    }
    
    private static boolean polygonsIntersect(PolygonHitbox polygon1, PolygonHitbox polygon2) {
        int numVertices1 = polygon1.getNumVertices();
        int numVertices2 = polygon2.getNumVertices();
        if (numVertices1 == 0) { //Polygon 1 is point at its center
            return pointIntersectsPolygon(polygon1.getAbsPosition(), polygon2); //Point is in polygon 2
        } else if (numVertices2 == 0) { //Polygon 2 is point at its center
            return pointIntersectsPolygon(polygon2.getAbsPosition(), polygon1); //Point is in polygon 1
        } else if (numVertices1 == 1) { //Polygon 1 is a point at its first vertex
            return pointIntersectsPolygon(polygon1.getAbsVertex(0), polygon2); //Point is in polygon 2
        } else if (numVertices2 == 1) { //Polygon 2 is a point at its first vertex
            return pointIntersectsPolygon(polygon2.getAbsVertex(0), polygon1); //Point is in polygon 1
        }
        CellVector firstVertex1 = polygon1.getAbsVertex(0);
        if (numVertices1 == 2) { //Polygon 1 is a line segment
            //Segment intersects polygon 2
            return lineSegmentIntersectsPolygon(
                    firstVertex1, polygon1.getAbsVertex(1).sub(firstVertex1), polygon2);
        }
        CellVector firstVertex2 = polygon2.getAbsVertex(0);
        if (numVertices2 == 2) { //Polygon 2 is a line segment
            //Segment intersects polygon 1
            return lineSegmentIntersectsPolygon(
                    firstVertex2, polygon2.getAbsVertex(1).sub(firstVertex2), polygon1);
        }
        CellVector secondVertex2 = polygon2.getAbsVertex(1);
        CellVector firstDiff2 = CellVector.sub(secondVertex2, firstVertex2);
        CellVector[] vertices1 = new CellVector[numVertices1];
        vertices1[0] = firstVertex1;
        CellVector[] diffs1 = new CellVector[numVertices1];
        //Any of polygon 1's edges intersect polygon 2's first edge
        for (int i = 0; i < numVertices1 - 1; i++) {
            vertices1[i + 1] = polygon1.getAbsVertex(i + 1);
            diffs1[i] = CellVector.sub(vertices1[i + 1], vertices1[i]);
            if (CellVector.lineSegmentsIntersect(firstVertex2, firstDiff2, vertices1[i], diffs1[i])) {
                return true;
            }
        }
        diffs1[numVertices1 - 1] = CellVector.sub(firstVertex1, vertices1[numVertices1 - 1]);
        if (CellVector.lineSegmentsIntersect(firstVertex2, firstDiff2,
                vertices1[numVertices1 - 1], diffs1[numVertices1 - 1])) {
            return true;
        }
        CellVector[] vertices2 = new CellVector[numVertices2];
        vertices2[0] = firstVertex2;
        vertices2[1] = secondVertex2;
        CellVector[] diffs2 = new CellVector[numVertices2];
        diffs2[0] = firstDiff2;
        //Any of polygon 1's edges intersect any of polygon 2's other edges
        for (int i = 1; i < numVertices2 - 1; i++) {
            vertices2[i + 1] = polygon2.getAbsVertex(i);
            diffs2[i] = CellVector.sub(vertices2[i + 1], vertices2[i]);
            for (int j = 0; j < numVertices1; j++) {
                if (CellVector.lineSegmentsIntersect(vertices2[i], diffs2[i], vertices1[j], diffs1[j])) {
                    return true;
                }
            }
        }
        diffs2[numVertices2 - 1] = CellVector.sub(firstVertex2, vertices2[numVertices2 - 1]);
        for (int j = 0; j < numVertices1; j++) {
            if (CellVector.lineSegmentsIntersect(vertices2[numVertices2 - 1], diffs2[numVertices2 - 1], vertices1[j], diffs1[j])) {
                return true;
            }
        }
        //Any of polygon 1's edges impale any of polygon 2's vertices
        for (int i = 0; i < numVertices1; i++) {
            if (lineSegmentImpalesPolygonVertices(vertices1[i], diffs1[i], vertices2, diffs2)) {
                return true;
            }
        }
        //Any of polygon 2's edges impale any of polygon 1's vertices
        for (int i = 0; i < numVertices2; i++) {
            if (lineSegmentImpalesPolygonVertices(vertices2[i], diffs2[i], vertices1, diffs1)) {
                return true;
            }
        }
        //Polygon 1's first vertex is in polygon 2, or polygon 2's first vertex is in polygon 1
        return pointIntersectsPolygon(firstVertex1, polygon2.getLeftEdge() - 1, vertices2, diffs2)
                || pointIntersectsPolygon(firstVertex2, polygon1.getLeftEdge() - 1, vertices1, diffs1);
    }
    
    private static boolean polygonIntersectsRectangle(
            PolygonHitbox polygon, long x1, long y1, long x2, long y2) {
        //Assumption: polygon and rectangle's bounding boxes intersect
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) { //Polygon can't overlap
            return false;
        } else if (numVertices == 1) { //Polygon is a point, which must be in the rectangle's bounding box
            return true;
        }
        if (numVertices == 2) { //Polygon is a line segment
            CellVector firstVertex = polygon.getAbsVertex(0);
            //Segment intersects rectangle
            return lineSegmentIntersectsRectangle(
                    firstVertex, polygon.getAbsVertex(1).sub(firstVertex), x1, y1, x2, y2);
        }
        CellVector[] vertices = new CellVector[numVertices];
        //Any of polygon's vertices are in rectangle
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = polygon.getAbsVertex(i);
            long vertexX = vertices[i].getX();
            long vertexY = vertices[i].getY();
            if (vertexX > x1 && vertexX < x2 && vertexY > y1 && vertexY < y2) {
                return true;
            }
        }
        CellVector horizontalDiff = new CellVector(x2 - x1, 0);
        CellVector verticalDiff = new CellVector(0, y2 - y1);
        CellVector topLeft = new CellVector(x1, y1);
        CellVector bottomLeft = new CellVector(x1, y2);
        CellVector topRight = new CellVector(x2, y1);
        CellVector[] diffs = new CellVector[numVertices];
        //Any of polygon's edges intersect any of rectangle's edges
        for (int i = 0; i < numVertices - 1; i++) {
            vertices[i + 1] = polygon.getAbsVertex(i + 1);
            diffs[i] = CellVector.sub(vertices[i + 1], vertices[i]);
            if (CellVector.lineSegmentsIntersect(vertices[i], diffs[i], topLeft, horizontalDiff)
                    || CellVector.lineSegmentsIntersect(vertices[i], diffs[i], bottomLeft, horizontalDiff)
                    || CellVector.lineSegmentsIntersect(vertices[i], diffs[i], topLeft, verticalDiff)
                    || CellVector.lineSegmentsIntersect(vertices[i], diffs[i], topRight, verticalDiff)) {
                return true;
            }
        }
        diffs[numVertices - 1] = CellVector.sub(vertices[0], vertices[numVertices - 1]);
        CellVector start = vertices[numVertices - 1];
        CellVector diff = diffs[numVertices - 1];
        if (CellVector.lineSegmentsIntersect(start, diff, topLeft, horizontalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, bottomLeft, horizontalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, topLeft, verticalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, topRight, verticalDiff)) {
            return true;
        }
        //Any of rectangle's edges impale any of polygon's vertices
        if (lineSegmentImpalesVertex(topLeft, horizontalDiff, diffs[numVertices - 1], vertices[0], diffs[0])
                || lineSegmentImpalesVertex(bottomLeft, horizontalDiff, diffs[numVertices - 1], vertices[0], diffs[0])
                || lineSegmentImpalesVertex(topLeft, verticalDiff, diffs[numVertices - 1], vertices[0], diffs[0])
                || lineSegmentImpalesVertex(topRight, verticalDiff, diffs[numVertices - 1], vertices[0], diffs[0])) {
            return true;
        }
        for (int i = 1; i < numVertices; i++) {
            if (lineSegmentImpalesVertex(topLeft, horizontalDiff, diffs[i - 1], vertices[i], diffs[i])
                    || lineSegmentImpalesVertex(bottomLeft, horizontalDiff, diffs[i - 1], vertices[i], diffs[i])
                    || lineSegmentImpalesVertex(topLeft, verticalDiff, diffs[i - 1], vertices[i], diffs[i])
                    || lineSegmentImpalesVertex(topRight, verticalDiff, diffs[i - 1], vertices[i], diffs[i])) {
                return true;
            }
        }
        //Any of polygon's edges impale any of rectangle's vertices
        CellVector bottomRight = new CellVector(x2, y2);
        CellVector bottomEdge = new CellVector(x1 - x2, 0);
        CellVector leftEdge = new CellVector(0, y1 - y2);
        for (int i = 0; i < numVertices; i++) {
            if (lineSegmentImpalesVertex(vertices[i], diffs[i], leftEdge, topLeft, horizontalDiff)
                    || lineSegmentImpalesVertex(vertices[i], diffs[i], horizontalDiff, topRight, verticalDiff)
                    || lineSegmentImpalesVertex(vertices[i], diffs[i], verticalDiff, bottomRight, bottomEdge)
                    || lineSegmentImpalesVertex(vertices[i], diffs[i], bottomEdge, bottomLeft, leftEdge)) {
                return true;
            }
        }
        //Rectangle's top left vertex is in polygon
        return pointIntersectsPolygon(topLeft, polygon.getLeftEdge() - 1, vertices, diffs);
    }
    
    /**
     * Returns whether this Hitbox overlaps the specified Hitbox. Two Hitboxes
     * overlap if they share any points that are in the interior of at least one
     * of them. If both Hitboxes are PointHitboxes or otherwise consist only of
     * single points, then they have no interiors and thus cannot overlap. Two
     * Hitboxes are not considered to overlap if they are both being used by the
     * same SpaceObject.
     * @param hitbox The Hitbox to check for an overlap
     * @return Whether this Hitbox overlaps the specified Hitbox
     */
    public final boolean overlaps(Hitbox hitbox) {
        return overlap(this, hitbox);
    }
    
    /**
     * Returns whether the two specified Hitboxes overlap. Two Hitboxes overlap
     * if they share any points that are in the interior of at least one of
     * them. If both Hitboxes are PointHitboxes or otherwise consist only of
     * single points, then they have no interiors and thus cannot overlap. Two
     * Hitboxes are not considered to overlap if they are both being used by the
     * same SpaceObject.
     * @param hitbox1 The first Hitbox
     * @param hitbox2 The second Hitbox
     * @return Whether the two Hitboxes overlap
     */
    public static boolean overlap(Hitbox hitbox1, Hitbox hitbox2) {
        if ((hitbox1.getObject() != hitbox2.getObject() || hitbox1.getObject() == null)
                && hitbox1.getLeftEdge() < hitbox2.getRightEdge()
                && hitbox1.getRightEdge() > hitbox2.getLeftEdge()
                && hitbox1.getTopEdge() < hitbox2.getBottomEdge()
                && hitbox1.getBottomEdge() > hitbox2.getTopEdge()) {
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
                    return hitbox1.distanceTo(hitbox2) < ((CircleHitbox)hitbox1).getRadius() + ((CircleHitbox)hitbox2).getRadius();
                } else if (hitbox2 instanceof LineHitbox) {
                    return circleIntersectsLineSegment(hitbox1.absPosition, ((CircleHitbox)hitbox1).getRadius(), hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference());
                } else if (hitbox2 instanceof PointHitbox) {
                    return hitbox1.distanceTo(hitbox2) < ((CircleHitbox)hitbox1).getRadius();
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return circleIntersectsPolygon(hitbox1.absPosition, ((CircleHitbox)hitbox1).getRadius(), (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return circleIntersectsRectangle(hitbox1.getAbsX(), hitbox1.getAbsY(), ((CircleHitbox)hitbox1).getRadius(), hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                }
            } else if (hitbox1 instanceof LineHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsLineSegment(hitbox2.absPosition, ((CircleHitbox)hitbox2).getRadius(), hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference());
                } else if (hitbox2 instanceof LineHitbox) {
                    return CellVector.lineSegmentsIntersect(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference());
                } else if (hitbox2 instanceof PointHitbox) {
                    return lineSegmentIntersectsPoint(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), hitbox2.absPosition);
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return lineSegmentIntersectsPolygon(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return lineSegmentIntersectsRectangle(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                }
            } else if (hitbox1 instanceof PointHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return hitbox1.distanceTo(hitbox2) < ((CircleHitbox)hitbox2).getRadius();
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsPoint(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), hitbox1.absPosition);
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return pointIntersectsPolygon(hitbox1.absPosition, (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return true;
                }
            } else if (hitbox1 instanceof PolygonHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsPolygon(hitbox2.absPosition, ((CircleHitbox)hitbox2).getRadius(), (PolygonHitbox)hitbox1);
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsPolygon(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), (PolygonHitbox)hitbox1);
                } else if (hitbox2 instanceof PointHitbox) {
                    return pointIntersectsPolygon(hitbox2.absPosition, (PolygonHitbox)hitbox1);
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return polygonsIntersect((PolygonHitbox)hitbox1, (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return polygonIntersectsRectangle((PolygonHitbox)hitbox1, hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                }
            } else if (hitbox1 instanceof RectangleHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsRectangle(hitbox2.getAbsX(), hitbox2.getAbsY(), ((CircleHitbox)hitbox2).getRadius(), hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsRectangle(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof PointHitbox) {
                    return true;
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return polygonIntersectsRectangle((PolygonHitbox)hitbox2, hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
