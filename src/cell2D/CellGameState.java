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

/**
 * <p>A CellGameState represents one state that a CellGame can be in, such as
 * the main menu, the options menu, in the middle of a level, etc.
 * CellGameStates are permanent parts of a specific CellGame and referenced by a
 * specific non-negative integer ID, both of which are specified upon the
 * CellGameState's creation.</p>
 * 
 * <p>A CellGameState has its own actions to take every time its CellGame
 * executes a frame and in response to specific events, but it only takes these
 * actions while the CellGame is in that state and it is thus active.</p>
 * 
 * <p>AnimationInstances may be assigned to one CellGameState each. An
 * AnimationInstance's assigned CellGameState will keep track of time for it,
 * thus allowing it to actually animate, while the CellGameState is active. An
 * AnimationInstance may be assigned to a CellGameState with or without an
 * integer ID in the context of that CellGameState. Only one AnimationInstance
 * may be assigned to a given CellGameState with a given ID at once.</p>
 * 
 * <p>Thinkers may be assigned to one CellGameState each. A Thinker's assigned
 * CellGameState will keep track of time for it, thus allowing it to take its
 * own time-dependent actions, while the CellGameState is active. Because a
 * CellGameState's internal list of Thinkers cannot be modified while it is
 * being iterated through, the actual addition or removal of a Thinker to or
 * from a CellGameState is delayed until all of its Thinkers have completed
 * their timeUnitActions() or frameActions() if the CellGameState was instructed
 * to add or remove the Thinker during those periods. Multiple delayed
 * instructions may be successfully given to CellGameStates regarding the same
 * Thinker without having to wait until the end of one of those periods.</p>
 * 
 * <p>The CellGameState class is intended to be directly extended by classes U
 * that extend CellGameState&lt;T,U,V,W&gt; and interact with Thinkers of class
 * V and ThinkerStates of class W. BasicGameState is an example of such a class.
 * This allows a CellGameState's Thinkers and their ThinkerStates to interact
 * with it in ways unique to its subclass of CellGameState.</p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses this CellGameState
 * @param <U> The subclass of CellGameState that this CellGameState is
 * @param <V> The subclass of Thinker that this CellGameState uses
 * @param <W> The subclass of ThinkerState that this CellGameState's Thinkers
 * use
 */
public abstract class CellGameState<T extends CellGame, U extends CellGameState<T,U,V,W>, V extends Thinker<T,U,V,W>, W extends ThinkerState<T,U,V,W>> {
    
    private static abstract class StateComparator<T> implements Comparator<T>, Serializable {}
    
    private static final Comparator<Thinker> actionPriorityComparator = new StateComparator<Thinker>() {
        
        @Override
        public final int compare(Thinker thinker1, Thinker thinker2) {
            int priorityDifference = thinker2.actionPriority - thinker1.actionPriority;
            return (priorityDifference == 0 ? Long.signum(thinker2.id - thinker1.id) : priorityDifference);
        }
        
    };
    
    private boolean initialized = false;
    private final U thisState;
    private T game;
    private int id;
    boolean active = false;
    private double timeFactor = 1;
    private boolean takingFrameActions = false;
    private final Set<AnimationInstance> animInstances = new HashSet<>();
    private final Map<Integer,AnimationInstance> IDInstances = new HashMap<>();
    private final SortedSet<V> thinkers = new TreeSet<>(actionPriorityComparator);
    private int thinkerIterators = 0;
    private final Queue<ThinkerChangeData<T,U,V,W>> thinkerChanges = new LinkedList<>();
    private boolean updatingThinkerList = false;
    
    /**
     * Creates a new CellGameState of the specified CellGame with the specified
     * ID. CellGameStates automatically register themselves with their CellGames
     * upon creation.
     * @param game The CellGame to which this CellGameState belongs
     * @param id This CellGameState's ID
     */
    public CellGameState(T game, int id) {
        if (game == null) {
            throw new RuntimeException("Attempted to create a CellGameState with no CellGame");
        }
        this.game = game;
        this.id = id;
        game.addState(this);
        thisState = getThis();
        initialized = true;
    }
    
    /**
     * A method which returns this CellGameState as a U, rather than as a
     * CellGameState&lt;T,U,V,W&gt;. This must be implemented somewhere in the
     * lineage of every subclass of CellGameState in order to get around Java's
     * limitations with regard to generic types.
     * @return This CellGameState as a U
     */
    public abstract U getThis();
    
    /**
     * Returns the CellGame to which this CellGameState belongs.
     * @return The CellGame to which this CellGameState belongs
     */
    public final T getGame() {
        return game;
    }
    
    /**
     * Returns this CellGameState's ID.
     * @return This CellGameState's ID
     */
    public final int getID() {
        return id;
    }
    
    /**
     * Returns whether this CellGameState is active - that is, whether its
     * CellGame is currently in this state.
     * @return Whether this CellGameState is active
     */
    public final boolean isActive() {
        return active;
    }
    
    /**
     * Returns this CellGameState's time factor; that is, how many time units it
     * experiences every frame.
     * @return This CellGameState's time factor
     */
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    /**
     * Sets this CellGameState's time factor to the specified value.
     * @param timeFactor The new time factor
     */
    public final void setTimeFactor(double timeFactor) {
        if (timeFactor < 0) {
            throw new RuntimeException("Attempted to give a CellGameState a negative time factor");
        }
        this.timeFactor = timeFactor;
    }
    
    /**
     * Returns the number of AnimationInstances that are currently assigned to
     * this CellGameState.
     * @return The number of AnimationInstances that are currently assigned to
     * this CellGameState
     */
    public final int getNumAnimInstances() {
        return animInstances.size();
    }
    
    /**
     * Adds the specified AnimationInstance to this CellGameState if it is not
     * already assigned to a CellGameState.
     * @param instance The AnimationInstance to add
     * @return Whether the addition occurred
     */
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
    
    /**
     * Adds a new AnimationInstance of the specified Animation to this
     * CellGameState.
     * @param animation The Animation to add a new AnimationInstance of
     * @return The new AnimationInstance
     */
    public final AnimationInstance addAnimInstance(Animation animation) {
        if (animation == Animation.BLANK) {
            return AnimationInstance.BLANK;
        }
        AnimationInstance instance = new AnimationInstance(animation);
        animInstances.add(instance);
        instance.state = this;
        return instance;
    }
    
    /**
     * Removes the specified AnimationInstance from this CellGameState if it
     * is currently assigned to this CellGameState.
     * @param instance The AnimationInstance to remove
     * @return Whether the removal occurred
     */
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
    
    /**
     * Returns the AnimationInstance that is assigned to this CellGameState with
     * the specified ID.
     * @param id The ID of the AnimationInstance to be returned
     * @return The AnimationInstance that is assigned to this CellGameState with
     * the specified ID
     */
    public final AnimationInstance getAnimInstance(int id) {
        AnimationInstance instance = IDInstances.get(id);
        return (instance == null ? AnimationInstance.BLANK : instance);
    }
    
    /**
     * Sets the AnimationInstance that is assigned to this CellGameState with
     * the specified ID to the specified AnimationInstance, if it is not already
     * assigned to a CellGameState. If there is already an AnimationInstance
     * assigned with the specified ID, it will be removed from this
     * CellGameState.
     * @param id The ID with which to assign the specified AnimationInstance
     * @param instance The AnimationInstance to add with the specified ID
     * @return Whether the addition occurred
     */
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
    
    /**
     * Returns the Animation of the AnimationInstance assigned to this
     * CellGameState with the specified ID, if there is one.
     * @param id The ID of the AnimationInstance whose Animation is to be
     * returned
     * @return The Animation of the AnimationInstance assigned to this
     * CellGameState with the specified ID
     */
    public final Animation getAnimation(int id) {
        AnimationInstance instance = IDInstances.get(id);
        return (instance == null ? Animation.BLANK : instance.getAnimation());
    }
    
    /**
     * Sets the AnimationInstance that is assigned to this CellGameState with
     * the specified ID to a new AnimationInstance of the specified Animation,
     * if there is not already an AnimationInstance of that Animation assigned
     * with that ID. In other words, this method will not replace an
     * AnimationInstance with another of the same Animation. If there is already
     * an AnimationInstance assigned with the specified ID, it will be removed
     * from this CellGameState.
     * @param id The ID with which to assign the new AnimationInstance
     * @param animation The Animation to add a new AnimationInstance of
     * @return The new AnimationInstance
     */
    public final AnimationInstance setAnimation(int id, Animation animation) {
        AnimationInstance instance = getAnimInstance(id);
        if (instance.getAnimation() != animation) {
            if (animation == Animation.BLANK) {
                AnimationInstance oldInstance = IDInstances.remove(id);
                if (oldInstance != null) {
                    animInstances.remove(oldInstance);
                    oldInstance.state = null;
                }
                return AnimationInstance.BLANK;
            }
            instance = new AnimationInstance(animation);
            AnimationInstance oldInstance = IDInstances.put(id, instance);
            if (oldInstance != null) {
                animInstances.remove(oldInstance);
                oldInstance.state = null;
            }
            animInstances.add(instance);
            instance.state = this;
        }
        return instance;
    }
    
    /**
     * Removes from this CellGameState all AnimationInstances that are currently
     * assigned to it, with or without IDs.
     */
    public final void clearAnimInstances() {
        for (AnimationInstance instance : animInstances) {
            instance.state = null;
        }
        animInstances.clear();
        IDInstances.clear();
    }
    
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
    
    private static class ThinkerChangeData<T extends CellGame, U extends CellGameState<T,U,V,W>, V extends Thinker<T,U,V,W>, W extends ThinkerState<T,U,V,W>> {
        
        private boolean used = false;
        private final boolean changePriority;
        private final V thinker;
        private final U newState;
        private final int actionPriority;
        
        private ThinkerChangeData(V thinker, U newState) {
            changePriority = false;
            this.thinker = thinker;
            this.newState = newState;
            actionPriority = 0;
        }
        
        private ThinkerChangeData(V thinker, int actionPriority) {
            changePriority = true;
            this.thinker = thinker;
            newState = null;
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
        if (initialized && thinker.newState == null) {
            addThinkerChangeData(thinker, thisState);
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
        if (initialized && thinker.newState == thisState) {
            addThinkerChangeData(thinker, null);
            return true;
        }
        return false;
    }
    
    final void changeThinkerActionPriority(V thinker, int actionPriority) {
        thinkerChanges.add(new ThinkerChangeData<>(thinker, actionPriority));
        updateThinkerList();
    }
    
    private void addThinkerChangeData(V thinker, U newState) {
        thinker.newState = newState;
        ThinkerChangeData<T,U,V,W> data = new ThinkerChangeData<>(thinker, newState);
        if (thinker.state != null) {
            CellGameState<T,U,V,W> state = thinker.state;
            state.thinkerChanges.add(data);
            state.updateThinkerList();
        }
        if (newState != null) {
            CellGameState<T,U,V,W> state = newState;
            state.thinkerChanges.add(data);
            state.updateThinkerList();
        }
    }
    
    private void addActions(V thinker) {
        thinkers.add(thinker);
        thinker.state = thisState;
        thinker.addActions();
        thinker.addedActions(game, thisState);
        addThinkerActions(game, thinker);
        if (takingFrameActions) {
            thinker.doFrame(game, thisState);
        }
    }
    
    /**
     * Actions for this CellGameState to take immediately after adding a Thinker
     * to itself.
     * @param game This CellGameState's CellGame
     * @param thinker The Thinker that was added
     */
    public void addThinkerActions(T game, V thinker) {}
    
    private void removeActions(V thinker) {
        removeThinkerActions(game, thinker);
        thinker.removedActions(game, thisState);
        thinkers.remove(thinker);
        thinker.state = null;
    }
    
    /**
     * Actions for this CellGameState to take immediately before removing a
     * Thinker from itself.
     * @param game This CellGameState's CellGame
     * @param thinker The Thinker that is about to be removed
     */
    public void removeThinkerActions(T game, V thinker) {}
    
    private void updateThinkerList() {
        if (thinkerIterators == 0 && !updatingThinkerList) {
            updatingThinkerList = true;
            while (!thinkerChanges.isEmpty()) {
                ThinkerChangeData<T,U,V,W> data = thinkerChanges.remove();
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
                        CellGameState<T,U,V,W> thinkerState = data.thinker.state;
                        if (thinkerState != null) {
                            thinkerState.removeActions(data.thinker);
                        }
                        CellGameState<T,U,V,W> newState = data.newState;
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
    
    /**
     * Actions for this CellGameState to take immediately after updating its
     * list of Thinkers.
     * @param game This CellGameState's CellGame
     */
    public void updateThinkerListActions(T game) {}
    
    final void frame() {
        if (timeFactor > 0) {
            for (AnimationInstance instance : animInstances) {
                instance.update();
            }
            Iterator<V> iterator = thinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().update(game);
            }
        }
        takingFrameActions = true;
        Iterator<V> iterator = thinkerIterator();
        while (iterator.hasNext()) {
            iterator.next().doFrame(game, thisState);
        }
        frameActions(game);
        takingFrameActions = false;
    }
    
    /**
     * Actions for this CellGameState to take once every frame, after all of its
     * Thinkers have taken their frameActions().
     * @param game This CellGameState's CellGame
     */
    public void frameActions(T game) {}
    
    final void render(Graphics g, int x1, int y1, int x2, int y2) {
        renderActions(game, g, x1, y1, x2, y2);
    }
    
    /**
     * Actions for this CellGameState to take each frame to render its visuals.
     * @param game This CellGameState's CellGame
     * @param g The Graphics context to which this CellGameState is rendering
     * itself this frame
     * @param x1 The x-coordinate in pixels of the screen's left edge on the
     * Graphics context
     * @param y1 The y-coordinate in pixels of the screen's top edge on the
     * Graphics context
     * @param x2 The x-coordinate in pixels of the screen's right edge on the
     * screen on the Graphics context
     * @param y2 The y-coordinate in pixels of the screen's bottom edge on the
     * Graphics context
     */
    public void renderActions(T game, Graphics g, int x1, int y1, int x2, int y2) {}
    
    final void entered() {
        enteredActions(game);
    }
    
    /**
     * Actions for this CellGameState to take immediately after being entered.
     * @param game This CellGameState's CellGame
     */
    public void enteredActions(T game) {}
    
    final void left() {
        leftActions(game);
    }
    
    /**
     * Actions for this CellGameState to take immediately before being exited.
     * @param game This CellGameState's CellGame
     */
    public void leftActions(T game) {}
    
    final void stringBegan(String s) {
        stringBeganActions(game, s);
    }
    
    /**
     * Actions for this CellGameState to take immediately after its CellGame
     * begins typing a new String.
     * @param game This CellGameState's CellGame
     * @param s The initial value of the typed String
     */
    public void stringBeganActions(T game, String s) {}
    
    final void charTyped(char c) {
        charTypedActions(game, c);
    }
    
    /**
     * Actions for this CellGameState to take immediately after a character is
     * typed to its CellGame's typed String.
     * @param game This CellGameState's CellGame
     * @param c The character that was just typed
     */
    public void charTypedActions(T game, char c) {}
    
    final void charDeleted(char c) {
        charDeletedActions(game, c);
    }
    
    /**
     * Actions for this CellGameState to take immediately after a character is
     * deleted from its CellGame's typed String.
     * @param game This CellGameState's CellGame
     * @param c The character that was just deleted
     */
    public void charDeletedActions(T game, char c) {}
    
    final void stringDeleted(String s) {
        stringDeletedActions(game, s);
    }
    
    /**
     * Actions for this CellGameState to take immediately after its CellGame's
     * typed String is deleted and reset to the empty String.
     * @param game This CellGameState's CellGame
     * @param s The String that was just deleted
     */
    public void stringDeletedActions(T game, String s) {}
    
    final void stringFinished(String s) {
        stringFinishedActions(game, s);
    }
    
    /**
     * Actions for this CellGameState to take immediately after its CellGame's
     * typed String is finished.
     * @param game This CellGameState's CellGame
     * @param s The String that was just finished
     */
    public void stringFinishedActions(T game, String s) {}
    
    final void stringCanceled(String s) {
        stringCanceledActions(game, s);
    }
    
    /**
     * Actions for this CellGameState to take immediately after its CellGame's
     * typed String is canceled.
     * @param game This CellGameState's CellGame
     * @param s The String that was just canceled
     */
    public void stringCanceledActions(T game, String s) {}
    
}
