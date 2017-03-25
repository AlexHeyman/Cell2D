package cell2D;

/**
 * A ThinkerState represents a state that a Thinker can occupy. A Thinker may
 * occupy at most one ThinkerState at a time. ThinkerStates take actions
 * alongside their Thinker's own, as well as when entered and left by a Thinker,
 * and can help a Thinker keep track of its position in a multi-frame procedure.
 * 
 * The ThinkerState class is intended to be directly extended by classes V that
 * extend ThinkerState<T,U,V> and interact with CellGameStates of class T and
 * Thinkers of class U. BasicThinkerState is an example of such a class. This
 * allows a ThinkerState's Thinkers and their CellGameStates to interact with it
 * in ways unique to its subclass of ThinkerState.
 * 
 * It is useful to create an individual ThinkerState instance within the class
 * of the Thinker that uses it and override its methods when creating it,
 * allowing those methods to easily access the internal fields and methods of
 * the Thinker.
 * 
 * @author Andrew Heyman
 * @param <T> The subclass of CellGameState that this ThinkerState's Thinker is
 * used by
 * @param <U> The subclass of Thinker that this ThinkerState is used by
 * @param <V> The subclass of ThinkerState that this ThinkerState is
 */
public abstract class ThinkerState<T extends CellGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
    
    /**
     * Returns how long in time units this ThinkerState should last. This method
     * is called and used to determine this ThinkerState's duration immediately
     * after its enteredActions() are taken. A negative return value indicates
     * an infinite duration.
     * @return How long in time units this ThinkerState should last
     */
    public abstract int getDuration();
    
    /**
     * Returns the ThinkerState that this ThinkerState's Thinker should enter
     * after it leaves this one. This method is called and used to determine
     * the Thinker's next state immediately after this ThinkerState's
     * leftActions() are taken. A null return value indicates that the Thinker
     * should not enter any state.
     * @return The ThinkerState that this ThinkerState's Thinker should enter
     * after it leaves this one
     */
    public abstract V getNextState();
    
    /**
     * Actions for this ThinkerState to take once every time unit, immediately
     * before its Thinker takes its own timeUnitActions().
     * @param game This ThinkerState's Thinker's CellGame
     * @param state This ThinkerState's Thinker's CellGameState
     */
    public void timeUnitActions(CellGame game, T state) {}
    
    /**
     * Actions for this ThinkerState to take once every frame, immediately
     * before its Thinker takes its own frameActions().
     * @param game This ThinkerState's Thinker's CellGame
     * @param state This ThinkerState's Thinker's CellGameState
     */
    public void frameActions(CellGame game, T state) {}
    
    /**
     * Actions for this ThinkerState to take immediately after being entered and
     * immediately before getDuration() is called.
     * @param game This ThinkerState's Thinker's CellGame
     * @param state This ThinkerState's Thinker's CellGameState
     */
    public void enteredActions(CellGame game, T state) {}
    
    /**
     * Actions for this ThinkerState to take before being left, and immediately
     * before getNextState() is called.
     * @param game This ThinkerState's Thinker's CellGame
     * @param state This ThinkerState's Thinker's CellGameState
     */
    public void leftActions(CellGame game, T state) {}
    
}
