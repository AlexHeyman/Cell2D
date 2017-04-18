package blah;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>A Thinker is a collection of methods that contributes to the mechanics of
 * the CellGameState to which it is assigned. A Thinker's assigned CellGameState
 * will keep track of time for it, thus allowing it to take its own
 * time-dependent actions, while the CellGameState is active. A Thinker's time
 * factor represents how many time units the Thinker will experience every frame
 * while assigned to an active CellGameState. If its own time factor is
 * negative, a Thinker will use its assigned CellGameState's time factor
 * instead. If a Thinker is assigned to an inactive CellGameState or none at
 * all, time will not pass for it.</p>
 * 
 * <p>A Thinker's action priority determines when it will act relative to other
 * Thinkers. All of the Thinkers assigned to the active CellGameState will take
 * their timeUnitActions() and their frameActions() in order from highest to
 * lowest action priority.</p>
 * 
 * <p>A Thinker may occupy at most one ThinkerState at a time. ThinkerStates
 * take actions alongside their Thinker's own, as well as when entered and left
 * by a Thinker, and can help a Thinker keep track of its position in a
 * multi-frame procedure. A ThinkerState can have a limited duration in time
 * units, and at the beginning of the time unit when that duration is up, its
 * Thinker automatically transitions to its next ThinkerState.</p>
 * 
 * <p>A Thinker has timers that can activate TimedEvents after a certain number
 * of time units. Timers have integer values, with a positive value x indicating
 * that the TimedEvent will be activated in x time units, a negative value
 * indicating that the timer is not running, and a value of 0 indicating that
 * either the TimedEvent was activated or the value was deliberately set to 0
 * this time unit. Each time unit, after a Thinker automatically changes
 * ThinkerStates (if it did) but before it and its ThinkerState (if it has one)
 * take their timeUnitActions(), its non-negative timers' values are decreased
 * by 1 and the TimedEvents whose timers have reached 0 are activated.
 * </p>
 * 
 * <p>The Thinker class is intended to be directly extended by classes V that
 * extend Thinker&lt;T,U,V,W&gt; and interact with CellGameStates of class U and
 * ThinkerStates of class W. BasicThinker is an example of such a class. This
 * allows a Thinker's CellGameStates and ThinkerStates to interact with it in
 * ways unique to its subclass of Thinker.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that this Thinker's CellGameState is used
 * by
 * @param <U> The subclass of CellGameState that this Thinker is used by
 * @param <V> The subclass of Thinker that this Thinker is
 * @param <W> The subclass of ThinkerState that this Thinker uses
 */
public abstract class Thinker<T extends CellGame, U extends CellGameState<T,U,V,W>, V extends Thinker<T,U,V,W>, W extends ThinkerState<T,U,V,W>> {
    
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    private boolean initialized = false;
    private final V thisThinker;
    U state = null;
    U newState = null;
    private double timeFactor = -1;
    private double timeToRun = 0;
    int actionPriority = 0;
    int newActionPriority = 0;
    private W thinkerState = null;
    private final Queue<W> upcomingStates = new LinkedList<>();
    private boolean delayedStateChange = false;
    private int thinkerStateDuration = -1;
    private final Map<TimedEvent<U>,Integer> timers = new HashMap<>();
    
    /**
     * Creates a new Thinker.
     */
    public Thinker() {
        id = getNextID();
        thisThinker = getThis();
        initialized = true;
    }
    
    private static long getNextID() {
        return idCounter.getAndIncrement();
    }
    
    /**
     * A method which returns this Thinker as a V, rather than as a
     * Thinker&lt;T,U,V,W&gt;. This must be implemented somewhere in the lineage
     * of every subclass of Thinker in order to get around Java's limitations
     * with regard to generic types.
     * @return This Thinker as a V
     */
    public abstract V getThis();
    
    /**
     * Returns the CellGameState to which this Thinker is currently assigned, or
     * null if it is assigned to none.
     * @return The CellGameState to which this Thinker is currently assigned
     */
    public final U getGameState() {
        return state;
    }
    
    /**
     * Returns the CellGameState to which this Thinker is about to be assigned,
     * but has not yet been due to one or more of the Thinker lists involved
     * being iterated over. If this Thinker is about to be removed from its
     * CellGameState without being added to a new one afterward, this will be
     * null. If this Thinker is not about to change CellGameStates, this method
     * will simply return its current CellGameState.
     * @return The CellGameState to which this Thinker is about to be assigned
     */
    public final U getNewGameState() {
        return newState;
    }
    
    /**
     * Sets the CellGameState to which this Thinker is currently assigned. If it
     * is set to a null CellGameState, this Thinker will be removed from its
     * current CellGameState if it has one.
     * @param state The CellGameState to which this Thinker should be assigned
     */
    public final void setGameState(U state) {
        if (!initialized) {
            return;
        }
        if (this.state != null) {
            this.state.removeThinker(thisThinker);
        }
        if (state != null) {
            state.addThinker(thisThinker);
        }
    }
    
    void addActions() {
        if (!upcomingStates.isEmpty() || thinkerStateDuration == 0) {
            endState(state.getGame(), state);
        }
    }
    
    /**
     * Returns this Thinker's time factor.
     * @return This Thinker's time factor
     */
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    /**
     * Returns this Thinker's effective time factor; that is, how many time
     * units it experiences every frame. If it is not assigned to a
     * CellGameState, this will be 0.
     * @return This Thinker's effective time factor
     */
    public final double getEffectiveTimeFactor() {
        return (state == null ? 0 : (timeFactor < 0 ? state.getTimeFactor() : timeFactor));
    }
    
    /**
     * Sets this Thinker's time factor to the specified value.
     * @param timeFactor The new time factor
     */
    public final void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
    }
    
    /**
     * Returns this Thinker's action priority.
     * @return This Thinker's action priority
     */
    public final int getActionPriority() {
        return actionPriority;
    }
    
    /**
     * Returns the action priority that this Thinker is about to have, but does
     * not yet have due to its CellGameState's Thinker list being iterated over.
     * If this Thinker is not about to change its action priority, this method
     * will simply return its current action priority.
     * @return The action priority that this Thinker is about to have
     */
    public final int getNewActionPriority() {
        return newActionPriority;
    }
    
    /**
     * Sets this Thinker's action priority.
     * @param actionPriority The new action priority
     */
    public final void setActionPriority(int actionPriority) {
        if (state == null) {
            newActionPriority = actionPriority;
            this.actionPriority = actionPriority;
        } else if (newActionPriority != actionPriority) {
            newActionPriority = actionPriority;
            state.changeThinkerActionPriority(thisThinker, actionPriority);
        }
    }
    
    /**
     * Returns this Thinker's current ThinkerState.
     * @return This Thinker's current ThinkerState
     */
    public final W getThinkerState() {
        return thinkerState;
    }
    
    private void endState(T game, U state) {
        if (thinkerState != null) {
            delayedStateChange = true;
            thinkerState.leftActions(game, state);
            delayedStateChange = false;
        }
        if (upcomingStates.isEmpty()) {
            delayedStateChange = true;
            W nextState = thinkerState.getNextState();
            delayedStateChange = false;
            beginState(game, state, nextState);
        } else {
            beginState(game, state, upcomingStates.remove());
        }
    }
    
    private void beginState(T game, U state, W newState) {
        thinkerState = newState;
        int duration;
        if (thinkerState == null) {
            duration = -1;
        } else {
            delayedStateChange = true;
            thinkerState.enteredActions(game, state);
            duration = thinkerState.getDuration();
            delayedStateChange = false;
        }
        setThinkerStateDuration(duration);
        if (!upcomingStates.isEmpty() || duration == 0) {
            endState(game, state);
        }
    }
    
    /**
     * Sets this Thinker's current ThinkerState to the specified one. If this
     * Thinker is not assigned to a CellGameState, the change will not occur
     * until it is added to one, immediately before it takes its addedActions().
     * @param thinkerState The new ThinkerState
     */
    public final void setThinkerState(W thinkerState) {
        upcomingStates.add(thinkerState);
        if (state != null && !delayedStateChange) {
            endState(state.getGame(), state);
        }
    }
    
    /**
     * Returns the remaining duration in time units of this Thinker's current
     * ThinkerState. A negative value indicates an infinite duration.
     * @return The remaining duration in time units of this Thinker's current
     * ThinkerState
     */
    public final int getThinkerStateDuration() {
        return thinkerStateDuration;
    }
    
    /**
     * Sets the remaining duration in time units of this Thinker's current
     * ThinkerState to the specified value. A negative value indicates an
     * infinite duration, and a value of 0 indicates that the ThinkerState
     * should end as soon as possible.
     * @param duration The new duration in time units of this Thinker's current
     * ThinkerState
     */
    public final void setThinkerStateDuration(int duration) {
        thinkerStateDuration = (duration < 0 ? -1 : duration);
        if (duration == 0 && state != null && !delayedStateChange) {
            endState(state.getGame(), state);
        }
    }
    
    /**
     * Returns the current value of this Thinker's timer for the specified
     * TimedEvent.
     * @param timedEvent The TimedEvent whose timer value should be returned
     * @return The current value of the timer for the specified TimedEvent
     */
    public final int getTimerValue(TimedEvent<U> timedEvent) {
        Integer value = timers.get(timedEvent);
        return (value == null ? -1 : value);
    }
    
    /**
     * Sets the value of this Thinker's timer for the specified TimedEvent to
     * the specified value.
     * @param timedEvent The TimedEvent whose timer value should be set
     * @param value The new value of the specified TimedEvent's timer
     */
    public final void setTimerValue(TimedEvent<U> timedEvent, int value) {
        if (timedEvent == null) {
            throw new RuntimeException("Attempted to set the value of a timer for a null TimedEvent");
        }
        if (value < 0) {
            timers.remove(timedEvent);
        } else {
            timers.put(timedEvent, value);
        }
    }
    
    /**
     * Actions for this Thinker to take once every time unit, after
     * AnimationInstances update their indices but before Thinkers take their
     * frameActions().
     * @param game This Thinker's CellGame
     * @param state This Thinker's CellGameState
     */
    public void timeUnitActions(T game, U state) {}
    
    final void doFrame(T game, U state) {
        if (thinkerState != null) {
            thinkerState.frameActions(game, state);
        }
        frameActions(game, state);
    }
    
    /**
     * Actions for this Thinker to take once every frame, after Thinkers take
     * their timeUnitActions() but before its CellGameState takes its own
     * frameActions().
     * @param game This Thinker's CellGame
     * @param state This Thinker's CellGameState
     */
    public void frameActions(T game, U state) {}
    
    /**
     * Actions for this Thinker to take immediately after being added to a new
     * CellGameState.
     * @param game This Thinker's CellGame
     * @param state This Thinker's CellGameState
     */
    public void addedActions(T game, U state) {}
    
    /**
     * Actions for this Thinker to take immediately before being removed from
     * its CellGameState.
     * @param game This Thinker's CellGame
     * @param state This Thinker's CellGameState
     */
    public void removedActions(T game, U state) {}
    
    final void update(T game) {
        timeToRun += getEffectiveTimeFactor();
        while (timeToRun >= 1) {
            if (thinkerStateDuration >= 0) {
                thinkerStateDuration--;
                if (thinkerStateDuration == 0) {
                    endState(game, state);
                }
            }
            if (!timers.isEmpty()) {
                List<TimedEvent<U>> timedEventsToDo = new ArrayList<>();
                Iterator<Map.Entry<TimedEvent<U>,Integer>> iterator = timers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<TimedEvent<U>,Integer> entry = iterator.next();
                    int value = entry.getValue();
                    if (value == 0) {
                        iterator.remove();
                    } else {
                        if (value == 1) {
                            timedEventsToDo.add(entry.getKey());
                        }
                        entry.setValue(value - 1);
                    }
                }
                for (TimedEvent<U> timedEvent : timedEventsToDo) {
                    timedEvent.eventActions(game, state);
                }
            }
            if (thinkerState != null) {
                thinkerState.timeUnitActions(game, state);
            }
            timeUnitActions(game, state);
            timeToRun -= 1;
        }
    }
    
}
