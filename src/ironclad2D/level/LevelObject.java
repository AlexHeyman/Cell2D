package ironclad2D.level;

import ironclad2D.Sprite;
import java.util.HashSet;
import java.util.Set;
import org.newdawn.slick.Graphics;

public abstract class LevelObject {
    
    LevelState levelState = null;
    LevelState newLevelState = null;
    private double timeFactor = 1;
    private Hitbox locatorHitbox = null;
    private final Set<Hitbox> nonLocatorHitboxes = new HashSet<>();
    private Hitbox collisionHitbox = null;
    private Hitbox solidHitbox = null;
    private int drawLayer;
    private Sprite sprite;
    private double alpha = 1;
    private String filter = null;
    
    public LevelObject(Hitbox locatorHitbox, Hitbox collisionHitbox,
            Hitbox solidHitbox, int drawLayer, Sprite sprite) {
        if (!setLocatorHitbox(locatorHitbox)) {
            throw new RuntimeException("Attempted to create a level object with an invalid locator hitbox");
        }
        this.collisionHitbox = collisionHitbox;
        this.solidHitbox = solidHitbox;
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
    
    void addActions() {}
    
    public final boolean remove() {
        if (newLevelState != null) {
            return newLevelState.removeObject(this);
        }
        return false;
    }
    
    void removeActions() {}
    
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
                    for (Hitbox hitbox : nonLocatorHitboxes) {
                        this.locatorHitbox.removeChild(hitbox);
                    }
                    this.locatorHitbox.removeRole(0);
                }
                this.locatorHitbox = locatorHitbox;
                locatorHitbox.setObject(this);
                locatorHitbox.addRole(0);
                updateNonLocatorHitboxes();
                for (Hitbox hitbox : nonLocatorHitboxes) {
                    locatorHitbox.addChild(hitbox);
                }
                return true;
            }
        }
        return false;
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
    
    private void updateNonLocatorHitboxes() {
        nonLocatorHitboxes.clear();
        nonLocatorHitboxes.add(collisionHitbox);
        nonLocatorHitboxes.add(solidHitbox);
        updateNonLocatorHitboxes(nonLocatorHitboxes);
        nonLocatorHitboxes.remove(null);
        nonLocatorHitboxes.remove(locatorHitbox);
    }
    
    void updateNonLocatorHitboxes(Set<Hitbox> nonLocatorHitboxes) {}
    
    public final Hitbox getCollisionHitbox() {
        return collisionHitbox;
    }
    
    public final Hitbox getSolidHitbox() {
        return solidHitbox;
    }
    
    public final int getDrawLayer() {
        return drawLayer;
    }
    
    public final void setDrawLayer(int drawLayer) {
        if (levelState != null) {
            levelState.updateObjectDrawLayer(this, this.drawLayer, drawLayer);
        }
        this.drawLayer = drawLayer;
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
