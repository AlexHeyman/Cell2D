package cell2d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.newdawn.slick.Graphics;

/**
 * <p>A GameState represents one state that a CellGame can be in, such as the
 * main menu, the options menu, in the middle of a level, etc. GameStates are
 * permanent parts of a specific CellGame and referenced by a specific
 * non-negative integer ID, both of which are specified upon the GameState's
 * creation.</p>
 * 
 * <p>A GameState has its own actions to take every time its CellGame executes a
 * frame and in response to specific events, but it only takes these actions
 * while the CellGame is in that state and it is thus active.</p>
 * 
 * <p>AnimationInstances may be assigned to one GameState each. An
 * AnimationInstance may be assigned to a GameState with or without an integer
 * ID in the context of that GameState. Only one AnimationInstance may be
 * assigned to a given GameState with a given ID at once.</p>
 * 
 * <p>A GameState is also a ThinkerGroup, which means that Thinkers may be
 * directly assigned to one GameState each.</p>
 * 
 * <p>The GameState class is intended to be directly extended by classes U that
 * extend GameState&lt;T,U,V&gt; and interact with Thinkers of class V.
 * BasicState is an example of such a class. This allows a GameState's Thinkers
 * to interact with it in ways unique to its subclass of GameState.</p>
 * @see CellGame
 * @see AnimationInstance
 * @author Andrew Heyman
 * @param <T> The type of CellGame that uses this GameState
 * @param <U> The type of GameState that this GameState is for Thinker
 * interaction purposes
 * @param <V> The type of Thinker that this GameState uses
 */
public abstract class GameState<T extends CellGame,
        U extends GameState<T,U,V>, V extends Thinker<T,U,V>> extends ThinkerGroup<T,U,V> {
    
    private final U thisState;
    final T game;
    private final int id;
    boolean active = false;
    private long timeFactor = Frac.UNIT;
    private final Set<AnimationInstance> animInstances = new HashSet<>();
    private final Map<Integer,AnimationInstance> IDInstances = new HashMap<>();
    
    /**
     * Creates a new GameState of the specified CellGame with the specified ID.
     * GameStates automatically register themselves with their CellGames upon
     * creation.
     * @param gameClass The Class object representing the subclass of CellGame
     * that uses this GameState
     * @param stateClass The Class object representing the subclass of GameState
     * that this GameState is for Thinker interaction purposes
     * @param thinkerClass The Class object representing the subclass of Thinker
     * that this GameState uses
     * @param game The CellGame to which this GameState belongs
     * @param id This GameState's ID
     */
    public GameState(Class<T> gameClass, Class<U> stateClass, Class<V> thinkerClass, T game, int id) {
        super(gameClass, stateClass, thinkerClass);
        thisState = (U)this;
        if (game == null) {
            throw new RuntimeException("Attempted to create a GameState with no CellGame");
        }
        this.game = game;
        this.id = id;
        game.addState(this);
    }
    
    /**
     * Returns this GameState as a U, rather than as a GameState&lt;T,U,V&gt;.
     * @return This GameState as a U
     */
    public final U getThis() {
        return thisState;
    }
    
    /**
     * Returns the CellGame to which this GameState belongs.
     * @return The CellGame to which this GameState belongs
     */
    public final T getGame() {
        return game;
    }
    
    /**
     * Returns this GameState's ID.
     * @return This GameState's ID
     */
    public final int getID() {
        return id;
    }
    
    /**
     * Returns whether this GameState is active - that is, whether its CellGame
     * is currently in this state.
     * @return Whether this GameState is active
     */
    public final boolean isActive() {
        return active;
    }
    
    /**
     * Returns this GameState's time factor; that is, the average number of
     * discrete time units it experiences every frame.
     * @return This GameState's time factor
     */
    public final long getTimeFactor() {
        return timeFactor;
    }
    
    /**
     * Sets this GameState's time factor to the specified value.
     * @param timeFactor The new time factor
     */
    public final void setTimeFactor(long timeFactor) {
        if (timeFactor < 0) {
            throw new RuntimeException("Attempted to give a GameState a negative time factor");
        }
        this.timeFactor = timeFactor;
    }
    
    /**
     * Returns the number of AnimationInstances that are assigned to this
     * GameState.
     * @return The number of AnimationInstances that are assigned to this
     * GameState
     */
    public final int getNumAnimInstances() {
        return animInstances.size();
    }
    
    /**
     * Adds the specified AnimationInstance to this GameState if it is not
     * already assigned to a GameState.
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
     * GameState.
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
     * Removes the specified AnimationInstance from this GameState if it is
     * currently assigned to this GameState.
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
     * Returns the AnimationInstance that is assigned to this GameState with the
     * specified ID.
     * @param id The ID of the AnimationInstance to be returned
     * @return The AnimationInstance that is assigned to this GameState with the
     * specified ID
     */
    public final AnimationInstance getAnimInstance(int id) {
        AnimationInstance instance = IDInstances.get(id);
        return (instance == null ? AnimationInstance.BLANK : instance);
    }
    
    /**
     * Sets the AnimationInstance that is assigned to this GameState with the
     * specified ID to the specified AnimationInstance, if it is not already
     * assigned to a GameState. If there is already an AnimationInstance
     * assigned with the specified ID, it will be removed from this GameState.
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
     * Returns the Animation of the AnimationInstance assigned to this GameState
     * with the specified ID, if there is one.
     * @param id The ID of the AnimationInstance whose Animation is to be
     * returned
     * @return The Animation of the AnimationInstance assigned to this GameState
     * with the specified ID
     */
    public final Animation getAnimation(int id) {
        AnimationInstance instance = IDInstances.get(id);
        return (instance == null ? Animation.BLANK : instance.getAnimation());
    }
    
    /**
     * Sets the AnimationInstance that is assigned to this GameState with the
     * specified ID to a new AnimationInstance of the specified Animation, if
     * there is not already an AnimationInstance of that Animation assigned with
     * that ID. In other words, this method will not replace an
     * AnimationInstance with another of the same Animation. If there is already
     * an AnimationInstance assigned with the specified ID, it will be removed
     * from this GameState.
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
     * Removes from this GameState all AnimationInstances that are currently
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
     * Actions for this GameState to take immediately after being entered.
     * @param game This GameState's CellGame
     */
    public void enteredActions(T game) {}
    
    /**
     * Actions for this GameState to take immediately before being exited.
     * @param game This GameState's CellGame
     */
    public void leftActions(T game) {}
    
    @Override
    public final void addThinkerActions(V thinker) {
        thinker.setGameAndState(game, thisState);
        addThinkerActions(game, thinker);
    }
    
    /**
     * Actions for this GameState to take immediately after adding a Thinker to
     * itself.
     * @param game This GameState's CellGame
     * @param thinker The Thinker that was added
     */
    public final void addThinkerActions(T game, V thinker) {}
    
    @Override
    public final void removeThinkerActions(V thinker) {
        removeThinkerActions(game, thinker);
        thinker.setGameAndState(null, null);
    }
    
    /**
     * Actions for this GameState to take immediately before removing a Thinker
     * from itself.
     * @param game This GameState's CellGame
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
     * Actions for this GameState to take once every frame, after all of its
     * Thinkers have taken their timeUnitActions() but before they take their
     * frameActions().
     * @param game This GameState's CellGame
     */
    public void frameActions(T game) {}
    
    /**
     * Actions for this GameState to take each frame to render its visuals.
     * @param game This GameState's CellGame
     * @param g The Graphics context to which this GameState is rendering its
     * visuals this frame
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
     * Actions for this GameState to take immediately after its CellGame has
     * bound the last valid control pressed to a specified command.
     * @param game This GameState's CellGame
     * @param commandNum The number of the command that was bound to
     */
    public void bindFinishedActions(T game, int commandNum) {}
    
    /**
     * Actions for this GameState to take immediately after its CellGame
     * begins typing a new String.
     * @param game This GameState's CellGame
     * @param s The initial value of the typed String
     */
    public void stringBeganActions(T game, String s) {}
    
    /**
     * Actions for this GameState to take immediately after a character is typed
     * to its CellGame's typed String.
     * @param game This GameState's CellGame
     * @param c The character that was just typed
     */
    public void charTypedActions(T game, char c) {}
    
    /**
     * Actions for this GameState to take immediately after a character is
     * deleted from its CellGame's typed String.
     * @param game This GameState's CellGame
     * @param c The character that was just deleted
     */
    public void charDeletedActions(T game, char c) {}
    
    /**
     * Actions for this GameState to take immediately after its CellGame's typed
     * String is deleted and reset to the empty String.
     * @param game This GameState's CellGame
     * @param s The String that was just deleted
     */
    public void stringDeletedActions(T game, String s) {}
    
    /**
     * Actions for this GameState to take immediately after its CellGame's typed
     * String is finished.
     * @param game This GameState's CellGame
     * @param s The String that was just finished
     */
    public void stringFinishedActions(T game, String s) {}
    
    /**
     * Actions for this GameState to take immediately after its CellGame's typed
     * String is canceled.
     * @param game This GameState's CellGame
     * @param s The String that was just canceled
     */
    public void stringCanceledActions(T game, String s) {}
    
}
