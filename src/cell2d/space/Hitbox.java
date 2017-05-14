package cell2d.space;

import cell2d.CellGame;
import cell2d.CellVector;
import cell2d.Frac;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>A Hitbox is a region of space that can be checked for intersection with
 * other regions. A Hitbox has a position at a single point that acts as an
 * origin point for the Hitbox's shape. This point is usually inside the Hitbox,
 * but may not always be. Hitboxes can be rotated around their positions and
 * flipped across horizontal and vertical axes through their positions. Rotating
 * a Hitbox will also rotate the axes along which it is flipped. As with
 * CellVectors, Hitboxes measure angles in degrees going counterclockwise from
 * directly right and normalize them to be between 0 and 360.</p>
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
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the SpaceStates that can use
 * this Hitbox
 */
public abstract class Hitbox<T extends CellGame> {
    
    static final int HITBOXES_PER_OBJECT = 5;
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    private Hitbox<T> parent = null;
    private final Set<Hitbox<T>> children = new HashSet<>();
    CompositeHitbox<T> componentOf = null;
    EnumSet<Direction> solidSurfaces = EnumSet.noneOf(Direction.class);
    private SpaceObject<T> object = null;
    final boolean[] roles = new boolean[HITBOXES_PER_OBJECT];
    private int numRoles = 0;
    SpaceState<T> state = null;
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
     * Creates a new Hitbox with the specified relative position.
     * @param relPosition This Hitbox's relative position
     */
    public Hitbox(CellVector relPosition) {
        id = idCounter.getAndIncrement();
        this.relPosition = new CellVector(relPosition);
        absPosition = new CellVector(relPosition);
    }
    
    /**
     * Creates a new Hitbox with the specified relative position.
     * @param relX The x-coordinate of this Hitbox's relative position
     * @param relY The y-coordinate of this Hitbox's relative position
     */
    public Hitbox(long relX, long relY) {
        id = idCounter.getAndIncrement();
        this.relPosition = new CellVector(relX, relY);
        absPosition = new CellVector(relPosition);
    }
    
    /**
     * Returns a copy of this Hitbox with its relative position at the origin
     * that is not flipped or rotated.
     * @return A copy of this Hitbox
     */
    public abstract Hitbox<T> getCopy();
    
    final Hitbox<T> getParent() {
        return parent;
    }
    
    final boolean addChild(Hitbox<T> child) {
        if (child != null && child != this
                && child.parent == null && child.object == null) {
            Hitbox<T> ancestor = parent;
            while (ancestor != null) {
                if (ancestor == child) {
                    return false;
                }
                ancestor = ancestor.parent;
            }
            children.add(child);
            child.parent = this;
            child.recursivelyUpdateData();
            return true;
        }
        return false;
    }
    
    final boolean removeChild(Hitbox<T> child) {
        if (child != null && child.parent == this) {
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
            for (Hitbox<T> child : children) {
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
    public final CompositeHitbox<T> getComponentOf() {
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
            if (solidSurfaces.add(direction) && solidSurfaces.size() == 1 && roles[3] && state != null) {
                state.addSolidHitbox(this);
            }
        } else {
            if (solidSurfaces.remove(direction) && solidSurfaces.isEmpty() && roles[3] && state != null) {
                state.removeSolidHitbox(this);
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
            if (solidSurfaces.isEmpty() && roles[3] && state != null) {
                state.addSolidHitbox(this);
            }
            solidSurfaces = EnumSet.allOf(Direction.class);
        } else {
            if (!solidSurfaces.isEmpty() && roles[3] && state != null) {
                state.removeSolidHitbox(this);
            }
            solidSurfaces.clear();
        }
    }
    
    /**
     * Returns the SpaceObject that is currently using this Hitbox, directly or
     * indirectly as part of a CompositeHitbox, or null if it is not being used
     * by a SpaceObject.
     * @return This Hitbox's SpaceObject
     */
    public final SpaceObject<T> getObject() {
        return object;
    }
    
    final void setObject(SpaceObject<T> object) {
        if (object != this.object) {
            recursivelySetObject(object);
        }
    }
    
    private void recursivelySetObject(SpaceObject<T> object) {
        this.object = object;
        state = (object == null ? null : object.state);
        if (!children.isEmpty()) {
            for (Hitbox<T> child : children) {
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
    
    final void addAsCenterHitbox() {
        if (state != null) {
            state.addCenterHitbox(this);
        }
        roles[1] = true;
        numRoles++;
    }
    
    final void removeAsCenterHitbox() {
        if (state != null) {
            state.removeCenterHitbox(this);
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
    
    final void addAsOverlapHitbox() {
        if (state != null) {
            state.addOverlapHitbox(this);
        }
        roles[2] = true;
        numRoles++;
    }
    
    final void removeAsOverlapHitbox() {
        if (state != null) {
            state.removeOverlapHitbox(this);
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
    
    final void addAsSolidHitbox() {
        if (state != null) {
            state.addSolidHitbox(this);
        }
        roles[3] = true;
        numRoles++;
    }
    
    final void removeAsSolidHitbox() {
        if (state != null) {
            state.removeSolidHitbox(this);
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
    
    final void addAsCollisionHitbox(boolean hasCollision) {
        if (state != null && hasCollision) {
            state.addCollisionHitbox(this);
        }
        roles[4] = true;
        numRoles++;
    }
    
    final void removeAsCollisionHitbox(boolean hasCollision) {
        if (state != null && hasCollision) {
            state.removeCollisionHitbox(this);
        }
        roles[4] = false;
        numRoles--;
        if (numRoles == 0) {
            setObject(null);
            if (parent != null) {
                parent.removeChild(this);
            }
        }
    }
    
    /**
     * Returns the SpaceState of the SpaceObject that is currently using this
     * Hitbox, or null if either the SpaceObject is not assigned to a SpaceState
     * or this Hitbox is not being used by a SpaceObject.
     * @return This Hitbox's SpaceObject's SpaceState
     */
    public final SpaceState<T> getGameState() {
        return state;
    }
    
    final void setGameState(SpaceState<T> state) {
        this.state = state;
        if (!children.isEmpty()) {
            for (Hitbox<T> child : children) {
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
            for (Hitbox<T> child : children) {
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
            for (Hitbox<T> child : children) {
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
            for (Hitbox<T> child : children) {
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
            for (Hitbox<T> child : children) {
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
            for (Hitbox<T> child : children) {
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
            for (Hitbox<T> child : children) {
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
            for (Hitbox<T> child : children) {
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
                angle = 360 - angle;
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
            for (Hitbox<T> child : children) {
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
    
    private static boolean circleEdgeIntersectsSeg(CellVector center, long radius, CellVector start, CellVector diff) {
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
    
    private static boolean circleIntersectsLineSegment(CellVector center, long radius, CellVector start, CellVector diff) {
        return center.distanceTo(start) < radius
                || center.distanceTo(CellVector.add(start, diff)) < radius
                || circleEdgeIntersectsSeg(center, radius, start, diff);
    }
    
    private static boolean circleIntersectsPolygon(CellVector center, long radius, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            return center.distanceTo(polygon.getAbsPosition()) < radius;
        } else if (numVertices == 1) {
            return center.distanceTo(polygon.getAbsVertex(0)) < radius;
        }
        CellVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) {
            return circleIntersectsLineSegment(center, radius, polygon.getAbsVertex(0), polygon.getAbsVertex(1).sub(firstVertex));
        }
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
        return pointIntersectsPolygon(center, polygon.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean circleIntersectsOrthogonalLine(long cu, long cv, long radius, long u1, long u2, long v) {
        v -= cv;
        if (Math.abs(v) < radius) {
            long rangeRadius = Frac.sqrt(Frac.mul(radius, radius) - Frac.mul(v, v));
            return u1 < cu + rangeRadius && u2 > cu - rangeRadius;
        }
        return false;
    }
    
    private static boolean circleIntersectsRectangle(long cx, long cy, long radius, long x1, long y1, long x2, long y2) {
        if (cx > x1 && cx < x2 && cy > y1 && cy < y2) {
            return true;
        }
        return circleIntersectsOrthogonalLine(cx, cy, radius, x1, x2, y1)
                || circleIntersectsOrthogonalLine(cx, cy, radius, x1, x2, y2)
                || circleIntersectsOrthogonalLine(cy, cx, radius, y1, y2, x1)
                || circleIntersectsOrthogonalLine(cy, cx, radius, y1, y2, x2);
    }
    /*
    private static boolean circleIntersectsSlope(CellVector center, double radius, SlopeHitbox slope) {
        if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return circleIntersectsLineSegment(center, radius, slope.getAbsPosition(), slope.getAbsDifference());
        } else if (!slope.isSloping()) {
            return circleIntersectsRectangle(center.getX(), center.getY(), radius, slope.getLeftEdge(), slope.getTopEdge(), slope.getRightEdge(), slope.getBottomEdge());
        }
        CellVector vertex1 = slope.getAbsPosition();
        if (center.distanceTo(vertex1) < radius) {
            return true;
        }
        CellVector vertex2 = slope.getPosition2();
        if (center.distanceTo(vertex2) < radius) {
            return true;
        }
        CellVector diff2 = new CellVector(-slope.getAbsDX(), 0);
        CellVector vertex3 = CellVector.add(vertex2, diff2);
        if (center.distanceTo(vertex3) < radius
                || circleEdgeIntersectsSeg(center, radius, vertex1, slope.getAbsDifference())
                || circleEdgeIntersectsSeg(center, radius, vertex2, diff2)
                || circleEdgeIntersectsSeg(center, radius, vertex3, new CellVector(0, -slope.getAbsDY()))) {
            return true;
        }
        return pointIntersectsRightSlope(center, slope);
    }
    */
    private static boolean lineSegmentIntersectsPoint(CellVector start, CellVector diff, CellVector point) {
        CellVector relPoint = CellVector.sub(point, start);
        if (diff.getX() == 0) {
            return relPoint.getX() == 0 && Math.signum(relPoint.getY()) == Math.signum(diff.getY()) && Math.abs(relPoint.getY()) < Math.abs(diff.getY());
        }
        return relPoint.cross(diff) == 0 && Math.signum(relPoint.getX()) == Math.signum(diff.getX()) && Math.abs(relPoint.getX()) < Math.abs(diff.getX());
    }
    
    private static boolean lineSegmentIntersectsPolygon(CellVector start, CellVector diff, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            return lineSegmentIntersectsPoint(start, diff, polygon.getAbsPosition());
        } else if (numVertices == 1) {
            return lineSegmentIntersectsPoint(start, diff, polygon.getAbsVertex(0));
        }
        CellVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) {
            return CellVector.lineSegmentsIntersect(start, diff, firstVertex, polygon.getAbsVertex(1).sub(firstVertex));
        }
        CellVector[] vertices = new CellVector[numVertices];
        vertices[0] = firstVertex;
        CellVector[] diffs = new CellVector[numVertices];
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
        return pointIntersectsPolygon(start, polygon.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean lineSegmentIntersectsRectangle(CellVector start, CellVector diff, long x1, long y1, long x2, long y2) {
        if (start.getX() > x1 && start.getX() < x2 && start.getY() > y1 && start.getY() < y2) {
            return true;
        }
        long lineX2 = start.getX() + diff.getX();
        long lineY2 = start.getY() + diff.getY();
        if (lineX2 > x1 && lineX2 < x2 && lineY2 > y1 && lineY2 < y2) {
            return true;
        }
        CellVector horizontalDiff = new CellVector(x2 - x1, 0);
        CellVector verticalDiff = new CellVector(0, y2 - y1);
        CellVector topLeft = new CellVector(x1, y1);
        return CellVector.lineSegmentsIntersect(start, diff, topLeft, horizontalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, new CellVector(x1, y2), horizontalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, topLeft, verticalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, new CellVector(x2, y1), verticalDiff);
    }
    /*
    private static boolean lineSegmentIntersectsSlope(CellVector start, CellVector diff, SlopeHitbox slope) {
        if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return CellVector.lineSegmentsIntersect(start, diff, slope.getAbsPosition(), slope.getAbsDifference());
        } else if (!slope.isSloping()) {
            return lineSegmentIntersectsRectangle(start, diff, slope.getLeftEdge(), slope.getTopEdge(), slope.getRightEdge(), slope.getBottomEdge());
        }
        if (CellVector.lineSegmentsIntersect(start, diff, slope.getAbsPosition(), slope.getAbsDifference())) {
            return true;
        }
        CellVector vertex2 = slope.getPosition2();
        CellVector diff2 = new CellVector(-slope.getAbsDX(), 0);
        if (CellVector.lineSegmentsIntersect(start, diff, vertex2, diff2)) {
            return true;
        }
        if (CellVector.lineSegmentsIntersect(start, diff, CellVector.add(vertex2, diff2), new CellVector(0, -slope.getAbsDY()))) {
            return true;
        }
        return pointIntersectsRightSlope(start, slope);
    }
    */
    //Credit to Mecki of StackOverflow for the point-polygon intersection algorithm.
    
    private static boolean pointIntersectsPolygon(CellVector point, long startX, CellVector[] vertices, CellVector[] diffs) {
        CellVector start = new CellVector(startX, point.getY());
        CellVector diff = new CellVector(point.getX() - startX, 0);
        boolean intersects = false;
        for (int i = 0; i < vertices.length; i++) {
            if (CellVector.lineSegmentsIntersect(start, diff, vertices[i], diffs[i])) {
                intersects = !intersects;
            }
        }
        return intersects;
    }
    
    private static boolean pointIntersectsPolygon(CellVector point, PolygonHitbox polygon) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            CellVector position = polygon.getAbsPosition();
            return point.getX() == position.getX() && point.getY() == position.getY();
        } else if (numVertices == 1) {
            CellVector position = polygon.getAbsVertex(0);
            return point.getX() == position.getX() && point.getY() == position.getY();
        }
        CellVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) {
            return lineSegmentIntersectsPoint(firstVertex, polygon.getAbsVertex(1).sub(firstVertex), point);
        }
        long startX = polygon.getLeftEdge() - 1;
        CellVector start = new CellVector(startX, point.getY());
        CellVector diff = new CellVector(point.getX() - startX, 0);
        CellVector lastVertex = firstVertex;
        boolean intersects = false;
        for (int i = 1; i < numVertices; i++) {
            CellVector vertex = polygon.getAbsVertex(i);
            if (CellVector.lineSegmentsIntersect(start, diff, lastVertex, CellVector.sub(vertex, lastVertex))) {
                intersects = !intersects;
            }
            lastVertex = vertex;
        }
        if (CellVector.lineSegmentsIntersect(start, diff, lastVertex, CellVector.sub(firstVertex, lastVertex))) {
            intersects = !intersects;
        }
        return intersects;
    }
    
    private static boolean pointIntersectsRectangle(CellVector point, long x1, long y1, long x2, long y2) {
        return point.getX() > x1 && point.getX() < x2
                && point.getY() > y1 && point.getY() < y2;
    }
    /*
    private static boolean pointIntersectsRightSlope(CellVector point, SlopeHitbox slope) {
        if (point.getX() > slope.getLeftEdge() && point.getX() < slope.getRightEdge()) {
            if (slope.isPresentAbove()) {
                return point.getY() > slope.getTopEdge() && point.getY() < slope.getSlopeY(point.getX());
            }
            return point.getY() < slope.getBottomEdge() && point.getY() > slope.getSlopeY(point.getX()); 
        }
        return false;
    }
    
    private static boolean pointIntersectsSlope(CellVector point, SlopeHitbox slope) {
        if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return lineSegmentIntersectsPoint(slope.getAbsPosition(), slope.getAbsDifference(), point);
        } else if (!slope.isSloping()) {
            return true;
        } 
        return pointIntersectsRightSlope(point, slope);
    }
    */
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
        CellVector firstVertex1 = polygon1.getAbsVertex(0);
        if (numVertices1 == 2) {
            return lineSegmentIntersectsPolygon(firstVertex1, polygon1.getAbsVertex(1).sub(firstVertex1), polygon2);
        }
        CellVector firstVertex2 = polygon2.getAbsVertex(0);
        if (numVertices2 == 2) {
            return lineSegmentIntersectsPolygon(firstVertex2, polygon2.getAbsVertex(1).sub(firstVertex2), polygon1);
        }
        CellVector secondVertex2 = polygon2.getAbsVertex(1);
        CellVector firstDiff2 = CellVector.sub(secondVertex2, firstVertex2);
        CellVector[] vertices1 = new CellVector[numVertices1];
        vertices1[0] = firstVertex1;
        CellVector[] diffs1 = new CellVector[numVertices1];
        for (int i = 0; i < numVertices1 - 1; i++) {
            vertices1[i + 1] = polygon1.getAbsVertex(i + 1);
            diffs1[i] = CellVector.sub(vertices1[i + 1], vertices1[i]);
            if (CellVector.lineSegmentsIntersect(firstVertex2, firstDiff2, vertices1[i], diffs1[i])) {
                return true;
            }
        }
        diffs1[numVertices1 - 1] = CellVector.sub(firstVertex1, vertices1[numVertices1 - 1]);
        if (CellVector.lineSegmentsIntersect(firstVertex2, firstDiff2, vertices1[numVertices1 - 1], diffs1[numVertices1 - 1])) {
            return true;
        }
        CellVector[] vertices2 = new CellVector[numVertices2];
        vertices2[0] = firstVertex2;
        vertices2[1] = secondVertex2;
        CellVector[] diffs2 = new CellVector[numVertices2];
        diffs2[0] = firstDiff2;
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
        return pointIntersectsPolygon(firstVertex1, polygon2.getLeftEdge() - 1, vertices2, diffs2)
                || pointIntersectsPolygon(firstVertex2, polygon1.getLeftEdge() - 1, vertices1, diffs1);
    }
    
    private static boolean polygonIntersectsRectangle(PolygonHitbox polygon, long x1, long y1, long x2, long y2) {
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            return pointIntersectsRectangle(polygon.getAbsPosition(), x1, y1, x2, y2);
        } else if (numVertices == 1) {
            return pointIntersectsRectangle(polygon.getAbsVertex(0), x1, y1, x2, y2);
        }
        if (numVertices == 2) {
            CellVector firstVertex = polygon.getAbsVertex(0);
            return lineSegmentIntersectsRectangle(firstVertex, polygon.getAbsVertex(1).sub(firstVertex), x1, y1, x2, y2);
        }
        CellVector[] vertices = new CellVector[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = polygon.getAbsVertex(i);
            if (pointIntersectsRectangle(vertices[i], x1, y1, x2, y2)) {
                return true;
            }
        }
        CellVector horizontalDiff = new CellVector(x2 - x1, 0);
        CellVector verticalDiff = new CellVector(0, y2 - y1);
        CellVector topLeft = new CellVector(x1, y1);
        CellVector bottomLeft = new CellVector(x1, y2);
        CellVector topRight = new CellVector(x2, y1);
        for (int i = 0; i < numVertices - 1; i++) {
            CellVector diff = CellVector.sub(vertices[i + 1], vertices[i]);
            if (CellVector.lineSegmentsIntersect(vertices[i], diff, topLeft, horizontalDiff)
                    || CellVector.lineSegmentsIntersect(vertices[i], diff, bottomLeft, horizontalDiff)
                    || CellVector.lineSegmentsIntersect(vertices[i], diff, topLeft, verticalDiff)
                    || CellVector.lineSegmentsIntersect(vertices[i], diff, topRight, verticalDiff)) {
                return true;
            }
        }
        CellVector start = vertices[numVertices - 1];
        CellVector diff = CellVector.sub(vertices[0], start);
        return CellVector.lineSegmentsIntersect(start, diff, topLeft, horizontalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, bottomLeft, horizontalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, topLeft, verticalDiff)
                || CellVector.lineSegmentsIntersect(start, diff, topRight, verticalDiff);
    }
    /*
    private static boolean polygonIntersectsSlope(PolygonHitbox polygon, SlopeHitbox slope) {
        if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return lineSegmentIntersectsPolygon(slope.getAbsPosition(), slope.getAbsDifference(), polygon);
        } else if (!slope.isSloping()) {
            return polygonIntersectsRectangle(polygon, slope.getLeftEdge(), slope.getTopEdge(), slope.getRightEdge(), slope.getBottomEdge());
        }
        int numVertices = polygon.getNumVertices();
        if (numVertices == 0) {
            return pointIntersectsRightSlope(polygon.getAbsPosition(), slope);
        } else if (numVertices == 1) {
            return pointIntersectsRightSlope(polygon.getAbsVertex(0), slope);
        }
        CellVector firstVertex = polygon.getAbsVertex(0);
        if (numVertices == 2) {
            return lineSegmentIntersectsSlope(firstVertex, polygon.getAbsVertex(1).sub(firstVertex), slope);
        }
        CellVector[] vertices = new CellVector[numVertices];
        vertices[0] = firstVertex;
        CellVector[] diffs = new CellVector[numVertices];
        CellVector[] slopeVertices = new CellVector[3];
        slopeVertices[0] = slope.getAbsPosition();
        CellVector[] slopeDiffs = new CellVector[3];
        slopeDiffs[0] = slope.getAbsDifference();
        for (int i = 0; i < numVertices - 1; i++) {
            vertices[i + 1] = polygon.getAbsVertex(i + 1);
            diffs[i] = CellVector.sub(vertices[i + 1], vertices[i]);
            if (CellVector.lineSegmentsIntersect(vertices[i], diffs[i], slopeVertices[0], slopeDiffs[0])) {
                return true;
            }
        }
        diffs[numVertices - 1] = CellVector.sub(firstVertex, vertices[numVertices - 1]);
        if (CellVector.lineSegmentsIntersect(vertices[numVertices - 1], diffs[numVertices - 1], slopeVertices[0], slopeDiffs[0])) {
            return true;
        }
        slopeVertices[1] = slope.getPosition2();
        slopeDiffs[1] = new CellVector(-slope.getAbsDX(), 0);
        for (int i = 0; i < numVertices; i++) {
            if (CellVector.lineSegmentsIntersect(vertices[i], diffs[i], slopeVertices[1], slopeDiffs[1])) {
                return true;
            }
        }
        slopeVertices[2] = CellVector.add(slopeVertices[1], slopeDiffs[1]);
        slopeDiffs[2] = new CellVector(0, -slope.getAbsDY());
        for (int i = 0; i < numVertices; i++) {
            if (CellVector.lineSegmentsIntersect(vertices[i], diffs[i], slopeVertices[2], slopeDiffs[2])) {
                return true;
            }
        }
        return pointIntersectsRightSlope(firstVertex, slope)
                || pointIntersectsPolygon(slopeVertices[0], polygon.getLeftEdge() - 1, vertices, diffs);
    }
    */
    private static boolean rectanglesIntersect(long x1_1, long y1_1, long x2_1, long y2_1, long x1_2, long y1_2, long x2_2, long y2_2) {
        return x2_1 > x1_2 && x1_1 < x2_2 && y2_1 > y1_2 && y1_1 < y2_2;
    }
    /*
    private static boolean rectangleIntersectsSlope(double x1, double y1, double x2, double y2, SlopeHitbox slope) {
        if (!slope.isPresentAbove() && !slope.isPresentBelow()) {
            return lineSegmentIntersectsRectangle(slope.getAbsPosition(), slope.getAbsDifference(), x1, y1, x2, y2);
        } else if (!slope.isSloping()) {
            return rectanglesIntersect(x1, y1, x2, y2, slope.getLeftEdge(), slope.getTopEdge(), slope.getRightEdge(), slope.getBottomEdge());
        }
        CellVector[] vertices = new CellVector[3];
        vertices[0] = slope.getAbsPosition();
        if (pointIntersectsRectangle(vertices[0], x1, y1, x2, y2)) {
            return true;
        }
        CellVector[] diffs = new CellVector[3];
        diffs[0] = slope.getAbsDifference();
        vertices[1] = slope.getPosition2();
        if (pointIntersectsRectangle(vertices[1], x1, y1, x2, y2)) {
            return true;
        }
        diffs[1] = new CellVector(-slope.getAbsDX(), 0);
        vertices[2] = CellVector.add(vertices[1], diffs[1]);
        if (pointIntersectsRectangle(vertices[2], x1, y1, x2, y2)) {
            return true;
        }
        diffs[2] = new CellVector(0, -slope.getAbsDY());
        CellVector horizontalDiff = new CellVector(x2 - x1, 0);
        CellVector verticalDiff = new CellVector(0, y2 - y1);
        CellVector topLeft = new CellVector(x1, y1);
        CellVector bottomLeft = new CellVector(x1, y2);
        CellVector topRight = new CellVector(x2, y1);
        for (int i = 0; i < 3; i++) {
            if (CellVector.lineSegmentsIntersect(topLeft, horizontalDiff, vertices[i], diffs[i])
                    || CellVector.lineSegmentsIntersect(bottomLeft, horizontalDiff, vertices[i], diffs[i])
                    || CellVector.lineSegmentsIntersect(topLeft, verticalDiff, vertices[i], diffs[i])
                    || CellVector.lineSegmentsIntersect(topRight, verticalDiff, vertices[i], diffs[i])) {
                return true;
            }
        }
        return pointIntersectsPolygon(new CellVector(x1, y1), slope.getLeftEdge() - 1, vertices, diffs);
    }
    
    private static boolean slopesIntersect(SlopeHitbox slope1, SlopeHitbox slope2) {
        if (!slope1.isPresentAbove() && !slope1.isPresentBelow()) {
            return lineSegmentIntersectsSlope(slope1.getAbsPosition(), slope1.getAbsDifference(), slope2);
        } else if (!slope2.isPresentAbove() && !slope2.isPresentBelow()) {
            return lineSegmentIntersectsSlope(slope2.getAbsPosition(), slope2.getAbsDifference(), slope1);
        } else if (!slope1.isSloping()) {
            return rectangleIntersectsSlope(slope1.getLeftEdge(), slope1.getTopEdge(), slope1.getRightEdge(), slope1.getBottomEdge(), slope2);
        } else if (!slope2.isSloping()) {
            return rectangleIntersectsSlope(slope2.getLeftEdge(), slope2.getTopEdge(), slope2.getRightEdge(), slope2.getBottomEdge(), slope1);
        }
        CellVector[] vertices1 = {slope1.getAbsPosition(), slope1.getPosition2(), null};
        CellVector[] diffs1 = {slope1.getAbsDifference(), new CellVector(-slope1.getAbsDX(), 0), new CellVector(0, -slope1.getAbsDY())};
        vertices1[2] = CellVector.add(vertices1[1], diffs1[1]);
        CellVector[] vertices2 = new CellVector[3];
        vertices2[0] = slope2.getAbsPosition();
        CellVector[] diffs2 = new CellVector[3];
        diffs2[0] = slope2.getAbsDifference();
        for (int i = 0; i < 3; i++) {
            if (CellVector.lineSegmentsIntersect(vertices1[i], diffs1[i], vertices2[0], diffs2[0])) {
                return true;
            }
        }
        vertices2[1] = slope2.getPosition2();
        diffs2[1] = new CellVector(-slope2.getAbsDX(), 0);
        for (int i = 0; i < 3; i++) {
            if (CellVector.lineSegmentsIntersect(vertices1[i], diffs1[i], vertices2[1], diffs2[1])) {
                return true;
            }
        }
        vertices2[2] = CellVector.add(vertices2[1], diffs2[1]);
        diffs2[2] = new CellVector(0, -slope2.getAbsDY());
        for (int i = 0; i < 3; i++) {
            if (CellVector.lineSegmentsIntersect(vertices1[i], diffs1[i], vertices2[2], diffs2[2])) {
                return true;
            }
        }
        return pointIntersectsPolygon(vertices1[0], slope2.getLeftEdge() - 1, vertices2, diffs2)
                || pointIntersectsPolygon(vertices2[0], slope1.getLeftEdge() - 1, vertices1, diffs1);
    }
    */
    /**
     * Returns whether this Hitbox overlaps the specified Hitbox. Two Hitboxes
     * are not considered to overlap if they are both being used by the same
     * SpaceObject.
     * @param hitbox The Hitbox to check for an overlap
     * @return Whether this Hitbox overlaps the specified Hitbox
     */
    public final boolean overlaps(Hitbox<T> hitbox) {
        return overlap(this, hitbox);
    }
    
    /**
     * Returns whether the two specified Hitboxes overlap. Two Hitboxes are not
     * considered to overlap if they are both being used by the same
     * SpaceObject.
     * @param <T> The subclass of CellGame that uses the SpaceStates that the
     * two Hitboxes can be used by
     * @param hitbox1 The first Hitbox
     * @param hitbox2 The second Hitbox
     * @return Whether the two Hitboxes overlap
     */
    public static final <T extends CellGame> boolean overlap(Hitbox<T> hitbox1, Hitbox<T> hitbox2) {
        if ((hitbox1.getObject() != hitbox2.getObject() || hitbox1.getObject() == null)
                && hitbox1.getLeftEdge() <= hitbox2.getRightEdge()
                && hitbox1.getRightEdge() >= hitbox2.getLeftEdge()
                && hitbox1.getTopEdge() <= hitbox2.getBottomEdge()
                && hitbox1.getBottomEdge() >= hitbox2.getTopEdge()) {
            if (hitbox1 instanceof CompositeHitbox) {
                for (Hitbox<T> component : ((CompositeHitbox<T>)hitbox1).components.values()) {
                    if (overlap(component, hitbox2)) {
                        return true;
                    }
                }
                return false;
            } else if (hitbox2 instanceof CompositeHitbox) {
                for (Hitbox<T> component : ((CompositeHitbox<T>)hitbox2).components.values()) {
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
                }/* else if (hitbox2 instanceof SlopeHitbox) {
                    return circleIntersectsSlope(hitbox1.absPosition, ((CircleHitbox)hitbox1).getRadius(), (SlopeHitbox)hitbox2);
                }*/
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
                }/* else if (hitbox2 instanceof SlopeHitbox) {
                    return lineSegmentIntersectsSlope(hitbox1.absPosition, ((LineHitbox)hitbox1).getAbsDifference(), (SlopeHitbox)hitbox2);
                }*/
            } else if (hitbox1 instanceof PointHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return hitbox1.distanceTo(hitbox2) < ((CircleHitbox)hitbox2).getRadius();
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsPoint(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), hitbox1.absPosition);
                } else if (hitbox2 instanceof PointHitbox) {
                    return true;
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return pointIntersectsPolygon(hitbox1.absPosition, (PolygonHitbox)hitbox2);
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return pointIntersectsRectangle(hitbox1.absPosition, hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                }/* else if (hitbox2 instanceof SlopeHitbox) {
                    return pointIntersectsSlope(hitbox1.absPosition, (SlopeHitbox)hitbox2);
                }*/
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
                }/* else if (hitbox2 instanceof SlopeHitbox) {
                    return polygonIntersectsSlope((PolygonHitbox)hitbox1, (SlopeHitbox)hitbox2);
                }*/
            } else if (hitbox1 instanceof RectangleHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsRectangle(hitbox2.getAbsX(), hitbox2.getAbsY(), ((CircleHitbox)hitbox2).getRadius(), hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof LineHitbox) {
                    return lineSegmentIntersectsRectangle(hitbox2.absPosition, ((LineHitbox)hitbox2).getAbsDifference(), hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof PointHitbox) {
                    return pointIntersectsRectangle(hitbox2.absPosition, hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof PolygonHitbox) {
                    return polygonIntersectsRectangle((PolygonHitbox)hitbox2, hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge());
                } else if (hitbox2 instanceof RectangleHitbox) {
                    return rectanglesIntersect(hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge(),
                            hitbox2.getLeftEdge(), hitbox2.getTopEdge(), hitbox2.getRightEdge(), hitbox2.getBottomEdge());
                }/* else if (hitbox2 instanceof SlopeHitbox) {
                    return rectangleIntersectsSlope(hitbox1.getLeftEdge(), hitbox1.getTopEdge(), hitbox1.getRightEdge(), hitbox1.getBottomEdge(), (SlopeHitbox)hitbox2);
                }*/
            }/* else if (hitbox1 instanceof SlopeHitbox) {
                if (hitbox2 instanceof CircleHitbox) {
                    return circleIntersectsSlope(hitbox2.absPosition, ((CircleHitbox)hitbox2).getRadius(), (SlopeHitbox)hitbox1);
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
            }*/
        }
        return false;
    }
    
}
