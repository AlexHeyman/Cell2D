package ironclad2D.level;

import ironclad2D.IroncladGame;
import ironclad2D.Thinker;

public abstract class LevelThinker extends Thinker<LevelState> {
    
    LevelState levelState = null;
    LevelState newLevelState = null;
    
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
    
    public void beforeMovementActions(IroncladGame game, LevelState levelState) {}
    
    public void afterMovementActions(IroncladGame game, LevelState levelState) {}
    
}
