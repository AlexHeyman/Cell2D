package ironclad2D.level;

public class LineHitbox extends Hitbox {
    
    private LevelVector relDifference, absDifference;
    private double left, right, top, bottom;
    
    public LineHitbox(double relX, double relY, double relDX, double relDY) {
        super(relX, relY);
        relDifference = new LevelVector(relDX, relDY);
        updateData();
    }
    
    private void updateData() {
        absDifference = relDifference.getCopy().flip(getAbsXFlip(), getAbsYFlip()).changeAngle(getAbsAngle());
        left = Math.min(absDifference.getX(), 0);
        right = Math.max(absDifference.getX(), 0);
        top = Math.min(absDifference.getY(), 0);
        bottom = Math.max(absDifference.getY(), 0);
        updateChunks();
    }
    
    public final double getRelDX() {
        return relDifference.getX();
    }
    
    public final void setRelDX(double relDX) {
        relDifference.setX(relDX);
        updateData();
    }
    
    public final double getAbsDX() {
        return absDifference.getX();
    }
    
    public final double getRelDY() {
        return relDifference.getY();
    }
    
    public final void setRelDY(double relDY) {
        relDifference.setY(relDY);
        updateData();
    }
    
    public final double getAbsDY() {
        return absDifference.getY();
    }
    
    public final LevelVector getRelDifference() {
        return relDifference.getCopy();
    }
    
    public final void setRelDifference(double relDX, double relDY) {
        relDifference.setCoordinates(relDX, relDY);
        updateData();
    }
    
    public final LevelVector getAbsDifference() {
        return absDifference.getCopy();
    }
    
    public final double getLength() {
        return relDifference.getLength();
    }
    
    public final void setLength(double length) {
        relDifference.setLength(length);
        updateData();
    }
    
    public final void scale(double scaleFactor) {
        relDifference.scale(scaleFactor);
        updateData();
    }
    
    public final double getX2() {
        return getAbsX() + absDifference.getX();
    }
    
    public final double getY2() {
        return getAbsY() + absDifference.getY();
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
        return getAbsX() + absDifference.getX()/2;
    }
    
    @Override
    public final double getCenterY() {
        return getAbsY() + absDifference.getY()/2;
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
