package cell2d;

import cell2d.space.Hitbox;
import cell2d.space.SpaceObject;
import java.util.Objects;

/**
 * <p>A CellVector represents a point in continuous space as a two-dimensional
 * vector. CellVectors are mutable; that is, the point that they represent can
 * be changed. However, since the point that a CellVector represents is the
 * basis of its equals() and hashCode() methods, care must be taken not to
 * mutate a CellVector while data structures such as Maps or Sets are using it.
 * CellVectors measure angles in degrees going counterclockwise from directly
 * right and normalize them to be between 0 and 360. All operations on a
 * CellVector return the CellVector itself to allow operations to be easily
 * strung together.</p>
 * @author Andrew Heyman
 */
public class CellVector {
    
    private long x, y;
    
    /**
     * Creates a new CellVector that represents the origin.
     */
    public CellVector() {
        x = 0;
        y = 0;
    }
    
    /**
     * Creates a new CellVector that represents the specified point.
     * @param point The point that this CellVector represents
     */
    public CellVector(CellVector point) {
        x = point.x;
        y = point.y;
    }
    
    /**
     * Creates a new CellVector that represents the specified point.
     * @param x The x-coordinate of the point that this CellVector represents
     * @param y The y-coordinate of the point that this CellVector represents
     */
    public CellVector(long x, long y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Creates a new CellVector that represents the point on the unit circle at
     * the specified angle from the origin.
     * @param angle The angle from the origin of the point on the unit circle
     * that this CellVector represents
     */
    public CellVector(double angle) {
        double radians = Math.toRadians(angle);
        x = Frac.units(Math.cos(radians));
        y = Frac.units(-Math.sin(radians));
    }
    
    /**
     * Returns whether two CellVectors are equal. Two CellVectors are equal if
     * and only if they represent the same point.
     * @param obj The object to be compared with this CellVector
     * @return Whether the specified object is a CellVector that is equal to
     * this CellVector
     */
    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof CellVector) {
            CellVector vector = (CellVector)obj;
            return x == vector.x && y == vector.y;
        }
        return false;
    }
    
    @Override
    public final int hashCode() {
        return Objects.hash(x, y);
    }
    
    /**
     * Resets this CellVector to represent the origin.
     * @return This CellVector
     */
    public final CellVector clear() {
        x = 0;
        y = 0;
        return this;
    }
    
    /**
     * Returns the x-coordinate of the point that this CellVector represents.
     * @return The x-coordinate of the point that this CellVector represents
     */
    public final long getX() {
        return x;
    }
    
    /**
     * Sets the x-coordinate of this CellVector's point to the specified value.
     * @param x The point's new x-coordinate
     * @return This CellVector
     */
    public final CellVector setX(long x) {
        this.x = x;
        return this;
    }
    
    /**
     * Returns the y-coordinate of the point that this CellVector represents.
     * @return The y-coordinate of the point that this CellVector represents
     */
    public final long getY() {
        return y;
    }
    
    /**
     * Sets the y-coordinate of this CellVector's point to the specified value.
     * @param y The point's new y-coordinate
     * @return This CellVector
     */
    public final CellVector setY(long y) {
        this.y = y;
        return this;
    }
    
    /**
     * Sets this CellVector's point to the specified value.
     * @param point The new point
     * @return This CellVector
     */
    public final CellVector setCoordinates(CellVector point) {
        x = point.x;
        y = point.y;
        return this;
    }
    
    /**
     * Sets this CellVector's point to the specified coordinates.
     * @param x The point's new x-coordinate
     * @param y The point's new y-coordinate
     * @return This CellVector
     */
    public final CellVector setCoordinates(long x, long y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    /**
     * Flips this CellVector across both coordinate axes, negating both of its
     * coordinates.
     * @return This CellVector
     */
    public final CellVector flip() {
        x = -x;
        y = -y;
        return this;
    }
    
    /**
     * Flips this CellVector across the x-axis, negating its x-coordinate.
     * @return This CellVector
     */
    public final CellVector flipX() {
        x = -x;
        return this;
    }
    
    /**
     * Flips this CellVector across the y-axis, negating its y-coordinate.
     * @return This CellVector
     */
    public final CellVector flipY() {
        y = -y;
        return this;
    }
    
    /**
     * Flips this CellVector across either, both, or neither of the coordinate
     * axes, depending on the parameters.
     * @param xFlip If true, this CellVector will be flipped across the x-axis
     * @param yFlip If true, this CellVector will be flipped across the y-axis
     * @return This CellVector
     */
    public final CellVector flip(boolean xFlip, boolean yFlip) {
        if (xFlip) {
            x = -x;
        }
        if (yFlip) {
            y = -y;
        }
        return this;
    }
    
    /**
     * Returns the square of this CellVector's magnitude.
     * @return The square of this CellVector's magnitude
     */
    public final long getMagnitudeSquared() {
        return Frac.mul(x, x) + Frac.mul(y, y);
    }
    
    /**
     * Returns this CellVector's magnitude.
     * @return This CellVector's magnitude
     */
    public final long getMagnitude() {
        return Frac.sqrt(getMagnitudeSquared());
    }
    
    /**
     * Sets this CellVector's magnitude to the specified value while retaining
     * its angle. If the new magnitude is negative, this CellVector will be
     * flipped and its magnitude set to the absolute value of the specified
     * magnitude.
     * @param magnitude The new magnitude
     * @return This CellVector
     */
    public final CellVector setMagnitude(long magnitude) {
        return (x == 0 && y == 0 ? setX(magnitude) : scale(Frac.div(magnitude, getMagnitude())));
    }
    
    /**
     * Multiplies this CellVector's coordinates by the specified factor.
     * @param scaleFactor The factor in fracunits by which to scale this
     * CellVector
     * @return This CellVector
     */
    public final CellVector scale(long scaleFactor) {
        x = Frac.mul(x, scaleFactor);
        y = Frac.mul(y, scaleFactor);
        return this;
    }
    
    /**
     * Returns this CellVector's angle.
     * @return This CellVector's angle
     */
    public final double getAngle() {
        if (x == 0 && y == 0) {
            return 0;
        }
        double angle = Math.toDegrees(Math.atan2(-y, x)) % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    /**
     * Returns the x-coordinate of the unit vector that points in the direction
     * of this CellVector's angle. This is equal to the cosine of the angle.
     * @return The x-coordinate of this CellVector's angle
     */
    public final long getAngleX() {
        return (x == 0 && y == 0 ? Frac.UNIT : Frac.div(x, getMagnitude()));
    }
    
    /**
     * Returns the y-coordinate of the unit vector that points in the direction
     * of this CellVector's angle. Since y-coordinates increase going downward,
     * this is equal to the negative sine of the angle.
     * @return The y-coordinate of this CellVector's angle
     */
    public final long getAngleY() {
        return (x == 0 && y == 0 ? 0 : Frac.div(y, getMagnitude()));
    }
    
    /**
     * Sets this CellVector's angle to the specified value while retaining its
     * magnitude.
     * @param angle The new angle
     * @return This CellVector
     */
    public final CellVector setAngle(double angle) {
        long magnitude = getMagnitude();
        double radians = Math.toRadians(angle);
        x = Frac.mul(magnitude, Frac.units(Math.cos(radians)));
        y = Frac.mul(magnitude, Frac.units(-Math.sin(radians)));
        return this;
    }
    
    /**
     * Changes this CellVector's angle by the specified amount while retaining
     * its magnitude.
     * @param angle The amount by which to change the angle
     * @return This CellVector
     */
    public final CellVector changeAngle(double angle) {
        return (x == 0 && y == 0 ? this : setAngle(Math.toDegrees(Math.atan2(-y, x)) + angle));
    }
    
    /**
     * Adds the specified CellVector to this CellVector.
     * @param vector The CellVector to be added
     * @return This CellVector
     */
    public final CellVector add(CellVector vector) {
        x += vector.x;
        y += vector.y;
        return this;
    }
    
    /**
     * Adds the specified coordinates to this CellVector's own.
     * @param x The x-coordinate to be added
     * @param y The y-coordinate to be added
     * @return This CellVector
     */
    public final CellVector add(long x, long y) {
        this.x += x;
        this.y += y;
        return this;
    }
    
    /**
     * Returns a new CellVector that represents the sum of the two specified
     * CellVectors.
     * @param first The first CellVector
     * @param second The second CellVector
     * @return The sum of the two CellVectors
     */
    public static CellVector add(CellVector first, CellVector second) {
        return new CellVector(first.x + second.x, first.y + second.y);
    }
    
    /**
     * Subtracts the specified CellVector from this CellVector.
     * @param vector The CellVector to be subtracted
     * @return This CellVector
     */
    public final CellVector sub(CellVector vector) {
        x -= vector.x;
        y -= vector.y;
        return this;
    }
    
    /**
     * Subtracts the specified coordinates from this CellVector's own.
     * @param x The x-coordinate to be subtracted
     * @param y The y-coordinate to be subtracted
     * @return This CellVector
     */
    public final CellVector sub(long x, long y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    
    /**
     * Returns a new CellVector that represents the second specified
     * CellVector subtracted from the first.
     * @param first The first CellVector
     * @param second The second CellVector
     * @return The second CellVector subtracted from the first
     */
    public static CellVector sub(CellVector first, CellVector second) {
        return new CellVector(first.x - second.x, first.y - second.y);
    }
    
    /**
     * Returns the dot product in fracunits of this CellVector and the specified
     * one.
     * @param vector The CellVector to take the dot product with
     * @return The dot product of this CellVector and the specified one
     */
    public final long dot(CellVector vector) {
        return Frac.mul(x, vector.x) + Frac.mul(y, vector.y);
    }
    
    /**
     * Returns the magnitude in fracunits of the cross product of this
     * CellVector and the specified one.
     * @param vector The CellVector to take the cross product with
     * @return The magnitude of the cross product of this CellVector and the
     * specified one
     */
    public final long cross(CellVector vector) {
        return Frac.mul(x, vector.y) - Frac.mul(y, vector.x);
    }
    
    /**
     * Flips this CellVector to reflect the flipped status of the specified
     * Hitbox and rotates it to reflect the Hitbox's angle of rotation, as if
     * those properties were formerly relative to the Hitbox.
     * @param hitbox The Hitbox to reflect
     * @return This CellVector
     */
    public final CellVector relativeTo(Hitbox hitbox) {
        return flip(hitbox.getAbsXFlip(), hitbox.getAbsYFlip()).changeAngle(hitbox.getAbsAngle());
    }
    
    /**
     * Flips this CellVector to reflect the flipped status of the specified
     * SpaceObject and rotates it to reflect the SpaceObject's angle of
     * rotation, as if those properties were formerly relative to the
     * SpaceObject.
     * @param object The SpaceObject to reflect
     * @return This CellVector
     */
    public final CellVector relativeTo(SpaceObject object) {
        return relativeTo(object.getLocatorHitbox());
    }
    
    /**
     * Returns the distance from this CellVector's point to that of the
     * specified CellVector.
     * @param point The point to return the distance to
     * @return The distance from this CellVector's point to that of the
     * specified CellVector
     */
    public final long distanceTo(CellVector point) {
        return distanceBetween(x, y, point.x, point.y);
    }
    
    /**
     * Returns the distance between the points (x1, y1) and (x2, y2).
     * @param x1 The x-coordinate of the first point
     * @param y1 The y-coordinate of the first point
     * @param x2 The x-coordinate of the second point
     * @param y2 The y-coordinate of the second point
     * @return The distance between (x1, y1) and (x2, y2)
     */
    public static long distanceBetween(long x1, long y1, long x2, long y2) {
        long xDist = x2 - x1;
        long yDist = y2 - y1;
        return Frac.sqrt(Frac.mul(xDist, xDist) + Frac.mul(yDist, yDist));
    }
    
    /**
     * Returns the angle from this CellVector's point to that of the specified
     * CellVector.
     * @param point The point to return the angle to
     * @return The angle from this CellVector's point to that of the specified
     * CellVector
     */
    public final double angleTo(CellVector point) {
        double angle = Math.toDegrees(Math.atan2(y - point.y, point.x - x)) % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    /**
     * Returns the angle from the point (x1, y1) to the point (x2, y2).
     * @param x1 The x-coordinate of the first point
     * @param y1 The y-coordinate of the first point
     * @param x2 The x-coordinate of the second point
     * @param y2 The y-coordinate of the second point
     * @return The angle from (x1, y1) to (x2, y2)
     */
    public static double angleBetween(long x1, long y1, long x2, long y2) {
        double angle = Math.toDegrees(Math.atan2(y1 - y2, x2 - x1)) % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    private static boolean segBoxesIntersect(
            CellVector start1, CellVector diff1, CellVector start2, CellVector diff2) {
        long minX1, maxX1, minX2, maxX2;
        if (diff1.x > 0) {
            minX1 = start1.x;
            maxX1 = minX1 + diff1.x;
        } else {
            maxX1 = start1.x;
            minX1 = maxX1 + diff1.x;
        }
        if (diff2.x > 0) {
            minX2 = start2.x;
            maxX2 = minX2 + diff2.x;
        } else {
            maxX2 = start2.x;
            minX2 = maxX2 + diff2.x;
        }
        if (minX2 >= maxX1 || minX1 >= maxX2) {
            return false;
        }
        long minY1, maxY1, minY2, maxY2;
        if (diff1.y > 0) {
            minY1 = start1.y;
            maxY1 = minY1 + diff1.y;
        } else {
            maxY1 = start1.y;
            minY1 = maxY1 + diff1.y;
        }
        if (diff2.y > 0) {
            minY2 = start2.y;
            maxY2 = minY2 + diff2.y;
        } else {
            maxY2 = start2.y;
            minY2 = maxY2 + diff2.y;
        }
        return minY2 < maxY1 && minY1 < maxY2;
    }
    
    //Credit to Gareth Rees of StackOverflow for the line segment intersection algorithm.
    
    /**
     * Returns whether the two specified line segments share any points. The
     * line segments do not contain their own endpoints.
     * @param start1 One of the first line segment's endpoints
     * @param diff1 The difference of the first line segment's endpoints
     * @param start2 One of the second line segment's endpoints
     * @param diff2 The difference of the second line segment's endpoints
     * @return Whether the two line segments intersect
     */
    public static boolean lineSegmentsIntersect(
            CellVector start1, CellVector diff1, CellVector start2, CellVector diff2) {
        if (!segBoxesIntersect(start1, diff1, start2, diff2)) {
            return false;
        }
        CellVector start2MinusStart1 = CellVector.sub(start2, start1);
        if (diff1.cross(diff2) == 0) {
            if (start2MinusStart1.cross(diff1) == 0) {
                long diff1Dot = diff1.dot(diff1);
                long t0 = Frac.div(start2MinusStart1.dot(diff1), diff1Dot);
                long diff2DotDiff1 = diff2.dot(diff1);
                long t1 = Frac.div(diff2DotDiff1, diff1Dot);
                return (diff2DotDiff1 < 0 ? (t1 > 0 || t0 < Frac.UNIT) : (t0 > 0 || t1 < Frac.UNIT));
            }
            return false;
        }
        long diff1CrossDiff2 = diff1.cross(diff2);
        long t = Frac.div(start2MinusStart1.cross(diff2), diff1CrossDiff2);
        long u = Frac.div(start2MinusStart1.cross(diff1), diff1CrossDiff2);
        return t > 0 && t < Frac.UNIT && u > 0 && u < Frac.UNIT;
    }
    
    /**
     * Returns the single point at which the two specified line segments
     * intersect, or null if they intersect at no points or at an infinity of
     * points. The line segments do not contain their own endpoints.
     * @param start1 One of the first line segment's endpoints
     * @param diff1 The difference of the first line segment's endpoints
     * @param start2 One of the second line segment's endpoints
     * @param diff2 The difference of the second line segment's endpoints
     * @return The point at which the two line segments intersect
     */
    public static CellVector lineSegmentsIntersectionPoint(
            CellVector start1, CellVector diff1, CellVector start2, CellVector diff2) {
        if (!segBoxesIntersect(start1, diff1, start2, diff2) || diff1.cross(diff2) == 0) {
            return null;
        }
        CellVector start2MinusStart1 = CellVector.sub(start2, start1);
        long diff1CrossDiff2 = diff1.cross(diff2);
        long t = Frac.div(start2MinusStart1.cross(diff2), diff1CrossDiff2);
        long u = Frac.div(start2MinusStart1.cross(diff1), diff1CrossDiff2);
        if (t > 0 && t < Frac.UNIT && u > 0 && u < Frac.UNIT) {
            return CellVector.add(start1, new CellVector(diff1).scale(t));
        }
        return null;
    }
    
}
