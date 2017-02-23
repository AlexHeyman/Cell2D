package cell2D;

import org.newdawn.slick.Graphics;

public interface Drawable {
    
    public abstract void draw(Graphics g, int x, int y);
    
    public abstract void draw(Graphics g, int x, int y, boolean xFlip, boolean yFlip, double angle, double alpha, String filter);
    
    public abstract void draw(Graphics g, int x, int y, double scale, boolean xFlip, boolean yFlip, double alpha, String filter);
    
    public abstract void draw(Graphics g, int x, int y, int left, int right, int top, int bottom);
    
    public abstract void draw(Graphics g, int x, int y, int left, int right, int top, int bottom, boolean xFlip, boolean yFlip, double angle, double alpha, String filter);
    
    public abstract void draw(Graphics g, int x, int y, int left, int right, int top, int bottom, double scale, boolean xFlip, boolean yFlip, double alpha, String filter);
    
}
