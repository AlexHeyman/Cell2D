package cell2D.level;

import cell2D.Sprite;
import java.util.HashSet;
import java.util.Set;
import org.newdawn.slick.Graphics;

public abstract class LevelObject {
    
    LevelState state = null;
    LevelState newState = null;
    private double timeFactor = 1;
    private Hitbox locatorHitbox = null;
    private Hitbox overlapHitbox = null;
    private Hitbox solidHitbox = null;
    private int drawLayer;
    private Sprite sprite = null;
    private double alpha = 1;
    private String filter = null;
    
    public LevelObject(Hitbox locatorHitbox, int drawLayer) {
        if (!setLocatorHitbox(locatorHitbox)) {
            throw new RuntimeException("Attempted to create a level object with an invalid locator hitbox");
        }
        this.drawLayer = drawLayer;
    }
    
    public final void copyProperties(LevelObject object) {
        setTimeFactor(object.timeFactor);
        setXFlip(object.getXFlip());
        setYFlip(object.getYFlip());
        setAngle(object.getAngle());
    }
    
    public final LevelState getGameState() {
        return state;
    }
    
    public final LevelState getNewGameState() {
        return newState;
    }
    
    public final boolean addTo(LevelState levelState) {
        return levelState.addObject(this);
    }
    
    void addCellData() {
        state.addLocatorHitbox(locatorHitbox);
        if (overlapHitbox != null) {
            state.addOverlapHitbox(overlapHitbox);
        }
        if (solidHitbox != null) {
            state.addSolidHitbox(solidHitbox);
        }
    }
    
    void addActions() {
        locatorHitbox.setGameState(state);
    }
    
    public final boolean remove() {
        if (newState != null) {
            return newState.removeObject(this);
        }
        return false;
    }
    
    void removeActions() {
        locatorHitbox.setGameState(null);
        state.removeLocatorHitbox(locatorHitbox);
        if (overlapHitbox != null) {
            state.removeOverlapHitbox(overlapHitbox);
        }
        if (solidHitbox != null) {
            state.removeSolidHitbox(solidHitbox);
        }
    }
    
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    public final void setTimeFactor(double timeFactor) {
        if (timeFactor < 0) {
            throw new RuntimeException("Attempted to give a level object a negative time factor");
        }
        this.timeFactor = timeFactor;
        setTimeFactorActions(timeFactor);
    }
    
    void setTimeFactorActions(double timeFactor) {}
    
    public final Hitbox getLocatorHitbox() {
        return locatorHitbox;
    }
    
    public final boolean setLocatorHitbox(Hitbox locatorHitbox) {
        if (locatorHitbox != null) {
            LevelObject object = locatorHitbox.getObject();
            Hitbox parent = locatorHitbox.getParent();
            if ((object == null && parent == null)
                    || (object == this && parent == this.locatorHitbox
                    && !locatorHitbox.isComponentOf(this.locatorHitbox))) {
                if (this.locatorHitbox != null) {
                    removeNonLocatorHitboxes(this.locatorHitbox);
                    this.locatorHitbox.removeAsLocatorHitbox();
                }
                this.locatorHitbox = locatorHitbox;
                locatorHitbox.setObject(this);
                addNonLocatorHitboxes(locatorHitbox);
                locatorHitbox.addAsLocatorHitbox(drawLayer);
                return true;
            }
        }
        return false;
    }
    
    void removeNonLocatorHitboxes(Hitbox locatorHitbox) {
        locatorHitbox.removeChild(overlapHitbox);
        locatorHitbox.removeChild(solidHitbox);
    }
    
    void addNonLocatorHitboxes(Hitbox locatorHitbox) {
        locatorHitbox.addChild(overlapHitbox);
        locatorHitbox.addChild(solidHitbox);
    }
    
    public final LevelVector getPosition() {
        return locatorHitbox.getRelPosition();
    }
    
    public final double getX() {
        return locatorHitbox.getRelX();
    }
    
    public final double getY() {
        return locatorHitbox.getRelY();
    }
    
    public final void setPosition(LevelVector position) {
        locatorHitbox.setRelPosition(position);
    }
    
    public final void setPosition(double x, double y) {
        locatorHitbox.setRelPosition(x, y);
    }
    
    public final void setX(double x) {
        locatorHitbox.setRelX(x);
    }
    
    public final void setY(double y) {
        locatorHitbox.setRelY(y);
    }
    
    public final boolean getXFlip() {
        return locatorHitbox.getRelXFlip();
    }
    
    public final int getXSign() {
        return locatorHitbox.getRelXSign();
    }
    
    public final void setXFlip(boolean xFlip) {
        locatorHitbox.setRelXFlip(xFlip);
    }
    
    public final boolean getYFlip() {
        return locatorHitbox.getRelYFlip();
    }
    
    public final int getYSign() {
        return locatorHitbox.getRelYSign();
    }
    
    public final void setYFlip(boolean yFlip) {
        locatorHitbox.setRelYFlip(yFlip);
    }
    
    public final double getAngle() {
        return locatorHitbox.getRelAngle();
    }
    
    public final double getAngleX() {
        return locatorHitbox.getRelAngleX();
    }
    
    public final double getAngleY() {
        return locatorHitbox.getRelAngleY();
    }
    
    public final void setAngle(double angle) {
        locatorHitbox.setRelAngle(angle);
    }
    
    public final void changeAngle(double angle) {
        locatorHitbox.changeRelAngle(angle);
    }
    
    public final double getLeftEdge() {
        return locatorHitbox.getLeftEdge();
    }
    
    public final double getRightEdge() {
        return locatorHitbox.getRightEdge();
    }
    
    public final double getTopEdge() {
        return locatorHitbox.getTopEdge();
    }
    
    public final double getBottomEdge() {
        return locatorHitbox.getBottomEdge();
    }
    
    public final double getCenterX() {
        return locatorHitbox.getCenterX();
    }
    
    public final double getCenterY() {
        return locatorHitbox.getCenterY();
    }
    
    public final Hitbox getOverlapHitbox() {
        return overlapHitbox;
    }
    
    public final boolean overlaps(LevelObject object) {
        return overlap(this, object);
    }
    
    public static final boolean overlap(LevelObject object1, LevelObject object2) {
        return object1.overlapHitbox != null && object2.overlapHitbox != null
                && Hitbox.overlap(object1.overlapHitbox, object2.overlapHitbox);
    }
    
    public final boolean setOverlapHitbox(Hitbox overlapHitbox) {
        if (overlapHitbox != this.overlapHitbox) {
            boolean acceptable;
            if (overlapHitbox == null) {
                acceptable = true;
            } else {
                LevelObject object = overlapHitbox.getObject();
                Hitbox parent = overlapHitbox.getParent();
                acceptable = (object == null && parent == null)
                        || (overlapHitbox == locatorHitbox)
                        || (object == this && parent == locatorHitbox
                        && !overlapHitbox.isComponentOf(locatorHitbox));
            }
            if (acceptable) {
                if (this.overlapHitbox != null) {
                    this.overlapHitbox.removeAsOverlapHitbox();
                }
                this.overlapHitbox = overlapHitbox;
                if (overlapHitbox != null) {
                    locatorHitbox.addChild(overlapHitbox);
                    overlapHitbox.addAsOverlapHitbox();
                }
                return true;
            }
        }
        return false;
    }
    
    public final <T extends LevelObject> boolean isOverlapping(Class<T> type) {
        return (state == null ? false : state.isOverlapping(this, type));
    }
    
    public final <T extends LevelObject> T overlappingObject(Class<T> type) {
        return (state == null ? null : state.overlappingObject(this, type));
    }
    
    public final <T extends LevelObject> Set<T> overlappingObjects(Class<T> type) {
        return (state == null ? new HashSet<>() : state.overlappingObjects(this, type));
    }
    
    public final Hitbox getSolidHitbox() {
        return solidHitbox;
    }
    
    public final boolean surfaceIsSolid(Direction direction) {
        return (solidHitbox == null ? false : solidHitbox.surfaceIsSolid(direction));
    }
    
    public final void setSurfaceSolid(Direction direction, boolean solid) {
        if (solidHitbox == null) {
            setSolidHitbox(locatorHitbox.getCopy());
        }
        solidHitbox.setSurfaceSolid(direction, solid);
    }
    
    public final void setSolid(boolean solid) {
        if (solidHitbox == null) {
            setSolidHitbox(locatorHitbox.getCopy());
        }
        solidHitbox.setSolid(solid);
    }
    
    public final boolean setSolidHitbox(Hitbox solidHitbox) {
        if (solidHitbox != this.solidHitbox) {
            boolean acceptable;
            if (solidHitbox == null) {
                acceptable = true;
            } else {
                LevelObject object = solidHitbox.getObject();
                Hitbox parent = solidHitbox.getParent();
                acceptable = (object == null && parent == null)
                        || (solidHitbox == locatorHitbox)
                        || (object == this && parent == locatorHitbox
                        && !solidHitbox.isComponentOf(locatorHitbox));
            }
            if (acceptable) {
                if (this.solidHitbox != null) {
                    this.solidHitbox.removeAsSolidHitbox();
                }
                this.solidHitbox = solidHitbox;
                if (solidHitbox != null) {
                    locatorHitbox.addChild(solidHitbox);
                    solidHitbox.addAsSolidHitbox();
                }
                return true;
            }
        }
        return false;
    }
    
    public final int getDrawLayer() {
        return drawLayer;
    }
    
    public final void setDrawLayer(int drawLayer) {
        this.drawLayer = drawLayer;
        locatorHitbox.changeDrawLayer(drawLayer);
    }
    
    public final Sprite getSprite() {
        return sprite;
    }
    
    public final void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
    
    public final double getAlpha() {
        return alpha;
    }
    
    public final void setAlpha(double alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
    }
    
    public final String getFilter() {
        return filter;
    }
    
    public final void setFilter(String filter) {
        if (filter != null && filter.equals("")) {
            filter = null;
        }
        this.filter = filter;
    }
    
    public void draw(Graphics g, int x, int y) {
        sprite.draw(g, x, y, getXFlip(), getYFlip(), getAngle(), alpha, filter);
    }
    
}
