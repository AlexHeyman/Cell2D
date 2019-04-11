package org.cell2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

/**
 * <p>A Thinker is a collection of methods that contributes to the mechanics of
 * a GameState to which it is assigned. A Thinker can be a GameState itself, in
 * which case it is considered assigned to itself. It can also be a SubThinker
 * that can be assigned to another Thinker, in which case it is considered
 * assigned to a GameState if it is assigned to another Thinker that is itself
 * assigned to the GameState.</p>
 * 
 * <p>A Thinker's assigned GameState will keep track of its time, thus allowing
 * it to take its own time-dependent actions, while the GameState is active. A
 * Thinker's <i>time factor</i> represents the average number of discrete time
 * units the Thinker will experience each frame while assigned to an active
 * GameState. If its time factor is negative, as it is by default, a Thinker
 * will use a default time factor that depends on whether it is a GameState or a
 * SubThinker. If a Thinker is assigned to an inactive GameState or none at all,
 * time will not pass for it.</p>
 * 
 * <p>A Thinker has <i>timers</i> that can perform Events after a certain number
 * of time units. Timers have integer values, with a positive value x indicating
 * that the Event will be performed in x time units, a negative value indicating
 * that the timer is not running, and a value of 0 indicating that either the
 * Event was performed or the value was deliberately set to 0 this time unit.
 * Each time unit, a Thinker decreases its non-negative timers' values by 1 and
 * performs the Events whose timers have reached 0. Each frame, each Thinker
 * experiences all of its time units immediately before its assigned SubThinkers
 * do.</p>
 * 
 * <p>A Thinker has frameActions() that it takes exactly once each frame, after
 * all Thinkers have experienced all of their time units for that frame. It also
 * has an EventGroup of <i>frame Events</i> that it performs once each frame
 * immediately after it takes its frameActions().</p>
 * 
 * <p>SubThinkers may be assigned to one Thinker each. Because a Thinker's
 * internal list of SubThinkers cannot be modified while it is being iterated
 * over, the actual addition or removal of a SubThinker to or from a Thinker is
 * delayed until any and all iterations over its SubThinkers, such as the
 * periods during which its SubThinkers perform their timer Events, have been
 * completed. Multiple delayed instructions may be successfully given to
 * Thinkers regarding the same SubThinker without having to wait until all
 * iterations have finished.</p>
 * @param <T> The type of CellGame that uses this Thinker's GameStates
 * @param <U> The type of GameState that uses this Thinker
 * @param <V> The type of SubThinker that can be assigned to this Thinker
 * @author Andrew Heyman
 */
public abstract class Thinker<T extends CellGame, U extends GameState<T,U,V>, V extends SubThinker<T,U,V>> {
    
    private final Class<T> gameClass;
    private final Class<U> stateClass;
    private final Class<V> subThinkerClass;
    private long timeFactor = -1;
    private long timeToRun = 0;
    private final Map<Event<T,U>,Integer> timers = new HashMap<>();
    private final EventGroup<T,U> frameEvents = new EventGroup<>();
    private final Set<V> subThinkers = new HashSet<>();
    private int subThinkerIterators = 0;
    private final Queue<SubThinkerChange<T,U,V>> subThinkerChanges = new LinkedList<>();
    private boolean updatingSubThinkers = false;
    
    final Event<T,U> frame = (game, state) -> {
        frameActions(game, state);
        if (frameEvents.size() > 0) {
            frameEvents.perform(state);
        }
    };
    
    /**
     * Constructs a Thinker.
     * @param gameClass The Class object representing the type of CellGame that
     * uses this Thinker's GameStates
     * @param stateClass The Class object representing the type of GameState
     * that uses this Thinker
     * @param subThinkerClass The Class object representing the type of
     * SubThinker that can be assigned to this Thinker
     */
    public Thinker(Class<T> gameClass, Class<U> stateClass, Class<V> subThinkerClass) {
        this.gameClass = gameClass;
        this.stateClass = stateClass;
        this.subThinkerClass = subThinkerClass;
    }
    
    /**
     * Returns the Class object representing the type of CellGame that uses this
     * Thinker's GameStates.
     * @return The Class object representing the type of CellGame that uses this
     * Thinker's GameStates
     */
    public final Class<T> getGameClass() {
        return gameClass;
    }
    
    /**
     * Returns the Class object representing the type of GameState that uses
     * this Thinker.
     * @return The Class object representing the type of GameState that uses
     * this Thinker
     */
    public final Class<U> getStateClass() {
        return stateClass;
    }
    
    /**
     * Returns the Class object representing the type of SubThinker that can be
     * assigned to this Thinker.
     * @return The Class object representing the type of SubThinker that can be
     * assigned to this Thinker
     */
    public final Class<V> getSubThinkerClass() {
        return subThinkerClass;
    }
    
    /**
     * Returns the CellGame of the GameState to which this Thinker is assigned,
     * or null if it is not assigned to a GameState.
     * @return This Thinker's GameState's CellGame
     */
    public abstract T getGame();
    
    /**
     * Returns the GameState to which this Thinker is assigned, or null if it is
     * not assigned to one.
     * @return The GameState to which this Thinker is assigned
     */
    public abstract U getGameState();
    
    /**
     * Returns this Thinker's time factor.
     * @return This Thinker's time factor
     */
    public final long getTimeFactor() {
        return timeFactor;
    }
    
    /**
     * Returns this Thinker's effective time factor; that is, the average number
     * of time units it experiences every frame. If it is not assigned to a
     * GameState, this will be 0.
     * @return This Thinker's effective time factor
     */
    public abstract long getEffectiveTimeFactor();
    
    /**
     * Sets this Thinker's time factor to the specified value.
     * @param timeFactor The new time factor
     */
    public final void setTimeFactor(long timeFactor) {
        this.timeFactor = timeFactor;
    }
    
    /**
     * Returns the current value of this Thinker's timer for the specified
     * Event.
     * @param event The Event whose timer value should be returned
     * @return The current value of the timer for the specified Event
     */
    public final int getTimerValue(Event<T,U> event) {
        return timers.getOrDefault(event, -1);
    }
    
    /**
     * Sets the value of this Thinker's timer for the specified Event to the
     * specified value.
     * @param event The Event whose timer value should be set
     * @param value The new value of the specified Event's timer
     */
    public final void setTimerValue(Event<T,U> event, int value) {
        if (value < 0) {
            timers.remove(event);
        } else {
            timers.put(event, value);
        }
    }
    
    final void update(T game, U state, long time) {
        if (timeFactor >= 0) {
            time = timeFactor;
        }
        timeToRun += time;
        while (timeToRun >= Frac.UNIT) {
            if (!timers.isEmpty()) {
                List<Event<T,U>> eventsToPerform = new ArrayList<>();
                Iterator<Map.Entry<Event<T,U>,Integer>> iterator = timers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Event<T,U>,Integer> entry = iterator.next();
                    int value = entry.getValue();
                    if (value == 0) {
                        iterator.remove();
                    } else {
                        if (value == 1) {
                            eventsToPerform.add(entry.getKey());
                        }
                        entry.setValue(value - 1);
                    }
                }
                for (Event<T,U> event : eventsToPerform) {
                    event.actions(game, state);
                }
            }
            timeToRun -= Frac.UNIT;
        }
        if (getNumSubThinkers() > 0) {
            Iterator<V> iterator = subThinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().update(game, state, time);
            }
        }
    }
    
    /**
     * Actions for this Thinker to take once each frame.
     * @param game This Thinker's GameState's CellGame
     * @param state This Thinker's GameState
     */
    public void frameActions(T game, U state) {}
    
    /**
     * Returns the EventGroup of this Thinker's frame Events.
     * @return The EventGroup of this Thinker's frame Events
     */
    public final EventGroup<T,U> getFrameEvents() {
        return frameEvents;
    }
    
    /**
     * Returns the number of SubThinkers that are assigned to this Thinker.
     * @return The number of SubThinkers that are assigned to this Thinker
     */
    public final int getNumSubThinkers() {
        return subThinkers.size();
    }
    
    private class SubThinkerIterator implements SafeIterator<V> {
        
        private boolean stopped = false;
        private final Iterator<V> iterator = subThinkers.iterator();
        private V lastSubThinker = null;
        
        private SubThinkerIterator() {
            subThinkerIterators++;
        }
        
        @Override
        public final boolean hasNext() {
            if (stopped) {
                return false;
            }
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                stop();
            }
            return hasNext;
        }
        
        @Override
        public final V next() {
            if (stopped) {
                throw new NoSuchElementException();
            }
            lastSubThinker = iterator.next();
            return lastSubThinker;
        }
        
        @Override
        public final void remove() {
            if (!stopped && lastSubThinker != null) {
                removeSubThinker(lastSubThinker);
                lastSubThinker = null;
            }
        }
        
        @Override
        public final void stop() {
            if (!stopped) {
                stopped = true;
                subThinkerIterators--;
                updateSubThinkers();
            }
        }
        
    }
    
    /**
     * Returns whether any Iterators over this Thinker's list of SubThinkers are
     * in progress.
     * @return Whether any Iterators over this Thinker's list of SubThinkers are
     * in progress
     */
    public final boolean iteratingThroughSubThinkers() {
        return subThinkerIterators > 0;
    }
    
    /**
     * Returns a new SafeIterator over this Thinker's list of SubThinkers.
     * @return A new SafeIterator over this Thinker's list of SubThinkers
     */
    public final SafeIterator<V> subThinkerIterator() {
        return new SubThinkerIterator();
    }
    
    private static class SubThinkerChange<T extends CellGame,
            U extends GameState<T,U,V>, V extends SubThinker<T,U,V>> {
        
        private boolean made = false;
        private final V subThinker;
        private final Thinker<T,U,V> newSuperThinker;
        
        private SubThinkerChange(V subThinker, Thinker<T,U,V> newSuperThinker) {
            this.subThinker = subThinker;
            this.newSuperThinker = newSuperThinker;
        }
        
    }
    
    /**
     * Adds the specified SubThinker to this Thinker if it is not already
     * assigned to a Thinker, and if doing so would not create a loop of
     * assignments in which SubThinkers are directly or indirectly assigned to
     * themselves.
     * @param subThinker The SubThinker to be added
     * @return Whether the addition occurred
     */
    public final boolean addSubThinker(V subThinker) {
        if (subThinker.newSuperThinker == null) {
            Thinker<T,U,V> ancestor = this;
            do {
                if (ancestor == subThinker) {
                    return false;
                } else if (ancestor instanceof SubThinker) {
                    ancestor = ((V)ancestor).newSuperThinker;
                } else {
                    break;
                }
            } while (ancestor != null);
            subThinker.newSuperThinker = this;
            addSubThinkerChange(subThinker, this);
            return true;
        }
        return false;
    }
    
    /**
     * Removes the specified SubThinker from this Thinker if it is currently
     * assigned to it.
     * @param subThinker The SubThinker to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeSubThinker(V subThinker) {
        if (subThinker.newSuperThinker == this) {
            addSubThinkerChange(subThinker, null);
            return true;
        }
        return false;
    }
    
    /**
     * Removes from this Thinker all of the SubThinkers that are currently
     * assigned to it.
     */
    public final void clearSubThinkers() {
        if (!subThinkers.isEmpty() || !subThinkerChanges.isEmpty()) {
            Set<V> subThinkersToRemove = new HashSet<>();
            for (V subThinker : subThinkers) {
                if (subThinker.newSuperThinker == this) {
                    subThinkersToRemove.add(subThinker);
                }
            }
            for (SubThinkerChange<T,U,V> change : subThinkerChanges) {
                V subThinker = change.subThinker;
                if (subThinker.newSuperThinker == this) {
                    subThinkersToRemove.add(subThinker);
                }
            }
            for (V subThinker : subThinkersToRemove) {
                subThinker.newSuperThinker = null;
                subThinkerChanges.add(new SubThinkerChange(subThinker, null));
            }
            updateSubThinkers();
        }
    }
    
    /**
     * Removes from their super-Thinkers all of the SubThinkers that are
     * directly or indirectly assigned to this Thinker, and are either assigned
     * to or assignees of the specified SubThinker. For instance, if a
     * SubThinker is assigned to a SubThinker that is assigned to a SubThinker
     * that is assigned to this Thinker, and the second SubThinker is the
     * specified SubThinker, the first SubThinker will be removed from the
     * second, the second from the third, and the third from this Thinker. This
     * method is useful for Thinkers that use SubThinkers to model a hierarchy
     * of states in which they can exist.
     * @param subThinker The SubThinker with which the removed SubThinkers must
     * share a lineage of assignments
     * @return Whether any removals occurred
     */
    public final boolean removeLineage(V subThinker) {
        List<V> lineage = new ArrayList<>();
        while (true) {
            lineage.add(subThinker);
            Thinker<T,U,V> superThinker = subThinker.newSuperThinker;
            if (superThinker == this) {
                break;
            } else if (superThinker instanceof SubThinker) {
                subThinker = (V)superThinker;
            } else {
                return false;
            }
        }
        subThinker = lineage.get(0);
        subThinker.clearLineages();
        for (int i = 1; i < lineage.size(); i++) {
            V superThinker = lineage.get(i);
            superThinker.removeSubThinker(subThinker);
            subThinker = superThinker;
        }
        removeSubThinker(subThinker);
        return true;
    }
    
    /**
     * Removes from their super-Thinkers all of the SubThinkers that are
     * directly or indirectly assigned to this Thinker. For instance, if a
     * SubThinker is assigned to a SubThinker that is assigned to this Thinker,
     * the first SubThinker will be removed from the second, and the second will
     * be removed from this Thinker.
     */
    public final void clearLineages() {
        if (!subThinkers.isEmpty() || !subThinkerChanges.isEmpty()) {
            Set<V> subThinkersToRemove = new HashSet<>();
            for (V subThinker : subThinkers) {
                if (subThinker.newSuperThinker == this) {
                    subThinkersToRemove.add(subThinker);
                }
            }
            for (SubThinkerChange<T,U,V> change : subThinkerChanges) {
                V subThinker = change.subThinker;
                if (subThinker.newSuperThinker == this) {
                    subThinkersToRemove.add(subThinker);
                }
            }
            for (V subThinker : subThinkersToRemove) {
                subThinker.clearLineages();
                subThinker.newSuperThinker = null;
                subThinkerChanges.add(new SubThinkerChange(subThinker, null));
            }
            updateSubThinkers();
        }
    }
    
    private void addSubThinkerChange(V subThinker, Thinker<T,U,V> newSuperThinker) {
        subThinker.newSuperThinker = newSuperThinker;
        SubThinkerChange<T,U,V> change = new SubThinkerChange<>(subThinker, newSuperThinker);
        if (subThinker.superThinker != null) {
            subThinker.superThinker.subThinkerChanges.add(change);
            subThinker.superThinker.updateSubThinkers();
        }
        if (newSuperThinker != null) {
            newSuperThinker.subThinkerChanges.add(change);
            newSuperThinker.updateSubThinkers();
        }
    }
    
    private void add(T game, U state, V subThinker) {
        subThinkers.add(subThinker);
        subThinker.superThinker = this;
        subThinker.setGameAndState(game, state);
        frameEvents.add(subThinker.frame, subThinker.getFramePriority());
        addSubThinkerActions(game, state, subThinker);
        subThinker.addedActions(game, state);
    }
    
    /**
     * Actions for this Thinker to take immediately after adding a SubThinker to
     * itself, before the added SubThinker takes its addedActions().
     * @param game This Thinker's GameState's CellGame, or null if it has no
     * GameState
     * @param state This Thinker's GameState, or null if it has none
     * @param subThinker The SubThinker that was added
     */
    public void addSubThinkerActions(T game, U state, V subThinker) {}
    
    private void remove(T game, U state, V subThinker) {
        subThinker.removedActions(game, state);
        removeSubThinkerActions(game, state, subThinker);
        frameEvents.remove(subThinker.frame, subThinker.getFramePriority());
        subThinkers.remove(subThinker);
        subThinker.superThinker = null;
        subThinker.setGameAndState(null, null);
    }
    
    /**
     * Actions for this Thinker to take immediately before removing a SubThinker
     * from itself, after the soon-to-be-removed SubThinker takes its
     * removedActions().
     * @param game This Thinker's GameState's CellGame, or null if it has no
     * GameState
     * @param state This Thinker's GameState, or null if it has none
     * @param subThinker The SubThinker that is about to be removed
     */
    public void removeSubThinkerActions(T game, U state, V subThinker) {}
    
    private void updateSubThinkers() {
        if (subThinkerIterators == 0 && !updatingSubThinkers) {
            updatingSubThinkers = true;
            T game = getGame();
            U state = getGameState();
            while (!subThinkerChanges.isEmpty()) {
                SubThinkerChange<T,U,V> change = subThinkerChanges.remove();
                if (!change.made) {
                    change.made = true;
                    Thinker<T,U,V> superThinker = change.subThinker.superThinker;
                    if (superThinker != null) {
                        superThinker.remove(game, state, change.subThinker);
                    }
                    if (change.newSuperThinker != null) {
                        change.newSuperThinker.add(game, state, change.subThinker);
                    }
                }
            }
            updatingSubThinkers = false;
        }
    }
    
}
