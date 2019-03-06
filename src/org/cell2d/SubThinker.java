package org.cell2d;

import java.util.Iterator;

/**
 * <p>A SubThinker is a type of Thinker that contributes to the mechanics of
 * another Thinker to which it is assigned. This Thinker is called its <i>
 * super-Thinker</i>.</p>
 * 
 * <p>The process in which a SubThinker takes its frameActions() and then
 * performs its frame Events is itself an Event, and the SubThinker
 * automatically ensures that this Event is in the frame Events of its current
 * super-Thinker. The SubThinker's <i>frame priority</i> is the Event's priority
 * in its super-Thinker's frame Events. This means that the SubThinkers assigned
 * to a given Thinker will take their frameActions() in order from highest to
 * lowest frame priority, and that a SubThinker will take its frameActions()
 * after its super-Thinker, but, if its super-Thinker is itself a SubThinker,
 * before the next Thinker assigned to its super-Thinker's super-Thinker.</p>
 * 
 * <p>The SubThinker class is intended to be extended by classes V that extend
 * SubThinker&lt;T,U,V&gt; and interact with GameStates of class U. BasicThinker
 * is an example of such a class. This allows a SubThinker's GameStates to
 * interact with it in ways unique to its subclass of SubThinker.</p>
 * 
 * <p>It is useful to implicitly extend subclasses of SubThinker to override
 * their methods for single instances without creating completely new class
 * files.</p>
 * @see Thinker#addSubThinker(cell2d.SubThinker)
 * @see Event
 * @param <T> The type of CellGame that uses this SubThinker's GameStates
 * @param <U> The type of GameState that uses this SubThinker
 * @param <V> The type of SubThinker that this SubThinker is for GameState
 * interaction purposes
 * @author Andrew Heyman
 */
public abstract class SubThinker<T extends CellGame,
        U extends GameState<T,U,V>, V extends SubThinker<T,U,V>> extends Thinker<T,U,V> {
    
    private final V thisSubThinker;
    Thinker<T,U,V> superThinker = null;
    Thinker<T,U,V> newSuperThinker = null;
    private T game = null;
    private U state = null;
    private int framePriority = 0;
    
    /**
     * Constructs a SubThinker.
     * @param gameClass The Class object representing the type of CellGame that
     * uses this SubThinker's GameStates
     * @param stateClass The Class object representing the type of GameState
     * that uses this SubThinker
     * @param subThinkerClass The Class object representing the type of
     * SubThinker that this SubThinker is for GameState interaction purposes
     */
    public SubThinker(Class<T> gameClass, Class<U> stateClass, Class<V> subThinkerClass) {
        super(gameClass, stateClass, subThinkerClass);
        thisSubThinker = (V)this;
    }
    
    /**
     * Returns this SubThinker as a V, rather than as a SubThinker&lt;T,U,V&gt;.
     * @return This SubThinker as a V
     */
    public final V getThis() {
        return thisSubThinker;
    }
    
    @Override
    public final T getGame() {
        return game;
    }
    
    @Override
    public final U getGameState() {
        return state;
    }
    
    final void setGameAndState(T game, U state) {
        this.game = game;
        this.state = state;
        if (getNumSubThinkers() > 0) {
            Iterator<V> iterator = subThinkerIterator();
            while (iterator.hasNext()) {
                iterator.next().setGameAndState(game, state);
            }
        }
    }
    
    @Override
    public final long getEffectiveTimeFactor() {
        if (superThinker == null) {
            return 0;
        }
        long timeFactor = getTimeFactor();
        return (timeFactor < 0 ? superThinker.getEffectiveTimeFactor() : timeFactor);
    }
    
    /**
     * Returns this SubThinker's super-Thinker, or null if it has none.
     * @return This SubThinker's super-Thinker
     */
    public final Thinker<T,U,V> getSuperThinker() {
        return superThinker;
    }
    
    /**
     * Returns the Thinker to which this SubThinker is about to be assigned, but
     * has not yet been due to one or more of the SubThinker lists involved
     * being iterated over. If this SubThinker is about to be removed from its
     * super-Thinker without being added to a new one afterward, this will be
     * null. If this SubThinker is not about to change super-Thinkers, this
     * method will simply return its current super-Thinker.
     * @return The Thinker to which this SubThinker is about to be assigned
     */
    public final Thinker<T,U,V> getNewSuperThinker() {
        return newSuperThinker;
    }
    
    /**
     * Sets this SubThinker's super-Thinker to the specified one. If it is set
     * to a null Thinker, or if this SubThinker cannot be added to the specified
     * one, this SubThinker will simply be removed from its current
     * super-Thinker if it has one.
     * @param thinker This SubThinker's new super-Thinker
     */
    public final void setSuperThinker(Thinker<T,U,V> thinker) {
        if (newSuperThinker != null) {
            newSuperThinker.removeSubThinker(thisSubThinker);
        }
        if (thinker != null) {
            thinker.addSubThinker(thisSubThinker);
        }
    }
    
    /**
     * Actions for this SubThinker to take after being added to a new
     * super-Thinker, immediately after the super-Thinker takes its
     * addSubThinkerActions().
     * @param game This SubThinker's GameState's CellGame, or null if it has no
     * GameState
     * @param state This SubThinker's GameState, or null if it has none
     */
    public void addedActions(T game, U state) {}
    
    /**
     * Actions for this SubThinker to take before being removed from its current
     * super-Thinker, immediately before the super-Thinker takes its
     * removeSubThinkerActions().
     * @param game This SubThinker's GameState's CellGame, or null if it has no
     * GameState
     * @param state This SubThinker's GameState, or null if it has none
     */
    public void removedActions(T game, U state) {}
    
    /**
     * Returns this SubThinker's frame priority.
     * @return This SubThinker's frame priority
     */
    public final int getFramePriority() {
        return framePriority;
    }
    
    /**
     * Sets this SubThinker's frame priority to the specified value.
     * @param framePriority This SubThinker's new frame priority
     */
    public final void setFramePriority(int framePriority) {
        this.framePriority = framePriority;
        if (superThinker != null) {
            EventGroup<T,U> frameEvents = superThinker.getFrameEvents();
            frameEvents.remove(frame, this.framePriority);
            frameEvents.add(frame, framePriority);
        }
    }
    
}
