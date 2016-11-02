package ironclad2D.level;

public class SlopeHitbox extends Hitbox {
    
    private final LevelVector relDifference, absDifference;
    private double left, right, top, bottom;
    
    public SlopeHitbox(LevelVector relPosition, LevelVector relDifference) {
        super(relPosition);
        this.relDifference = new LevelVector(relDifference);
        absDifference = new LevelVector();
        updateData();
    }
    
    public SlopeHitbox(double relX, double relY, double relDX, double relDY) {
        super(relX, relY);
        relDifference = new LevelVector(relDX, relDY);
        absDifference = new LevelVector();
        updateData();
    }
    
    @Override
    public Hitbox getCopy() {
        return new SlopeHitbox(new LevelVector(0, 0), relDifference);
    }
    
    private void updateData() {
        absDifference.copy(relDifference).flip(getAbsXFlip(), getAbsYFlip());
        left = Math.min(absDifference.getX(), 0);
        right = Math.max(absDifference.getX(), 0);
        top = Math.min(absDifference.getY(), 0);
        bottom = Math.max(absDifference.getY(), 0);
        updateChunks();
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
        relDifference.copy(difference);
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
    
}
