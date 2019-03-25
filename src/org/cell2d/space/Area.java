package org.cell2d.space;

import java.util.Collection;
import org.cell2d.CellGame;

/**
 * <p>An Area is a pattern of SpaceObjects that can be generated and loaded by a
 * SpaceState on demand. Areas represent environments that can exist within a
 * SpaceState's space. Areas are loaded about a specified origin point, and the
 * positions of an Area's SpaceObjects are changed upon addition to their new
 * SpaceState to be relative to this origin.</p>
 * @see SpaceObject
 * @see SpaceState
 * @param <T> The type of CellGame that uses the SpaceStates that can load this
 * Area
 * @param <U> The type of SpaceState that can load this Area
 * @author Andrew Heyman
 */
public interface Area<T extends CellGame, U extends SpaceState<T,U,?>> {
    
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
    Collection<SpaceObject> load(T game, U state);
    
}
