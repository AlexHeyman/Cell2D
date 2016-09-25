package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.Thinker;

public class LevelThinker extends Thinker<LevelState> {
    
    LevelState levelState = null;
    LevelState newLevelState = null;
    
    public LevelThinker() {}
    
    public final LevelState getLevelState() {
        return levelState;
    }
    
    public final boolean addToLevelState(LevelState levelState) {
        return levelState.addThinker(this);
    }
    
    public final boolean removeFromLevelState() {
        if (levelState != null) {
            return levelState.removeThinker(this);
        }
        return false;
    }
    
    public void reactToLevel(IroncladGame game, LevelState levelState) {}
    
    public void reactToInput(IroncladGame game, LevelState levelState) {}
    
}
