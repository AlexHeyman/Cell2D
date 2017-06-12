package cell2d;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.command.BasicCommand;
import org.newdawn.slick.command.Command;
import org.newdawn.slick.command.Control;
import org.newdawn.slick.command.ControllerButtonControl;
import org.newdawn.slick.command.ControllerDirectionControl;
import org.newdawn.slick.command.InputProvider;
import org.newdawn.slick.command.InputProviderListener;
import org.newdawn.slick.command.KeyControl;
import org.newdawn.slick.command.MouseButtonControl;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.Transition;
import org.newdawn.slick.util.Log;

/**
 * <p>A CellGame is a game made with Cell2D. A certain number of times per
 * second, a CellGame executes a frame, in which it processes input, updates the
 * logic of the game, and renders visuals.</p>
 * 
 * <p>A CellGame has one or more CellGameStates, each with a non-negative
 * integer ID that is unique within the CellGame. A CellGame is in exactly one
 * of these CellGameStates at any given time, and can transition between them.
 * Each CellGameState has its own actions to take every frame and in response to
 * specific events, but it only takes these actions while the CellGame is in
 * that state and it is thus active. If a CellGameState is created with an ID
 * that another CellGameState of the same CellGame already has, the old
 * CellGameState is replaced and can no longer be entered.</p>
 * 
 * <p>A CellGame renders visuals on a rectangular grid of pixels called its
 * screen. Points on the screen have x-coordinates that increase from left to
 * right, as well as y-coordinates that increase from top to bottom. The
 * dimensions of the screen are not necessarily the same as the dimensions of
 * the CellGame's program window, as the screen may be scaled to different
 * apparent sizes using the CellGame's scale factor. A CellGame may be displayed
 * in windowed or fullscreen mode.</p>
 * 
 * <p>While a CellGame is rendering visuals, the region of the Graphics context
 * to which it is rendering that is outside its screen cannot be drawn to. When
 * drawn to the Graphics context, shapes and Drawables will automatically be
 * clipped so that they do not extend beyond the screen.</p>
 * 
 * <p>A CellGame processes input in the form of a fixed number of binary
 * commands, numbered from 0 to getNumCommands() - 1 inclusive, as well as the
 * position of the mouse cursor on the screen and the movement of the mouse
 * wheel. Slick2D objects called Controls, which represent keys, mouse buttons,
 * controller buttons, etc. may be bound to at most one command at a time so
 * that when they are pressed, held, and released, so too are the commands to
 * which they are bound. A CellGame also allows for the temporary processing of
 * input as assignments of Controls to specific commands, or as the typing of
 * text to a specific String.</p>
 * 
 * <p>A CellGame also controls the playing, looping, stopping, pausing, and
 * fading of Music tracks. It contains a data structure called a music stack in
 * which Music tracks may be assigned to different integer priority values. Only
 * the Music track with the greatest priority will play; all others will be
 * paused. If the currently playing Music track finishes, it will automatically
 * be removed from the music stack and the Music track with the next greatest
 * priority will begin playing.</p>
 * @author Andrew Heyman
 */
public abstract class CellGame {
    
    /**
     * The version number of Cell2D, currently 1.4.0.
     */
    public static final String VERSION = "1.4.0";
    
    private static enum CommandState {
        NOTHELD, PRESSED, HELD, RELEASED, TAPPED, UNTAPPED
    }
    
    /**
     * Loads the native libraries that are necessary for LWJGL 2, and thus
     * Slick2D, and thus Cell2D, to run. This method should be called exactly
     * once before any CellGames are created.
     * @param path The relative path to the folder containing the native
     * libraries
     */
    public static final void loadNatives(String path) {
        System.setProperty("java.library.path", path);
        System.setProperty("org.lwjgl.librarypath", new File(path).getAbsolutePath());
    }
    
    /**
     * Starts a CellGame. Once the started CellGame is closed, the program will
     * end.
     * @param game The CellGame to start
     */
    public static final void startGame(CellGame game) {
        Log.info("Cell2D Version: " + VERSION);
        try {
            AppGameContainer container = new AppGameContainer(game.game);
            game.updateScreen(container);
            container.setTargetFrameRate(game.getFPS());
            container.setShowFPS(false);
            container.start();
        } catch (SlickException e) {
            throw new RuntimeException("Failed to start a CellGame");
        }
    }
    
    private boolean closeRequested = false;
    private final StateBasedGame game;
    private final Map<Integer,CellGameState> states = new HashMap<>();
    private CellGameState currentState = null;
    private boolean negativeIDsOffLimits = false;
    private boolean loaded = false;
    private InputProvider provider = null;
    private Input input = null;
    private int commandToBind = -1;
    private Command[] commands;
    private CommandState[] commandStates;
    private CommandState[] commandChanges;
    private int adjustedMouseX = 0;
    private int newMouseX = 0;
    private int adjustedMouseY = 0;
    private int newMouseY = 0;
    private int mouseWheelChange = 0;
    private int newMouseWheelChange = 0;
    private String typingString = null;
    private int maxTypingStringLength = 0;
    private int fps;
    private double msPerFrame;
    private double msToRun;
    private final DisplayMode[] displayModes;
    private int screenWidth;
    private int screenHeight;
    private double scaleFactor;
    private double effectiveScaleFactor = 1;
    private int screenXOffset = 0;
    private int screenYOffset = 0;
    private boolean fullscreen;
    private boolean updateScreen = true;
    private Image loadingImage = null;
    private boolean loadingScreenRenderedOnce = false;
    private MusicInstance currentMusic = new MusicInstance(null, 0, 0, false);
    private final SortedMap<Integer,MusicInstance> musicStack = new TreeMap<>();
    private boolean stackOverridden = false;
    private boolean musicPaused = false;
    private double musicPosition = 0;
    private int musicFadeType = 0;
    private double fadeStartVolume = 0;
    private double fadeEndVolume = 0;
    private double fadeDuration = 0;
    private double msFading = 0;
    
    /**
     * Creates a new CellGame.
     * @param gamename The name of this CellGame as seen on its program window
     * @param numCommands The total number of input commands that this CellGame
     * needs to keep track of
     * @param fps The number of frames that this CellGame will execute every
     * second
     * @param screenWidth The initial width of this CellGame's screen in pixels
     * @param screenHeight The initial height of this CellGame's screen in
     * pixels
     * @param scaleFactor The initial factor by which the screen should be
     * scaled to make the size of the program window
     * @param fullscreen Whether this CellGame should start in fullscreen mode
     * @param loadingImagePath The relative path to the image that this CellGame
     * will display while loading before the first CellGameState is entered. If
     * this is null, no loading image will be displayed.
     */
    public CellGame(String gamename, int numCommands, int fps, int screenWidth,
            int screenHeight, double scaleFactor, boolean fullscreen, String loadingImagePath) {
        game = new Game(gamename);
        if (numCommands < 0) {
            throw new RuntimeException("Attempted to create a CellGame with a negative number of controls");
        }
        commands = new Command[numCommands];
        commandStates = new CommandState[numCommands];
        commandChanges = new CommandState[numCommands];
        for (int i = 0; i < numCommands; i++) {
            commands[i] = new BasicCommand("Command " + i);
            commandStates[i] = CommandState.NOTHELD;
            commandChanges[i] = CommandState.NOTHELD;
        }
        setFPS(fps);
        msToRun = 0;
        try {
            displayModes = Display.getAvailableDisplayModes();
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        setScaleFactor(scaleFactor);
        this.fullscreen = fullscreen;
        if (loadingImagePath != null) {
            try {
                loadingImage = new Image(loadingImagePath);
            } catch (SlickException e) {
                throw new RuntimeException("Failed to load a CellGame's loading image: " + loadingImagePath);
            }
        }
    }
    
    private void updateScreen(GameContainer container) throws SlickException {
        updateScreen = false;
        if (container instanceof AppGameContainer) {
            AppGameContainer appContainer = (AppGameContainer)container;
            if (fullscreen) {
                int wastedArea = -1;
                int newWidth = -1;
                int newHeight = -1;
                double newScale = -1;
                int newXOffset = -1;
                int newYOffset = -1;
                double screenRatio = ((double)screenHeight)/screenWidth;
                for (int i = 0; i < displayModes.length; i++) {
                    int modeWidth = displayModes[i].getWidth();
                    int modeHeight = displayModes[i].getHeight();
                    if (modeWidth < screenWidth || modeHeight < screenHeight) {
                        continue;
                    }
                    double modeScale;
                    int modeXOffset = 0;
                    int modeYOffset = 0;
                    if (((double)modeHeight)/modeWidth > screenRatio) {
                        modeScale = ((double)modeWidth)/screenWidth;
                        modeYOffset = (int)((modeHeight/modeScale - screenHeight)/2);
                    } else {
                        modeScale = ((double)modeHeight)/screenHeight;
                        modeXOffset = (int)((modeWidth/modeScale - screenWidth)/2);
                    }
                    int modeArea = modeWidth*modeHeight - (int)(screenWidth*screenHeight*modeScale*modeScale);
                    if (modeArea < wastedArea || wastedArea == -1) {
                        wastedArea = modeArea;
                        newWidth = modeWidth;
                        newHeight = modeHeight;
                        newScale = modeScale;
                        newXOffset = modeXOffset;
                        newYOffset = modeYOffset;
                    }
                }
                if (wastedArea != -1) {
                    effectiveScaleFactor = newScale;
                    screenXOffset = newXOffset;
                    screenYOffset = newYOffset;
                    appContainer.setDisplayMode(newWidth, newHeight, true);
                    return;
                }
            }
            effectiveScaleFactor = scaleFactor;
            screenXOffset = 0;
            screenYOffset = 0;
            appContainer.setDisplayMode((int)(screenWidth*scaleFactor), (int)(screenHeight*scaleFactor), false);
            return;
        }
        double screenRatio = ((double)screenHeight)/screenWidth;
        int containerWidth = container.getWidth();
        int containerHeight = container.getHeight();
        if (((double)containerHeight)/containerWidth > screenRatio) {
            effectiveScaleFactor = ((double)containerWidth)/screenWidth;
            screenXOffset = 0;
            screenYOffset = (int)((containerHeight/effectiveScaleFactor - screenHeight)/2);
            return;
        }
        effectiveScaleFactor = ((double)containerHeight)/screenHeight;
        screenXOffset = (int)((containerWidth/effectiveScaleFactor - screenWidth)/2);
        screenYOffset = 0;
    }
    
    /**
     * Instructs this CellGame to close itself the next time it finishes a game
     * logic update.
     */
    public final void close() {
        closeRequested = true;
    }
    
    private class Game extends StateBasedGame implements InputProviderListener {
        
        private Game(String name) {
            super(name);
        }
        
        @Override
        public final void initStatesList(GameContainer container) throws SlickException {
            new LoadingState();
            negativeIDsOffLimits = true;
        }
        
        @Override
        public final void preUpdateState(GameContainer container, int delta) {}
        
        @Override
        public final void postUpdateState(GameContainer container, int delta) throws SlickException {
            if (loadingScreenRenderedOnce) {
                if (loaded) {
                    double timeElapsed = Math.min(delta, msPerFrame);
                    msToRun += timeElapsed;
                    if (currentMusic.music != null && !musicPaused) {
                        if (musicFadeType != 0) {
                            msFading = Math.min(msFading + timeElapsed, fadeDuration);
                            if (msFading == fadeDuration) {
                                currentMusic.music.setVolume(fadeEndVolume);
                                if (musicFadeType == 2) {
                                    currentMusic.music.stop();
                                }
                                musicFadeType = 0;
                            } else {
                                currentMusic.music.setVolume(fadeStartVolume + (msFading/fadeDuration)*(fadeEndVolume - fadeStartVolume));
                            }
                        }
                        if (!currentMusic.music.isPlaying()) {
                            stopMusic();
                        }
                    }
                    if (msToRun >= msPerFrame) {
                        for (int i = 0; i < commandChanges.length; i++) {
                            commandStates[i] = commandChanges[i];
                            if (commandChanges[i] == CommandState.PRESSED
                                    || commandChanges[i] == CommandState.UNTAPPED) {
                                commandChanges[i] = CommandState.HELD;
                            } else if (commandChanges[i] == CommandState.RELEASED
                                    || commandChanges[i] == CommandState.TAPPED) {
                                commandChanges[i] = CommandState.NOTHELD;
                            }
                        }
                        adjustedMouseX = Math.min(Math.max((int)(newMouseX/effectiveScaleFactor) - screenXOffset, 0), screenWidth - 1);
                        adjustedMouseY = Math.min(Math.max((int)(newMouseY/effectiveScaleFactor) - screenYOffset, 0), screenHeight - 1);
                        mouseWheelChange = newMouseWheelChange;
                        newMouseWheelChange = 0;
                        currentState.frame();
                        msToRun -= msPerFrame;
                        if (closeRequested) {
                            Audio.close();
                            container.exit();
                        } else if (updateScreen) {
                            updateScreen(container);
                        }
                    }
                } else {
                    provider = new InputProvider(container.getInput());
                    provider.addListener(this);
                    input = new Input(container.getScreenHeight());
                    newMouseX = input.getMouseX();
                    newMouseY = input.getMouseY();
                    initActions();
                    loaded = true;
                }
            }
        }
        
        @Override
        public final void preRenderState(GameContainer container, Graphics g) {
            float scale = (float)effectiveScaleFactor;
            g.scale(scale, scale);
            g.setWorldClip(screenXOffset, screenYOffset, screenWidth, screenHeight);
        }
        
        @Override
        public final void postRenderState(GameContainer container, Graphics g) throws SlickException {
            if (loaded) {
                renderActions(g, screenXOffset, screenYOffset, screenXOffset + screenWidth, screenYOffset + screenHeight);
            }
            g.clearWorldClip();
        }
        
        @Override
        public final void mouseMoved(int oldx, int oldy, int newx, int newy) {
            newMouseX = newx;
            newMouseY = newy;
        }
        
        @Override
        public final void mouseWheelMoved(int delta) {
            newMouseWheelChange += delta;
        }
        
        @Override
        public final void keyPressed(int key, char c) {
            if (typingString != null) {
                if (key == Input.KEY_ESCAPE) {
                    cancelTypingString();
                } else if (key == Input.KEY_BACK) {
                    if (typingString.length() > 0) {
                        char toDelete = typingString.charAt(typingString.length() - 1);
                        typingString = typingString.substring(0, typingString.length() - 1);
                        currentState.charDeletedActions(currentState.game, toDelete);
                    }
                } else if (key == Input.KEY_DELETE) {
                    String s = typingString;
                    typingString = "";
                    currentState.stringDeletedActions(currentState.game, s);
                } else if (key == Input.KEY_ENTER) {
                    finishTypingString();
                } else if (c != '\u0000' && typingString.length() < maxTypingStringLength) {
                    typingString += c;
                    currentState.charTypedActions(currentState.game, c);
                }
            } else if (commandToBind >= 0) {
                finishBindToCommand(new KeyControl(key));
            }
        }
        
        @Override
        public final void mouseClicked(int button, int x, int y, int clickCount) {
            if (commandToBind >= 0) {
                finishBindToCommand(new MouseButtonControl(button));
            }
        }
        
        @Override
        public final void controllerUpPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.UP));
            }
        }
        
        @Override
        public final void controllerDownPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.DOWN));
            }
        }
        
        @Override
        public final void controllerLeftPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.LEFT));
            }
        }
        
        @Override
        public final void controllerRightPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.RIGHT));
            }
        }
        
        @Override
        public final void controllerButtonPressed(int controller, int button) {
            if (commandToBind >= 0) {
                finishBindToCommand(new ControllerButtonControl(controller, button));
            }
        }
        
        @Override
        public final void controlPressed(Command command) {
            if (commandToBind >= 0 || typingString != null) {
                return;
            }
            int i = Arrays.asList(commands).indexOf(command);
            if (i >= 0) {
                if (commandChanges[i] == CommandState.NOTHELD
                        || commandChanges[i] == CommandState.TAPPED) {
                    commandChanges[i] = CommandState.PRESSED;
                } else if (commandChanges[i] == CommandState.RELEASED) {
                    commandChanges[i] = CommandState.UNTAPPED;
                }
            }
        }
        
        @Override
        public final void controlReleased(Command command) {
            if (commandToBind >= 0 || typingString != null) {
                return;
            }
            int i = Arrays.asList(commands).indexOf(command);
            if (i >= 0) {
                if (commandChanges[i] == CommandState.HELD
                        || commandChanges[i] == CommandState.UNTAPPED) {
                    commandChanges[i] = CommandState.RELEASED;
                } else if (commandChanges[i] == CommandState.PRESSED) {
                    commandChanges[i] = CommandState.TAPPED;
                }
            }
        }
        
    }
    
    private class State extends BasicGameState {
        
        private final CellGameState state;
        
        private State(CellGameState state) {
            this.state = state;
        }
        
        @Override
        public int getID() {
            return state.getID();
        }
        
        @Override
        public final void init(GameContainer container, StateBasedGame game) throws SlickException {}
        
        @Override
        public final void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {}
        
        @Override
        public final void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
            state.renderActions(state.getGame(), g, screenXOffset, screenYOffset, screenXOffset + screenWidth, screenYOffset + screenHeight);
        }
        
        @Override
        public final void enter(GameContainer container, StateBasedGame game) {
            state.active = true;
            state.enteredActions(state.getGame());
        }
        
        @Override
        public final void leave(GameContainer container, StateBasedGame game) {
            state.leftActions(state.getGame());
            state.active = false;
        }
        
    }
    
    private class LoadingState extends cell2d.BasicGameState<CellGame> {
        
        private LoadingState() {
            super(CellGame.this, -2);
        }
        
        @Override
        public final void renderActions(CellGame game, Graphics g, int x1, int y1, int x2, int y2) {
            if (loadingImage != null) {
                loadingImage.draw((x1 + x2 - loadingImage.getWidth())/2, (y1 + y2 - loadingImage.getHeight())/2);
            }
            loadingScreenRenderedOnce = true;
        }
        
    }
    
    /**
     * Returns this CellGame's CellGameState with the specified ID, or null if
     * there is none.
     * @param id The ID of the CellGameState to return
     * @return The CellGameState with the specified ID
     */
    public final CellGameState getState(int id) {
        return (id < 0 ? null : states.get(id));
    }
    
    /**
     * Returns the CellGameState that this CellGame is currently in - in other
     * words, this CellGame's only active CellGameState.
     * @return The CellGameState that this CellGame is currently in
     */
    public final CellGameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Returns the ID of this CellGame's current CellGameState.
     * @return The ID of this CellGame's current CellGameState
     */
    public final int getCurrentStateID() {
        return currentState.getID();
    }
    
    final void addState(CellGameState state) {
        int id = state.getID();
        if (id < 0 && negativeIDsOffLimits) {
            throw new RuntimeException("Attempted to add a CellGameState with negative ID " + id);
        }
        states.put(id, state);
        game.addState(new State(state));
    }
    
    /**
     * Instructs this CellGame to enter its CellGameState with the specified ID
     * at the end of the current frame. If this CellGame has no CellGameState
     * with the specified ID, this method will do nothing.
     * @param id The ID of the CellGameState to enter
     */
    public final void enterState(int id) {
        enterState(id, null, null);
    }
    
    /**
     * Instructs this CellGame to enter its CellGameState with the specified ID,
     * using the specified Transitions when leaving the current CellGameState
     * and entering the new one, at the end of the current frame. If this
     * CellGame has no CellGameState with the specified ID, this method will do
     * nothing.
     * @param id The ID of the CellGameState to enter
     * @param leave The Transition to use when leaving the current CellGameState
     * @param enter The Transition to use when entering the new CellGameState
     */
    public final void enterState(int id, Transition leave, Transition enter) {
        if (id < 0 && negativeIDsOffLimits) {
            return;
        }
        CellGameState state = states.get(id);
        if (state != null) {
            currentState = state;
            game.enterState(id, leave, enter);
        }
    }
    
    /**
     * Actions for this CellGame to take when initializing itself before
     * entering its first state. This should include creating at least one
     * CellGameState for it, binding default controls to its commands, loading
     * assets, etc. enterState() must be called during this method to tell the
     * CellGame which CellGameState to start out in, or the game will crash.
     */
    public abstract void initActions();
    
    /**
     * Actions for this CellGame to take each frame to render visuals after its
     * current CellGameState has finished rendering.
     * @param g The Graphics context to which this CellGame is rendering its
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
    public void renderActions(Graphics g, int x1, int y1, int x2, int y2) {}
    
    /**
     * Returns how many commands this CellGame has.
     * @return How many commands this CellGame has
     */
    public final int getNumCommands() {
        return commands.length;
    }
    
    /**
     * Returns all of the Controls that are bound to the specified command.
     * @param command The number of the command whose controls are to be
     * returned
     * @return The Controls that are bound to the specified command
     */
    public final List<Control> getControlsFor(int command) {
        if (command < 0 || command >= commands.length) {
            throw new RuntimeException("Attempted to get the controls for nonexistent command number " + command);
        }
        return (provider == null ? new ArrayList<>() : provider.getControlsFor(commands[command]));
    }
    
    /**
     * Binds the specified control to the specified command.
     * @param command The command to bind the specified control to
     * @param control The control to bind to the specified command
     */
    public final void bindControl(int command, Control control) {
        if (command < 0 || command >= commands.length) {
            throw new RuntimeException("Attempted to bind nonexistent command number " + command);
        }
        if (provider != null) {
            provider.bindCommand(control, commands[command]);
        }
    }
    
    /**
     * Unbinds the specified control from its command, if it is bound to one.
     * @param control The control to be unbound
     */
    public final void unbindControl(Control control) {
        if (provider != null) {
            provider.unbindCommand(control);
        }
    }
    
    /**
     * Unbinds all of the specified command's controls from it.
     * @param command The number of the command whose controls are to be unbound
     */
    public final void clearControls(int command) {
        if (command < 0 || command >= commands.length) {
            throw new RuntimeException("Attempted to clear nonexistent command number " + command);
        }
        if (provider != null) {
            provider.clearCommand(commands[command]);
        }
    }
    
    /**
     * Returns the number of the command to which this CellGame has been
     * instructed to bind the next valid control pressed, or -1 if there is
     * none.
     * @return The number of the command to which this CellGame has been
     * instructed to bind the next valid control pressed
     */
    public final int getBindingCommand() {
        return commandToBind;
    }
    
    /**
     * Instructs this CellGame to bind the next valid control pressed to the
     * specified command. This method will throw an Exception if the CellGame is
     * already being used to type a string.
     * @param command The number of the command to which the next valid control
     * pressed should be bound
     */
    public final void waitToBindToCommand(int command) {
        if (command < 0 || command >= commands.length) {
            throw new RuntimeException("Attempted to begin waiting to bind to nonexistent command number " + command);
        }
        if (typingString != null) {
            throw new RuntimeException("Attempted to begin waiting to bind to command number " + command + " while already typing to a String");
        }
        commandToBind = command;
        for (int i = 0; i < commandChanges.length; i++) {
            commandChanges[i] = CommandState.NOTHELD;
        }
    }
    
    private void finishBindToCommand(Control control) {
        bindControl(commandToBind, control);
        commandToBind = -1;
    }
    
    /**
     * Cancels this CellGame's instruction to bind the next valid control
     * pressed to a specified command, if it has been instructed to do so.
     */
    public final void cancelBindToCommand() {
        commandToBind = -1;
    }
    
    /**
     * Returns whether the specified command was pressed this frame; that is,
     * whether it transitioned from not being held to being held.
     * @param command The command to examine
     * @return Whether the specified command was pressed this frame
     */
    public final boolean commandPressed(int command) {
        return commandStates[command] == CommandState.PRESSED
                || commandStates[command] == CommandState.TAPPED
                || commandStates[command] == CommandState.UNTAPPED;
    }
    
    /**
     * Returns whether the specified command is being held this frame.
     * @param command The command to examine
     * @return Whether the specified command is being held this frame
     */
    public final boolean commandHeld(int command) {
        return commandStates[command] == CommandState.PRESSED
                || commandStates[command] == CommandState.HELD
                || commandStates[command] == CommandState.TAPPED;
    }
    
    /**
     * Returns whether the specified command was released this frame; that is,
     * whether it transitioned from being held to not being held.
     * @param command The command to examine
     * @return Whether the specified command was released this frame
     */
    public final boolean commandReleased(int command) {
        return commandStates[command] == CommandState.RELEASED
                || commandStates[command] == CommandState.TAPPED
                || commandStates[command] == CommandState.UNTAPPED;
    }
    
    /**
     * Returns the x-coordinate in pixels of the mouse cursor on this CellGame's
     * screen.
     * @return The x-coordinate in pixels of the mouse cursor on this CellGame's
     * screen
     */
    public final int getMouseX() {
        return adjustedMouseX;
    }
    
    /**
     * Returns the y-coordinate in pixels of the mouse cursor on this CellGame's
     * screen.
     * @return The y-coordinate in pixels of the mouse cursor on this CellGame's
     * screen
     */
    public final int getMouseY() {
        return adjustedMouseY;
    }
    
    /**
     * Returns the change in the position of the mouse wheel since last frame.
     * This value will be positive if the mouse wheel is being rotated up,
     * negative if it is being rotated down, and zero if it is not being
     * rotated.
     * @return The change in the position of the mouse wheel since last frame
     */
    public final int getMouseWheelChange() {
        return mouseWheelChange;
    }
    
    /**
     * Returns the String that this CellGame is being used to type, or null if
     * there is none.
     * @return The String that this CellGame is being used to type
     */
    public final String getTypingString() {
        return typingString;
    }
    
    /**
     * Instructs this CellGame to interpret all inputs as typing a String with a
     * specified maximum length until further notice. While typing, the
     * Backspace key deletes individual characters, the Delete key resets the
     * String to the empty string, the Escape key calls cancelTypingString(),
     * and the Enter key calls finishTypingString(). This method will throw an
     * Exception if this CellGame has already been instructed to bind the next
     * control pressed to a specified command.
     * @param maxLength The maximum length in characters of the String to be
     * typed
     */
    public final void beginTypingString(int maxLength) {
        beginTypingString("", maxLength);
    }
    
    /**
     * Instructs this CellGame to interpret all inputs as typing a String with a
     * specified initial value and maximum length until further notice. While
     * typing, the Backspace key deletes individual characters, the Delete key
     * resets the String to the empty string, the Escape key calls
     * cancelTypingString(), and the Enter key calls finishTypingString(). This
     * method will throw an Exception if this CellGame has already been
     * instructed to bind the next control pressed to a specified command.
     * @param initialString The initial value of the String to be typed
     * @param maxLength The maximum length in characters of the String to be
     * typed
     */
    public final void beginTypingString(String initialString, int maxLength) {
        if (maxLength <= 0) {
            throw new RuntimeException("Attempted to begin typing to a String with non-positive maximum length " + maxLength);
        }
        if (commandToBind >= 0) {
            throw new RuntimeException("Attempted to begin typing to a String while already binding to command number " + commandToBind);
        }
        if (initialString == null) {
            initialString = "";
        }
        if (initialString.length() > maxLength) {
            initialString = initialString.substring(0, maxLength);
        }
        typingString = initialString;
        maxTypingStringLength = maxLength;
        for (int i = 0; i < commandChanges.length; i++) {
            commandChanges[i] = CommandState.NOTHELD;
        }
        currentState.stringBeganActions(currentState.game, initialString);
    }
    
    /**
     * Instructs this CellGame to stop interpreting inputs as typing a String,
     * if it was doing so, and consider the String finished. This will call the
     * stringFinishedActions() method of this CellGame's current CellGameState.
     */
    public final void finishTypingString() {
        if (typingString != null) {
            String s = typingString;
            typingString = null;
            maxTypingStringLength = 0;
            currentState.stringFinishedActions(currentState.game, s);
        }
    }
    
    /**
     * Instructs this CellGame to stop interpreting inputs as typing a String,
     * if it was doing so, and consider the typing canceled. This will call the
     * stringCanceledActions() method of this CellGame's current CellGameState.
     */
    public final void cancelTypingString() {
        if (typingString != null) {
            String s = typingString;
            typingString = null;
            maxTypingStringLength = 0;
            currentState.stringCanceledActions(currentState.game, s);
        }
    }
    
    /**
     * Returns the number of frames that this CellGame executes per second.
     * @return The number of frames that this CellGame executes per second
     */
    public final int getFPS() {
        return fps;
    }
    
    /**
     * Sets the number of frames that this CellGame executes per second to the
     * specified value.
     * @param fps The value to which the number of frames per second will be set
     */
    public final void setFPS(int fps) {
        if (fps <= 0) {
            throw new RuntimeException("Attempted to run a CellGame at a non-positive FPS");
        }
        this.fps = fps;
        msPerFrame = 1000/((double)fps);
        if (game.getContainer() != null) {
            game.getContainer().setTargetFrameRate(fps);
        }
    }
    
    /**
     * Returns the width in pixels of this CellGame's screen.
     * @return The width in pixels of this CellGame's screen.
     */
    public final int getScreenWidth() {
        return screenWidth;
    }
    
    /**
     * Sets the width in pixels of this CellGame's screen to the specified
     * value.
     * @param screenWidth The value to which the screen width will be set
     */
    public final void setScreenWidth(int screenWidth) {
        if (screenWidth <= 0) {
            throw new RuntimeException("Attempted to give a CellGame a non-positive screen width");
        }
        this.screenWidth = screenWidth;
        updateScreen = true;
    }
    
    /**
     * Returns the height in pixels of this CellGame's screen.
     * @return The height in pixels of this CellGame's screen
     */
    public final int getScreenHeight() {
        return screenHeight;
    }
    
    /**
     * Sets the height in pixels of this CellGame's screen to the specified
     * value.
     * @param screenHeight The value to which the screen height will be set
     */
    public final void setScreenHeight(int screenHeight) {
        if (screenHeight <= 0) {
            throw new RuntimeException("Attempted to give a CellGame a non-positive screen height");
        }
        this.screenHeight = screenHeight;
        updateScreen = true;
    }
    
    /**
     * Returns the factor by which this CellGame's screen is scaled to make the
     * size of the program window.
     * @return This CellGame's screen scale factor
     */
    public final double getScaleFactor() {
        return scaleFactor;
    }
    
    /**
     * Sets the factor by which this CellGame's screen is scaled to make the
     * size of the program window to the specified value.
     * @param scaleFactor The value to which the scale factor will be set
     */
    public final void setScaleFactor(double scaleFactor) {
        if (scaleFactor <= 0) {
            throw new RuntimeException("Attempted to give a CellGame a non-positive scale factor");
        }
        this.scaleFactor = scaleFactor;
        updateScreen = true;
    }
    
    /**
     * Returns whether this CellGame is in fullscreen mode.
     * @return Whether this CellGame is in fullscreen mode
     */
    public final boolean isFullscreen() {
        return fullscreen;
    }
    
    /**
     * Sets whether this CellGame is in fullscreen mode.
     * @param fullscreen Whether this CellGame should be in fullscreen mode
     */
    public final void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        updateScreen = true;
    }
    
    private class MusicInstance {
        
        private final Music music;
        private final double pitch;
        private double volume;
        private final boolean loop;
        
        private MusicInstance(Music music, double pitch, double volume, boolean loop) {
            this.music = music;
            this.pitch = pitch;
            this.volume = volume;
            this.loop = loop;
        }
        
    }
    
    /**
     * Returns the Music track that this CellGame is currently playing, or null
     * if there is none.
     * @return The Music track that this CellGame is currently playing
     */
    public final Music getCurrentMusic() {
        return currentMusic.music;
    }
    
    /**
     * Returns the Music track in this CellGame's music stack at the specified
     * priority, or null if there is none.
     * @param priority The priority of the Music track to return
     * @return The Music track at the specified priority
     */
    public final Music getMusic(int priority) {
        MusicInstance instance = musicStack.get(priority);
        return (instance == null ? null : instance.music);
    }
    
    private void changeMusic(MusicInstance instance) {
        if (!musicPaused) {
            if (currentMusic.music != null) {
                currentMusic.music.stop();
            }
            if (instance.music != null) {
                if (instance.loop) {
                    instance.music.loop(instance.pitch, instance.volume);
                } else {
                    instance.music.play(instance.pitch, instance.volume);
                }
            }
        }
        currentMusic = instance;
        musicPosition = 0;
        musicFadeType = 0;
    }
    
    private void startMusic(Music music, double pitch, double volume, boolean loop) {
        if (music == null) {
            stopMusic();
            return;
        }
        if (musicFadeType == 2 && !musicStack.isEmpty() && !stackOverridden) {
            musicStack.remove(musicStack.lastKey());
        }
        changeMusic(new MusicInstance(music, pitch, volume, loop));
        stackOverridden = true;
    }
    
    private void addMusicToStack(int priority, Music music, double pitch, double volume, boolean loop) {
        if (music == null) {
            stopMusic(priority);
            return;
        }
        MusicInstance instance = new MusicInstance(music, pitch, volume, loop);
        if ((musicStack.isEmpty() || priority >= musicStack.lastKey()) && !stackOverridden) {
            if (musicFadeType == 2 && !musicStack.isEmpty() && priority > musicStack.lastKey()) {
                musicStack.remove(musicStack.lastKey());
            }
            changeMusic(instance);
        }
        musicStack.put(priority, instance);
    }
    
    /**
     * Plays the specified Music track once. The track will be treated as having
     * a higher priority than any of those in the music stack.
     * @param music The Music track to play
     */
    public final void playMusic(Music music) {
        startMusic(music, 1, 1, false);
    }
    
    /**
     * Plays the specified Music track once at the specified pitch and volume.
     * The track will be treated as having a higher priority than any of those
     * in the music stack.
     * @param music The Music track to play
     * @param pitch The pitch at which to play the specified Music track, with 1
     * representing no pitch change
     * @param volume The volume at which to play the specified Music track, with
     * 1 representing no volume change
     */
    public final void playMusic(Music music, double pitch, double volume) {
        startMusic(music, pitch, volume, false);
    }
    
    /**
     * Plays the specified Music track once in this CellGame's music stack at
     * the specified priority.
     * @param priority The priority at which to play the specified Music track
     * @param music The Music track to play
     */
    public final void playMusic(int priority, Music music) {
        addMusicToStack(priority, music, 1, 1, false);
    }
    
    /**
     * Plays the specified Music track once in this CellGame's music stack at
     * the specified priority, pitch, and volume.
     * @param priority The priority at which to play the specified Music track
     * @param music The Music track to play
     * @param pitch The pitch at which to play the specified Music track, with 1
     * representing no pitch change
     * @param volume The volume at which to play the specified Music track, with
     * 1 representing no volume change
     */
    public final void playMusic(int priority, Music music, double pitch, double volume) {
        addMusicToStack(priority, music, pitch, volume, false);
    }
    
    /**
     * Loops the specified Music track indefinitely. The track will be treated
     * as having a higher priority than any of those in the music stack.
     * @param music The Music track to loop
     */
    public final void loopMusic(Music music) {
        startMusic(music, 1, 1, true);
    }
    
    /**
     * Loops the specified Music track indefinitely at the specified pitch and
     * volume. The track will be treated as having a higher priority than any of
     * those in the music stack.
     * @param music The Music track to loop
     * @param pitch The pitch at which to play the specified Music track, with 1
     * representing no pitch change
     * @param volume The volume at which to play the specified Music track, with
     * 1 representing no volume change
     */
    public final void loopMusic(Music music, double pitch, double volume) {
        startMusic(music, pitch, volume, true);
    }
    
    /**
     * Loops the specified Music track indefinitely in this CellGame's music
     * stack at the specified priority.
     * @param priority The priority at which to play the specified Music track
     * @param music The Music track to loop
     */
    public final void loopMusic(int priority, Music music) {
        addMusicToStack(priority, music, 1, 1, true);
    }
    
    /**
     * Loops the specified Music track indefinitely in this CellGame's music
     * stack at the specified priority, pitch, and volume.
     * @param priority The priority at which to play the specified Music track
     * @param music The Music track to loop
     * @param pitch The pitch at which to play the specified Music track, with 1
     * representing no pitch change
     * @param volume The volume at which to play the specified Music track, with
     * 1 representing no volume change
     */
    public final void loopMusic(int priority, Music music, double pitch, double volume) {
        addMusicToStack(priority, music, pitch, volume, true);
    }
    
    /**
     * Stops the Music track that this CellGame is currently playing, if there
     * is one.
     */
    public final void stopMusic() {
        if (musicStack.isEmpty()) {
            changeMusic(new MusicInstance(null, 0, 0, false));
            stackOverridden = false;
            return;
        }
        if (!stackOverridden) {
            musicStack.remove(musicStack.lastKey());
            if (musicStack.isEmpty()) {
                changeMusic(new MusicInstance(null, 0, 0, false));
                return;
            }
        }
        changeMusic(musicStack.get(musicStack.lastKey()));
        stackOverridden = false;
    }
    
    /**
     * Stops the specified Music track if this CellGame is currently playing it.
     * @param music The Music track to be stopped
     */
    public final void stopMusic(Music music) {
        if (currentMusic.music != null && currentMusic.music == music) {
            stopMusic();
        }
    }
    
    /**
     * Stops the Music track in this CellGame's music stack at the specified
     * priority, if there is one.
     * @param priority The priority of the Music track to be stopped
     */
    public final void stopMusic(int priority) {
        if (musicStack.isEmpty()) {
            return;
        }
        if (priority == musicStack.lastKey() && !stackOverridden) {
            stopMusic();
        } else {
            musicStack.remove(priority);
        }
    }
    
    /**
     * Stops the specified Music track in this CellGame's music stack at the
     * specified priority if it is currently playing there.
     * @param priority The priority of the Music track to be stopped
     * @param music The Music track to be stopped
     */
    public final void stopMusic(int priority, Music music) {
        if (musicStack.isEmpty()) {
            return;
        }
        MusicInstance instance = musicStack.get(priority);
        if (instance != null && instance.music != null && instance.music == music) {
            if (priority == musicStack.lastKey() && !stackOverridden) {
                stopMusic();
            } else {
                musicStack.remove(priority);
            }
        }
    }
    
    /**
     * Returns whether this CellGame's music is paused.
     * @return Whether this CellGame's music is paused
     */
    public final boolean musicIsPaused() {
        return musicPaused;
    }
    
    /**
     * Pauses this CellGame's music if it is not already paused.
     */
    public final void pauseMusic() {
        if (!musicPaused) {
            if (currentMusic.music != null) {
                musicPosition = currentMusic.music.getPosition();
                currentMusic.music.stop();
            }
            musicPaused = true;
        }
    }
    
    /**
     * Resumes this CellGame's music if it is paused.
     */
    public final void resumeMusic() {
        if (musicPaused) {
            if (currentMusic.music != null) {
                if (currentMusic.loop) {
                    currentMusic.music.loop(currentMusic.pitch, currentMusic.volume);
                } else {
                    currentMusic.music.play(currentMusic.pitch, currentMusic.volume);
                }
                currentMusic.music.setPosition(musicPosition);
            }
            musicPaused = false;
        }
    }
    
    /**
     * Returns the music player's position in seconds in the currently playing
     * Music track, or 0 if no Music track is currently playing.
     * @return The music player's position in seconds in the currently playing
     * Music track
     */
    public final double getMusicPosition() {
        return (currentMusic.music == null ? 0 : currentMusic.music.getPosition());
    }
    
    /**
     * Sets the music player's position in seconds in the currently playing
     * Music track, if there is one.
     * @param position The music player's new position in seconds
     */
    public final void setMusicPosition(double position) {
        if (currentMusic.music != null) {
            currentMusic.music.setPosition(position);
        }
    }
    
    /**
     * Returns the volume of the currently playing Music track, with 1
     * representing no volume change, or 0 if there is no Music track playing.
     * @return The volume of the currently playing Music track
     */
    public final double getMusicVolume() {
        return (currentMusic.music == null ? 0 : currentMusic.volume);
    }
    
    /**
     * Sets the volume of the currently playing Music track, with 1 representing
     * no volume change, if a Music track is currently playing. This will
     * cancel any active volume fades.
     * @param volume The volume of the currently playing Music track
     */
    public final void setMusicVolume(double volume) {
        if (currentMusic.music != null) {
            currentMusic.volume = volume;
            currentMusic.music.setVolume(volume);
            musicFadeType = 0;
        }
    }
    
    /**
     * Instructs the music player to gradually fade the volume of the currently
     * playing Music track to the specified volume over the specified duration,
     * if a Music track is currently playing.
     * @param volume The eventual volume of the Music track
     * @param duration The duration in seconds of the fade
     */
    public final void fadeMusicVolume(double volume, double duration) {
        if (currentMusic.music != null) {
            musicFadeType = 1;
            fadeStartVolume = currentMusic.volume;
            fadeEndVolume = volume;
            fadeDuration = duration*1000;
            msFading = 0;
            currentMusic.volume = volume;
        }
    }
    
    /**
     * Instructs the music player to gradually fade the volume of the currently
     * playing music track to 0 over the specified duration, stopping the Music
     * track once it is silent, if a Music track is currently playing.
     * @param duration The duration in seconds of the fade-out
     */
    public final void fadeMusicOut(double duration) {
        if (currentMusic.music != null) {
            musicFadeType = 2;
            fadeStartVolume = currentMusic.volume;
            fadeEndVolume = 0;
            fadeDuration = duration*1000;
            msFading = 0;
        }
    }
    
}
