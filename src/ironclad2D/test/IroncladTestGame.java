package ironclad2D.test;

import ironclad2D.IroncladGame;
import ironclad2D.level.Viewport;
import ironclad2D.level.LevelState;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.command.KeyControl;

public class IroncladTestGame extends IroncladGame {
    
    public IroncladTestGame() throws SlickException {
        super("Ironclad2D Test Game", 6, 60, 640, 512, 1, false, "assets", true, null);
    }
    
    @Override
    public void initStates() throws SlickException {
        LevelState levelState = new LevelState(this, 0);
        levelState.setViewport(0, new Viewport(new IroncladTestHUD(), 0, 0, getScreenWidth(), getScreenHeight()));
    }
    
    @Override
    public void initAssets() throws SlickException {
        add("megaman", createSprite("megaman.png", 12, 24, 0, 255, 255, null));
        add("wilystage", createSpriteSheet("wilytiles.png", 6, 5, 16, 16, 1, 0, 0, null));
    }
    
    @Override
    public void initActions() throws SlickException {
        bindControl(0, new KeyControl(Input.KEY_UP));
        bindControl(1, new KeyControl(Input.KEY_DOWN));
        bindControl(2, new KeyControl(Input.KEY_LEFT));
        bindControl(3, new KeyControl(Input.KEY_RIGHT));
        bindControl(4, new KeyControl(Input.KEY_Z));
        bindControl(5, new KeyControl(Input.KEY_X));
    }
    
}
