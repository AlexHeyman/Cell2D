package cell2d.space;

import cell2d.CellGame;
import cell2d.CellGameState;
import cell2d.SafeIterator;
import cell2d.Thinker;
import cell2d.TimedEvent;

/**
 * <p>A ThinkerObject is a MobileObject that mimics a type of SpaceThinker by
 * permanently assigning to itself a SpaceThinker of that type. A ThinkerObject
 * has methods that access and manipulate properties of its assigned
 * SpaceThinker, such as its timers and list of its own SpaceThinkers, allowing
 * the ThinkerObject to effectively inherit those properties. A ThinkerObject
 * will automatically set its assigned SpaceThinker's time factor and add and
 * remove it from SpaceStates that can use it as appropriate to match the
 * ThinkerObject's own time factor and assigned SpaceState.</p>
 * @see SpaceThinker
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this ThinkerObject's SpaceThinker's
 * SpaceStates
 * @param <U> The type of SpaceState that uses this ThinkerObject's SpaceThinker
 * @param <V> The type of SpaceThinker that this ThinkerObject mimics
 */
public abstract class ThinkerObject<T extends CellGame, U extends SpaceState<T,U,V>,
        V extends SpaceThinker<T,U,V>> extends MobileObject {
    
    private U compatibleState = null;
    private V thinker = null;
    
    /**
     * Creates a new ThinkerObject with no locator Hitbox or SpaceThinker. This
     * ThinkerObject must be assigned a locator Hitbox with its
     * setLocatorHitbox() method and a SpaceThinker with its setThinker() method
     * before any of its other methods are called.
     * @see #setLocatorHitbox(cell2d.space.Hitbox)
     * @see #setThinker(cell2d.space.SpaceThinker)
     */
    public ThinkerObject() {}
    
    @Override
    void addNonCellData() {
        super.addNonCellData();
        if (state.getGameClass() == getGameClass()
                && state.getStateClass() == getStateClass()
                && state.getThinkerClass() == getThinkerClass()) {
            compatibleState = (U)state;
            compatibleState.addThinker(thinker);
        }
    }
    
    @Override
    void removeData() {
        super.removeData();
        if (compatibleState != null) {
            compatibleState.removeThinker(thinker);
            compatibleState = null;
        }
    }
    
    @Override
    void setTimeFactorActions(long timeFactor) {
        super.setTimeFactorActions(timeFactor);
        thinker.setTimeFactor(timeFactor);
    }
    
    /**
     * Returns the Class object representing the subclass of CellGame that uses
     * this ThinkerObject's SpaceThinker's SpaceStates.
     * @return The subclass of CellGame that uses this ThinkerObject's
     * SpaceThinker's SpaceStates
     */
    public final Class<? extends CellGame> getGameClass() {
        return thinker.getGameClass();
    }
    
    /**
     * Returns the Class object representing the subclass of SpaceState that
     * uses this ThinkerObject's SpaceThinker.
     * @return The subclass of SpaceState that uses this ThinkerObject's
     * SpaceThinker
     */
    public final Class<? extends CellGameState> getStateClass() {
        return thinker.getStateClass();
    }
    
    /**
     * Returns the Class object representing the subclass of SpaceThinker that
     * this ThinkerObject mimics.
     * @return The subclass of SpaceThinker that this ThinkerObject mimics
     */
    public final Class<? extends Thinker> getThinkerClass() {
        return thinker.getThinkerClass();
    }
    
    /**
     * Returns this ThinkerObject's SpaceThinker, or null if it has none.
     * @return This ThinkerObject's SpaceThinker
     */
    public final V getThinker() {
        return thinker;
    }
    
    /**
     * Sets this ThinkerObject's assigned SpaceThinker to the specified one, if
     * this ThinkerObject does not already have a SpaceThinker and the
     * specified SpaceThinker is not already assigned to a ThinkerGroup.
     * @param thinker The SpaceThinker to be assigned
     * @return Whether the assignment occurred
     */
    public final boolean setThinker(V thinker) {
        if (this.thinker == null && thinker.getNewThinkerGroup() == null) {
            this.thinker = thinker;
            return true;
        }
        return false;
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
    public final SafeIterator<V> thinkerIterator() {
        return thinker.thinkerIterator();
    }
    
    /**
     * Adds the specified SpaceThinker to this ThinkerObject if it is not
     * already assigned to a ThinkerGroup.
     * @param thinker The SpaceThinker to be added
     * @return Whether the addition occurred
     */
    public final boolean addThinker(V thinker) {
        return this.thinker.addThinker(thinker);
    }
    
    /**
     * Removes the specified SpaceThinker from this ThinkerObject if it is
     * currently assigned to it.
     * @param thinker The SpaceThinker to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeThinker(V thinker) {
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
    public final boolean removeLineage(V thinker) {
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
    
}
