package cell2d.space;

import cell2d.CellGame;
import cell2d.Event;
import cell2d.EventGroup;
import cell2d.SubThinker;

/**
 * <p>A SpaceThinker is the type of SubThinker that is used by SpaceStates. A
 * SpaceThinker has beforeMovementActions() that it takes once each frame, after
 * all Thinkers have experienced all of their time units for that frame, but
 * before its SpaceState moves its assigned MobileObjects. Like a SpaceState, it
 * also has an EventGroup of <i>before-movement Events</i> that it performs once
 * each frame. A SpaceThinker performs these events immediately after it takes
 * its beforeMovementActions().</p>
 * 
 * <p>Similarly to its frameActions(), the process in which a SpaceThinker takes
 * its beforeMovementActions() and then performs its before-movement Events is
 * itself an Event, and the SpaceThinker automatically ensures that this Event
 * is in the before-movement Events of its current super-Thinker. The
 * SpaceThinker's <i>before-movement priority</i> is the Event's priority in its
 * super-Thinker's before-movement Events. This means that the SpaceThinkers
 * assigned to a given Thinker will take their beforeMovementActions() in order
 * from highest to lowest before-movement priority, and that a SpaceThinker will
 * take its beforeMovementActions() after its super-Thinker, but, if its
 * super-Thinker is itself a SpaceThinker, before the next Thinker assigned to
 * its super-Thinker's super-Thinker.</p>
 * @see SpaceState
 * @see ThinkerObject
 * @param <T> The type of CellGame that uses this SpaceThinker's SpaceStates
 * @param <U> The type of SpaceState that uses this SpaceThinker
 * @param <V> The type of SpaceThinker that this SpaceThinker is for SpaceState
 * interaction purposes
 * @author Andrew Heyman
 */
public abstract class SpaceThinker<T extends CellGame,
        U extends SpaceState<T,U,V>, V extends SpaceThinker<T,U,V>> extends SubThinker<T,U,V> {
    
    EventGroup<T,U> superBeforeMovementEvents = null;
    private final EventGroup<T,U> beforeMovementEvents = new EventGroup<>();
    private int beforeMovementPriority = 0;
    
    final Event<T,U> beforeMovement = (game, state) -> {
        beforeMovementActions(game, state);
        if (beforeMovementEvents.size() > 0) {
            beforeMovementEvents.perform(state);
        }
    };
    
    /**
     * Creates a new SpaceThinker.
     * @param gameClass The Class object representing the type of CellGame that
     * uses this SpaceThinker's SpaceStates
     * @param stateClass The Class object representing the type of SpaceState
     * that uses this SpaceThinker
     * @param subThinkerClass The Class object representing the type of
     * SpaceThinker that this SpaceThinker is for SpaceState interaction
     * purposes
     */
    public SpaceThinker(Class<T> gameClass, Class<U> stateClass, Class<V> subThinkerClass) {
        super(gameClass, stateClass, subThinkerClass);
    }
    
    /**
     * Actions for this SpaceThinker to take once each frame, after all Thinkers
     * have experienced all of their time units for that frame, but before its
     * SpaceState moves its assigned MobileObjects.
     * @param game This SpaceThinker's SpaceState's CellGame
     * @param state This SpaceThinker's SpaceState
     */
    public void beforeMovementActions(T game, U state) {}
    
    @Override
    public void addSubThinkerActions(T game, U state, V subThinker) {
        beforeMovementEvents.add(subThinker.beforeMovement, subThinker.getBeforeMovementPriority());
        subThinker.superBeforeMovementEvents = beforeMovementEvents;
    }
    
    @Override
    public void removeSubThinkerActions(T game, U state, V subThinker) {
        beforeMovementEvents.remove(subThinker.beforeMovement, subThinker.getBeforeMovementPriority());
        subThinker.superBeforeMovementEvents = null;
    }
    
    /**
     * Returns the EventGroup of this SpaceThinker's before-movement Events.
     * @return The EventGroup of this SpaceThinker's before-movement Events
     */
    public final EventGroup<T,U> getBeforeMovementEvents() {
        return beforeMovementEvents;
    }
    
    /**
     * Returns this SpaceThinker's before-movement priority.
     * @return This SpaceThinker's before-movement priority
     */
    public final int getBeforeMovementPriority() {
        return beforeMovementPriority;
    }
    
    /**
     * Sets this SpaceThinker's before-movement priority to the specified value.
     * @param beforeMovementPriority This SpaceThinker's new before-movement
     * priority
     */
    public final void setBeforeMovementPriority(int beforeMovementPriority) {
        this.beforeMovementPriority = beforeMovementPriority;
        if (superBeforeMovementEvents != null) {
            superBeforeMovementEvents.remove(beforeMovement, this.beforeMovementPriority);
            superBeforeMovementEvents.add(beforeMovement, beforeMovementPriority);
        }
    }
    
}
