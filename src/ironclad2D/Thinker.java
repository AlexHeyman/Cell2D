package ironclad2D;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Thinker<T extends IroncladGameState> {
    
    T state = null;
    T newState = null;
    private double timeFactor = 1;
    private double timeToRun = 0;
    private final Map<TimedEvent<T>,Integer> timers = new HashMap<>();
    
    public Thinker() {}
    
    public final T getState() {
        return state;
    }
    
    public final T getNewState() {
        return newState;
    }
    
    public final boolean addTo(T state) {
        if (newState == null) {
            newState = state;
            state.addThinker(this);
            return true;
        }
        return false;
    }
    
    public final boolean remove() {
        if (state != null) {
            return state.removeThinker(this);
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
            timeUnitActions(game, state);
            timeToRun -= 1;
        }
    }
    
}
