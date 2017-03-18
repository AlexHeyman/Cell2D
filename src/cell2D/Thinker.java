package cell2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Thinker<T extends CellGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
    
    private static final AtomicLong idCounter = new AtomicLong(0);
    
    final long id;
    private boolean initialized = false;
    private final U thisThinker;
    T state = null;
    T newState = null;
    private double timeFactor = -1;
    private double timeToRun = 0;
    int actionPriority = 0;
    int newActionPriority = 0;
    private final Map<TimedEvent<T>,Integer> timers = new HashMap<>();
    private V thinkerState = null;
    private final Queue<V> upcomingStates = new LinkedList<>();
    private boolean changingState = false;
    private final TimedEvent<T> nextState = new TimedEvent<T>() {
        
        @Override
        public void eventActions(CellGame game, T state) {
            endState(game, state, true);
        }
        
    };
    
    public Thinker() {
        id = getNextID();
        thisThinker = getThis();
        initialized = true;
    }
    
    private static long getNextID() {
        return idCounter.getAndIncrement();
    }
    
    public abstract U getThis();
    
    public final T getGameState() {
        return state;
    }
    
    public final T getNewGameState() {
        return newState;
    }
    
    public final void setGameState(T state) {
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
    
    public final boolean addTo(T state) {
        return (initialized ? state.addThinker(thisThinker) : false);
    }
    
    public final boolean remove() {
        return (initialized ? (state == null ? false : state.removeThinker(thisThinker)) : false);
    }
    
    void addActions() {
        if (!upcomingStates.isEmpty()) {
            endState(state.getGame(), state, false);
        }
    }
    
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    public final double getEffectiveTimeFactor() {
        return (timeFactor < 0 ? (state == null ? 0 : state.getTimeFactor()) : timeFactor);
    }
    
    public final void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
    }
    
    public final int getActionPriority() {
        return actionPriority;
    }
    
    public final int getNewActionPriority() {
        return newActionPriority;
    }
    
    public final void setActionPriority(int actionPriority) {
        if (state == null) {
            newActionPriority = actionPriority;
            this.actionPriority = actionPriority;
        } else if (newActionPriority != actionPriority) {
            newActionPriority = actionPriority;
            state.changeThinkerActionPriority(thisThinker, actionPriority);
        }
    }
    
    public final V getThinkerState() {
        return thinkerState;
    }
    
    private void endState(CellGame game, T state, boolean useNextState) {
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
    
    private void beginState(CellGame game, T state, V newState) {
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
    
    public final void changeThinkerState(V thinkerState) {
        upcomingStates.add(thinkerState);
        if (state != null && !changingState) {
            endState(state.getGame(), state, false);
        }
    }
    
    public final int getTimerValue(TimedEvent<T> timedEvent) {
        Integer value = timers.get(timedEvent);
        return (value == null ? -1 : value);
    }
    
    public final void setTimerValue(TimedEvent<T> timedEvent, int value) {
        if (timedEvent == null) {
            throw new RuntimeException("Attempted to set the value of a timer with no TimedEvent");
        }
        if (value < 0) {
            timers.remove(timedEvent);
        } else {
            timers.put(timedEvent, value);
        }
    }
    
    public void timeUnitActions(CellGame game, T state) {}
    
    final void doFrame(CellGame game, T state) {
        if (thinkerState != null) {
            thinkerState.frameActions(game, state);
        }
        frameActions(game, state);
    }
    
    public void frameActions(CellGame game, T state) {}
    
    public void addedActions(CellGame game, T state) {}
    
    public void removedActions(CellGame game, T state) {}
    
    final void update(CellGame game) {
        double time = getEffectiveTimeFactor();
        if (time == 0) {
            return;
        }
        timeToRun += time;
        while (timeToRun >= 1) {
            List<TimedEvent<T>> timedEventsToDo = new ArrayList<>();
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
