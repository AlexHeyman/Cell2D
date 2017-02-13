package cell2D.level;

public class LevelVector {
    
    private double x, y;
    
    public LevelVector() {
        x = 0;
        y = 0;
    }
    
    public LevelVector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public LevelVector(double angle) {
        double radians = Math.toRadians(modAngle(angle));
        x = Math.cos(radians);
        y = -Math.sin(radians);
    }
    
    public LevelVector(LevelVector vector) {
        x = vector.x;
        y = vector.y;
    }
    
    public final LevelVector clear() {
        x = 0;
        y = 0;
        return this;
    }
    
    public final LevelVector copy(LevelVector vector) {
        x = vector.x;
        y = vector.y;
        return this;
    }
    
    public final double getX() {
        return x;
    }
    
    public final LevelVector setX(double x) {
        this.x = x;
        return this;
    }
    
    public final double getY() {
        return y;
    }
    
    public final LevelVector setY(double y) {
        this.y = y;
        return this;
    }
    
    public final LevelVector setCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public final LevelVector flip() {
        x = -x;
        y = -y;
        return this;
    }
    
    public final LevelVector flipX() {
        x = -x;
        return this;
    }
    
    public final LevelVector flipY() {
        y = -y;
        return this;
    }
    
    public final LevelVector flip(boolean xFlip, boolean yFlip) {
        if (xFlip) {
            x = -x;
        }
        if (yFlip) {
            y = -y;
        }
        return this;
    }
    
    public final double getLengthSquared() {
        return x*x + y*y;
    }
    
    public final double getLength() {
        return Math.sqrt(x*x + y*y);
    }
    
    public final LevelVector setLength(double length) {
        return (x == 0 && y == 0 ? setX(length) : scale(length/Math.sqrt(x*x + y*y)));
    }
    
    public final LevelVector scale(double scaleFactor) {
        x *= scaleFactor;
        y *= scaleFactor;
        return this;
    }
    
    private static double modAngle(double angle) {
        angle %= 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    public final double getAngle() {
        return (x == 0 && y == 0 ? 0 : Math.atan2(-y, x));
    }
    
    public final double getAngleX() {
        return (x == 0 && y == 0 ? 1 : x/Math.sqrt(x*x + y*y));
    }
    
    public final double getAngleY() {
        return (x == 0 && y == 0 ? 0 : y/Math.sqrt(x*x + y*y));
    }
    
    public final LevelVector setAngle(double angle) {
        double length = Math.sqrt(x*x + y*y);
        double radians = Math.toRadians(modAngle(angle));
        x = length*Math.cos(radians);
        y = -length*Math.sin(radians);
        return this;
    }
    
    public final LevelVector changeAngle(double angle) {
        return (x == 0 && y == 0 ? this : setAngle(Math.atan2(-y, x) + angle));
    }
    
    public final LevelVector add(LevelVector vector) {
        x += vector.x;
        y += vector.y;
        return this;
    }
    
    public final LevelVector add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }
    
    public static final LevelVector add(LevelVector first, LevelVector second) {
        return new LevelVector(first.x + second.x, first.y + second.y);
    }
    
    public final LevelVector sub(LevelVector vector) {
        x -= vector.x;
        y -= vector.y;
        return this;
    }
    
    public final LevelVector sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    
    public static final LevelVector sub(LevelVector first, LevelVector second) {
        return new LevelVector(first.x - second.x, first.y - second.y);
    }
    
    public final double dot(LevelVector vector) {
        return x*vector.x + y*vector.y;
    }
    
    public static final double dot(LevelVector first, LevelVector second) {
        return first.x*second.x + first.y*second.y;
    }
    
    public final double cross(LevelVector vector) {
        return x*vector.y - y*vector.x;
    }
    
    public static final double cross(LevelVector first, LevelVector second) {
        return first.x*second.y - first.y*second.x;
    }
    
    public final LevelVector relativeTo(Hitbox hitbox) {
        return flip(hitbox.getAbsXFlip(), hitbox.getAbsYFlip()).changeAngle(hitbox.getAbsAngle());
    }
    
    public final LevelVector relativeTo(LevelObject object) {
        return relativeTo(object.getLocatorHitbox());
    }
    
    public final double distanceTo(LevelVector position) {
        return distanceBetween(x, y, position.x, position.y);
    }
    
    public static final double distanceBetween(double x1, double y1, double x2, double y2) {
        double xDist = x2 - x1;
        double yDist = y2 - y1;
        return Math.sqrt(xDist*xDist + yDist*yDist);
    }
    
    public final double angleTo(LevelVector position) {
        return angleBetween(x, y, position.x, position.y);
    }
    
    public static final double angleBetween(double x1, double y1, double x2, double y2) {
        return modAngle(Math.toDegrees(Math.atan2(y1 - y2, x2 - x1)));
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
    
    public static final boolean lineSegmentsIntersect(LevelVector start1, LevelVector diff1, LevelVector start2, LevelVector diff2) {
        return segBoxesIntersect(start1, diff1, start2, diff2) && directSegsIntersect(start1, diff1, start2, diff2);
    }
    
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
