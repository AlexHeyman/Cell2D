package cell2D.level;

public class RectangleHitbox extends Hitbox {
    
    private double relLeft, relRight, relTop, relBottom, absLeft, absRight, absTop, absBottom;
    
    public RectangleHitbox(LevelVector relPosition, double relLeft, double relRight, double relTop, double relBottom) {
        super(relPosition);
        if (relLeft > relRight) {
            throw new RuntimeException("Attempted to give a rectangle hitbox a negative width");
        }
        if (relTop > relBottom) {
            throw new RuntimeException("Attempted to give a rectangle hitbox a negative height");
        }
        this.relLeft = relLeft;
        this.relRight = relRight;
        this.relTop = relTop;
        this.relBottom = relBottom;
        absLeft = relLeft;
        absRight = relRight;
        absTop = relTop;
        absBottom = relBottom;
    }
    
    public RectangleHitbox(double relX, double relY, double relLeft, double relRight, double relTop, double relBottom) {
        this(new LevelVector(relX, relY), relLeft, relRight, relTop, relBottom);
    }
    
    @Override
    public Hitbox getCopy() {
        return new RectangleHitbox(0, 0, relLeft, relRight, relTop, relBottom);
    }
    
    public final double getRelLeft() {
        return relLeft;
    }
    
    public final boolean setRelLeft(double relLeft) {
        if (relLeft <= relRight) {
            this.relLeft = relLeft;
            if (getAbsXFlip()) {
                absRight = -relLeft;
            } else {
                absLeft = relLeft;
            }
            updateCells();
            return true;
        }
        return false;
    }
    
    public final double getAbsLeft() {
        return absLeft;
    }
    
    public final double getRelRight() {
        return relRight;
    }
    
    public final boolean setRelRight(double relRight) {
        if (relLeft <= relRight) {
            this.relRight = relRight;
            if (getAbsXFlip()) {
                absLeft = -relRight;
            } else {
                absRight = relRight;
            }
            updateCells();
            return true;
        }
        return false;
    }
    
    public final double getAbsRight() {
        return absRight;
    }
    
    public final double getRelTop() {
        return relTop;
    }
    
    public final boolean setRelTop(double relTop) {
        if (relTop <= relBottom) {
            this.relTop = relTop;
            if (getAbsYFlip()) {
                absBottom = -relTop;
            } else {
                absTop = relTop;
            }
            updateCells();
            return true;
        }
        return false;
    }
    
    public final double getAbsTop() {
        return absTop;
    }
    
    public final double getRelBottom() {
        return relBottom;
    }
    
    public final boolean setRelBottom(double relBottom) {
        if (relTop <= relBottom) {
            this.relBottom = relBottom;
            if (getAbsYFlip()) {
                absTop = -relBottom;
            } else {
                absBottom = relBottom;
            }
            updateCells();
            return true;
        }
        return false;
    }
    
    public final double getAbsBottom() {
        return absBottom;
    }
    
    public final double getWidth() {
        return absRight - absLeft;
    }
    
    public final double getHeight() {
        return absBottom - absTop;
    }
    
    @Override
    public final double getLeftEdge() {
        return getAbsX() + absLeft;
    }
    
    @Override
    public final double getRightEdge() {
        return getAbsX() + absRight;
    }
    
    @Override
    public final double getTopEdge() {
        return getAbsY() + absTop;
    }
    
    @Override
    public final double getBottomEdge() {
        return getAbsY() + absBottom;
    }
    
    @Override
    public final double getCenterX() {
        return getAbsX() + (absLeft + absRight)/2;
    }
    
    @Override
    public final double getCenterY() {
        return getAbsY() + (absTop + absBottom)/2;
    }
    
    @Override
    final void updateAbsXFlipActions() {
        if (getAbsXFlip()) {
            absLeft = -relRight;
            absRight = -relLeft;
        } else {
            absLeft = relLeft;
            absRight = relRight;
        }
        updateCells();
    }
    
    @Override
    final void updateAbsYFlipActions() {
        if (getAbsYFlip()) {
            absTop = -relBottom;
            absBottom = -relTop;
        } else {
            absTop = relTop;
            absBottom = relBottom;
        }
        updateCells();
    }
    
}
