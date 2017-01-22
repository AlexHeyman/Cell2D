package cell2D.level;

import org.newdawn.slick.util.FastTrig;

public class LevelVector {
    
    private double x, y, lengthSquared, length, angle, angleX, angleY;
    
    public LevelVector() {
        clear();
    }
    
    public LevelVector(double x, double y) {
        setCoordinates(x, y);
    }
    
    public LevelVector(double angle) {
        this.angle = modAngle(angle);
        double radians = Math.toRadians(angle);
        angleX = FastTrig.cos(radians);
        angleY = -FastTrig.sin(radians);
        x = angleX;
        y = angleY;
        lengthSquared = 1;
        length = 1;
    }
    
    public LevelVector(LevelVector vector) {
        copy(vector);
    }
    
    public final LevelVector clear() {
        x = 0;
        y = 0;
        lengthSquared = 0;
        length = 0;
        angle = 0;
        angleX = 1;
        angleY = 0;
        return this;
    }
    
    public final LevelVector copy(LevelVector vector) {
        x = vector.x;
        y = vector.y;
        lengthSquared = vector.lengthSquared;
        length = vector.length;
        angle = vector.angle;
        angleX = vector.angleX;
        angleY = vector.angleY;
        return this;
    }
    
    public final double getX() {
        return x;
    }
    
    public final LevelVector setX(double x) {
        this.x = x;
        return updateToMatchCoordinates();
    }
    
    public final double getY() {
        return y;
    }
    
    public final LevelVector setY(double y) {
        this.y = y;
        return updateToMatchCoordinates();
    }
    
    public final LevelVector setCoordinates(double x, double y) {
        this.x = x;
        this.y = y;
        return updateToMatchCoordinates();
    }
    
    private LevelVector updateToMatchCoordinates() {
        lengthSquared = x*x + y*y;
        length = Math.sqrt(lengthSquared);
        double radians = Math.atan2(-y, x);
        angle = modAngle(Math.toDegrees(radians));
        angleX = FastTrig.cos(radians);
        angleY = -FastTrig.sin(radians);
        return this;
    }
    
    public final LevelVector flip() {
        return flip(true, true);
    }
    
    public final LevelVector flipX() {
        return flip(true, false);
    }
    
    public final LevelVector flipY() {
        return flip(false, true);
    }
    
    public final LevelVector flip(boolean xFlip, boolean yFlip) {
        if (xFlip) {
            x = -x;
            angle = modAngle(180 - angle);
            angleX = -angleX;
        }
        if (yFlip) {
            y = -y;
            angle = modAngle(360 - angle);
            angleY = -angleY;
        }
        return this;
    }
    
    public final double getLengthSquared() {
        return lengthSquared;
    }
    
    public final double getLength() {
        return length;
    }
    
    public final LevelVector setLength(double length) {
        if (length < 0) {
            flip();
            length = -length;
        }
        x = length*angleX;
        y = length*angleY;
        this.length = length;
        lengthSquared = length*length;
        return this;
    }
    
    public final LevelVector scale(double scaleFactor) {
        return setLength(length*scaleFactor);
    }
    
    private static double modAngle(double angle) {
        angle %= 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    public final double getAngle() {
        return angle;
    }
    
    public final double getAngleX() {
        return angleX;
    }
    
    public final double getAngleY() {
        return angleY;
    }
    
    public final LevelVector setAngle(double angle) {
        this.angle = modAngle(angle);
        double radians = Math.toRadians(angle);
        angleX = FastTrig.cos(radians);
        angleY = -FastTrig.sin(radians);
        x = length*angleX;
        y = length*angleY;
        return this;
    }
    
    public final LevelVector changeAngle(double angle) {
        return setAngle(this.angle + angle);
    }
    
    public final LevelVector add(LevelVector vector) {
        return setCoordinates(x + vector.x, y + vector.y);
    }
    
    public final LevelVector add(double x, double y) {
        return setCoordinates(this.x + x, this.y + y);
    }
    
    public static final LevelVector add(LevelVector first, LevelVector second) {
        return new LevelVector(first.x + second.x, first.y + second.y);
    }
    
    public final LevelVector sub(LevelVector vector) {
        return setCoordinates(x - vector.x, y - vector.y);
    }
    
    public final LevelVector sub(double x, double y) {
        return setCoordinates(this.x - x, this.y - y);
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
    
    private static boolean segBoxesMeet(LevelVector start1, LevelVector diff1, LevelVector start2, LevelVector diff2) {
        double minX1, maxX1, minX2, maxX2;
        if (diff1.getX() > 0) {
            minX1 = start1.getX();
            maxX1 = minX1 + diff1.getX();
        } else {
            maxX1 = start1.getX();
            minX1 = maxX1 + diff1.getX();
        }
        if (diff2.getX() > 0) {
            minX2 = start2.getX();
            maxX2 = minX2 + diff2.getX();
        } else {
            maxX2 = start2.getX();
            minX2 = maxX2 + diff2.getX();
        }
        if (minX2 >= maxX1 || minX1 >= maxX2) {
            return false;
        }
        double minY1, maxY1, minY2, maxY2;
        if (diff1.getY() > 0) {
            minY1 = start1.getY();
            maxY1 = minY1 + diff1.getY();
        } else {
            maxY1 = start1.getY();
            minY1 = maxY1 + diff1.getY();
        }
        if (diff2.getY() > 0) {
            minY2 = start2.getY();
            maxY2 = minY2 + diff2.getY();
        } else {
            maxY2 = start2.getY();
            minY2 = maxY2 + diff2.getY();
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
        return segBoxesMeet(start1, diff1, start2, diff2) && directSegsIntersect(start1, diff1, start2, diff2);
    }
    
    public static final LevelVector lineSegmentsIntersectionPoint(LevelVector start1, LevelVector diff1, LevelVector start2, LevelVector diff2) {
        if (!segBoxesMeet(start1, diff1, start2, diff2) || diff1.cross(diff2) == 0) {
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
