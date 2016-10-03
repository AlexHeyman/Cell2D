package ironclad2D.level;

import java.util.HashSet;
import java.util.Set;
import org.newdawn.slick.util.FastTrig;

public abstract class Hitbox {
    
    private Hitbox parent = null;
    private final Set<Hitbox> children = new HashSet<>();
    private LevelObject object = null;
    LevelState levelState = null;
    int[] chunkRange = null;
    final boolean[] roles = new boolean[4];
    int numRoles = 0;
    private boolean active = true;
    private LevelVector relPosition, absPosition;
    private boolean relXFlip = false;
    private boolean absXFlip = false;
    private boolean relYFlip = false;
    private boolean absYFlip = false;
    private double relAngle = 0;
    private double relAngleX = 1;
    private double relAngleY = 0;
    private double absAngle = 0;
    private double absAngleX = 1;
    private double absAngleY = 0;
    
    public Hitbox(double relX, double relY) {
        relPosition = new LevelVector(relX, relY);
        absPosition = relPosition.getCopy();
    }
    
    final Hitbox getParent() {
        return parent;
    }
    
    final boolean addChild(Hitbox hitbox) {
        if (hitbox.parent == null && hitbox.object == null) {
            children.add(hitbox);
            hitbox.parent = this;
            hitbox.recursivelyUpdateData();
            return true;
        }
        return false;
    }
    
    final boolean removeChild(Hitbox hitbox) {
        if (hitbox.parent == this) {
            children.remove(hitbox);
            hitbox.parent = null;
            hitbox.recursivelyUpdateData();
            return true;
        }
        return false;
    }
    
    private void recursivelyUpdateData() {
        if (parent == null) {
            object = null;
            absXFlip = relXFlip;
            absYFlip = relYFlip;
        } else {
            object = parent.object;
            absXFlip = parent.absXFlip ^ relXFlip;
            absYFlip = parent.absYFlip ^ relYFlip;
        }
        updateAbsAngle();
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateData();
            }
        }
        updateAbsXFlipActions();
        updateAbsYFlipActions();
        updateAbsAngleActions();
    }
    
    public final boolean isComponentOf(Hitbox hitbox) {
        return hitbox instanceof CompositeHitbox && ((CompositeHitbox)hitbox).isComponent(this);
    }
    
    public final LevelObject getObject() {
        return object;
    }
    
    final void setObject(LevelObject object) {
        if (object != this.object) {
            setLevelState(object == null ? null : object.levelState);
            recursivelySetObject(object);
        }
    }
    
    private void recursivelySetObject(LevelObject object) {
        this.object = object;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelySetObject(object);
            }
        }
    }
    
    final void addAsLocatorHitbox() {
        if (levelState != null) {
            levelState.addLocatorHitbox(this, object.getDrawLayer());
        }
        roles[0] = true;
        numRoles++;
    }
    
    final void removeAsLocatorHitbox() {
        roles[0] = false;
        numRoles--;
        if (levelState != null) {
            levelState.removeLocatorHitbox(this, object.getDrawLayer());
        }
        if (numRoles == 0) {
            setObject(null);
        }
    }
    
    public final LevelState getLevelState() {
        return levelState;
    }
    
    final void setLevelState(LevelState levelState) {
        this.levelState = levelState;
        chunkRange = (levelState == null ? null : levelState.getChunkRange(this));
    }
    
    public final boolean getActive() {
        return active;
    }
    
    public final void setActive(boolean active) {
        this.active = active;
    }
    
    public final double getRelX() {
        return relPosition.getX();
    }
    
    public final void setRelX(double relX) {
        relPosition.setX(relX);
        recursivelyUpdateAbsPosition();
    }
    
    public final double getAbsX() {
        return absPosition.getX();
    }
    
    public final double getRelY() {
        return relPosition.getY();
    }
    
    public final void setRelY(double relY) {
        relPosition.setY(relY);
        recursivelyUpdateAbsPosition();
    }
    
    public final double getAbsY() {
        return absPosition.getY();
    }
    
    public final LevelVector getRelPosition() {
        return relPosition.getCopy();
    }
    
    public final void setRelPosition(double relX, double relY) {
        relPosition.setCoordinates(relX, relY);
        recursivelyUpdateAbsPosition();
    }
    
    public final LevelVector getAbsPosition() {
        return absPosition.getCopy();
    }
    
    private void updateAbsPosition() {
        if (parent == null) {
            absPosition = relPosition.getCopy();
        } else {
            absPosition = parent.absPosition.getCopy().add(relPosition.getCopy().flip(parent.absXFlip, parent.absYFlip).changeAngle(parent.absAngle));
        }
    }
    
    private void recursivelyUpdateAbsPosition() {
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsPosition();
            }
        }
    }
    
    public final boolean getRelXFlip() {
        return relXFlip;
    }
    
    public final int getRelXSign() {
        return (relXFlip ? -1 : 1);
    }
    
    public final void setRelXFlip(boolean relXFlip) {
        this.relXFlip = relXFlip;
        absXFlip = (parent == null ? false : parent.absXFlip) ^ relXFlip;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsXFlip();
            }
        }
        updateAbsXFlipActions();
    }
    
    public final boolean getAbsXFlip() {
        return absXFlip;
    }
    
    public final int getAbsXSign() {
        return (absXFlip ? -1 : 1);
    }
    
    void updateAbsXFlipActions() {}
    
    private void recursivelyUpdateAbsXFlip() {
        absXFlip = parent.absXFlip ^ relXFlip;
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsXFlip();
            }
        }
        updateAbsXFlipActions();
    }
    
    public final boolean getRelYFlip() {
        return relYFlip;
    }
    
    public final int getRelYSign() {
        return (relYFlip ? -1 : 1);
    }
    
    public final void setRelYFlip(boolean relYFlip) {
        this.relYFlip = relYFlip;
        absYFlip = (parent == null ? false : parent.absYFlip) ^ relYFlip;
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsYFlip();
            }
        }
        updateAbsYFlipActions();
    }
    
    public final boolean getAbsYFlip() {
        return absYFlip;
    }
    
    public final int getAbsYSign() {
        return (absYFlip ? -1 : 1);
    }
    
    void updateAbsYFlipActions() {}
    
    private void recursivelyUpdateAbsYFlip() {
        absYFlip = parent.absYFlip ^ relYFlip;
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsYFlip();
            }
        }
        updateAbsYFlipActions();
    }
    
    private double modAngle(double angle) {
        angle %= 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    public final double getRelAngle() {
        return relAngle;
    }
    
    public final double getRelAngleX() {
        return relAngleX;
    }
    
    public final double getRelAngleY() {
        return relAngleY;
    }
    
    public final void setRelAngle(double relAngle) {
        this.relAngle = modAngle(relAngle);
        double radians = Math.toRadians(relAngle);
        relAngleX = FastTrig.cos(radians);
        relAngleY = -FastTrig.sin(radians);
        updateAbsAngle();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsAngle();
            }
        }
        updateAbsAngleActions();
    }
    
    public final double getAbsAngle() {
        return absAngle;
    }
    
    public final double getAbsAngleX() {
        return absAngleX;
    }
    
    public final double getAbsAngleY() {
        return absAngleY;
    }
    
    private void updateAbsAngle() {
        if (parent == null) {
            absAngle = relAngle;
        } else {
            double angle = relAngle;
            if (parent.absXFlip) {
                angle = 180 - angle;
            }
            if (parent.absYFlip) {
                angle = 360 - angle;
            }
            absAngle = modAngle(parent.absAngle + angle);
        }
        double radians = Math.toRadians(absAngle);
        absAngleX = FastTrig.cos(radians);
        absAngleY = -FastTrig.sin(radians);
    }
    
    void updateAbsAngleActions() {}
    
    private void recursivelyUpdateAbsAngle() {
        updateAbsAngle();
        updateAbsPosition();
        if (!children.isEmpty()) {
            for (Hitbox child : children) {
                child.recursivelyUpdateAbsAngle();
            }
        }
        updateAbsAngleActions();
    }
    
    public abstract double getLeftEdge();
    
    public abstract double getRightEdge();
    
    public abstract double getTopEdge();
    
    public abstract double getBottomEdge();
    
    public abstract double getCenterX();
    
    public abstract double getCenterY();
    
}
