package ironclad2D.level;

import ironclad2D.IroncladGame;

public class Viewport extends LevelThinker {
    
    LevelObject camera = null;
    HUD hud;
    private double x1, y1, x2, y2;
    int roundX1, roundY1, roundX2, roundY2, left, right, top, bottom;
    
    public Viewport(HUD hud, double x1, double y1, double x2, double y2) {
        this.hud = hud;
        if (x1 > x2) {
            this.x1 = x2;
            this.x2 = x1;
        } else {
            this.x1 = x1;
            this.x2 = x2;
        }
        if (y1 > y2) {
            this.y1 = y2;
            this.y2 = y1;
        } else {
            this.y1 = y1;
            this.y2 = y2;
        }
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
    public final void addedActions(IroncladGame game, LevelState levelState) {
        if (hud != null) {
            levelState.addThinker(hud);
        }
    }
    
    @Override
    public final void removedActions(IroncladGame game, LevelState levelState) {
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
        LevelState state = getGameState();
        if (state == null) {
            if (hud == null || hud.getGameState() == null) {
                this.hud = hud;
                return true;
            }
            return false;
        } else if (hud == null || state.addThinker(hud)) {
            if (this.hud != null) {
                state.removeThinker(this.hud);
            }
            this.hud = hud;
            return true;
        }
        return false;
    }
    
    public final double getLeftEdge() {
        return x1;
    }
    
    public final double getRightEdge() {
        return x2;
    }
    
    public final double getTopEdge() {
        return y1;
    }
    
    public final double getBottomEdge() {
        return y2;
    }
    
    public final void setLeftEdge(double x) {
        if (x > x2) {
            throw new RuntimeException("Attempted to give a viewport a negative width");
        }
        x1 = x;
        roundX1 = (int)Math.round(x1);
        updateXData();
    }
    
    public final void setRightEdge(double x) {
        if (x < x1) {
            throw new RuntimeException("Attempted to give a viewport a negative width");
        }
        x2 = x;
        roundX2 = (int)Math.round(x2);
        updateXData();
    }
    
    public final void setTopEdge(double y) {
        if (y > y2) {
            throw new RuntimeException("Attempted to give a viewport a negative height");
        }
        y1 = y;
        roundY1 = (int)Math.round(y1);
        updateYData();
    }
    
    public final void setBottomEdge(double y) {
        if (y < y1) {
            throw new RuntimeException("Attempted to give a viewport a negative height");
        }
        y2 = y;
        roundY2 = (int)Math.round(y2);
        updateYData();
    }
    
}
