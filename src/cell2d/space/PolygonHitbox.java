package cell2d.space;

import cell2d.CellGame;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A PolygonHitbox is a polygonal Hitbox defined by a List of vertices. A
 * PolygonHitbox occupies the area enclosed by a loop of line segments between
 * each of its vertices and the next, and between the first and last vertices.
 * It is the responsibility of the creators and modifiers of a PolygonHitbox to
 * ensure that its vertices are not positioned in such a way that this loop
 * crosses itself. Both relative and absolute vertices are relative to their
 * PolygonHitbox's position. A PolygonHitbox with no vertices is a point at its
 * absolute position.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the SpaceStates that can use
 * this PolygonHitbox
 */
public class PolygonHitbox<T extends CellGame> extends Hitbox<T> {
    
    private static class RelAbsPair {
        
        private final SpaceVector rel;
        private final SpaceVector abs;
        
        private RelAbsPair(SpaceVector rel, SpaceVector abs) {
            this.rel = rel;
            this.abs = abs;
        }
        
    }
    
    private final List<RelAbsPair> vertices;
    private double left, right, top, bottom;
    
    /**
     * Creates a new PolygonHitbox with the specified relative position and
     * vertices.
     * @param relPosition This PolygonHitbox's relative position
     * @param relVertices This PolygonHitbox's relative vertices
     */
    public PolygonHitbox(SpaceVector relPosition, SpaceVector[] relVertices) {
        this(relPosition.getX(), relPosition.getY(), relVertices);
    }
    
    /**
     * Creates a new PolygonHitbox with the specified relative position and
     * vertices.
     * @param relX The x-coordinate of this PolygonHitbox's relative position
     * @param relY The y-coordinate of this PolygonHitbox's relative position
     * @param relVertices This PolygonHitbox's relative vertices
     */
    public PolygonHitbox(double relX, double relY, SpaceVector[] relVertices) {
        super(relX, relY);
        vertices = new ArrayList<>(relVertices.length);
        for (SpaceVector relVertex : relVertices) {
            vertices.add(new RelAbsPair(new SpaceVector(relVertex), null));
        }
        updateData();
    }
    
    /**
     * Creates a new PolygonHitbox with the specified relative position and no
     * vertices.
     * @param relPosition This PolygonHitbox's relative position
     */
    public PolygonHitbox(SpaceVector relPosition) {
        this(relPosition.getX(), relPosition.getY());
    }
    
    /**
     * Creates a new PolygonHitbox with the specified relative position and no
     * vertices.
     * @param relX The x-coordinate of this PolygonHitbox's relative position
     * @param relY The y-coordinate of this PolygonHitbox's relative position
     */
    public PolygonHitbox(double relX, double relY) {
        super(relX, relY);
        vertices = new ArrayList<>();
        updateData();
    }
    
    private PolygonHitbox(double relX, double relY, List<RelAbsPair> vertices) {
        super(relX, relY);
        this.vertices = vertices;
        updateData();
    }
    
    /**
     * Returns a new PolygonHitbox in the shape of a regular polygon.
     * @param relX The x-coordinate of the new PolygonHitbox's relative position
     * @param relY The y-coordinate of the new PolygonHitbox's relative position
     * @param numVertices The new PolygonHitbox's number of vertices. This must
     * be at least 3.
     * @param radius The distance from the new PolygonHitbox's position to each
     * of its vertices
     * @param angle The angle from the origin to the new PolygonHitbox's first
     * relative vertex
     * @return The new PolygonHitbox
     */
    public static final PolygonHitbox regularPolygon(double relX, double relY,
            int numVertices, double radius, double angle) {
        if (numVertices < 3) {
            throw new RuntimeException("Attempted to make a regular polygon with fewer than 3 vertices");
        }
        if (radius < 0) {
            throw new RuntimeException("Attempted to make a regular polygon with a negative radius");
        }
        SpaceVector[] relVertices = new SpaceVector[numVertices];
        double angleChange = 360.0/numVertices;
        for (int i = 0; i < numVertices; i++) {
            relVertices[i] = new SpaceVector(angle).scale(radius);
            angle += angleChange;
        }
        return new PolygonHitbox(relX, relY, relVertices);
    }
    
    @Override
    public Hitbox<T> getCopy() {
        List<RelAbsPair> newVertices = new ArrayList<>(vertices.size());
        for (RelAbsPair vertex : vertices) {
            newVertices.add(new RelAbsPair(new SpaceVector(vertex.rel), null));
        }
        return new PolygonHitbox<>(0, 0, newVertices);
    }
    
    private void updateData() {
        if (vertices.isEmpty()) {
            left = 0;
            right = 0;
            top = 0;
            bottom = 0;
        } else {
            boolean comparing = false;
            for (RelAbsPair vertex : vertices) {
                vertex.abs.setCoordinates(vertex.rel).relativeTo(this);
                if (comparing) {
                    left = Math.min(left, vertex.abs.getX());
                    right = Math.max(right, vertex.abs.getX());
                    top = Math.min(top, vertex.abs.getY());
                    bottom = Math.max(bottom, vertex.abs.getY());
                } else {
                    left = vertex.abs.getX();
                    right = left;
                    top = vertex.abs.getY();
                    bottom = top;
                    comparing = true;
                }
            }
        }
        updateBoundaries();
    }
    
    public final List<SpaceVector> getRelVertices() {
        List<SpaceVector> relVertices = new ArrayList<>(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            relVertices.add(new SpaceVector(vertices.get(i).rel));
        }
        return relVertices;
    }
    
    public final List<SpaceVector> getAbsVertices() {
        List<SpaceVector> absVertices = new ArrayList<>(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            absVertices.add(new SpaceVector(vertices.get(i).abs));
        }
        return absVertices;
    }
    
    public final int getNumVertices() {
        return vertices.size();
    }
    
    public final SpaceVector getRelVertex(int index) {
        return new SpaceVector(vertices.get(index).rel);
    }
    
    public final double getRelVertexX(int index) {
        return vertices.get(index).rel.getX();
    }
    
    public final double getRelVertexY(int index) {
        return vertices.get(index).rel.getY();
    }
    
    public final SpaceVector getAbsVertex(int index) {
        return new SpaceVector(vertices.get(index).abs);
    }
    
    public final double getAbsVertexX(int index) {
        return vertices.get(index).abs.getX();
    }
    
    public final double getAbsVertexY(int index) {
        return vertices.get(index).abs.getY();
    }
    
    public final void addVertex(SpaceVector relVertex) {
        vertices.add(new RelAbsPair(new SpaceVector(relVertex), null));
        updateData();
    }
    
    public final void addVertex(double relX, double relY) {
        vertices.add(new RelAbsPair(new SpaceVector(relX, relY), null));
        updateData();
    }
    
    public final void setRelVertex(int index, SpaceVector relVertex) {
        vertices.set(index, new RelAbsPair(new SpaceVector(relVertex), null));
        updateData();
    }
    
    public final void setRelVertex(int index, double relX, double relY) {
        vertices.set(index, new RelAbsPair(new SpaceVector(relX, relY), null));
        updateData();
    }
    
    public final void setRelVertexX(int index, double relX) {
        vertices.get(index).rel.setX(relX);
        updateData();
    }
    
    public final void setRelVertexY(int index, double relY) {
        vertices.get(index).rel.setY(relY);
        updateData();
    }
    
    public final void removeVertex(int index) {
        vertices.remove(index);
        updateData();
    }
    
    public final void clearVertices() {
        vertices.clear();
        updateData();
    }
    
    public final PolygonHitbox translate(SpaceVector change) {
        return translate(change.getX(), change.getY());
    }
    
    public final PolygonHitbox translate(double changeX, double changeY) {
        for (RelAbsPair vertex : vertices) {
            vertex.rel.add(changeX, changeY);
        }
        updateData();
        return this;
    }
    
    public final PolygonHitbox flip() {
        return flip(true, true);
    }
    
    public final PolygonHitbox flipX() {
        return flip(true, false);
    }
    
    public final PolygonHitbox flipY() {
        return flip(false, true);
    }
    
    public final PolygonHitbox flip(boolean xFlip, boolean yFlip) {
        for (RelAbsPair vertex : vertices) {
            vertex.rel.flip(xFlip, yFlip);
        }
        updateData();
        return this;
    }
    
    public final PolygonHitbox scale(double scaleFactor) {
        for (RelAbsPair vertex : vertices) {
            vertex.rel.scale(scaleFactor);
        }
        updateData();
        return this;
    }
    
    public final PolygonHitbox rotate(double angle) {
        for (RelAbsPair vertex : vertices) {
            vertex.rel.changeAngle(angle);
        }
        updateData();
        return this;
    }
    
    @Override
    public final double getLeftEdge() {
        return getAbsX() + left;
    }
    
    @Override
    public final double getRightEdge() {
        return getAbsX() + right;
    }
    
    @Override
    public final double getTopEdge() {
        return getAbsY() + top;
    }
    
    @Override
    public final double getBottomEdge() {
        return getAbsY() + bottom;
    }
    
    @Override
    final void updateAbsXFlipActions() {
        updateData();
    }
    
    @Override
    final void updateAbsYFlipActions() {
        updateData();
    }
    
    @Override
    final void updateAbsAngleActions() {
        updateData();
    }
    
}
