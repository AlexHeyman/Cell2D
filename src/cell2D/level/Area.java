package cell2D.level;

import cell2D.CellGame;
import java.util.List;

public abstract class Area {
    
    public abstract List<LevelObject> load(CellGame game, LevelState levelState);
    
}
