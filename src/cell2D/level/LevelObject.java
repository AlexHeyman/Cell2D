package cell2D.level;

import cell2D.Sprite;
import java.util.ArrayList;
import java.util.List;
import org.newdawn.slick.Graphics;

public abstract class LevelObject {
    
    LevelState state = null;
    LevelState newState = null;
    private double timeFactor = -1;
    private Hitbox locatorHitbox = null;
    private final Hitbox centerHitbox;
    private Hitbox overlapHitbox = null;
    private Hitbox solidHitbox = null;
    private int drawPriority = 0;
    private Sprite sprite = null;
    private double alpha = 1;
    private String filter = null;
    
    public LevelObject(Hitbox locatorHitbox) {
        centerHitbox = new PointHitbox(0, 0);
        centerHitbox.addAsCenterHitbox();
        if (!setLocatorHitbox(locatorHitbox)) {
            throw new RuntimeException("Attempted to create a LevelObject with an invalid locator hitbox");
        }
    }
    
    public LevelObject(Hitbox locatorHitbox, LevelObject spawner) {
        this(locatorHitbox);
        setTimeFactor(spawner.timeFactor);
        setXFlip(spawner.getXFlip());
        setYFlip(spawner.getYFlip());
        setAngle(spawner.getAngle());
    }
    
    public final LevelState getGameState() {
        return state;
    }
    
    public final LevelState getNewGameState() {
        return newState;
    }
    
    public final void setGameState(LevelState state) {
        if (this.state != null) {
            this.state.removeObject(this);
        }
        if (state != null) {
            state.addObject(this);
        }
    }
    
    public final boolean addTo(LevelState state) {
        return state.addObject(this);
    }
    
    public final boolean remove() {
        return (state == null ? false : state.removeObject(this));
    }
    
    void addActions() {
        locatorHitbox.setGameState(state);
    }
    
    void addCellData() {
        state.addLocatorHitbox(locatorHitbox);
        state.addCenterHitbox(centerHitbox);
        if (overlapHitbox != null) {
            state.addOverlapHitbox(overlapHitbox);
        }
        if (solidHitbox != null) {
            state.addAllSolidSurfaces(solidHitbox);
        }
    }
    
    void removeActions() {
        locatorHitbox.setGameState(null);
        state.removeLocatorHitbox(locatorHitbox);
        state.removeCenterHitbox(centerHitbox);
        if (overlapHitbox != null) {
            state.removeOverlapHitbox(overlapHitbox);
        }
        if (solidHitbox != null) {
            state.removeAllSolidSurfaces(solidHitbox);
        }
    }
    
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    public final double getEffectiveTimeFactor() {
        return (timeFactor < 0 ? (state == null ? 0 : state.getTimeFactor()) : timeFactor);
    }
    
    public final void setTimeFactor(double timeFactor) {
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
                    && locatorHitbox.getComponentOf() != this.locatorHitbox)) {
                if (this.locatorHitbox != null) {
                    removeNonLocatorHitboxes(this.locatorHitbox);
                    this.locatorHitbox.removeAsLocatorHitbox();
                }
                this.locatorHitbox = locatorHitbox;
                locatorHitbox.setObject(this);
                addNonLocatorHitboxes(locatorHitbox);
                locatorHitbox.addAsLocatorHitbox(drawPriority);
                return true;
            }
        }
        return false;
    }
    
    void removeNonLocatorHitboxes(Hitbox locatorHitbox) {
        locatorHitbox.removeChild(centerHitbox);
        locatorHitbox.removeChild(overlapHitbox);
        locatorHitbox.removeChild(solidHitbox);
    }
    
    void addNonLocatorHitboxes(Hitbox locatorHitbox) {
        locatorHitbox.addChild(centerHitbox);
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
    
    public final void flipX() {
        locatorHitbox.relFlipX();
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
    
    public final void flipY() {
        locatorHitbox.relFlipY();
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
    
    public final LevelVector getCenterOffset() {
        return centerHitbox.getRelPosition();
    }
    
    public final double getCenterOffsetX() {
        return centerHitbox.getRelX();
    }
    
    public final double getCenterOffsetY() {
        return centerHitbox.getRelY();
    }
    
    public final void setCenterOffset(LevelVector offset) {
        centerHitbox.setRelPosition(offset);
    }
    
    public final void setCenterOffset(double x, double y) {
        centerHitbox.setRelPosition(x, y);
    }
    
    public final void setCenterOffsetX(double x) {
        centerHitbox.setRelX(x);
    }
    
    public final void setCenterOffsetY(double y) {
        centerHitbox.setRelY(y);
    }
    
    public final LevelVector getCenter() {
        return centerHitbox.getAbsPosition();
    }
    
    public final double getCenterX() {
        return centerHitbox.getAbsX();
    }
    
    public final double getCenterY() {
        return centerHitbox.getAbsY();
    }
    
    public final double distanceTo(LevelObject object) {
        return centerHitbox.distanceTo(object.centerHitbox);
    }
    
    public final double angleTo(LevelObject object) {
        return centerHitbox.angleTo(object.centerHitbox);
    }
    
    public final <T extends LevelObject> T nearestObject(Class<T> cls) {
        return (state == null ? null : state.nearestObject(centerHitbox.getAbsX(), centerHitbox.getAbsY(), cls));
    }
    
    public final <T extends LevelObject> T nearestObjectWithinRectangle(double x1, double y1, double x2, double y2, Class<T> cls) {
        return (state == null ? null : state.nearestObjectWithinRectangle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), x1, y1, x2, y2, cls));
    }
    
    public final <T extends LevelObject> T nearestObjectWithinCircle(LevelVector center, double radius, Class<T> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), center.getX(), center.getY(), radius, cls));
    }
    
    public final <T extends LevelObject> T nearestObjectWithinCircle(double centerX, double centerY, double radius, Class<T> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), centerX, centerY, radius, cls));
    }
    
    public final <T extends LevelObject> boolean objectIsWithinRadius(double radius, Class<T> cls) {
        return (state == null ? false : state.objectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls) != null);
    }
    
    public final <T extends LevelObject> T objectWithinRadius(double radius, Class<T> cls) {
        return (state == null ? null : state.objectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
    }
    
    public final <T extends LevelObject> List<T> objectsWithinRadius(double radius, Class<T> cls) {
        return (state == null ? new ArrayList<>() : state.objectsWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
    }
    
    public final <T extends LevelObject> T nearestObjectWithinRadius(double radius, Class<T> cls) {
        return (state == null ? null : state.nearestObjectWithinCircle(centerHitbox.getAbsX(), centerHitbox.getAbsY(), centerHitbox.getAbsX(), centerHitbox.getAbsY(), radius, cls));
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
                        && overlapHitbox.getComponentOf() != locatorHitbox);
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
    
    public final boolean overlaps(LevelObject object) {
        return overlap(this, object);
    }
    
    public static final boolean overlap(LevelObject object1, LevelObject object2) {
        return object1.overlapHitbox != null && object2.overlapHitbox != null
                && Hitbox.overlap(object1.overlapHitbox, object2.overlapHitbox);
    }
    
    public final <T extends LevelObject> boolean isOverlappingObject(Class<T> cls) {
        return (state == null || overlapHitbox == null ? false : state.overlappingObject(overlapHitbox, cls) != null);
    }
    
    public final <T extends LevelObject> T overlappingObject(Class<T> cls) {
        return (state == null || overlapHitbox == null ? null : state.overlappingObject(overlapHitbox, cls));
    }
    
    public final <T extends LevelObject> List<T> overlappingObjects(Class<T> cls) {
        return (state == null || overlapHitbox == null ? new ArrayList<>() : state.overlappingObjects(overlapHitbox, cls));
    }
    
    public final <T extends LevelObject> T nearestOverlappingObject(Class<T> cls) {
        return (state == null || overlapHitbox == null ? null : state.nearestOverlappingObject(centerHitbox.getAbsX(), centerHitbox.getAbsY(), overlapHitbox, cls));
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
                        && solidHitbox.getComponentOf() != locatorHitbox);
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
    
    public final int getDrawPriority() {
        return drawPriority;
    }
    
    public final void setDrawPriority(int drawPriority) {
        this.drawPriority = drawPriority;
        locatorHitbox.changeDrawPriority(drawPriority);
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
