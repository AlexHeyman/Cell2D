package cell2d.space;

import cell2d.CellGame;

/**
 * <p>A BasicSpaceState is a type of SpaceState that uses BasicSpaceThinkers,
 * which have no special capabilities, and treats its CellGame as a basic
 * CellGame. It is designed to be easily extended by types of SpaceStates that
 * do not require custom fields or methods to be automatically shared between
 * themselves and their SpaceThinkers or CellGames.</p>
 * @author Andrew Heyman
 */
public class BasicSpaceState extends SpaceState<CellGame,BasicSpaceState,BasicSpaceThinker> {
    
    /**
     * Creates a new BasicSpaceState of the specified CellGame with the
     * specified ID.
     * @param game The CellGame to which this BasicSpaceState belongs
     * @param id This BasicSpaceState's ID
     * @param cellWidth The width of each of this BasicSpaceState's cells
     * @param cellHeight The height of each of this BasicSpaceState's cells
     * @param drawMode This BasicSpaceState's DrawMode
     */
    public BasicSpaceState(CellGame game, int id, long cellWidth, long cellHeight, DrawMode drawMode) {
        super(CellGame.class, BasicSpaceState.class, BasicSpaceThinker.class,
                game, id, cellWidth, cellHeight, drawMode);
    }
    
}
