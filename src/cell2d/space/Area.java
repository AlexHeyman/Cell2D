package cell2d.space;

import cell2d.CellGame;
import java.util.Collection;

/**
 * <p>An Area is a pattern of SpaceObjects that can be generated and loaded by a
 * SpaceState on demand. Areas represent environments that can exist within a
 * SpaceState's space. Areas are loaded about a specified origin point, and the
 * positions of an Area's SpaceObjects are changed upon addition to their new
 * SpaceState to be relative to this origin.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses the SpaceStates that can load
 * this Area
 */
public abstract class Area<T extends CellGame> {
    
    /**
     * Actions for this Area to take in order for the specified SpaceState to
     * load it. All of the SpaceObjects in the Collection of SpaceObjects that
     * this method returns will be added to the SpaceState as part of this Area,
     * but only if they have not already been added to a SpaceState. The
     * positions of these SpaceObjects are relative to the origin point about
     * which this Area is being loaded.
     * @param game The CellGame of the SpaceState that is loading this Area
     * @param state The SpaceState that is loading this Area
     * @return All of the SpaceObjects to be added to the SpaceState as part of
     * this Area
     */
    public abstract Collection<SpaceObject<T>> load(T game, SpaceState<T> state);
    
}
