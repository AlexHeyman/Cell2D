package cell2D;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.newdawn.slick.Graphics;

public abstract class CellGameState<T extends CellGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
    
    private static abstract class StateComparator<T> implements Comparator<T>, Serializable {}
    
    private static final Comparator<Thinker> actionPriorityComparator = new StateComparator<Thinker>() {
        
        @Override
        public final int compare(Thinker thinker1, Thinker thinker2) {
            int priorityDifference = thinker1.actionPriority - thinker2.actionPriority;
            return (priorityDifference == 0 ? Long.signum(thinker1.id - thinker2.id) : priorityDifference);
        }
        
    };
    
    private boolean initialized = false;
    private final T thisState;
    private CellGame game;
    private int id;
    boolean isActive = false;
    private double timeFactor = 1;
    private boolean performingStepActions = false;
    private final Set<AnimationInstance> animInstances = new HashSet<>();
    private final Map<Integer,AnimationInstance> IDInstances = new HashMap<>();
    private final SortedSet<U> thinkers = new TreeSet<>(actionPriorityComparator);
    private int thinkerIterators = 0;
    private final Queue<ThinkerChangeData<T,U,V>> thinkerChanges = new LinkedList<>();
    private boolean updatingThinkerList = false;
    
    public CellGameState(CellGame game, int id) {
        if (game == null) {
            throw new RuntimeException("Attempted to create an Ironclad game state with no game");
        }
        this.game = game;
        this.id = id;
        game.addState(this);
        thisState = getThis();
        initialized = true;
    }
    
    public abstract T getThis();
    
    public final CellGame getGame() {
        return game;
    }
    
    public final int getID() {
        return id;
    }
    
    public final boolean isActive() {
        return isActive;
    }
    
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    public final void setTimeFactor(double timeFactor) {
        if (timeFactor < 0) {
            throw new RuntimeException("Attempted to give a game state a negative time factor");
        }
        this.timeFactor = timeFactor;
    }
    
    public final boolean addAnimInstance(AnimationInstance instance) {
        if (instance == AnimationInstance.BLANK) {
            return true;
        }
        if (instance.state == null) {
            animInstances.add(instance);
            instance.state = this;
            return true;
        }
        return false;
    }
    
    public final AnimationInstance addAnimation(Animation animation) {
        if (animation == Animation.BLANK) {
            return AnimationInstance.BLANK;
        }
        AnimationInstance instance = new AnimationInstance(animation);
        animInstances.add(instance);
        instance.state = this;
        return instance;
    }
    
    public final boolean removeAnimInstance(AnimationInstance instance) {
        if (instance == AnimationInstance.BLANK) {
            return true;
        }
        if (instance.state == this) {
            animInstances.remove(instance);
            instance.state = null;
            return true;
        }
        return false;
    }
    
    public final AnimationInstance getAnimInstance(int id) {
        AnimationInstance instance = IDInstances.get(id);
        return (instance == null ? AnimationInstance.BLANK : instance);
    }
    
    public final Animation getAnimation(int id) {
        return getAnimInstance(id).getAnimation();
    }
    
    public final boolean setAnimInstance(int id, AnimationInstance instance) {
        if (instance == AnimationInstance.BLANK) {
            AnimationInstance oldInstance = IDInstances.remove(id);
            if (oldInstance != null) {
                oldInstance.state = null;
            }
            return true;
        }
        if (instance.state == null) {
            AnimationInstance oldInstance = IDInstances.put(id, instance);
            if (oldInstance != null) {
                animInstances.remove(oldInstance);
                oldInstance.state = null;
            }
            animInstances.add(instance);
            instance.state = this;
            return true;
        }
        return false;
    }
    
    public final AnimationInstance setAnimation(int id, Animation animation) {
        if (animation == Animation.BLANK) {
            AnimationInstance oldInstance = IDInstances.remove(id);
            if (oldInstance != null) {
                oldInstance.state = null;
            }
            return AnimationInstance.BLANK;
        }
        AnimationInstance instance = new AnimationInstance(animation);
        AnimationInstance oldInstance = IDInstances.put(id, instance);
        if (oldInstance != null) {
            animInstances.remove(oldInstance);
            oldInstance.state = null;
        }
        animInstances.add(instance);
        instance.state = this;
        return instance;
    }
    
    public final void clearAnimInstances() {
        for (AnimationInstance instance : animInstances) {
            instance.state = null;
        }
        animInstances.clear();
        IDInstances.clear();
    }
    
    private class ThinkerIterator implements SafeIterator<U> {
        
        private boolean finished = false;
        private final Iterator<U> iterator = thinkers.iterator();
        private U lastThinker = null;
        
        private ThinkerIterator() {
            thinkerIterators++;
        }
        
        @Override
        public final boolean hasNext() {
            if (finished) {
                return false;
            }
            boolean hasNext = iterator.hasNext();
            if (!hasNext) {
                finish();
            }
            return hasNext;
        }
        
        @Override
        public final U next() {
            if (finished) {
                return null;
            }
            lastThinker = iterator.next();
            return lastThinker;
        }
        
        @Override
        public final void remove() {
            if (!finished && lastThinker != null) {
                removeThinker(lastThinker);
                lastThinker = null;
            }
        }
        
        @Override
        public final boolean isFinished() {
            return finished;
        }
        
        @Override
        public final void finish() {
            if (!finished) {
                finished = true;
                thinkerIterators--;
                updateThinkerList();
            }
        }
        
    }
    
    public final boolean iteratingThroughThinkers() {
        return thinkerIterators > 0;
    }
    
    public final SafeIterator<U> thinkerIterator() {
        return new ThinkerIterator();
    }
    
    private static class ThinkerChangeData<T extends CellGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
        
        private boolean used = false;
        private final boolean changePriority;
        private final U thinker;
        private final T newState;
        private final int actionPriority;
        
        private ThinkerChangeData(U thinker, T newState) {
            changePriority = false;
            this.thinker = thinker;
            this.newState = newState;
            actionPriority = 0;
        }
        
        private ThinkerChangeData(U thinker, int actionPriority) {
            changePriority = true;
            this.thinker = thinker;
            newState = null;
            this.actionPriority = actionPriority;
        }
        
    }
    
    public final boolean addThinker(U thinker) {
        if (initialized && thinker.newState == null) {
            addThinkerChangeData(thinker, thisState);
            return true;
        }
        return false;
    }
    
    private void addActions(U thinker) {
        thinkers.add(thinker);
        thinker.state = thisState;
        thinker.addActions();
        thinker.addedActions(game, thisState);
        addThinkerActions(game, thinker);
        if (performingStepActions) {
            thinker.doStep(game, thisState);
        }
    }
    
    public final boolean removeThinker(U thinker) {
        if (initialized && thinker.newState == thisState) {
            addThinkerChangeData(thinker, null);
            return true;
        }
        return false;
    }
    
    private void removeActions(U thinker) {
        removeThinkerActions(game, thinker);
        thinker.removedActions(game, thisState);
        thinkers.remove(thinker);
        thinker.state = null;
    }
    
    final void changeThinkerActionPriority(U thinker, int actionPriority) {
        thinkerChanges.add(new ThinkerChangeData<>(thinker, actionPriority));
        updateThinkerList();
    }
    
    private void addThinkerChangeData(U thinker, T newState) {
        thinker.newState = newState;
        ThinkerChangeData<T,U,V> data = new ThinkerChangeData<>(thinker, newState);
        if (thinker.state != null) {
            CellGameState<T,U,V> state = thinker.state;
            state.thinkerChanges.add(data);
            state.updateThinkerList();
        }
        if (newState != null) {
            CellGameState<T,U,V> state = newState;
            state.thinkerChanges.add(data);
            state.updateThinkerList();
        }
    }
    
    private void updateThinkerList() {
        if (thinkerIterators == 0 && !updatingThinkerList) {
            updatingThinkerList = true;
            while (!thinkerChanges.isEmpty()) {
                ThinkerChangeData<T,U,V> data = thinkerChanges.remove();
                if (!data.used) {
                    data.used = true;
                    if (data.changePriority) {
                        if (data.thinker.state == null) {
                            data.thinker.actionPriority = data.actionPriority;
                        } else {
                            thinkers.remove(data.thinker);
                            data.thinker.actionPriority = data.actionPriority;
                            thinkers.add(data.thinker);
                        }
                    } else {
                        CellGameState<T,U,V> thinkerState = data.thinker.state;
                        if (thinkerState != null) {
                            thinkerState.removeActions(data.thinker);
                        }
                        CellGameState<T,U,V> newState = data.newState;
                        if (newState != null) {
                            newState.addActions(data.thinker);
                        }
                    }
                }
            }
            updatingThinkerList = false;
            updateThinkerListActions(game);
        }
    }
    
    final void doStep() {
        if (timeFactor > 0) {
            for (AnimationInstance instance : animInstances) {
                instance.update();
            }
            Iterator<U> iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().update(game);
            }
        }
        performingStepActions = true;
        Iterator<U> iterator = thinkerIterator();
        while (iterator.hasNext()) {
            iterator.next().doStep(game, thisState);
        }
        stepActions(game);
        performingStepActions = false;
    }
    
    public void addThinkerActions(CellGame game, U thinker) {}
    
    public void removeThinkerActions(CellGame game, U thinker) {}
    
    public void updateThinkerListActions(CellGame game) {}
    
    public void stepActions(CellGame game) {}
    
    public void renderActions(CellGame game, Graphics g, int x1, int y1, int x2, int y2) {}
    
    public void enteredActions(CellGame game) {}
    
    public void leftActions(CellGame game) {}
    
    public void charTyped(char c) {}
    
    public void charDeleted(char c) {}
    
    public void stringBegan(String s) {}
    
    public void stringTyped(String s) {}
    
    public void stringDeleted(String s) {}
    
    public void stringCanceled(String s) {}
    
}
