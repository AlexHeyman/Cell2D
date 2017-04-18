package cell2d;

/**
 * <p>A TimedEvent represents a set of actions that can be taken after a delay
 * managed by a Thinker. It is useful to create an individual TimedEvent
 * instance within the class of the Thinker that uses it and override its
 * eventActions() method when creating it, allowing that method to easily access
 * the internal fields and methods of the Thinker.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGameState that this TimedEvent's Thinker is
 * used by
 */
public abstract class TimedEvent<T extends CellGameState> {
    
    /**
     * Actions for this TimedEvent to take when activated.
     * @param game This TimedEvent's Thinker's CellGame
     * @param state This TimedEvent's Thinker's CellGameState
     */
    public abstract void eventActions(CellGame game, T state);
    
}
