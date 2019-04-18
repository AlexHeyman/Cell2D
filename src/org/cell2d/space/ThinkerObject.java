package org.cell2d.space;

import org.cell2d.CellGame;
import org.cell2d.Event;
import org.cell2d.EventGroup;
import org.cell2d.SafeIterator;

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
 * @param <T> The type of CellGame that uses this ThinkerObject's SpaceThinker's
 * SpaceStates
 * @param <U> The type of SpaceState that uses this ThinkerObject's SpaceThinker
 * @param <V> The type of SpaceThinker that this ThinkerObject mimics
 * @author Andrew Heyman
 */
public abstract class ThinkerObject<T extends CellGame,
        U extends SpaceState<T,U,V>, V extends SpaceThinker<T,U,V>> extends MobileObject {
    
    private U compatibleState = null;
    private V thinker = null;
    
    /**
     * Constructs a ThinkerObject with no locator Hitbox or assigned
     * SpaceThinker. This ThinkerObject must be assigned a locator Hitbox with
     * its setLocatorHitbox() method and a SpaceThinker with its setThinker()
     * method before any of its other methods are called.
     * @see #setLocatorHitbox(org.cell2d.space.Hitbox)
     * @see #setThinker(org.cell2d.space.SpaceThinker)
     */
    public ThinkerObject() {}
    
    @Override
    void addNonCellData() {
        super.addNonCellData();
        if (state.getGameClass() == getGameClass()
                && state.getStateClass() == getStateClass()
                && state.getSubThinkerClass() == getSubThinkerClass()) {
            compatibleState = (U)state;
            compatibleState.addSubThinker(thinker);
        }
    }
    
    @Override
    void removeData() {
        super.removeData();
        if (compatibleState != null) {
            compatibleState.removeSubThinker(thinker);
            compatibleState = null;
        }
    }
    
    @Override
    void setTimeFactorActions(long timeFactor) {
        super.setTimeFactorActions(timeFactor);
        thinker.setTimeFactor(timeFactor);
    }
    
    /**
     * Returns the Class object representing the type of CellGame that uses this
     * ThinkerObject's SpaceThinker's SpaceStates.
     * @return The Class object representing the type of CellGame that uses this
     * ThinkerObject's SpaceThinker's SpaceStates
     */
    public final Class<T> getGameClass() {
        return thinker.getGameClass();
    }
    
    /**
     * Returns the Class object representing the type of SpaceState that uses
     * this ThinkerObject's SpaceThinker.
     * @return The Class object representing the type of SpaceState that uses
     * this ThinkerObject's SpaceThinker
     */
    public final Class<U> getStateClass() {
        return thinker.getStateClass();
    }
    
    /**
     * Returns the Class object representing the type of SpaceThinker that
     * this ThinkerObject mimics.
     * @return The Class object representing the type of SpaceThinker that
     * this ThinkerObject mimics
     */
    public final Class<V> getSubThinkerClass() {
        return thinker.getSubThinkerClass();
    }
    
    /**
     * Returns this ThinkerObject's assigned SpaceThinker, or null if it has
     * none.
     * @return This ThinkerObject's assigned SpaceThinker
     */
    public final V getThinker() {
        return thinker;
    }
    
    /**
     * Sets this ThinkerObject's assigned SpaceThinker to the specified one, if
     * this ThinkerObject does not already have a SpaceThinker and the
     * specified SpaceThinker is not already assigned to a Thinker.
     * @param thinker The SpaceThinker to be assigned
     * @return Whether the assignment occurred
     */
    public final boolean setThinker(V thinker) {
        if (this.thinker == null && thinker.getNewSuperThinker() == null) {
            this.thinker = thinker;
            return true;
        }
        return false;
    }
    
    /**
     * Returns the current value of this ThinkerObject's timer for the specified
     * Event.
     * @param event The Event whose timer value should be returned
     * @return The current value of the timer for the specified Event
     */
    public final int getTimerValue(Event<T,U> event) {
        return thinker.getTimerValue(event);
    }
    
    /**
     * Sets the value of this ThinkerObject's timer for the specified Event to
     * the specified value.
     * @param event The Event whose timer value should be set
     * @param value The new value of the specified Event's timer
     */
    public final void setTimerValue(Event<T,U> event, int value) {
        thinker.setTimerValue(event, value);
    }
    
    /**
     * Returns the EventGroup of this ThinkerObject's frame Events.
     * @return The EventGroup of this ThinkerObject's frame Events
     */
    public final EventGroup<T,U> getFrameEvents() {
        return thinker.getFrameEvents();
    }
    
    /**
     * Returns the number of SpaceThinkers that are assigned to this
     * ThinkerObject.
     * @return The number of SpaceThinkers that are assigned to this
     * ThinkerObject
     */
    public final int getNumSubThinkers() {
        return thinker.getNumSubThinkers();
    }
    
    /**
     * Returns whether any Iterators over this ThinkerObject's list of
     * SpaceThinkers are in progress.
     * @return Whether any Iterators over this ThinkerObject's list of
     * SpaceThinkers are in progress
     */
    public final boolean iteratingThroughSubThinkers() {
        return thinker.iteratingThroughSubThinkers();
    }
    
    /**
     * Returns a new SafeIterator over this ThinkerObject's list of
     * SpaceThinkers.
     * @return A new SafeIterator over this ThinkerObject's list of
     * SpaceThinkers
     */
    public final SafeIterator<V> subThinkerIterator() {
        return thinker.subThinkerIterator();
    }
    
    /**
     * Adds the specified SpaceThinker to this ThinkerObject if it is not
     * already assigned to a Thinker, and if doing so would not create a loop of
     * assignments in which SpaceThinkers are directly or indirectly assigned to
     * themselves.
     * @param subThinker The SpaceThinker to be added
     * @return Whether the addition occurred
     */
    public final boolean addSubThinker(V subThinker) {
        return this.thinker.addSubThinker(subThinker);
    }
    
    /**
     * Removes the specified SpaceThinker from this ThinkerObject if it is
     * currently assigned to it.
     * @param subThinker The SpaceThinker to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeSubThinker(V subThinker) {
        return this.thinker.removeSubThinker(subThinker);
    }
    
    /**
     * Removes from this ThinkerObject all of the SpaceThinkers that are
     * currently assigned to it.
     */
    public final void clearSubThinkers() {
        thinker.clearSubThinkers();
    }
    
    /**
     * Removes from their super-Thinkers all of the SpaceThinkers that are
     * directly or indirectly assigned to this ThinkerObject, and are either
     * assigned to or assignees of the specified SpaceThinker. For instance, if
     * a SpaceThinker is assigned to a SpaceThinker that is assigned to a
     * SpaceThinker that is assigned to this ThinkerObject, and the second
     * SpaceThinker is the specified SubThinker, the first SpaceThinker will be
     * removed from the second, the second from the third, and the third from
     * this ThinkerObject. This method is useful for ThinkerObjects that use
     * SpaceThinkers to model a hierarchy of states in which they can exist.
     * @param subThinker The SpaceThinker with which the removed SpaceThinkers
     * must share a lineage of assignments
     * @return Whether any removals occurred
     */
    public final boolean removeLineage(V subThinker) {
        return this.thinker.removeLineage(subThinker);
    }
    
    /**
     * Removes from their super-Thinkers all of the SpaceThinkers that are
     * directly or indirectly assigned to this Thinker. For instance, if a
     * SpaceThinker is assigned to a SpaceThinker that is assigned to this
     * ThinkerObject, the first SpaceThinker will be removed from the second,
     * and the second will be removed from this ThinkerObject.
     */
    public final void clearLineages() {
        thinker.clearLineages();
    }
    
    /**
     * Returns this ThinkerObject's frame priority.
     * @return This ThinkerObject's frame priority
     */
    public int getFramePriority() {
        return thinker.getFramePriority();
    }
    
    /**
     * Sets this ThinkerObject's frame priority to the specified value.
     * @param framePriority This ThinkerObject's new frame priority
     */
    public void setFramePriority(int framePriority) {
        thinker.setFramePriority(framePriority);
    }
    
    /**
     * Returns the EventGroup of this ThinkerObject's before-movement Events.
     * @return The EventGroup of this ThinkerObject's before-movement Events
     */
    public final EventGroup<T,U> getBeforeMovementEvents() {
        return thinker.getBeforeMovementEvents();
    }
    
    /**
     * Returns this ThinkerObject's before-movement priority.
     * @return This ThinkerObject's before-movement priority
     */
    public final int getBeforeMovementPriority() {
        return thinker.getBeforeMovementPriority();
    }
    
    /**
     * Sets this ThinkerObject's before-movement priority to the specified
     * value.
     * @param beforeMovementPriority This ThinkerObject's new before-movement
     * priority
     */
    public final void setBeforeMovementPriority(int beforeMovementPriority) {
        thinker.setBeforeMovementPriority(beforeMovementPriority);
    }
    
}
