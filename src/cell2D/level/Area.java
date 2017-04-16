package cell2D.level;

import cell2D.CellGame;
import java.util.Collection;

/**
 * <p>An Area is a pattern of LevelObjects that can be generated and loaded by a
 * LevelState on demand. Areas represent environments that can exist within a
 * LevelState's space. Areas are loaded about a specified origin point, and the
 * positions of an Area's LevelObjects are changed upon addition to their new
 * LevelState to be relative to this origin.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the LevelStates that can load
 * this Area
 */
public abstract class Area<T extends CellGame> {
    
    /**
     * Actions for this Area to take in order for the specified LevelState to
     * load it. All of the LevelObjects in the Collection of LevelObjects that
     * this method returns will be added to the LevelState as part of this Area,
     * but only if they have not already been added to a LevelState. The
     * positions of these LevelObjects are relative to the origin point about
     * which this Area is being loaded.
     * @param game The CellGame of the LevelState that is loading this Area
     * @param levelState The LevelState that is loading this Area
     * @return All of the LevelObjects to be added to the LevelState as part of
     * this Area
     */
    public abstract Collection<LevelObject<T>> load(T game, LevelState<T> levelState);
    
}
