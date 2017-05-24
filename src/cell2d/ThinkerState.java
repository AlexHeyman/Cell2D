package cell2d;

/**
 * <p>A ThinkerState represents a state that a Thinker can occupy. A Thinker may
 * occupy at most one ThinkerState at a time. ThinkerStates take actions
 * alongside their Thinker's own, as well as when entered and left by a Thinker,
 * and can help a Thinker keep track of its position in a multi-frame procedure.
 * </p>
 * 
 * <p>The ThinkerState class is intended to be directly extended by classes W
 * that extend ThinkerState&lt;T,U,V,W&gt; and interact with CellGameStates of
 * class U and Thinkers of class V. BasicThinkerState is an example of such a
 * class. This allows a ThinkerState's Thinkers and their CellGameStates to
 * interact with it in ways unique to its subclass of ThinkerState.</p>
 * 
 * <p>It is useful to create an individual ThinkerState instance within the
 * class of the Thinker that uses it and override its methods when creating it,
 * allowing those methods to easily access the internal fields and methods of
 * the Thinker.</p>
 * 
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this ThinkerState's Thinker's
 * CellGameState is used by
 * @param <U> The subclass of CellGameState that this ThinkerState's Thinker is
 * used by
 * @param <V> The subclass of Thinker that this ThinkerState is used by
 * @param <W> The subclass of ThinkerState that this ThinkerState is
 */
public abstract class ThinkerState<T extends CellGame, U extends CellGameState<T,U,V,W>, V extends Thinker<T,U,V,W>, W extends ThinkerState<T,U,V,W>> {
    
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
    public abstract W getNextState();
    
    /**
     * Actions for this ThinkerState to take once every time unit, immediately
     * before its Thinker takes its own timeUnitActions().
     * @param game This ThinkerState's Thinker's CellGame
     * @param state This ThinkerState's Thinker's CellGameState
     */
    public void timeUnitActions(CellGame game, U state) {}
    
    /**
     * Actions for this ThinkerState to take once every frame, immediately
     * before its Thinker takes its own frameActions().
     * @param game This ThinkerState's Thinker's CellGame
     * @param state This ThinkerState's Thinker's CellGameState
     */
    public void frameActions(CellGame game, U state) {}
    
    /**
     * Actions for this ThinkerState to take immediately after being entered and
     * immediately before getDuration() is called.
     * @param game This ThinkerState's Thinker's CellGame
     * @param state This ThinkerState's Thinker's CellGameState
     */
    public void enteredActions(CellGame game, U state) {}
    
    /**
     * Actions for this ThinkerState to take before being left and immediately
     * before getNextState() is called.
     * @param game This ThinkerState's Thinker's CellGame
     * @param state This ThinkerState's Thinker's CellGameState
     */
    public void leftActions(CellGame game, U state) {}
    
}
