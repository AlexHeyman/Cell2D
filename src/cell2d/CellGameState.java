package cell2d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
 * AnimationInstance may be assigned to a CellGameState with or without an
 * integer ID in the context of that CellGameState. Only one AnimationInstance
 * may be assigned to a given CellGameState with a given ID at once.</p>
 * 
 * <p>A CellGameState is also a ThinkerGroup, which means that Thinkers may be
 * directly assigned to one CellGameState each.</p>
 * 
 * <p>The CellGameState class is intended to be directly extended by classes U
 * that extend CellGameState&lt;T,U,V&gt; and interact with Thinkers of class V.
 * BasicGameState is an example of such a class. This allows a CellGameState's
 * Thinkers to interact with it in ways unique to its subclass of CellGameState.
 * </p>
 * @author Andrew Heyman
 * @param <T> The subclass of CellGame that uses this CellGameState
 * @param <U> The subclass of CellGameState that this CellGameState is
 * @param <V> The subclass of Thinker that this CellGameState uses
 */
public abstract class CellGameState<T extends CellGame, U extends CellGameState<T,U,V>,
        V extends Thinker<T,U,V>> extends ThinkerGroup<T,U,V> {
    
    private final U thisState;
    final T game;
    private int id;
    boolean active = false;
    private long timeFactor = Frac.UNIT;
    private final Set<AnimationInstance> animInstances = new HashSet<>();
    private final Map<Integer,AnimationInstance> IDInstances = new HashMap<>();
    
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
    }
    
    /**
     * A method which returns this CellGameState as a U, rather than as a
     * CellGameState&lt;T,U,V&gt;. This must be implemented somewhere in the
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
     * Returns this CellGameState's time factor; that is, the average number of
     * discrete time units it experiences every frame.
     * @return This CellGameState's time factor
     */
    public final long getTimeFactor() {
        return timeFactor;
    }
    
    /**
     * Sets this CellGameState's time factor to the specified value.
     * @param timeFactor The new time factor
     */
    public final void setTimeFactor(long timeFactor) {
        if (timeFactor < 0) {
            throw new RuntimeException("Attempted to give a CellGameState a negative time factor");
        }
        this.timeFactor = timeFactor;
    }
    
    /**
     * Returns the number of AnimationInstances that are assigned to this
     * CellGameState.
     * @return The number of AnimationInstances that are assigned to this
     * CellGameState
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
     * @return Whether the change occurred
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
     * @return The AnimationInstance assigned with the specified ID
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
    
    @Override
    public final void addThinkerActions(V thinker) {
        thinker.setGameAndState(game, thisState);
        addThinkerActions(game, thinker);
    }
    
    /**
     * Actions for this CellGameState to take immediately after adding a Thinker
     * to itself.
     * @param game This CellGameState's CellGame
     * @param thinker The Thinker that was added
     */
    public final void addThinkerActions(T game, V thinker) {}
    
    @Override
    public final void removeThinkerActions(V thinker) {
        removeThinkerActions(game, thinker);
        thinker.setGameAndState(null, null);
    }
    
    /**
     * Actions for this CellGameState to take immediately before removing a
     * Thinker from itself.
     * @param game This CellGameState's CellGame
     * @param thinker The Thinker that is about to be removed
     */
    public final void removeThinkerActions(T game, V thinker) {}
    
    final void frame() {
        for (AnimationInstance instance : animInstances) {
            instance.update();
        }
        Iterator<V> iterator = thinkerIterator();
        while (iterator.hasNext()) {
            iterator.next().update();
        }
        frameActions(game);
        iterator = thinkerIterator();
        while (iterator.hasNext()) {
            iterator.next().frame();
        }
    }
    
    /**
     * Actions for this CellGameState to take once every frame, after all of its
     * Thinkers have taken their timeUnitActions() but before they take their
     * frameActions().
     * @param game This CellGameState's CellGame
     */
    public void frameActions(T game) {}
    
    /**
     * Actions for this CellGameState to take each frame to render its visuals.
     * @param game This CellGameState's CellGame
     * @param g The Graphics context to which this CellGameState is rendering
     * its visuals this frame
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
    
    /**
     * Actions for this CellGameState to take immediately after being entered.
     * @param game This CellGameState's CellGame
     */
    public void enteredActions(T game) {}
    
    /**
     * Actions for this CellGameState to take immediately before being exited.
     * @param game This CellGameState's CellGame
     */
    public void leftActions(T game) {}
    
    /**
     * Actions for this CellGameState to take immediately after its CellGame
     * begins typing a new String.
     * @param game This CellGameState's CellGame
     * @param s The initial value of the typed String
     */
    public void stringBeganActions(T game, String s) {}
    
    /**
     * Actions for this CellGameState to take immediately after a character is
     * typed to its CellGame's typed String.
     * @param game This CellGameState's CellGame
     * @param c The character that was just typed
     */
    public void charTypedActions(T game, char c) {}
    
    /**
     * Actions for this CellGameState to take immediately after a character is
     * deleted from its CellGame's typed String.
     * @param game This CellGameState's CellGame
     * @param c The character that was just deleted
     */
    public void charDeletedActions(T game, char c) {}
    
    /**
     * Actions for this CellGameState to take immediately after its CellGame's
     * typed String is deleted and reset to the empty String.
     * @param game This CellGameState's CellGame
     * @param s The String that was just deleted
     */
    public void stringDeletedActions(T game, String s) {}
    
    /**
     * Actions for this CellGameState to take immediately after its CellGame's
     * typed String is finished.
     * @param game This CellGameState's CellGame
     * @param s The String that was just finished
     */
    public void stringFinishedActions(T game, String s) {}
    
    /**
     * Actions for this CellGameState to take immediately after its CellGame's
     * typed String is canceled.
     * @param game This CellGameState's CellGame
     * @param s The String that was just canceled
     */
    public void stringCanceledActions(T game, String s) {}
    
}
