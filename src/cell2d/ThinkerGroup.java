package cell2d;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class ThinkerGroup<T extends CellGame, U extends CellGameState<T,U,V>, V extends Thinker<T,U,V>> {
    
    private static abstract class GroupComparator<T> implements Comparator<T>, Serializable {}
    
    private final Comparator<V> actionPriorityComparator = new GroupComparator<V>() {
        
        @Override
        public final int compare(V thinker1, V thinker2) {
            int priorityDifference = thinker2.actionPriority - thinker1.actionPriority;
            return (priorityDifference == 0 ? Long.signum(thinker2.id - thinker1.id) : priorityDifference);
        }
        
    };
    
    private final SortedSet<V> thinkers = new TreeSet<>(actionPriorityComparator);
    private int thinkerIterators = 0;
    private final Queue<ThinkerChangeData<T,U,V>> thinkerChanges = new LinkedList<>();
    private boolean updatingThinkerList = false;
    
    /**
     * Returns the number of Thinkers that are currently assigned to this
     * CellGameState.
     * @return The number of Thinkers that are currently assigned to this
     * CellGameState
     */
    public final int getNumThinkers() {
        return thinkers.size();
    }
    
    private class ThinkerIterator implements SafeIterator<V> {
        
        private boolean stopped = false;
        private final Iterator<V> iterator = thinkers.iterator();
        private V lastThinker = null;
        
        private ThinkerIterator() {
            thinkerIterators++;
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
                return null;
            }
            lastThinker = iterator.next();
            return lastThinker;
        }
        
        @Override
        public final void remove() {
            if (!stopped && lastThinker != null) {
                removeThinker(lastThinker);
                lastThinker = null;
            }
        }
        
        @Override
        public final void stop() {
            if (!stopped) {
                stopped = true;
                thinkerIterators--;
                updateThinkerList();
            }
        }
        
    }
    
    /**
     * Returns whether any Iterators over this CellGameState's list of Thinkers
     * are currently in progress.
     * @return Whether any Iterators over this CellGameState's list of Thinkers
     * are currently in progress
     */
    public final boolean iteratingThroughThinkers() {
        return thinkerIterators > 0;
    }
    
    /**
     * Returns a new Iterator over this CellGameState's list of Thinkers.
     * @return A new Iterator over this CellGameState's list of Thinkers
     */
    public final SafeIterator<V> thinkerIterator() {
        return new ThinkerIterator();
    }
    
    private static class ThinkerChangeData<T extends CellGame, U extends CellGameState<T,U,V>, V extends Thinker<T,U,V>> {
        
        private boolean used = false;
        private final boolean changePriority;
        private final V thinker;
        private final ThinkerGroup<T,U,V> newGroup;
        private final int actionPriority;
        
        private ThinkerChangeData(V thinker, ThinkerGroup<T,U,V> newGroup) {
            changePriority = false;
            this.thinker = thinker;
            this.newGroup = newGroup;
            actionPriority = 0;
        }
        
        private ThinkerChangeData(V thinker, int actionPriority) {
            changePriority = true;
            this.thinker = thinker;
            newGroup = null;
            this.actionPriority = actionPriority;
        }
        
    }
    
    /**
     * Adds the specified Thinker to this CellGameState if it is not already
     * assigned to a CellGameState.
     * @param thinker The Thinker to be added
     * @return Whether the addition occurred
     */
    public final boolean addThinker(V thinker) {
        if (thinker.newGroup == null) {
            ThinkerGroup ancestor = this;
            do {
                if (ancestor == thinker) {
                    return false;
                } else if (ancestor instanceof Thinker) {
                    ancestor = ((Thinker)ancestor).newGroup;
                } else {
                    break;
                }
            } while (ancestor != null);
            addThinkerChangeData(thinker, this);
            return true;
        }
        return false;
    }
    
    /**
     * Removes the specified Thinker from this CellGameState if it is currently
     * assigned to it.
     * @param thinker The Thinker to be removed
     * @return Whether the removal occurred
     */
    public final boolean removeThinker(V thinker) {
        if (thinker.newGroup == this) {
            addThinkerChangeData(thinker, null);
            return true;
        }
        return false;
    }
    
    final void changeThinkerActionPriority(V thinker, int actionPriority) {
        thinkerChanges.add(new ThinkerChangeData<>(thinker, actionPriority));
        updateThinkerList();
    }
    
    /**
     * Removes from this SpaceState all of the SpaceObjects that are currently
     * assigned to it.
     */
    public final void removeAllThinkers() {
        for (V thinker : thinkers) {
            if (thinker.newGroup == this) {
                thinker.newGroup = null;
                thinkerChanges.add(new ThinkerChangeData(thinker, null));
            }
        }
        updateThinkerList();
    }
    
    public final void removeAllSubThinkers() {
        if (!thinkers.isEmpty()) {
            for (V thinker : thinkers) {
                if (thinker.newGroup == this) {
                    thinker.removeAllSubThinkers();
                    thinker.newGroup = null;
                    thinkerChanges.add(new ThinkerChangeData(thinker, null));
                }
            }
            updateThinkerList();
        }
    }
    
    public final boolean removeLineage(V thinker) {
        List<V> lineage = new ArrayList<>();
        do {
            V parent = thinker.getSuperThinker();
            if ((parent == null && thinker.newGroup != this)
                    || (parent != null && thinker.newGroup != parent)) {
                return false;
            }
            lineage.add(thinker);
            thinker = parent;
        } while (thinker != null && thinker != this);
        thinker = lineage.get(0);
        thinker.removeAllSubThinkers();
        for (int i = 1; i < lineage.size(); i++) {
            V group = lineage.get(i);
            group.removeThinker(thinker);
            thinker = group;
        }
        removeThinker(thinker);
        return true;
    }
    
    private void addThinkerChangeData(V thinker, ThinkerGroup<T,U,V> newGroup) {
        thinker.newGroup = newGroup;
        ThinkerChangeData<T,U,V> data = new ThinkerChangeData<>(thinker, newGroup);
        if (thinker.group != null) {
            thinker.group.thinkerChanges.add(data);
            thinker.group.updateThinkerList();
        }
        if (newGroup != null) {
            newGroup.thinkerChanges.add(data);
            newGroup.updateThinkerList();
        }
    }
    
    private void addActions(V thinker) {
        thinkers.add(thinker);
        thinker.group = this;
        addThinkerActions(thinker);
        thinker.added();
    }
    
    /**
     * Actions for this CellGameState to take immediately after adding a Thinker
     * to itself.
     * @param thinker The Thinker that was added
     */
    public void addThinkerActions(V thinker) {}
    
    private void removeActions(V thinker) {
        thinker.removed();
        removeThinkerActions(thinker);
        thinkers.remove(thinker);
        thinker.group = null;
    }
    
    /**
     * Actions for this CellGameState to take immediately before removing a
     * Thinker from itself.
     * @param thinker The Thinker that is about to be removed
     */
    public void removeThinkerActions(V thinker) {}
    
    private void updateThinkerList() {
        if (thinkerIterators == 0 && !updatingThinkerList) {
            updatingThinkerList = true;
            while (!thinkerChanges.isEmpty()) {
                ThinkerChangeData<T,U,V> data = thinkerChanges.remove();
                if (!data.used) {
                    data.used = true;
                    if (data.changePriority) {
                        if (data.thinker.group == null) {
                            data.thinker.actionPriority = data.actionPriority;
                        } else {
                            thinkers.remove(data.thinker);
                            data.thinker.actionPriority = data.actionPriority;
                            thinkers.add(data.thinker);
                        }
                    } else {
                        ThinkerGroup<T,U,V> currentGroup = data.thinker.group;
                        if (currentGroup != null) {
                            currentGroup.removeActions(data.thinker);
                        }
                        if (data.newGroup != null) {
                            data.newGroup.addActions(data.thinker);
                        }
                    }
                }
            }
            updatingThinkerList = false;
            updateThinkerListActions();
        }
    }
    
    /**
     * Actions for this CellGameState to take immediately after updating its
     * list of Thinkers.
     */
    public void updateThinkerListActions() {}
    
}
