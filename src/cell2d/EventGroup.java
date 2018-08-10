package cell2d;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>An EventGroup is an ordered group of Events. Events in an EventGroup are
 * ordered by integer priority values that are specified when they are added.
 * Multiple instances of the same Event can be added to an EventGroup at the
 * same or different priorities. Because an EventGroup's internal list of Events
 * cannot be modified while it is being iterated over, the actual addition or
 * removal of an Event to or from an EventGroup is delayed until any and all
 * iterations over its Events have been completed. Multiple delayed instructions
 * may be successfully given to EventGroups regarding the same Event without
 * having to wait until all iterations have finished.</p>
 * @see Event
 * @see Thinker
 * @param <T> The type of CellGame that uses the GameStates that can involve
 * this EventGroup's Events
 * @param <U> The type of GameState that can involve this EventGroup's Events
 * @author Andrew Heyman
 */
public class EventGroup<T extends CellGame, U extends GameState<T,U,?>> {
    
    private static class Entry<T extends CellGame, U extends GameState<T,U,?>> {
        
        private final Event<T,U> event;
        private final int priority;
        private final int tiebreaker;
        
        private Entry(Event<T,U> event, int priority, int tiebreaker) {
            this.event = event;
            this.priority = priority;
            this.tiebreaker = tiebreaker;
        }
        
    }
    
    private final Comparator<Entry<T,U>> priorityComparator = (entry1, entry2) -> {
        int priorityDiff = entry2.priority - entry1.priority;
        if (priorityDiff == 0) {
            int eventCodeDiff = entry1.event.hashCode() - entry2.event.hashCode();
            if (eventCodeDiff == 0) {
                int tiebreakerDiff = entry1.tiebreaker - entry2.tiebreaker;
                if (tiebreakerDiff == 0) {
                    return System.identityHashCode(entry1) - System.identityHashCode(entry2);
                }
                return tiebreakerDiff;
            }
            return eventCodeDiff;
        }
        return priorityDiff;
    };
    
    private final SortedSet<Entry<T,U>> entries = new TreeSet<>(priorityComparator);
    private int numIterators = 0;
    private final SortedSet<Entry<T,U>> entriesToAdd = new TreeSet<>(priorityComparator);
    private final SortedSet<Entry<T,U>> entriesToRemove = new TreeSet<>(priorityComparator);
    
    /**
     * Creates an empty EventGroup.
     */
    public EventGroup() {}
    
    /**
     * Returns the number of Event instances in this EventGroup.
     * @return The number of Event instances in this EventGroup
     */
    public final int size() {
        return entries.size();
    }
    
    private class EventIterator implements SafeIterator<Event<T,U>> {
        
        private boolean stopped = false;
        private final Iterator<Entry<T,U>> iterator = entries.iterator();
        private Entry<T,U> lastEntry = null;
        
        private EventIterator() {
            numIterators++;
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
        public final Event<T,U> next() {
            if (stopped) {
                throw new NoSuchElementException();
            }
            lastEntry = iterator.next();
            return lastEntry.event;
        }
        
        @Override
        public final void remove() {
            if (!stopped && lastEntry != null) {
                EventGroup.this.remove(lastEntry.event, lastEntry.priority);
                lastEntry = null;
            }
        }
        
        @Override
        public final void stop() {
            if (!stopped) {
                stopped = true;
                numIterators--;
                if (numIterators == 0) {
                    for (Entry<T,U> entry : entriesToRemove) {
                        entries.remove(entry);
                    }
                    entriesToRemove.clear();
                    for (Entry<T,U> entry : entriesToAdd) {
                        entries.add(entry);
                    }
                    entriesToAdd.clear();
                }
            }
        }
        
    }
    
    /**
     * Returns whether any Iterators over this EventGroup's Events are in
     * progress.
     * @return Whether any Iterators over this EventGroup's Events are in
     * progress
     */
    public final boolean iterating() {
        return numIterators > 0;
    }
    
    /**
     * Returns a new SafeIterator over this EventGroup's Events from highest to
     * lowest priority.
     * @return A new SafeIterator over this EventGroup's Events from highest to
     * lowest priority
     */
    public final SafeIterator<Event<T,U>> iterator() {
        return new EventIterator();
    }
    
    /**
     * Iterates through this EventGroup's Events from highest to lowest priority
     * and performs each of them as part of the specified GameState.
     * @param state The GameState in which to involve this EventGroup's Events
     * when performing them
     */
    public final void perform(U state) {
        T game = state.getGame();
        Iterator<Event<T,U>> eventIterator = iterator();
        while (eventIterator.hasNext()) {
            eventIterator.next().actions(game, state);
        }
    }
    
    /**
     * Adds to this EventGroup one instance of the specified Event at the
     * specified priority.
     * @param event The Event to add
     * @param priority The priority at which to add it
     */
    public final void add(Event<T,U> event, int priority) {
        if (numIterators == 0) {
            entries.add(new Entry<>(event, priority, 0));
            return;
        }
        //subSet(before, after) contains all and only the entries with the specified event and priority
        Entry<T,U> before = new Entry<>(event, priority, -1);
        Entry<T,U> after = new Entry<>(event, priority, 1);
        Iterator<Entry<T,U>> iterator = entriesToRemove.subSet(before, after).iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            return;
        }
        entriesToAdd.add(new Entry<>(event, priority, 0));
    }
    
    /**
     * Removes from this EventGroup one instance of the specified Event at the
     * specified priority, if such an instance exists.
     * @param event The Event to remove
     * @param priority The priority at which to remove it
     * @return Whether the removal occurred
     */
    public final boolean remove(Event<T,U> event, int priority) {
        //subSet(before, after) contains all and only the entries with the specified event and priority
        Entry<T,U> before = new Entry<>(event, priority, -1);
        Entry<T,U> after = new Entry<>(event, priority, 1);
        if (numIterators == 0) {
            Iterator<Entry<T,U>> iterator = entries.subSet(before, after).iterator();
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
                return true;
            }
            return false;
        }
        Iterator<Entry<T,U>> iterator = entriesToAdd.subSet(before, after).iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            return true;
        }
        iterator = entries.subSet(before, after).iterator();
        while (iterator.hasNext()) {
            if (entriesToRemove.add(iterator.next())) {
                return true;
            }
        }
        return false;
    }
    
}
