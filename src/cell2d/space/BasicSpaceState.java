package cell2d.space;

import cell2d.CellGame;

/**
 * <p>A BasicSpaceState is a type of SpaceState that uses BasicSpaceThinkers,
 * which have no special capabilities. It is designed to be easily extended by
 * types of SpaceStates that do not require custom fields or methods to be
 * automatically shared between themselves and their SpaceThinkers.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this BasicSpaceState
 */
public class BasicSpaceState<T extends CellGame>
        extends SpaceState<T,BasicSpaceState<T>,BasicSpaceThinker<T>> {
    
    /**
     * Creates a new BasicSpaceState of the specified CellGame with the
     * specified ID.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this BasicSpaceState
     * @param game The CellGame to which this BasicSpaceState belongs
     * @param id This BasicSpaceState's ID
     * @param cellWidth The width of each of this BasicSpaceState's cells
     * @param cellHeight The height of each of this BasicSpaceState's cells
     * @param drawMode This BasicSpaceState's DrawMode
     */
    public BasicSpaceState(Class<? extends CellGame> gameClass,
            T game, int id, long cellWidth, long cellHeight, DrawMode drawMode) {
        super(gameClass, BasicSpaceState.class, BasicSpaceThinker.class,
                game, id, cellWidth, cellHeight, drawMode);
    }
    
}
