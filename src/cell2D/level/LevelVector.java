package cell2D.level;

/**
 * <p>A LevelVector represents a point in LevelState space as a two-dimensional
 * vector. A LevelVector retains its identity as an Object even if the point
 * that it represents is changed, and thus two different LevelVectors with the
 * same point are not considered equal. LevelVectors measure angles in degrees
 * going counterclockwise from directly right and normalize them to be between 0
 * and 360. All operations on a LevelVector return the LevelVector itself to
 * allow operations to be easily strung together.</p>
 * @author Andrew Heyman
 */
public class LevelVector {
    
    private double x, y;
    
    /**
     * Creates a new LevelVector that represents the origin.
     */
    public LevelVector() {
        x = 0;
        y = 0;
    }
    
    /**
     * Creates a new LevelVector that represents the specified point.
     * @param point The point that this LevelVector represents
     */
    public LevelVector(LevelVector point) {
        x = point.x;
        y = point.y;
    }
    
    /**
     * Creates a new LevelVector that represents the specified point.
     * @param x The x-coordinate of the point that this LevelVector represents
     * @param y The y-coordinate of the point that this LevelVector represents
     */
    public LevelVector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Creates a new LevelVector that represents the point on the unit circle
     * at the specified angle from the origin.
     * @param angle The angle from the origin of the point on the unit circle
     * that this LevelVector represents
     */
    public LevelVector(double angle) {
        double radians = Math.toRadians(angle);
        x = Math.cos(radians);
        y = -Math.sin(radians);
    }
    
    /**
     * Resets this LevelVector to represent the origin.
     * @return This LevelVector
     */
    public final LevelVector clear() {
        x = 0;
        y = 0;
        return this;
    }
    
    /**
     * Returns the x-coordinate of the point that this LevelVector represents.
     * @return The x-coordinate of the point that this LevelVector represents
     */
    public final double getX() {
        return x;
    }
    
    /**
     * Sets the x-coordinate of this LevelVector's point to the specified value.
     * @param x The point's new x-coordinate
     * @return This LevelVector
     */
    public final LevelVector setX(double x) {
        this.x = x;
        return this;
    }
    
    /**
     * Returns the y-coordinate of the point that this LevelVector represents.
     * @return The y-coordinate of the point that this LevelVector represents
     */
    public final double getY() {
        return y;
    }
    
    /**
     * Sets the y-coordinate of this LevelVector's point to the specified value.
     * @param y The point's new y-coordinate
     * @return This LevelVector
     */
    public final LevelVector setY(double y) {
        this.y = y;
        return this;
    }
    
    /**
     * Sets this LevelVector's point to the specified value.
     * @param point The new point
     * @return This LevelVector
     */
    public final LevelVector setCoordinates(LevelVector point) {
        x = point.x;
        y = point.y;
        return this;
    }
    
    /**
     * Sets this LevelVector's point to the specified coordinates.
     * @param x The point's new x-coordinate
     * @param y The point's new y-coordinate
     * @return This LevelVector
     */
    public final LevelVector setCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    /**
     * Flips this LevelVector across both coordinate axes, negating both of its
     * coordinates.
     * @return This LevelVector
     */
    public final LevelVector flip() {
        x = -x;
        y = -y;
        return this;
    }
    
    /**
     * Flips this LevelVector across the x-axis, negating its x-coordinate.
     * @return This LevelVector
     */
    public final LevelVector flipX() {
        x = -x;
        return this;
    }
    
    /**
     * Flips this LevelVector across the y-axis, negating its y-coordinate.
     * @return This LevelVector
     */
    public final LevelVector flipY() {
        y = -y;
        return this;
    }
    
    /**
     * Flips this LevelVector across either, both, or neither of the coordinate
     * axes, depending on the parameters.
     * @param xFlip If true, this LevelVector will be flipped across the x-axis
     * @param yFlip If true, this LevelVector will be flipped across the y-axis
     * @return This LevelVector
     */
    public final LevelVector flip(boolean xFlip, boolean yFlip) {
        if (xFlip) {
            x = -x;
        }
        if (yFlip) {
            y = -y;
        }
        return this;
    }
    
    /**
     * Returns the square of this LevelVector's magnitude.
     * @return The square of this LevelVector's magnitude
     */
    public final double getMagnitudeSquared() {
        return x*x + y*y;
    }
    
    /**
     * Returns this LevelVector's magnitude.
     * @return This LevelVector's magnitude
     */
    public final double getMagnitude() {
        return Math.sqrt(x*x + y*y);
    }
    
    /**
     * Sets this LevelVector's magnitude to the specified value while retaining
     * its angle. If the new magnitude is negative, the LevelVector will be
     * flipped and its magnitude set to the absolute value of the new magnitude.
     * @param magnitude The new magnitude
     * @return This LevelVector
     */
    public final LevelVector setMagnitude(double magnitude) {
        return (x == 0 && y == 0 ? setX(magnitude) : scale(magnitude/Math.sqrt(x*x + y*y)));
    }
    
    /**
     * Multiplies this LevelVector's coordinates by the specified factor.
     * @param scaleFactor The factor by which to scale this LevelVector
     * @return This LevelVector
     */
    public final LevelVector scale(double scaleFactor) {
        x *= scaleFactor;
        y *= scaleFactor;
        return this;
    }
    
    /**
     * Returns this LevelVector's angle.
     * @return This LevelVector's angle
     */
    public final double getAngle() {
        return (x == 0 && y == 0 ? 0 : Math.atan2(-y, x));
    }
    
    /**
     * Returns the x-coordinate of the unit vector that points in the direction
     * of this LevelVector's angle. This is equal to the cosine of the angle.
     * @return The x-coordinate of this LevelVector's angle
     */
    public final double getAngleX() {
        return (x == 0 && y == 0 ? 1 : x/Math.sqrt(x*x + y*y));
    }
    
    /**
     * Returns the y-coordinate of the unit vector that points in the direction
     * of this LevelVector's angle. Since y-coordinates increase going downward,
     * this is equal to the negative sine of the angle.
     * @return The y-coordinate of this LevelVector's angle
     */
    public final double getAngleY() {
        return (x == 0 && y == 0 ? 0 : y/Math.sqrt(x*x + y*y));
    }
    
    /**
     * Sets this LevelVector's angle to the specified value while retaining its
     * magnitude.
     * @param angle The new angle
     * @return This LevelVector
     */
    public final LevelVector setAngle(double angle) {
        double length = Math.sqrt(x*x + y*y);
        double radians = Math.toRadians(angle);
        x = length*Math.cos(radians);
        y = -length*Math.sin(radians);
        return this;
    }
    
    /**
     * Changes this LevelVector's angle by the specified amount while retaining
     * its magnitude.
     * @param angle The amount by which to change the angle
     * @return This LevelVector
     */
    public final LevelVector changeAngle(double angle) {
        return (x == 0 && y == 0 ? this : setAngle(Math.atan2(-y, x) + angle));
    }
    
    /**
     * Adds the specified LevelVector to this LevelVector.
     * @param vector The LevelVector to be added
     * @return This LevelVector
     */
    public final LevelVector add(LevelVector vector) {
        x += vector.x;
        y += vector.y;
        return this;
    }
    
    /**
     * Adds the specified coordinates to this LevelVector's own.
     * @param x The x-coordinate to be added
     * @param y The y-coordinate to be added
     * @return This LevelVector
     */
    public final LevelVector add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }
    
    /**
     * Returns a new LevelVector that represents the sum of the two specified
     * LevelVectors.
     * @param first The first LevelVector
     * @param second The second LevelVector
     * @return The sum of the two LevelVectors
     */
    public static final LevelVector add(LevelVector first, LevelVector second) {
        return new LevelVector(first.x + second.x, first.y + second.y);
    }
    
    /**
     * Subtracts the specified LevelVector from this LevelVector.
     * @param vector The LevelVector to be subtracted
     * @return This LevelVector
     */
    public final LevelVector sub(LevelVector vector) {
        x -= vector.x;
        y -= vector.y;
        return this;
    }
    
    /**
     * Subtracts the specified coordinates from this LevelVector's own.
     * @param x The x-coordinate to be subtracted
     * @param y The y-coordinate to be subtracted
     * @return This LevelVector
     */
    public final LevelVector sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    
    /**
     * Returns a new LevelVector that represents the second specified
     * LevelVector subtracted from the first.
     * @param first The first LevelVector
     * @param second The second LevelVector
     * @return The second LevelVector subtracted from the first
     */
    public static final LevelVector sub(LevelVector first, LevelVector second) {
        return new LevelVector(first.x - second.x, first.y - second.y);
    }
    
    /**
     * Returns the dot product of this LevelVector and the specified one.
     * @param vector The LevelVector to take the dot product with
     * @return The dot product of this LevelVector and the specified one
     */
    public final double dot(LevelVector vector) {
        return x*vector.x + y*vector.y;
    }
    
    /**
     * Returns the magnitude of the cross product of this LevelVector and the
     * specified one.
     * @param vector The LevelVector to take the cross product with
     * @return The magnitude of the cross product of this LevelVector and the
     * specified one
     */
    public final double cross(LevelVector vector) {
        return x*vector.y - y*vector.x;
    }
    
    /**
     * Flips this LevelVector to reflect the flipped status of the specified
     * Hitbox and rotates it to reflect the Hitbox's angle of rotation, as if
     * those properties were formerly relative to the Hitbox.
     * @param hitbox The Hitbox to reflect
     * @return This LevelVector
     */
    public final LevelVector relativeTo(Hitbox hitbox) {
        return flip(hitbox.getAbsXFlip(), hitbox.getAbsYFlip()).changeAngle(hitbox.getAbsAngle());
    }
    
    /**
     * Flips this LevelVector to reflect the flipped status of the specified
     * LevelObject and rotates it to reflect the LevelObject's angle of
     * rotation, as if those properties were formerly relative to the
     * LevelObject.
     * @param object The LevelObject to reflect
     * @return This LevelVector
     */
    public final LevelVector relativeTo(LevelObject object) {
        return relativeTo(object.getLocatorHitbox());
    }
    
    /**
     * Returns the distance from this LevelVector's point to that of the
     * specified LevelVector.
     * @param point The point to return the distance to
     * @return The distance from this LevelVector's point to that of the
     * specified LevelVector
     */
    public final double distanceTo(LevelVector point) {
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
    public static final double distanceBetween(double x1, double y1, double x2, double y2) {
        double xDist = x2 - x1;
        double yDist = y2 - y1;
        return Math.sqrt(xDist*xDist + yDist*yDist);
    }
    
    /**
     * Returns the angle from this LevelVector's point to that of the specified
     * LevelVector.
     * @param point The point to return the angle to
     * @return The angle from this LevelVector's point to that of the specified
     * LevelVector
     */
    public final double angleTo(LevelVector point) {
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
    public static final double angleBetween(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(y1 - y2, x2 - x1)) % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    private static boolean segBoxesIntersect(LevelVector start1, LevelVector diff1, LevelVector start2, LevelVector diff2) {
        double minX1, maxX1, minX2, maxX2;
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
        double minY1, maxY1, minY2, maxY2;
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
    
    static final boolean directSegsIntersect(LevelVector start1, LevelVector diff1, LevelVector start2, LevelVector diff2) {
        LevelVector start2MinusStart1 = LevelVector.sub(start2, start1);
        if (diff1.cross(diff2) == 0) {
            if (start2MinusStart1.cross(diff1) == 0) {
                double diff1Dot = diff1.dot(diff1);
                double t0 = start2MinusStart1.dot(diff1)/diff1Dot;
                double diff2DotDiff1 = diff2.dot(diff1);
                double t1 = diff2DotDiff1/diff1Dot;
                if (diff2DotDiff1 < 0) {
                    return t1 > 0 || t0 < 1;
                }
                return t0 > 0 || t1 < 1;
            }
            return false;
        }
        double diff1CrossDiff2 = diff1.cross(diff2);
        double t = start2MinusStart1.cross(diff2)/diff1CrossDiff2;
        double u = start2MinusStart1.cross(diff1)/diff1CrossDiff2;
        return t > 0 && t < 1 && u > 0 && u < 1;
    }
    
    /**
     * Returns whether the two specified open line segments share any points.
     * @param start1 One of the first line segment's endpoints
     * @param diff1 The difference of the first line segment's endpoints
     * @param start2 One of the second line segment's endpoints
     * @param diff2 The difference of the second line segment's endpoints
     * @return Whether the two line segments intersect
     */
    public static final boolean lineSegmentsIntersect(LevelVector start1, LevelVector diff1, LevelVector start2, LevelVector diff2) {
        return segBoxesIntersect(start1, diff1, start2, diff2) && directSegsIntersect(start1, diff1, start2, diff2);
    }
    
    /**
     * Returns the single point at which the two specified open line segments
     * intersect, or null if they intersect at no points or at an infinity of
     * points.
     * @param start1 One of the first line segment's endpoints
     * @param diff1 The difference of the first line segment's endpoints
     * @param start2 One of the second line segment's endpoints
     * @param diff2 The difference of the second line segment's endpoints
     * @return The point at which the two line segments intersect
     */
    public static final LevelVector lineSegmentsIntersectionPoint(LevelVector start1, LevelVector diff1, LevelVector start2, LevelVector diff2) {
        if (!segBoxesIntersect(start1, diff1, start2, diff2) || diff1.cross(diff2) == 0) {
            return null;
        }
        LevelVector start2MinusStart1 = LevelVector.sub(start2, start1);
        double diff1CrossDiff2 = diff1.cross(diff2);
        double t = start2MinusStart1.cross(diff2)/diff1CrossDiff2;
        double u = start2MinusStart1.cross(diff1)/diff1CrossDiff2;
        if (t > 0 && t < 1 && u > 0 && u < 1) {
            return LevelVector.add(start1, new LevelVector(diff1).scale(t));
        }
        return null;
    }
    
}
