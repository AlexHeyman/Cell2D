package org.cell2d;

import java.util.HashMap;
import java.util.Map;
import org.cell2d.celick.Graphics;

/**
 * <p>A GameState represents one state that a CellGame can be in, such as the
 * main menu, the options menu, in the middle of a level, etc. GameStates are
 * permanent parts of a specific CellGame and referenced by a specific
 * non-negative integer ID, both of which are specified upon the GameState's
 * creation. If a CellGame is in a GameState, that GameState is considered
 * <i>active</i>.</p>
 * 
 * <p>A GameState is also a Thinker that is considered assigned to itself. This
 * means that it has timers, frameActions(), frame events, etc. If its time
 * factor is negative, a GameState will use a time factor of one fracunit. A
 * GameState will take its frameActions() immediately after all Thinkers have
 * experienced all of their time units for that frame, first among the Thinkers
 * assigned to it.</p>
 * 
 * <p>AnimationInstances may be assigned to one GameState each. An
 * AnimationInstance may be assigned to a GameState with or without an integer
 * ID in the context of that GameState. Only one AnimationInstance may be
 * assigned to a given GameState with a given ID at once.</p>
 * 
 * <p>The GameState class is intended to be extended by classes U that extend
 * GameState&lt;T,U,V&gt; and interact with SubThinkers of class V. BasicState
 * is an example of such a class. This allows a GameState's SubThinkers to
 * interact with it in ways unique to its subclass of GameState.</p>
 * @see CellGame
 * @see AnimationInstance
 * @param <T> The type of CellGame that uses this GameState
 * @param <U> The type of GameState that this GameState is for SubThinker
 * interaction purposes
 * @param <V> The type of SubThinker that can be assigned to this GameState
 * @author Alex Heyman
 */
public abstract class GameState<T extends CellGame,
        U extends GameState<T,U,V>, V extends SubThinker<T,U,V>> extends Thinker<T,U,V> {
    
    private final U thisState;
    final T game;
    private final int id;
    boolean active = false;
    
    //If an AnimationInstance was not added with an ID, it's in this Map, but with a null value
    private final Map<AnimationInstance,Integer> animInstancesToIDs = new HashMap<>();
    
    //If an AnimationInstance was not added with an ID, it's not in this Map
    private final Map<Integer,AnimationInstance> idsToAnimInstances = new HashMap<>();
    
    /**
     * Constructs a GameState of the specified CellGame with the specified ID.
     * GameStates automatically register themselves with their CellGames upon
     * creation.
     * @param gameClass The Class object representing the type of CellGame that
     * uses this GameState
     * @param stateClass The Class object representing the type of GameState
     * that this GameState is for SubThinker interaction purposes
     * @param subThinkerClass The Class object representing the type of
     * SubThinker that can be assigned to this GameState
     * @param game The CellGame to which this GameState belongs
     * @param id This GameState's ID
     */
    public GameState(Class<T> gameClass, Class<U> stateClass, Class<V> subThinkerClass, T game, int id) {
        super(gameClass, stateClass, subThinkerClass);
        thisState = (U)this;
        if (game == null) {
            throw new RuntimeException("Attempted to construct a GameState with no CellGame");
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
    
    @Override
    public final T getGame() {
        return game;
    }
    
    @Override
    public final U getGameState() {
        return thisState;
    }
    
    @Override
    public final long getEffectiveTimeFactor() {
        long timeFactor = getTimeFactor();
        return (timeFactor < 0 ? Frac.UNIT : timeFactor);
    }
    
    /**
     * Returns this GameState's ID.
     * @return This GameState's ID
     */
    public final int getID() {
        return id;
    }
    
    /**
     * Returns whether this GameState is active.
     * @return Whether this GameState is active
     */
    public final boolean isActive() {
        return active;
    }
    
    /**
     * Returns the number of AnimationInstances that are assigned to this
     * GameState, with or without IDs.
     * @return The number of AnimationInstances that are assigned to this
     * GameState
     */
    public final int getNumAnimInstances() {
        return animInstancesToIDs.size();
    }
    
    /**
     * Adds the specified AnimationInstance to this GameState without an ID, if
     * it is not already assigned to a GameState.
     * @param instance The AnimationInstance to add
     * @return Whether the addition occurred
     */
    public final boolean addAnimInstance(AnimationInstance instance) {
        if (instance == AnimationInstance.BLANK) {
            return true;
        }
        if (instance.state == null) {
            animInstancesToIDs.put(instance, null);
            instance.state = this;
            return true;
        }
        return false;
    }
    
    /**
     * Adds a new AnimationInstance of the specified Animation to this GameState
     * without an ID.
     * @param animation The Animation to add a new AnimationInstance of
     * @return The new AnimationInstance
     */
    public final AnimationInstance addAnimInstance(Animation animation) {
        if (animation == Animation.BLANK) {
            return AnimationInstance.BLANK;
        }
        AnimationInstance instance = new AnimationInstance(animation);
        animInstancesToIDs.put(instance, null);
        instance.state = this;
        return instance;
    }
    
    /**
     * Removes the specified AnimationInstance from this GameState if it is
     * currently assigned to this GameState without an ID.
     * @param instance The AnimationInstance to remove
     * @return Whether the removal occurred
     */
    public final boolean removeAnimInstance(AnimationInstance instance) {
        if (instance == AnimationInstance.BLANK) {
            return true;
        }
        if (animInstancesToIDs.containsKey(instance) && animInstancesToIDs.get(instance) == null) {
            animInstancesToIDs.remove(instance);
            instance.state = null;
            return true;
        }
        return false;
    }
    
    /**
     * Returns the AnimationInstance that is assigned to this GameState with the
     * specified ID, or AnimationInstance.BLANK if there is none.
     * @param id The ID of the AnimationInstance to be returned
     * @return The AnimationInstance that is assigned to this GameState with the
     * specified ID
     */
    public final AnimationInstance getAnimInstance(int id) {
        return idsToAnimInstances.getOrDefault(id, AnimationInstance.BLANK);
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
            AnimationInstance oldInstance = idsToAnimInstances.remove(id);
            if (oldInstance != null) {
                animInstancesToIDs.remove(oldInstance);
                oldInstance.state = null;
            }
            return true;
        }
        if (instance.state == null) {
            animInstancesToIDs.put(instance, id);
            AnimationInstance oldInstance = idsToAnimInstances.put(id, instance);
            if (oldInstance != null) {
                animInstancesToIDs.remove(oldInstance);
                oldInstance.state = null;
            }
            instance.state = this;
            return true;
        }
        return false;
    }
    
    /**
     * Returns the Animation of the AnimationInstance assigned to this GameState
     * with the specified ID, or Animation.BLANK if there is none.
     * @param id The ID of the AnimationInstance whose Animation is to be
     * returned
     * @return The Animation of the AnimationInstance assigned to this GameState
     * with the specified ID
     */
    public final Animation getAnimation(int id) {
        AnimationInstance instance = idsToAnimInstances.get(id);
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
     * @return The AnimationInstance assigned with the specified ID, or
     * AnimationInstance.BLANK if there is none
     */
    public final AnimationInstance setAnimation(int id, Animation animation) {
        AnimationInstance instance = getAnimInstance(id);
        if (instance.getAnimation() != animation) {
            if (animation == Animation.BLANK) {
                AnimationInstance oldInstance = idsToAnimInstances.remove(id);
                if (oldInstance != null) {
                    animInstancesToIDs.remove(oldInstance);
                    oldInstance.state = null;
                }
                return AnimationInstance.BLANK;
            }
            instance = new AnimationInstance(animation);
            animInstancesToIDs.put(instance, id);
            AnimationInstance oldInstance = idsToAnimInstances.put(id, instance);
            if (oldInstance != null) {
                animInstancesToIDs.remove(oldInstance);
                oldInstance.state = null;
            }
            instance.state = this;
        }
        return instance;
    }
    
    /**
     * Removes from this GameState all AnimationInstances that are currently
     * assigned to it, with or without IDs.
     */
    public final void clearAnimInstances() {
        for (AnimationInstance instance : animInstancesToIDs.keySet()) {
            instance.state = null;
        }
        animInstancesToIDs.clear();
        idsToAnimInstances.clear();
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
    
    final void stateUpdate() {
        for (AnimationInstance instance : animInstancesToIDs.keySet()) {
            instance.update();
        }
        update(game, thisState, Frac.UNIT);
        frame.actions(game, thisState);
    }
    
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
