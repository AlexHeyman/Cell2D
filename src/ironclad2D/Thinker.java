package ironclad2D;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class Thinker<T extends IroncladGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
    
    private boolean initialized = false;
    private final U thisThinker;
    T state = null;
    T newState = null;
    private double timeFactor = 1;
    private double timeToRun = 0;
    private final Map<TimedEvent<T>,Integer> timers = new HashMap<>();
    private V thinkerState = null;
    private final Queue<V> upcomingStates = new LinkedList<>();
    private boolean changingState = false;
    private final TimedEvent<T> nextState = new TimedEvent<T>() {
        
        @Override
        public void eventActions(IroncladGame game, T state) {
            endState(game, state, true);
        }
        
    };
    
    public Thinker() {
        thisThinker = getThis();
        initialized = true;
    }
    
    public abstract U getThis();
    
    public final T getGameState() {
        return state;
    }
    
    public final T getNewGameState() {
        return newState;
    }
    
    public final boolean addTo(T state) {
        if (initialized) {
            return state.addThinker(thisThinker);
        }
        return false;
    }
    
    void addActions() {
        if (!upcomingStates.isEmpty()) {
            endState(state.getGame(), state, false);
        }
    }
    
    public final boolean remove() {
        if (initialized && state != null) {
            return state.removeThinker(thisThinker);
        }
        return false;
    }
    
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    public final void setTimeFactor(double timeFactor) {
        if (timeFactor < 0) {
            throw new RuntimeException("Attempted to give a thinker a negative time factor");
        }
        this.timeFactor = timeFactor;
    }
    
    public final V getThinkerState() {
        return thinkerState;
    }
    
    private void endState(IroncladGame game, T state, boolean useNextState) {
        if (thinkerState != null) {
            setTimerValue(nextState, -1);
            changingState = true;
            thinkerState.leftActions(game, state);
            changingState = false;
        }
        if (!upcomingStates.isEmpty()) {
            beginState(game, state, upcomingStates.remove());
        } else if (useNextState) {
            beginState(game, state, thinkerState.getNextState());
        }
    }
    
    private void beginState(IroncladGame game, T state, V newState) {
        thinkerState = newState;
        if (thinkerState != null) {
            changingState = true;
            thinkerState.enteredActions(game, state);
            changingState = false;
        }
        if (upcomingStates.isEmpty()) {
            if (thinkerState != null) {
                int duration = thinkerState.getDuration();
                if (duration > 0) {
                    setTimerValue(nextState, duration);
                } else if (duration == 0) {
                    endState(game, state, true);
                }
            }
        } else {
            endState(game, state, false);
        }
    }
    
    public final void changeThinkerState(V newState) {
        upcomingStates.add(newState);
        if (state != null && !changingState) {
            endState(state.getGame(), state, false);
        }
    }
    
    public final int getTimerValue(TimedEvent<T> timedEvent) {
        Integer value = timers.get(timedEvent);
        if (value == null) {
            return -1;
        }
        return value;
    }
    
    public final void setTimerValue(TimedEvent<T> timedEvent, int value) {
        if (timedEvent == null) {
            throw new RuntimeException("Attempted to set the value of a timer with no timed event");
        }
        if (value < 0) {
            timers.remove(timedEvent);
        } else {
            timers.put(timedEvent, value);
        }
    }
    
    public void timeUnitActions(IroncladGame game, T state) {}
    
    final void step(IroncladGame game, T state) {
        if (thinkerState != null) {
            thinkerState.stepActions(game, state);
        }
        stepActions(game, state);
    }
    
    public void stepActions(IroncladGame game, T state) {}
    
    public void addedActions(IroncladGame game, T state) {}
    
    public void removedActions(IroncladGame game, T state) {}
    
    final void update(IroncladGame game, double time) {
        if (timeFactor == 0) {
            return;
        }
        timeToRun += time*timeFactor;
        while (timeToRun >= 1) {
            List<TimedEvent<T>> timedEventsToDo = new LinkedList<>();
            Iterator<Map.Entry<TimedEvent<T>,Integer>> iterator = timers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<TimedEvent<T>,Integer> entry = iterator.next();
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
            for (TimedEvent<T> timedEvent : timedEventsToDo) {
                timedEvent.eventActions(game, state);
            }
            if (thinkerState != null) {
                thinkerState.timeUnitActions(game, state);
            }
            timeUnitActions(game, state);
            timeToRun -= 1;
        }
    }
    
}
