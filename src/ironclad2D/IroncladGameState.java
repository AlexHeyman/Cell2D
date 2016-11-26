package ironclad2D;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.newdawn.slick.Graphics;

public abstract class IroncladGameState<T extends IroncladGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
    
    private boolean initialized = false;
    private final T thisState;
    private IroncladGame game;
    private int id;
    boolean isActive = false;
    private double timeFactor = 1;
    private boolean performingStepActions = false;
    private final Set<AnimationInstance> animInstances = new HashSet<>();
    private final Map<Integer,AnimationInstance> IDInstances = new HashMap<>();
    private final Set<U> thinkers = new HashSet<>();
    private int thinkerIterators = 0;
    private final Queue<ThinkerChangeData<T,U,V>> thinkerChanges = new LinkedList<>();
    private boolean changingThinkers = false;
    
    public IroncladGameState(IroncladGame game, int id) {
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
    
    public final IroncladGame getGame() {
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
        if (instance == null) {
            return AnimationInstance.BLANK;
        }
        return instance;
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
            animInstances.add(instance);
            instance.state = this;
            AnimationInstance oldInstance = IDInstances.put(id, instance);
            if (oldInstance != null) {
                animInstances.remove(oldInstance);
                oldInstance.state = null;
            }
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
        animInstances.add(instance);
        instance.state = this;
        AnimationInstance oldInstance = IDInstances.put(id, instance);
        if (oldInstance != null) {
            animInstances.remove(oldInstance);
            oldInstance.state = null;
        }
        return instance;
    }
    
    public final void clearAnimInstances() {
        for (AnimationInstance instance : animInstances) {
            instance.state = null;
        }
        animInstances.clear();
        IDInstances.clear();
    }
    
    public class ThinkerIterator implements Iterator<U> {
        
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
        
        public final boolean isFinished() {
            return finished;
        }
        
        public final void finish() {
            if (!finished) {
                finished = true;
                thinkerIterators--;
                changeThinkers();
            }
        }
        
    }
    
    public final ThinkerIterator thinkerIterator() {
        return new ThinkerIterator();
    }
    
    private static class ThinkerChangeData<T extends IroncladGameState<T,U,V>, U extends Thinker<T,U,V>, V extends ThinkerState<T,U,V>> {
        
        private boolean used = false;
        private final U thinker;
        private final T newState;
        
        private ThinkerChangeData(U thinker, T newState) {
            this.thinker = thinker;
            this.newState = newState;
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
            thinker.step(game, thisState);
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
    
    private void addThinkerChangeData(U thinker, T newState) {
        thinker.newState = newState;
        ThinkerChangeData<T,U,V> data = new ThinkerChangeData<>(thinker, newState);
        if (thinker.state != null) {
            IroncladGameState<T,U,V> state = thinker.state;
            state.thinkerChanges.add(data);
            state.changeThinkers();
        }
        if (newState != null) {
            IroncladGameState<T,U,V> state = newState;
            state.thinkerChanges.add(data);
            state.changeThinkers();
        }
    }
    
    private void changeThinkers() {
        if (thinkerIterators == 0 && !changingThinkers) {
            changingThinkers = true;
            while (!thinkerChanges.isEmpty()) {
                ThinkerChangeData<T,U,V> data = thinkerChanges.remove();
                if (!data.used) {
                    data.used = true;
                    IroncladGameState<T,U,V> thinkerState = data.thinker.state;
                    if (thinkerState != null) {
                        thinkerState.removeActions(data.thinker);
                    }
                    IroncladGameState<T,U,V> newState = data.newState;
                    if (newState != null) {
                        newState.addActions(data.thinker);
                    }
                }
            }
            changingThinkers = false;
        }
    }
    
    final void doStep() {
        double currentTimeFactor = timeFactor;
        if (currentTimeFactor > 0) {
            for (AnimationInstance instance : animInstances) {
                instance.update(currentTimeFactor);
            }
            Iterator<U> iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().update(game, currentTimeFactor);
            }
        }
        performingStepActions = true;
        Iterator<U> iterator = thinkerIterator();
        while (iterator.hasNext()) {
            iterator.next().step(game, thisState);
        }
        stepActions(game);
        performingStepActions = false;
    }
    
    public void addThinkerActions(IroncladGame game, U thinker) {}
    
    public void removeThinkerActions(IroncladGame game, U thinker) {}
    
    public void stepActions(IroncladGame game) {}
    
    public void renderActions(IroncladGame game, Graphics g, int x1, int y1, int x2, int y2) {}
    
    public void enteredActions(IroncladGame game) {}
    
    public void leftActions(IroncladGame game) {}
    
    public void charTyped(char c) {}
    
    public void charDeleted(char c) {}
    
    public void stringBegan(String s) {}
    
    public void stringTyped(String s) {}
    
    public void stringDeleted(String s) {}
    
    public void stringCanceled(String s) {}
    
}
