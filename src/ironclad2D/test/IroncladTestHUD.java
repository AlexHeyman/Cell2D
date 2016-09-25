package ironclad2D.test;

import ironclad2D.IroncladGame;
import ironclad2D.level.HUD;
import ironclad2D.level.LevelState;
import org.newdawn.slick.Graphics;

public class IroncladTestHUD extends HUD {
    
    public IroncladTestHUD() {}
    
    @Override
    public void renderActions(IroncladGame game, LevelState levelState,
            Graphics g, int x1, int y1, int x2, int y2) {
        g.drawString("Howdy!", x1 + 16, y1 + 16);
    }
    
}
