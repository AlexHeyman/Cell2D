package ironclad2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.newdawn.slick.Graphics;

public abstract class IroncladGameState {
    
    private static IroncladGameState delayState = null;
    private static final List<ThinkerChangeData> thinkersToChange = new LinkedList<>();
    
    private IroncladGame game;
    private int id;
    boolean isActive = false;
    private final Set<AnimationInstance> animInstances = new HashSet<>();
    private final Map<Integer,AnimationInstance> IDInstances = new HashMap<>();
    private final Thinker<IroncladGameState> thinker = new StateThinker();
    private final Set<Thinker> thinkers = new HashSet<>();
    
    public IroncladGameState(IroncladGame game, int id) {
        if (game == null) {
            throw new RuntimeException("Attempted to create an Ironclad game state with no game");
        }
        this.game = game;
        this.id = id;
        game.addState(this);
    }
    
    public final IroncladGame getGame() {
        return game;
    }
    
    public final int getID() {
        return id;
    }
    
    public final boolean isActive() {
        return isActive;
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
    
    private class ThinkerChangeData {
        
        private final Thinker thinker;
        private final IroncladGameState newState;
        
        private ThinkerChangeData(Thinker thinker, IroncladGameState newState) {
            this.thinker = thinker;
            this.newState = newState;
        }
        
    }
    
    final void addThinker(Thinker thinker) {
        if (delayState != null && (delayState == this || delayState == thinker.state)) {
            thinkersToChange.add(new ThinkerChangeData(thinker, this));
        } else {
            addActions(game, thinker);
        }
    }
    
    private void addActions(IroncladGame game, Thinker thinker) {
        thinkers.add(thinker);
        thinker.state = this;
        thinker.addedActions(game, thinker.state);
    }
    
    final boolean removeThinker(Thinker thinker) {
        if (thinker.newState == this) {
            thinker.newState = null;
            if (delayState != null && (delayState == this || delayState == thinker.state)) {
                thinkersToChange.add(new ThinkerChangeData(thinker, null));
            } else {
                removeActions(game, thinker);
            }
            return true;
        }
        return false;
    }
    
    private void removeActions(IroncladGame game, Thinker thinker) {
        thinker.removedActions(game, thinker.state);
        thinkers.remove(thinker);
        thinker.state = null;
    }
    
    private void changeThinkers(IroncladGame game) {
        while (!thinkersToChange.isEmpty()) {
            List<ThinkerChangeData> newChanges = new ArrayList<>(thinkersToChange);
            thinkersToChange.clear();
            for (ThinkerChangeData data : newChanges) {
                if (data.thinker.state != null) {
                    data.thinker.state.removeActions(game, data.thinker);
                }
                if (data.newState != null) {
                    data.newState.addActions(game, data.thinker);
                }
            }
        }
    }
    
    final void doStep(IroncladGame game) {
        delayState = this;
        double timeFactor = getTimeFactor();
        if (timeFactor > 0) {
            for (AnimationInstance instance : animInstances) {
                instance.update(timeFactor);
            }
            for (Thinker listThinker : thinkers) {
                listThinker.update(game, timeFactor);
            }
            changeThinkers(game);
            thinker.update(game, 1);
            changeThinkers(game);
        }
        for (Thinker listThinker : thinkers) {
            listThinker.stepActions(game, listThinker.state);
        }
        changeThinkers(game);
        delayState = null;
        thinker.stepActions(game, thinker.state);
    }
    
    public final double getTimeFactor() {
        return thinker.getTimeFactor();
    }
    
    public final void setTimeFactor(double timeFactor) {
        thinker.setTimeFactor(timeFactor);
    }
    
    public void timeUnitActions(IroncladGame game) {}
    
    public final int getTimerValue(TimedEvent<IroncladGameState> timedEvent) {
        return thinker.getTimerValue(timedEvent);
    }
    
    public final void setTimerValue(TimedEvent<IroncladGameState> timedEvent, int value) {
        thinker.setTimerValue(timedEvent, value);
    }
    
    public void stepActions(IroncladGame game) {}
    
    private class StateThinker extends Thinker<IroncladGameState> {
        
        private StateThinker() {}
        
        @Override
        public final void timeUnitActions(IroncladGame game, IroncladGameState state) {
            IroncladGameState.this.timeUnitActions(game);
        }
        
        @Override
        public final void stepActions(IroncladGame game, IroncladGameState state) {
            IroncladGameState.this.stepActions(game);
        }
        
    }
    
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
