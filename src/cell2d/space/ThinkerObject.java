package cell2d.space;

import cell2d.CellGame;
import cell2d.CellVector;
import cell2d.SafeIterator;
import cell2d.TimedEvent;

/**
 * <p>A ThinkerObject is a MobileObject that acts like a SpaceThinker,
 * possessing timers, various actions in response to events, and the capacity to
 * act like a ThinkerGroup, even though it is not technically one.</p>
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses the SpaceStates that this
 * ThinkerObject can be assigned to
 */
public abstract class ThinkerObject<T extends CellGame> extends MobileObject<T> {
    
    private final SpaceThinker<T> thinker = new SpaceThinker<T>() {
        
        @Override
        public final void addedActions(T game, SpaceState<T> state) {
            ThinkerObject.this.addedActions(game, state);
        }
        
        @Override
        public final void removedActions(T game, SpaceState<T> state) {
            ThinkerObject.this.removedActions(game, state);
        }
        
        @Override
        public final void timeUnitActions(T game, SpaceState<T> state) {
            ThinkerObject.this.timeUnitActions(game, state);
        }
        
        @Override
        public final void beforeMovementActions(T game, SpaceState<T> state) {
            ThinkerObject.this.beforeMovementActions(game, state);
        }
        
        @Override
        public final void frameActions(T game, SpaceState<T> state) {
            ThinkerObject.this.frameActions(game, state);
        }
        
    };
    
    /**
     * Creates a new ThinkerObject with the specified locator Hitbox.
     * @param locatorHitbox This ThinkerObject's locator Hitbox
     */
    public ThinkerObject(Hitbox<T> locatorHitbox) {
        super(locatorHitbox);
    }
    
    /**
     * Creates a new ThinkerObject with a new PointHitbox at the specified
     * position as its locator Hitbox.
     * @param position This ThinkerObject's position
     */
    public ThinkerObject(CellVector position) {
        super(position);
    }
    
    /**
     * Creates a new ThinkerObject with a new PointHitbox at the specified
     * position as its locator Hitbox.
     * @param x The x-coordinate of this ThinkerObject's position
     * @param y The y-coordinate of this ThinkerObject's position
     */
    public ThinkerObject(long x, long y) {
        super(x, y);
    }
    
    @Override
    void addNonCellData() {
        super.addNonCellData();
        state.addThinker(thinker);
    }
    
    @Override
    void removeData() {
        super.removeData();
        state.removeThinker(thinker);
    }
    
    @Override
    void setTimeFactorActions(long timeFactor) {
        super.setTimeFactorActions(timeFactor);
        thinker.setTimeFactor(timeFactor);
    }
    
    /**
     * Returns the number of SpaceThinkers that are assigned to this
     * ThinkerObject.
     * @return The number of SpaceThinkers that are assigned to this
     * ThinkerObject
     */
    public final int getNumThinkers() {
        return thinker.getNumThinkers();
    }
    
    /**
     * Returns whether any Iterators over this ThinkerObject's list of
     * SpaceThinkers are in progress.
     * @return Whether any Iterators over this ThinkerObject's list of
     * SpaceThinkers are in progress
     */
    public final boolean iteratingThroughThinkers() {
        return thinker.iteratingThroughThinkers();
    }
    
    /**
     * Returns a new Iterator over this ThinkerObject's list of SpaceThinkers.
     * @return A new Iterator over this ThinkerObject's list of SpaceThinkers
     */
    public final SafeIterator<SpaceThinker<T>> thinkerIterator() {
        return thinker.thinkerIterator();
    }
    
    /**
     * Adds the specified SpaceThinker to this ThinkerObject if it is not
     * already assigned to a ThinkerGroup.
     * @param thinker The SpaceThinker to be added
     * @return Whether the addition occurred
     */
    public final boolean addThinker(SpaceThinker<T> thinker) {
        return this.thinker.addThinker(thinker);
    }
    
    /**
     * Removes the specified SpaceThinker from this ThinkerObject if it is
     * currently assigned to it.
     * @param thinker The SpaceThinker to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeThinker(SpaceThinker<T> thinker) {
        return this.thinker.removeThinker(thinker);
    }
    
    /**
     * Removes from this ThinkerObject all of the SpaceThinkers that are
     * currently assigned to it.
     */
    public final void removeAllThinkers() {
        thinker.removeAllThinkers();
    }
    
    /**
     * Removes from their ThinkerGroups all of the SpaceThinkers that are
     * directly or indirectly assigned to this ThinkerObject. For instance, if a
     * SpaceThinker is assigned to a SpaceThinker that is assigned to this
     * ThinkerObject, the first SpaceThinker will be removed from the second,
     * and the second will be removed from this ThinkerObject.
     */
    public final void removeAllSubThinkers() {
        thinker.removeAllSubThinkers();
    }
    
    /**
     * Removes from their ThinkerGroups all of the SpaceThinkers that are
     * directly or indirectly assigned to this ThinkerObject, and are either
     * assigned to or assignees of the specified SpaceThinker. For instance, if
     * a SpaceThinker is assigned to a SpaceThinker that is assigned to a
     * SpaceThinker that is assigned to this ThinkerObject, and the second
     * SpaceThinker is the specified SpaceThinker, the first SpaceThinker will
     * be removed from the second, the second from the third, and the third from
     * this ThinkerObject. This method is useful for ThinkerObjects that use
     * SpaceThinker to model a hierarchy of states in which they can exist.
     * @param thinker The SpaceThinker with which the removed SpaceThinkers must
     * share a lineage of assignments
     * @return Whether any removals occurred
     */
    public final boolean removeLineage(SpaceThinker<T> thinker) {
        return this.thinker.removeLineage(thinker);
    }
    
    /**
     * Returns this ThinkerObject's action priority.
     * @return This ThinkerObject's action priority
     */
    public final int getActionPriority() {
        return thinker.getActionPriority();
    }
    
    /**
     * Returns the action priority that this ThinkerObject is about to have, but
     * does not yet have due to its SpaceState's SpaceThinker list being
     * iterated over. If this ThinkerObject is not about to change its action
     * priority, this method will simply return its current action priority.
     * @return The action priority that this ThinkerObject is about to have
     */
    public final int getNewActionPriority() {
        return thinker.getNewActionPriority();
    }
    
    /**
     * Sets this ThinkerObject's action priority to the specified value.
     * @param actionPriority The new action priority
     */
    public final void setActionPriority(int actionPriority) {
        thinker.setActionPriority(actionPriority);
    }
    
    /**
     * Returns the current value of this ThinkerObject's timer for the specified
     * TimedEvent.
     * @param timedEvent The TimedEvent whose timer value should be returned
     * @return The current value of the timer for the specified TimedEvent
     */
    public final int getTimerValue(TimedEvent timedEvent) {
        return thinker.getTimerValue(timedEvent);
    }
    
    /**
     * Sets the value of this ThinkerObject's timer for the specified TimedEvent
     * to the specified value.
     * @param timedEvent The TimedEvent whose timer value should be set
     * @param value The new value of the specified TimedEvent's timer
     */
    public final void setTimerValue(TimedEvent timedEvent, int value) {
        thinker.setTimerValue(timedEvent, value);
    }
    
    /**
     * Actions for this ThinkerObject to take immediately after being added to a
     * new SpaceState.
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void addedActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take immediately before being removed
     * from its current SpaceState.
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void removedActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every time unit, after
     * AnimationInstances update their indices but before SpaceThinkers take
     * their beforeMovementActions().
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void timeUnitActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every frame, after
     * SpaceThinkers take their timeUnitActions() but before its SpaceState
     * moves its assigned ThinkerObjects.
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void beforeMovementActions(T game, SpaceState<T> state) {}
    
    /**
     * Actions for this ThinkerObject to take once every frame after its
     * SpaceState moves its assigned ThinkerObjects.
     * @param game This ThinkerObject's SpaceState's CellGame
     * @param state This ThinkerObject's SpaceState
     */
    public void frameActions(T game, SpaceState<T> state) {}
    
}
