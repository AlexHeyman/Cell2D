package cell2d.space;

import cell2d.CellVector;
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
        
        private final CellVector rel;
        private final CellVector abs;
        
        private RelAbsPair(CellVector rel, CellVector abs) {
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
    public PolygonHitbox(CellVector relPosition, CellVector[] relVertices) {
        this(relPosition.getX(), relPosition.getY(), relVertices);
    }
    
    /**
     * Creates a new PolygonHitbox with the specified relative position and
     * vertices.
     * @param relX The x-coordinate of this PolygonHitbox's relative position
     * @param relY The y-coordinate of this PolygonHitbox's relative position
     * @param relVertices This PolygonHitbox's relative vertices
     */
    public PolygonHitbox(double relX, double relY, CellVector[] relVertices) {
        super(relX, relY);
        vertices = new ArrayList<>(relVertices.length);
        for (CellVector relVertex : relVertices) {
            vertices.add(new RelAbsPair(new CellVector(relVertex), null));
        }
        updateData();
    }
    
    /**
     * Creates a new PolygonHitbox with the specified relative position and no
     * vertices.
     * @param relPosition This PolygonHitbox's relative position
     */
    public PolygonHitbox(CellVector relPosition) {
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
        CellVector[] relVertices = new CellVector[numVertices];
        double angleChange = 360.0/numVertices;
        for (int i = 0; i < numVertices; i++) {
            relVertices[i] = new CellVector(angle).scale(radius);
            angle += angleChange;
        }
        return new PolygonHitbox(relX, relY, relVertices);
    }
    
    @Override
    public Hitbox<T> getCopy() {
        List<RelAbsPair> newVertices = new ArrayList<>(vertices.size());
        for (RelAbsPair vertex : vertices) {
            newVertices.add(new RelAbsPair(new CellVector(vertex.rel), null));
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
    
    /**
     * Returns the List of this PolygonHitbox's relative vertices. Changes to
     * the returned List will not be reflected in this PolygonHitbox.
     * @return The List of this PolygonHitbox's relative vertices
     */
    public final List<CellVector> getRelVertices() {
        List<CellVector> relVertices = new ArrayList<>(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            relVertices.add(new CellVector(vertices.get(i).rel));
        }
        return relVertices;
    }
    
    /**
     * Returns the List of this PolygonHitbox's absolute vertices. Changes to
     * the returned List will not be reflected in this PolygonHitbox.
     * @return The List of this PolygonHitbox's absolute vertices
     */
    public final List<CellVector> getAbsVertices() {
        List<CellVector> absVertices = new ArrayList<>(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            absVertices.add(new CellVector(vertices.get(i).abs));
        }
        return absVertices;
    }
    
    /**
     * Returns how many vertices this PolygonHitbox has.
     * @return How many vertices this PolygonHitbox has
     */
    public final int getNumVertices() {
        return vertices.size();
    }
    
    /**
     * Returns this PolygonHitbox's relative vertex at the specified index.
     * @param index The index of the relative vertex
     * @return The relative vertex at the specified index
     */
    public final CellVector getRelVertex(int index) {
        return new CellVector(vertices.get(index).rel);
    }
    
    /**
     * Returns the x-coordinate of this PolygonHitbox's relative vertex at the
     * specified index.
     * @param index The index of the relative vertex
     * @return The x-coordinate of the relative vertex at the specified index
     */
    public final double getRelVertexX(int index) {
        return vertices.get(index).rel.getX();
    }
    
    /**
     * Returns the y-coordinate of this PolygonHitbox's relative vertex at the
     * specified index.
     * @param index The index of the relative vertex
     * @return The y-coordinate of the relative vertex at the specified index
     */
    public final double getRelVertexY(int index) {
        return vertices.get(index).rel.getY();
    }
    
    /**
     * Returns this PolygonHitbox's absolute vertex at the specified index.
     * @param index The index of the absolute vertex
     * @return The absolute vertex at the specified index
     */
    public final CellVector getAbsVertex(int index) {
        return new CellVector(vertices.get(index).abs);
    }
    
    /**
     * Returns the x-coordinate of this PolygonHitbox's absolute vertex at the
     * specified index.
     * @param index The index of the absolute vertex
     * @return The x-coordinate of the absolute vertex at the specified index
     */
    public final double getAbsVertexX(int index) {
        return vertices.get(index).abs.getX();
    }
    
    /**
     * Returns the y-coordinate of this PolygonHitbox's absolute vertex at the
     * specified index.
     * @param index The index of the absolute vertex
     * @return The y-coordinate of the absolute vertex at the specified index
     */
    public final double getAbsVertexY(int index) {
        return vertices.get(index).abs.getY();
    }
    
    /**
     * Adds the specified relative vertex to this PolygonHitbox at the index
     * after its last one, between its last and first relative vertices.
     * @param relVertex The new relative vertex
     */
    public final void addVertex(CellVector relVertex) {
        vertices.add(new RelAbsPair(new CellVector(relVertex), null));
        updateData();
    }
    
    /**
     * Adds the specified relative vertex to this PolygonHitbox at the index
     * after its last one, between its last and first relative vertices.
     * @param relX The x-coordinate of the new relative vertex
     * @param relY The y-coordinate of the new relative vertex
     */
    public final void addVertex(double relX, double relY) {
        vertices.add(new RelAbsPair(new CellVector(relX, relY), null));
        updateData();
    }
    
    /**
     * Adds the specified relative vertex to this PolygonHitbox at the specified
     * index.
     * @param index The index at which to add the new relative vertex
     * @param relVertex The new relative vertex
     */
    public final void addRelVertex(int index, CellVector relVertex) {
        vertices.add(index, new RelAbsPair(new CellVector(relVertex), null));
        updateData();
    }
    
    /**
     * Adds the specified relative vertex to this PolygonHitbox at the specified
     * index.
     * @param index The index at which to add the new relative vertex
     * @param relX The x-coordinate of the new relative vertex
     * @param relY The y-coordinate of the new relative vertex
     */
    public final void addRelVertex(int index, double relX, double relY) {
        vertices.add(index, new RelAbsPair(new CellVector(relX, relY), null));
        updateData();
    }
    
    /**
     * Sets this PolygonHitbox's relative vertex at the specified index to the
     * specified value.
     * @param index The index of the relative vertex to be changed
     * @param relVertex The new relative vertex
     */
    public final void setRelVertex(int index, CellVector relVertex) {
        vertices.set(index, new RelAbsPair(new CellVector(relVertex), null));
        updateData();
    }
    
    /**
     * Sets this PolygonHitbox's relative vertex at the specified index to the
     * specified value.
     * @param index The index of the relative vertex to be changed
     * @param relX The x-coordinate of the new relative vertex
     * @param relY The y-coordinate of the new relative vertex
     */
    public final void setRelVertex(int index, double relX, double relY) {
        vertices.set(index, new RelAbsPair(new CellVector(relX, relY), null));
        updateData();
    }
    
    /**
     * Sets the x-coordinate of this PolygonHitbox's relative vertex at the
     * specified index to the specified value.
     * @param index The index of the relative vertex to be changed
     * @param relX The relative vertex's new x-coordinate
     */
    public final void setRelVertexX(int index, double relX) {
        vertices.get(index).rel.setX(relX);
        updateData();
    }
    
    /**
     * Sets the y-coordinate of this PolygonHitbox's relative vertex at the
     * specified index to the specified value.
     * @param index The index of the relative vertex to be changed
     * @param relY The relative vertex's new y-coordinate
     */
    public final void setRelVertexY(int index, double relY) {
        vertices.get(index).rel.setY(relY);
        updateData();
    }
    
    /**
     * Removes this PolygonHitbox's vertex at the specified index.
     * @param index The index of the vertex to be removed
     */
    public final void removeVertex(int index) {
        vertices.remove(index);
        updateData();
    }
    
    /**
     * Removes all of this PolygonHitbox's vertices.
     */
    public final void clearVertices() {
        vertices.clear();
        updateData();
    }
    
    /**
     * Multiplies the coordinates of all of this PolygonHitbox's relative
     * vertices by the specified factor.
     * @param scaleFactor The factor by which to scale this PolygonHitbox
     */
    public final void scale(double scaleFactor) {
        for (RelAbsPair vertex : vertices) {
            vertex.rel.scale(scaleFactor);
        }
        updateData();
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
