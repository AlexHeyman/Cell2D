package cell2d;

/**
 * <p>An Event represents a set of actions that can be taken as part of the
 * mechanics of a CellGameState. It is useful to create an Event as a lambda
 * expression to simplify code.</p>
 * @see Thinker#setTimerValue(cell2d.Event, int)
 * @param <T> The type of CellGame that uses the CellGameStates that can involve
 * this Event
 * @param <U> The type of CellGameState that can involve this Event
 * @author Andrew Heyman
 */
public interface Event<T extends CellGame, U extends CellGameState<T,U,?>> {
    
    /**
     * Actions that this Event consists in taking.
     * @param game The CellGame of the CellGameState that this Event is involved
     * in
     * @param state The CellGameState that this Event is involved in
     */
    void actions(T game, U state);
    
}
