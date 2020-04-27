package org.cell2d.space;

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
 * @author Alex Heyman
 */
public interface Area<T extends CellGame, U extends SpaceState<T,U,?>> {
    
    /**
     * Actions for this Area to take to in order for the specified SpaceState to
     * load it. This method should involve constructing a set of SpaceObjects
     * for the SpaceState to add to itself. These SpaceObjects should not be
     * added to a SpaceState in the course of this method. The positions of the
     * SpaceObjects will be treated as relative to the origin point about which
     * this Area is being loaded. The SpaceObjects must be returned as an
     * Iterable, such as a List or Set, that contains each SpaceObject exactly
     * once.
     * @param game The CellGame of the SpaceState that is loading this Area
     * @param state The SpaceState that is loading this Area
     * @return The SpaceObjects for the SpaceState to add to itself
     */
    Iterable<SpaceObject> load(T game, U state);
    
}
