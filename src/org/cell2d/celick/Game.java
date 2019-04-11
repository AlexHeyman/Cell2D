package org.cell2d.celick;

/**
 * The main game interface that should be implemented by any game being developed
 * using the container system. There will be some utility type sub-classes as development
 * continues.
 *
 * @author kevin
 */
public interface Game {
	/**
	 * Initialise the game. This can be used to load static resources. It's called
	 * before the game loop starts
	 * 
	 * @param container The container holding the game
	 * @throws SlickException Throw to indicate an internal error
	 */
	public void init(GameContainer container) throws SlickException;
	
	/**
	 * Update the game logic and render the game's screen here.
	 * @param container The container holding this game
	 * @param msElapsed The amount of time thats passed since last update in milliseconds
         * @param g The graphics context that can be used to render. However, normal rendering
	 * routines can also be used.
	 * @throws SlickException Throw to indicate an internal error
	 */
	public void gameLoop(GameContainer container, int msElapsed, Graphics g) throws SlickException;
	
	/**
	 * Notification that a game close has been requested
	 * 
	 * @return True if the game should close
	 */
	public boolean closeRequested();
	
	/**
	 * Get the title of this game 
	 * 
	 * @return The title of the game
	 */
	public String getTitle();
}
