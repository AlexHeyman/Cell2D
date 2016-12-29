package cell2D.level;

public class SlopeHitbox extends Hitbox {
    
    private final LevelVector relDifference, absDifference;
    private double left, right, top, bottom;
    private SlopeType slopeType;
    private boolean presentAbove, presentBelow;
    
    public SlopeHitbox(LevelVector relPosition, LevelVector relDifference,
            SlopeType slopeType, boolean presentAbove, boolean presentBelow) {
        super(relPosition);
        this.relDifference = new LevelVector(relDifference);
        absDifference = new LevelVector();
        this.slopeType = slopeType;
        this.presentAbove = presentAbove;
        this.presentBelow = presentBelow;
        updateData();
    }
    
    public SlopeHitbox(double relX, double relY, double relDX, double relDY,
            SlopeType slopeType, boolean presentAbove, boolean presentBelow) {
        this(new LevelVector(relX, relY), new LevelVector(relDX, relDY), slopeType, presentAbove, presentBelow);
    }
    
    @Override
    public Hitbox getCopy() {
        return new SlopeHitbox(new LevelVector(), relDifference, slopeType, presentAbove, presentBelow);
    }
    
    private void updateData() {
        absDifference.copy(relDifference).flip(getAbsXFlip(), getAbsYFlip());
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
    
    public final SlopeType getSlopeType() {
        return slopeType;
    }
    
    public final void setSlopeType(SlopeType slopeType) {
        this.slopeType = slopeType;
    }
    
    public final boolean getPresentAbove() {
        return presentAbove;
    }
    
    public final void setPresentAbove(boolean presentAbove) {
        this.presentAbove = presentAbove;
    }
    
    public final boolean getPresentBelow() {
        return presentBelow;
    }
    
    public final void setPresentBelow(boolean presentBelow) {
        this.presentBelow = presentBelow;
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
    
}
