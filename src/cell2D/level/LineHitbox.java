package cell2D.level;

import cell2D.CellGame;

public class LineHitbox<T extends CellGame> extends Hitbox<T> {
    
    private final LevelVector relDifference, absDifference;
    private double left, right, top, bottom;
    
    public LineHitbox(LevelVector relPosition, LevelVector relDifference) {
        super(relPosition);
        this.relDifference = new LevelVector(relDifference);
        absDifference = new LevelVector();
        updateData();
    }
    
    public LineHitbox(double relX, double relY, double relDX, double relDY) {
        this(new LevelVector(relX, relY), new LevelVector(relDX, relDY));
    }
    
    @Override
    public Hitbox<T> getCopy() {
        return new LineHitbox<>(new LevelVector(0, 0), relDifference);
    }
    
    private void updateData() {
        absDifference.setCoordinates(relDifference).relativeTo(this);
        left = Math.min(absDifference.getX(), 0);
        right = Math.max(absDifference.getX(), 0);
        top = Math.min(absDifference.getY(), 0);
        bottom = Math.max(absDifference.getY(), 0);
        updateBoundaries();
    }
    
    public final LevelVector getRelDifference() {
        return new LevelVector(relDifference);
    }
    
    public final double getRelDX() {
        return relDifference.getX();
    }
    
    public final double getRelDY() {
        return relDifference.getY();
    }
    
    public final void setRelDifference(LevelVector difference) {
        relDifference.setCoordinates(difference);
        updateData();
    }
    
    public final void setRelDifference(double relDX, double relDY) {
        relDifference.setCoordinates(relDX, relDY);
        updateData();
    }
    
    public final void setRelDX(double relDX) {
        relDifference.setX(relDX);
        updateData();
    }
    
    public final void setRelDY(double relDY) {
        relDifference.setY(relDY);
        updateData();
    }
    
    public final LevelVector getAbsDifference() {
        return new LevelVector(absDifference);
    }
    
    public final double getAbsDX() {
        return absDifference.getX();
    }
    
    public final double getAbsDY() {
        return absDifference.getY();
    }
    
    public final LevelVector getPosition2() {
        return new LevelVector(getAbsX() + absDifference.getX(), getAbsY() + absDifference.getY());
    }
    
    public final double getX2() {
        return getAbsX() + absDifference.getX();
    }
    
    public final double getY2() {
        return getAbsY() + absDifference.getY();
    }
    
    public final double getLength() {
        return relDifference.getMagnitude();
    }
    
    public final void setLength(double length) {
        relDifference.setMagnitude(length);
        updateData();
    }
    
    public final void scale(double scaleFactor) {
        relDifference.scale(scaleFactor);
        updateData();
    }
    
    public final double getLineAngle() {
        return absDifference.getAngle();
    }
    
    public final double getLineAngleX() {
        return absDifference.getAngleX();
    }
    
    public final double getLineAngleY() {
        return absDifference.getAngleY();
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