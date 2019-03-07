package org.cell2d.celick.state;

import org.cell2d.celick.Game;
import org.cell2d.celick.GameContainer;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.SlickException;
import org.cell2d.celick.state.transition.EmptyTransition;
import org.cell2d.celick.state.transition.Transition;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A state based game isolated different stages of the game (menu, ingame, hiscores, etc) into 
 * different states so they can be easily managed and maintained.
 *
 * @author kevin
 */
public abstract class StateBasedGame implements Game {
	/** The list of states making up this game */
	private HashMap<Integer,GameState> states = new HashMap<>();
	/** The current state */
	private GameState currentState;
	/** The next state we're moving into */
	private GameState nextState;
	/** The container holding this game */
	private GameContainer container;
	/** The title of the game */
	private String title;
	
	/** The transition being used to enter the state */
	private Transition enterTransition;
	/** The transition being used to leave the state */
	private Transition leaveTransition;
	
	/**
	 * Create a new state based game
	 * 
	 * @param name The name of the game
	 */
	public StateBasedGame(String name) {
		this.title = name;
		
		currentState = new BasicGameState() {
			public int getID() {
				return -1;
			}
			public void init(GameContainer container, StateBasedGame game) throws SlickException {
			}
			public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
			}
			public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
			}
		};
	}
	
	/**
	 * Get the number of states that have been added to this game
	 * 
	 * @return The number of states that have been added to this game
	 */
	public int getStateCount() {
		return states.keySet().size();
	}
	
	/**
	 * Get the ID of the state the game is currently in
	 * 
	 * @return The ID of the state the game is currently in
	 */
	public int getCurrentStateID() {
		return currentState.getID();
	}
	
	/**
	 * Get the state the game is currently in
	 * 
	 * @return The state the game is currently in
	 */
	public GameState getCurrentState() {
		return currentState;
	}
	
	/**
	 * Add a state to the game. The state will be updated and maintained
	 * by the game
	 * 
	 * @param state The state to be added
	 */
	public void addState(GameState state) {
		states.put(state.getID(), state);
		
		if (currentState.getID() == -1) {
			currentState = state;
		}
	}
	
	/**
	 * Get a state based on it's identifier
	 * 
	 * @param id The ID of the state to retrieve
	 * @return The state requested or null if no state with the specified ID exists
	 */
	public GameState getState(int id) {
		return states.get(id);
	}

	/**
	 * Enter a particular game state with no transition
	 * 
	 * @param id The ID of the state to enter
	 */
	public void enterState(int id) {
		enterState(id, new EmptyTransition(), new EmptyTransition());
	}
	
	/**
	 * Enter a particular game state with the transitions provided
	 * 
	 * @param id The ID of the state to enter
	 * @param leave The transition to use when leaving the current state
	 * @param enter The transition to use when entering the new state
	 */
	public void enterState(int id, Transition leave, Transition enter) {
		if (leave == null) {
			leave = new EmptyTransition();
		}
		if (enter == null) {
			enter = new EmptyTransition();
		}
		leaveTransition = leave;
		enterTransition = enter;
		
		nextState = getState(id);
		if (nextState == null) {
			throw new RuntimeException("No game state registered with the ID: "+id);
		}
		
		leaveTransition.init(currentState, nextState);
	}
	
	@Override
	public final void init(GameContainer container) throws SlickException {
		this.container = container;
		initStatesList(container);
		
		Iterator<GameState> gameStates = states.values().iterator();
		
		while (gameStates.hasNext()) {
			gameStates.next().init(container, this);
		}
		
		if (currentState != null) {
			currentState.enter(container, this);
		}
	}

	/**
	 * Initialise the list of states making up this game
	 * 
	 * @param container The container holding the game
	 * @throws SlickException Indicates a failure to initialise the state based game resources
	 */
	public abstract void initStatesList(GameContainer container) throws SlickException;
	
	@Override
	public final void render(GameContainer container, Graphics g) throws SlickException {
		preRenderState(container, g);
		
		if (leaveTransition != null) {
			leaveTransition.preRender(this, container, g);
		} else if (enterTransition != null) {
			enterTransition.preRender(this, container, g);
		}
		
		currentState.render(container, this, g);
		
		if (leaveTransition != null) {
			leaveTransition.postRender(this, container, g);
		} else if (enterTransition != null) {
			enterTransition.postRender(this, container, g);
		}
		
		postRenderState(container, g);
	}
	
	/**
	 * User hook for rendering at the before the current state
	 * and/or transition have been rendered
	 * 
	 * @param container The container in which the game is hosted
	 * @param g The graphics context on which to draw
	 * @throws SlickException Indicates a failure within render
	 */
	protected void preRenderState(GameContainer container, Graphics g) throws SlickException {
		// NO-OP
	}
	
	/**
	 * User hook for rendering at the game level after the current state
	 * and/or transition have been rendered
	 * 
	 * @param container The container in which the game is hosted
	 * @param g The graphics context on which to draw
	 * @throws SlickException Indicates a failure within render
	 */
	protected void postRenderState(GameContainer container, Graphics g) throws SlickException {
		// NO-OP
	}
	
	@Override
	public final void update(GameContainer container, int delta) throws SlickException {
		preUpdateState(container, delta);
		
		if (leaveTransition != null) {
			leaveTransition.update(this, container, delta);
			if (leaveTransition.isComplete()) {
				currentState.leave(container, this);
				GameState prevState = currentState;
				currentState = nextState;
				nextState = null;
				leaveTransition = null;
				currentState.enter(container, this);
				if (enterTransition != null) {
					enterTransition.init(currentState, prevState);
				}
			} else {
				return;
			}
		}
		
		if (enterTransition != null) {
			enterTransition.update(this, container, delta);
			if (enterTransition.isComplete()) {
				enterTransition = null;
			} else {
				return;
			}
		}
		
		currentState.update(container, this, delta);
		
		postUpdateState(container, delta);
	}

	/**
	 * User hook for updating at the game before the current state
	 * and/or transition have been updated
	 * 
	 * @param container The container in which the game is hosted
	 * @param delta The amount of time in milliseconds since last update
	 * @throws SlickException Indicates a failure within render
	 */
	protected void preUpdateState(GameContainer container, int delta) throws SlickException {
		// NO-OP
	}
	
	/**
	 * User hook for rendering at the game level after the current state
	 * and/or transition have been updated
	 * 
	 * @param container The container in which the game is hosted
	 * @param delta The amount of time in milliseconds since last update
	 * @throws SlickException Indicates a failure within render
	 */
	protected void postUpdateState(GameContainer container, int delta) throws SlickException {
		// NO-OP
	}
	
	/**
	 * Check if the game is transitioning between states
	 * 
	 * @return True if we're transitioning between states 
	 */
	private boolean transitioning() {
		return (leaveTransition != null) || (enterTransition != null);
	}
	
	@Override
	public boolean closeRequested() {
		return true;
	}

	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * Get the container holding this game
	 * 
	 * @return The game container holding this game
	 */
	public GameContainer getContainer() {
		return container;
	}

}
