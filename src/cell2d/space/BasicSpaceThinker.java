package cell2d.space;

import cell2d.CellGame;

/**
 * <p>A BasicSpaceThinker is a type of SpaceThinker that is used by
 * BasicSpaceStates, which have no special capabilities. It does not
 * automatically share any custom fields or methods between itself and its
 * SpaceStates.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this BasicSpaceThinker's
 * BasicSpaceStates
 */
public abstract class BasicSpaceThinker<T extends CellGame>
        extends SpaceThinker<T,BasicSpaceState<T>,BasicSpaceThinker<T>> {
    
    /**
     * Creates a new BasicSpaceThinker.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this BasicSpaceThinker's BasicSpaceStates
     */
    public BasicSpaceThinker(Class<? extends CellGame> gameClass) {
        super(gameClass, BasicSpaceState.class, BasicSpaceThinker.class);
    }
    
}
