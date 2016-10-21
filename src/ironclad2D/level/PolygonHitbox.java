package ironclad2D.level;

import java.util.ArrayList;
import java.util.List;

public class PolygonHitbox extends Hitbox {
    
    private final List<RelAbsPair> vertices;
    private double left, right, top, bottom, centerX, centerY;
    
    public PolygonHitbox(LevelVector relPosition, LevelVector[] relVertices) {
        super(relPosition);
        vertices = new ArrayList<>(relVertices.length);
        for (LevelVector relVertex : relVertices) {
            vertices.add(new RelAbsPair(new LevelVector(relVertex), null));
        }
        updateData();
    }
    
    public PolygonHitbox(double relX, double relY, LevelVector[] relVertices) {
        this(new LevelVector(relX, relY), relVertices);
    }
    
    public PolygonHitbox(LevelVector relPosition) {
        super(relPosition);
        vertices = new ArrayList<>();
        updateData();
    }
    
    public PolygonHitbox(double relX, double relY) {
        this(new LevelVector(relX, relY));
    }
    
    private PolygonHitbox(double relX, double relY, List<RelAbsPair> vertices) {
        super(relX, relY);
        this.vertices = vertices;
        updateData();
    }
    
    public static final PolygonHitbox makeRegularPolygon(double relX, double relY,
            int numVertices, double radius, double angle) {
        if (numVertices < 2) {
            throw new RuntimeException("Attempted to make a regular polygon with less than 2 vertices");
        }
        if (radius < 0) {
            throw new RuntimeException("Attempted to make a regular polygon with a negative radius");
        }
        LevelVector[] relVertices = new LevelVector[numVertices];
        double angleChange = 360/numVertices;
        for (int i = 0; i < numVertices; i++) {
            relVertices[i] = new LevelVector(angle).scale(radius);
            angle += angleChange;
        }
        return new PolygonHitbox(relX, relY, relVertices);
    }
    
    private class RelAbsPair {
        
        private LevelVector rel;
        private LevelVector abs;
        
        private RelAbsPair(LevelVector rel, LevelVector abs) {
            this.rel = rel;
            this.abs = abs;
        }
        
    }
    
    @Override
    public Hitbox getCopy() {
        List<RelAbsPair> newVertices = new ArrayList<>(vertices.size());
        for (RelAbsPair vertex : vertices) {
            newVertices.add(new RelAbsPair(new LevelVector(vertex.rel), null));
        }
        return new PolygonHitbox(0, 0, newVertices);
    }
    
    private void updateData() {
        centerX = 0;
        centerY = 0;
        if (vertices.isEmpty()) {
            left = 0;
            right = 0;
            top = 0;
            bottom = 0;
        } else {
            boolean comparing = false;
            for (RelAbsPair vertex : vertices) {
                vertex.abs.copy(vertex.rel).relativeTo(this);
                centerX += vertex.abs.getX();
                centerY += vertex.abs.getY();
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
            centerX /= vertices.size();
            centerY /= vertices.size();
        }
        updateChunks();
    }
    
    public final int getNumVertices() {
        return vertices.size();
    }
    
    public final LevelVector getRelVertex(int index) {
        return new LevelVector(vertices.get(index).rel);
    }
    
    public final double getRelVertexX(int index) {
        return vertices.get(index).rel.getX();
    }
    
    public final double getRelVertexY(int index) {
        return vertices.get(index).rel.getY();
    }
    
    public final LevelVector getAbsVertex(int index) {
        return new LevelVector(vertices.get(index).abs);
    }
    
    public final double getAbsVertexX(int index) {
        return vertices.get(index).abs.getX();
    }
    
    public final double getAbsVertexY(int index) {
        return vertices.get(index).abs.getY();
    }
    
    public final void addVertex(LevelVector relVertex) {
        vertices.add(new RelAbsPair(new LevelVector(relVertex), null));
        updateData();
    }
    
    public final void addVertex(double relX, double relY) {
        vertices.add(new RelAbsPair(new LevelVector(relX, relY), null));
        updateData();
    }
    
    public final void setRelVertex(int index, LevelVector relVertex) {
        vertices.set(index, new RelAbsPair(new LevelVector(relVertex), null));
        updateData();
    }
    
    public final void setRelVertex(int index, double relX, double relY) {
        vertices.set(index, new RelAbsPair(new LevelVector(relX, relY), null));
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
    
    public final PolygonHitbox translate(LevelVector translation) {
        for (RelAbsPair vertex : vertices) {
            vertex.rel.add(translation);
        }
        updateData();
        return this;
    }
    
    public final PolygonHitbox translate(double x, double y) {
        return translate(new LevelVector(x, y));
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
    
    public final PolygonHitbox changeAngle(double angle) {
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
    public final double getCenterX() {
        return getAbsX() + centerX;
    }
    
    @Override
    public final double getCenterY() {
        return getAbsY() + centerY;
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
