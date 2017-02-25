package cell2D.level;

import cell2D.CellGame;

public class Viewport extends LevelThinker {
    
    private LevelObject camera = null;
    private HUD hud;
    private double x1, y1, x2, y2;
    int roundX1, roundY1, roundX2, roundY2, left, right, top, bottom;
    
    public Viewport(HUD hud, double left, double right, double top, double bottom) {
        if (left > right) {
            throw new RuntimeException("Attempted to give a Viewport a negative width");
        }
        if (top > bottom) {
            throw new RuntimeException("Attempted to give a Viewport a negative height");
        }
        this.hud = (hud == null || hud.getNewGameState() == null ? hud : null);
        x1 = left;
        y1 = top;
        x2 = right;
        y2 = bottom;
        roundX1 = (int)Math.round(x1);
        roundY1 = (int)Math.round(y1);
        roundX2 = (int)Math.round(x2);
        roundY2 = (int)Math.round(y2);
        updateXData();
        updateYData();
    }
    
    private void updateXData() {
        left = -(int)Math.round((roundX1 + roundX2)/2.0);
        right = left + roundX2 - roundX1;
    }
    
    private void updateYData() {
        top = -(int)Math.round((roundY1 + roundY2)/2.0);
        bottom = top + roundY2 - roundY1;
    }
    
    @Override
    public final void addedActions(CellGame game, LevelState levelState) {
        if (hud != null) {
            levelState.addThinker(hud);
        }
    }
    
    @Override
    public final void removedActions(CellGame game, LevelState levelState) {
        if (hud != null) {
            levelState.removeThinker(hud);
        }
    }
    
    public final LevelObject getCamera() {
        return camera;
    }
    
    public final void setCamera(LevelObject camera) {
        this.camera = camera;
    }
    
    public final HUD getHUD() {
        return hud;
    }
    
    public final boolean setHUD(HUD hud) {
        if (getNewGameState() == null) {
            if (hud == null || hud.getNewGameState() == null) {
                this.hud = hud;
                return true;
            }
            return false;
        } else if (hud == null || getNewGameState().addThinker(hud)) {
            if (this.hud != null) {
                getNewGameState().removeThinker(this.hud);
            }
            this.hud = hud;
            return true;
        }
        return false;
    }
    
    public final double getScreenLeft() {
        return x1;
    }
    
    public final double getScreenRight() {
        return x2;
    }
    
    public final double getScreenTop() {
        return y1;
    }
    
    public final double getScreenBottom() {
        return y2;
    }
    
    public final void setScreenLeft(double left) {
        if (left > x2) {
            throw new RuntimeException("Attempted to give a Viewport a negative width");
        }
        x1 = left;
        roundX1 = (int)Math.round(x1);
        updateXData();
    }
    
    public final void setScreenRight(double right) {
        if (right < x1) {
            throw new RuntimeException("Attempted to give a Viewport a negative width");
        }
        x2 = right;
        roundX2 = (int)Math.round(x2);
        updateXData();
    }
    
    public final void setScreenTop(double top) {
        if (top > y2) {
            throw new RuntimeException("Attempted to give a Viewport a negative height");
        }
        y1 = top;
        roundY1 = (int)Math.round(y1);
        updateYData();
    }
    
    public final void setScreenBottom(double bottom) {
        if (bottom < y1) {
            throw new RuntimeException("Attempted to give a Viewport a negative height");
        }
        y2 = bottom;
        roundY2 = (int)Math.round(y2);
        updateYData();
    }
    
    public final int getLeftEdge() {
        return (int)Math.round(camera.getCenterX()) + left;
    }
    
    public final int getRightEdge() {
        return (int)Math.round(camera.getCenterX()) + right;
    }
    
    public final int getTopEdge() {
        return (int)Math.round(camera.getCenterY()) + top;
    }
    
    public final int getBottomEdge() {
        return (int)Math.round(camera.getCenterY()) + bottom;
    }
    
    public final boolean rectangleIsVisible(double x1, double y1, double x2, double y2) {
        if (camera != null && camera.newState == getNewGameState()) {
            double centerX = Math.round(camera.getCenterX());
            double centerY = Math.round(camera.getCenterY());
            return Math.round(x1) < centerX + right && Math.round(x2) > centerX + left
                    && Math.round(y1) < centerY + bottom && Math.round(y2) > centerY + top;
        }
        return false;
    }
    
}
