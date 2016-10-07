package ironclad2D.level;

import ironclad2D.Sprite;
import org.newdawn.slick.Graphics;

public abstract class LevelObject {
    
    LevelState levelState = null;
    LevelState newLevelState = null;
    private double timeFactor = 1;
    private Hitbox locatorHitbox = null;
    private Hitbox overlapHitbox = null;
    private Hitbox solidHitbox = null;
    private int drawLayer;
    private Sprite sprite;
    private double alpha = 1;
    private String filter = null;
    
    public LevelObject(Hitbox locatorHitbox, Hitbox overlapHitbox,
            Hitbox solidHitbox, int drawLayer, Sprite sprite) {
        if (!setLocatorHitbox(locatorHitbox)) {
            throw new RuntimeException("Attempted to create a level object with an invalid locator hitbox");
        }
        setOverlapHitbox(overlapHitbox);
        setSolidHitbox(solidHitbox);
        this.drawLayer = drawLayer;
        this.sprite = sprite;
    }
    
    public final void copyProperties(LevelObject object) {
        setTimeFactor(object.timeFactor);
        setXFlip(object.getXFlip());
        setYFlip(object.getYFlip());
        setAngle(object.getAngle());
    }
    
    public final LevelState getLevelState() {
        return levelState;
    }
    
    public final LevelState getNewLevelState() {
        return newLevelState;
    }
    
    public final boolean addTo(LevelState levelState) {
        return levelState.addObject(this);
    }
    
    void addActions() {
        locatorHitbox.setLevelState(levelState);
        levelState.addLocatorHitbox(locatorHitbox);
        levelState.addOverlapHitbox(overlapHitbox);
        levelState.addSolidHitbox(solidHitbox);
    }
    
    public final boolean remove() {
        if (newLevelState != null) {
            return newLevelState.removeObject(this);
        }
        return false;
    }
    
    void removeActions() {
        locatorHitbox.setLevelState(null);
        levelState.removeLocatorHitbox(locatorHitbox);
        levelState.removeOverlapHitbox(overlapHitbox);
        levelState.removeSolidHitbox(solidHitbox);
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
    
    public final double getX() {
        return locatorHitbox.getRelX();
    }
    
    public final void setX(double x) {
        locatorHitbox.setRelX(x);
    }
    
    public final double getY() {
        return locatorHitbox.getRelY();
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
    
    public final Hitbox getSolidHitbox() {
        return solidHitbox;
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
    
    public final void setAlpha(float alpha) {
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 1) {
            alpha = 1;
        }
        this.alpha = alpha;
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
