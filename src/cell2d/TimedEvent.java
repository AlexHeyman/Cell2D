package cell2d;

/**
 * <p>A TimedEvent represents a set of actions that can be taken after a delay
 * managed by a Thinker. It is useful to create an individual TimedEvent
 * instance within the class of the Thinker that uses it and override its
 * eventActions() method when creating it, allowing that method to easily access
 * the internal fields and methods of the Thinker.</p>
 * @author Andrew Heyman
 */
public abstract class TimedEvent {
    
    /**
     * Actions for this TimedEvent to take when activated.
     */
    public abstract void eventActions();
    
}
