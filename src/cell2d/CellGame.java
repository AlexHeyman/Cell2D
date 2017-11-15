package cell2d;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
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
 * <p>A CellGame also controls the playing and stopping of Music tracks. It
 * contains a data structure called a music stack in which different integer
 * priority values may be assigned one or more Music tracks each. Only the Music
 * tracks assigned to the greatest priority in the stack will play at any given
 * time. If a currently playing Music track finishes, it will automatically be
 * removed from the top of the music stack.</p>
 * @author Andrew Heyman
 */
public abstract class CellGame {
    
    /**
     * The version number of Cell2D, currently 1.5.1.
     */
    public static final String VERSION = "1.5.1";
    
    private static enum CommandState {
        NOTHELD, PRESSED, HELD, RELEASED, TAPPED, UNTAPPED
    }
    
    private static Map<Integer,String> KEYCODE_NAMES = null;
    private static Map<String,Integer> NAME_KEYCODES = null;
    private static final int MAX_CONTROLLERS = 16;
    private static final int NORMAL_CTLR_BUTTONS = 16;
    private static final int MAX_CTLR_BUTTONS = 100;
    
    /**
     * Loads the native libraries that are necessary for LWJGL 2, and thus
     * Slick2D, and thus Cell2D, to run. This method should be called exactly
     * once before any CellGames are created.
     * @param path The relative path to the folder containing the native
     * libraries
     */
    public static final void loadNatives(String path) {
        System.setProperty("java.library.path", path);
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException("Failed to load native libraries");
        }
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
            container.setTargetFrameRate(game.fps);
            container.setShowFPS(game.showFPS);
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
    private Command[] commands;
    private CommandState[] commandStates;
    private CommandState[] commandChanges;
    private int bindingCommandNum = -1;
    private int boundCommandNum = -1;
    private int adjustedMouseX = 0;
    private int newMouseX = 0;
    private int adjustedMouseY = 0;
    private int newMouseY = 0;
    private int mouseWheelChange = 0;
    private int newMouseWheelChange = 0;
    private String typingString = null;
    private int maxTypingStringLength = 0;
    private boolean finishTypingString = false;
    private int fps;
    private boolean showFPS = false;
    private double msPerFrame;
    private double msToRun = 0;
    private final DisplayMode[] displayModes;
    private int screenWidth, screenHeight;
    private double scaleFactor;
    private double effectiveScaleFactor = 1;
    private int screenXOffset = 0;
    private int screenYOffset = 0;
    private boolean fullscreen;
    private boolean updateScreen = true;
    private boolean loadingVisualsRenderedOnce = false;
    private final SortedMap<Integer,Map<Music,MusicInstance>> musicStack = new TreeMap<>();
    
    /**
     * Creates a new CellGame.
     * @param name The name of this CellGame as seen on its program window
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
     */
    public CellGame(String name, int numCommands, int fps,
            int screenWidth, int screenHeight, double scaleFactor, boolean fullscreen) {
        game = new Game(name);
        if (numCommands < 0) {
            throw new RuntimeException("Attempted to create a CellGame with a negative number of commands");
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
        try {
            displayModes = Display.getAvailableDisplayModes();
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        setScaleFactor(scaleFactor);
        this.fullscreen = fullscreen;
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
                    if (modeArea < wastedArea || wastedArea < 0) {
                        wastedArea = modeArea;
                        newWidth = modeWidth;
                        newHeight = modeHeight;
                        newScale = modeScale;
                        newXOffset = modeXOffset;
                        newYOffset = modeYOffset;
                    }
                }
                if (wastedArea >= 0) {
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
        public final void postUpdateState(GameContainer container, int delta) throws SlickException {
            if (loadingVisualsRenderedOnce) {
                if (loaded) {
                    double msElapsed = Math.min(delta, msPerFrame);
                    if (!musicStack.isEmpty()) {
                        int top = musicStack.lastKey();
                        Map<Music,MusicInstance> musics = musicStack.get(top);
                        Iterator<Music> iterator = musics.keySet().iterator();
                        while (iterator.hasNext()) {
                            Music music = iterator.next();
                            if (music.update(msElapsed)) {
                                iterator.remove();
                            }
                        }
                        if (musics.isEmpty()) {
                            musicStack.remove(top);
                            if (!musicStack.isEmpty()) {
                                for (Map.Entry<Music,MusicInstance> entry
                                        : musicStack.get(musicStack.lastKey()).entrySet()) {
                                    entry.getKey().play(entry.getValue());
                                }
                            }
                        }
                    }
                    msToRun += msElapsed;
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
                        adjustedMouseX = Math.min(Math.max(
                                (int)(newMouseX/effectiveScaleFactor) - screenXOffset, 0), screenWidth - 1);
                        adjustedMouseY = Math.min(Math.max(
                                (int)(newMouseY/effectiveScaleFactor) - screenYOffset, 0), screenHeight - 1);
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
                    if (currentState == null) {
                        throw new RuntimeException("A CellGame did not enter any of its CellGameStates "
                                + "during initialization");
                    }
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
                renderActions(g, screenXOffset, screenYOffset,
                        screenXOffset + screenWidth, screenYOffset + screenHeight);
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
            if (bindingCommandNum >= 0) {
                finishBindToCommand(new KeyControl(key));
            } else if (typingString != null) {
                if (key == Input.KEY_ESCAPE) {
                    cancelTypingString();
                } else if (key == Input.KEY_BACK) {
                    if (typingString.length() > 0) {
                        char toDelete = typingString.charAt(typingString.length() - 1);
                        typingString = typingString.substring(0, typingString.length() - 1);
                        if (currentState != null) {
                            currentState.charDeletedActions(currentState.game, toDelete);
                        }
                    }
                } else if (key == Input.KEY_DELETE) {
                    String s = typingString;
                    typingString = "";
                    if (currentState != null) {
                        currentState.stringDeletedActions(currentState.game, s);
                    }
                } else if (key == Input.KEY_ENTER) {
                    finishTypingString = true;
                } else if (c != '\u0000' && typingString.length() < maxTypingStringLength) {
                    typingString += c;
                    if (currentState != null) {
                        currentState.charTypedActions(currentState.game, c);
                    }
                }
            }
        }
        
        @Override
        public final void mouseClicked(int button, int x, int y, int clickCount) {
            if (bindingCommandNum >= 0) {
                finishBindToCommand(new MouseButtonControl(button));
            }
        }
        
        @Override
        public final void controllerUpPressed(int controller) {
            if (bindingCommandNum >= 0) {
                finishBindToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.UP));
            }
        }
        
        @Override
        public final void controllerDownPressed(int controller) {
            if (bindingCommandNum >= 0) {
                finishBindToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.DOWN));
            }
        }
        
        @Override
        public final void controllerLeftPressed(int controller) {
            if (bindingCommandNum >= 0) {
                finishBindToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.LEFT));
            }
        }
        
        @Override
        public final void controllerRightPressed(int controller) {
            if (bindingCommandNum >= 0) {
                finishBindToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.RIGHT));
            }
        }
        
        @Override
        public final void controllerButtonPressed(int controller, int button) {
            if (bindingCommandNum >= 0) {
                finishBindToCommand(new ControllerButtonControl(controller, button));
            }
        }
        
        @Override
        public final void controlPressed(Command command) {
            if (bindingCommandNum == -1 && boundCommandNum == -1 && typingString == null) {
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
        }
        
        @Override
        public final void controlReleased(Command command) {
            if (bindingCommandNum == -1 && boundCommandNum == -1 && typingString == null) {
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
        
        @Override
        public final void inputEnded() {
            if (boundCommandNum >= 0) {
                int commandNum = boundCommandNum;
                boundCommandNum = -1;
                if (currentState != null) {
                    currentState.bindFinishedActions(currentState.game, commandNum);
                }
            } else if (finishTypingString) {
                String s = typingString;
                typingString = null;
                maxTypingStringLength = 0;
                finishTypingString = false;
                if (currentState != null) {
                    currentState.stringFinishedActions(currentState.game, s);
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
            state.renderActions(state.getGame(), g, screenXOffset, screenYOffset,
                    screenXOffset + screenWidth, screenYOffset + screenHeight);
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
            if (!loadingVisualsRenderedOnce) {
                renderLoadingVisuals(g, x1, y1, x2, y2);
                loadingVisualsRenderedOnce = true;
            }
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
     * using the specified Slick2D Transitions when leaving the current
     * CellGameState and entering the new one, at the end of the current frame.
     * If this CellGame has no CellGameState with the specified ID, this method
     * will do nothing.
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
     * Renders the visuals that this CellGame will display while its
     * initActions() are in progress. This method is called automatically before
     * initActions() is, so it cannot employ assets that are first loaded in
     * initActions().
     * @param g The Graphics context to which this CellGame is rendering the
     * loading visuals
     * @param x1 The x-coordinate in pixels of the screen's left edge on the
     * Graphics context
     * @param y1 The y-coordinate in pixels of the screen's top edge on the
     * Graphics context
     * @param x2 The x-coordinate in pixels of the screen's right edge on the
     * screen on the Graphics context
     * @param y2 The y-coordinate in pixels of the screen's bottom edge on the
     * Graphics context
     */
    public void renderLoadingVisuals(Graphics g, int x1, int y1, int x2, int y2) {}
    
    /**
     * Actions for this CellGame to take when initializing itself before
     * entering its first state. This should include creating at least one
     * CellGameState for it, binding default controls to its commands, loading
     * assets, etc. enterState() must be called during this method to tell the
     * CellGame which CellGameState to start out in, or an Exception will be
     * thrown.
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
     * @param commandNum The number of the command whose controls are to be
     * returned
     * @return The Controls that are bound to the specified command
     */
    public final List<Control> getControlsFor(int commandNum) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to get the controls for nonexistent command number " + commandNum);
        }
        return (provider == null ? new ArrayList<>() : provider.getControlsFor(commands[commandNum]));
    }
    
    /**
     * Binds the specified control to the specified command.
     * @param commandNum The command to bind the specified control to
     * @param control The control to bind to the specified command
     */
    public final void bindControl(int commandNum, Control control) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to bind nonexistent command number " + commandNum);
        }
        if (provider != null) {
            provider.bindCommand(control, commands[commandNum]);
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
     * @param commandNum The number of the command whose controls are to be
     * unbound
     */
    public final void clearControls(int commandNum) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to clear nonexistent command number " + commandNum);
        }
        if (provider != null) {
            provider.clearCommand(commands[commandNum]);
        }
    }
    
    /**
     * Returns the number of the command to which this CellGame has been
     * instructed to bind the next valid control pressed, or -1 if there is
     * none.
     * @return The number of the command to which this CellGame has been
     * instructed to bind the next valid control pressed
     */
    public final int getBindingCommandNum() {
        return bindingCommandNum;
    }
    
    /**
     * Instructs this CellGame to bind the next valid control pressed to the
     * specified command. After that control is bound, the bindFinishedActions()
     * method of this CellGame's current CellGameState will be called. This
     * method will throw an Exception if this CellGame is already being used to
     * type a string.
     * @param commandNum The number of the command to which the next valid
     * control pressed should be bound
     */
    public final void waitToBindToCommand(int commandNum) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to begin waiting to bind to nonexistent command number " + commandNum);
        }
        if (typingString != null) {
            throw new RuntimeException("Attempted to begin waiting to bind to command number " + commandNum + " while already typing to a String");
        }
        bindingCommandNum = commandNum;
        for (int i = 0; i < commandChanges.length; i++) {
            commandChanges[i] = CommandState.NOTHELD;
        }
    }
    
    private void finishBindToCommand(Control control) {
        bindControl(bindingCommandNum, control);
        boundCommandNum = bindingCommandNum;
        bindingCommandNum = -1;
    }
    
    /**
     * Cancels this CellGame's instruction to bind the next valid control
     * pressed to a specified command, if it has been instructed to do so.
     */
    public final void cancelBindToCommand() {
        bindingCommandNum = -1;
    }
    
    private static void putKeycodeName(int keycode, String name) {
        KEYCODE_NAMES.put(keycode, name);
        NAME_KEYCODES.put(name, keycode);
    }
    
    private static void initKeycodeData() {
        KEYCODE_NAMES = new HashMap<>();
        NAME_KEYCODES = new HashMap<>();
        putKeycodeName(Input.KEY_ESCAPE, "Escape");
	putKeycodeName(Input.KEY_1, "1");
	putKeycodeName(Input.KEY_2, "2");
        putKeycodeName(Input.KEY_3, "3");
	putKeycodeName(Input.KEY_4, "4");
        putKeycodeName(Input.KEY_5, "5");
	putKeycodeName(Input.KEY_6, "6");
        putKeycodeName(Input.KEY_7, "7");
	putKeycodeName(Input.KEY_8, "8");
        putKeycodeName(Input.KEY_9, "9");
	putKeycodeName(Input.KEY_0, "0");
	putKeycodeName(Input.KEY_MINUS, "-");
	putKeycodeName(Input.KEY_EQUALS, "=");
	putKeycodeName(Input.KEY_BACK, "Back");
	putKeycodeName(Input.KEY_TAB, "Tab");
	putKeycodeName(Input.KEY_Q, "Q");
	putKeycodeName(Input.KEY_W, "W");
	putKeycodeName(Input.KEY_E, "E");
	putKeycodeName(Input.KEY_R, "R");
	putKeycodeName(Input.KEY_T, "T");
	putKeycodeName(Input.KEY_Y, "Y");
	putKeycodeName(Input.KEY_U, "U");
	putKeycodeName(Input.KEY_I, "I");
	putKeycodeName(Input.KEY_O, "O");
	putKeycodeName(Input.KEY_P, "P");
	putKeycodeName(Input.KEY_LBRACKET, "[");
	putKeycodeName(Input.KEY_RBRACKET, "]");
	putKeycodeName(Input.KEY_ENTER, "Enter");
	putKeycodeName(Input.KEY_LCONTROL, "LCtrl");
	putKeycodeName(Input.KEY_A, "A");
	putKeycodeName(Input.KEY_S, "S");
	putKeycodeName(Input.KEY_D, "D");
	putKeycodeName(Input.KEY_F, "F");
	putKeycodeName(Input.KEY_G, "G");
	putKeycodeName(Input.KEY_H, "H");
	putKeycodeName(Input.KEY_J, "J");
	putKeycodeName(Input.KEY_K, "K");
	putKeycodeName(Input.KEY_L, "L");
	putKeycodeName(Input.KEY_SEMICOLON, ";");
	putKeycodeName(Input.KEY_APOSTROPHE, "'");
	putKeycodeName(Input.KEY_GRAVE, "`");
	putKeycodeName(Input.KEY_LSHIFT, "LShift");
	putKeycodeName(Input.KEY_BACKSLASH, "\\");
	putKeycodeName(Input.KEY_Z, "Z");
	putKeycodeName(Input.KEY_X, "X");
	putKeycodeName(Input.KEY_C, "C");
	putKeycodeName(Input.KEY_V, "V");
	putKeycodeName(Input.KEY_B, "B");
	putKeycodeName(Input.KEY_N, "N");
	putKeycodeName(Input.KEY_M, "M");
	putKeycodeName(Input.KEY_COMMA, ",");
	putKeycodeName(Input.KEY_PERIOD, ".");
	putKeycodeName(Input.KEY_SLASH, "/");
	putKeycodeName(Input.KEY_RSHIFT, "RShift");
	putKeycodeName(Input.KEY_MULTIPLY, "Numpd*");
	putKeycodeName(Input.KEY_LMENU, "LAlt");
	putKeycodeName(Input.KEY_SPACE, "Space");
	putKeycodeName(Input.KEY_CAPITAL, "CapsLk");
	putKeycodeName(Input.KEY_F1, "F1");
        putKeycodeName(Input.KEY_F2, "F2");
        putKeycodeName(Input.KEY_F3, "F3");
        putKeycodeName(Input.KEY_F4, "F4");
        putKeycodeName(Input.KEY_F5, "F5");
        putKeycodeName(Input.KEY_F6, "F6");
        putKeycodeName(Input.KEY_F7, "F7");
        putKeycodeName(Input.KEY_F8, "F8");
        putKeycodeName(Input.KEY_F9, "F9");
        putKeycodeName(Input.KEY_F10, "F10");
	putKeycodeName(Input.KEY_NUMLOCK, "NumLk");
	putKeycodeName(Input.KEY_SCROLL, "ScrlLk");
	putKeycodeName(Input.KEY_NUMPAD7, "Numpd7");
	putKeycodeName(Input.KEY_NUMPAD8, "Numpd8");
	putKeycodeName(Input.KEY_NUMPAD9, "Numpd9");
	putKeycodeName(Input.KEY_SUBTRACT, "Numpd-");
	putKeycodeName(Input.KEY_NUMPAD4, "Numpd4");
	putKeycodeName(Input.KEY_NUMPAD5, "Numpd5");
	putKeycodeName(Input.KEY_NUMPAD6, "Numpd6");
	putKeycodeName(Input.KEY_ADD, "Numpd+");
	putKeycodeName(Input.KEY_NUMPAD1, "Numpd1");
	putKeycodeName(Input.KEY_NUMPAD2, "Numpd2");
	putKeycodeName(Input.KEY_NUMPAD3, "Numpd3");
	putKeycodeName(Input.KEY_NUMPAD0, "Numpd0");
	putKeycodeName(Input.KEY_DECIMAL, "Numpd.");
	putKeycodeName(Input.KEY_F11, "F11");
	putKeycodeName(Input.KEY_F12, "F12");
	putKeycodeName(Input.KEY_F13, "F13");
	putKeycodeName(Input.KEY_F14, "F14");
	putKeycodeName(Input.KEY_F15, "F15");
	putKeycodeName(Input.KEY_KANA, "Kana");
	putKeycodeName(Input.KEY_CONVERT, "Conv");
	putKeycodeName(Input.KEY_NOCONVERT, "NoConv");
	putKeycodeName(Input.KEY_YEN, "Yen");
	putKeycodeName(Input.KEY_NUMPADEQUALS, "Numpd=");
	putKeycodeName(Input.KEY_CIRCUMFLEX, "^");
	putKeycodeName(Input.KEY_AT, "@");
	putKeycodeName(Input.KEY_COLON, ":");
	putKeycodeName(Input.KEY_UNDERLINE, "_");
	putKeycodeName(Input.KEY_KANJI, "Kanji");
	putKeycodeName(Input.KEY_STOP, "Stop");
	putKeycodeName(Input.KEY_AX, "AX");
	putKeycodeName(Input.KEY_UNLABELED, "Unlabl");
	putKeycodeName(Input.KEY_NUMPADENTER, "NumpdE");
	putKeycodeName(Input.KEY_RCONTROL, "RCtrl");
	putKeycodeName(Input.KEY_NUMPADCOMMA, "Numpd,");
	putKeycodeName(Input.KEY_DIVIDE, "Numpd%");
	putKeycodeName(Input.KEY_SYSRQ, "SysRq");
	putKeycodeName(Input.KEY_RMENU, "RAlt");
	putKeycodeName(Input.KEY_PAUSE, "Pause");
	putKeycodeName(Input.KEY_HOME, "Home");
	putKeycodeName(Input.KEY_UP, "Up");
	putKeycodeName(Input.KEY_PRIOR, "PageUp");
	putKeycodeName(Input.KEY_LEFT, "Left");
	putKeycodeName(Input.KEY_RIGHT, "Right");
	putKeycodeName(Input.KEY_END, "End");
	putKeycodeName(Input.KEY_DOWN, "Down");
	putKeycodeName(Input.KEY_NEXT, "PageDn");
	putKeycodeName(Input.KEY_INSERT, "Insert");
	putKeycodeName(Input.KEY_DELETE, "Delete");
	putKeycodeName(Input.KEY_LWIN, "LWndws");
	putKeycodeName(Input.KEY_RWIN, "RWndws");
	putKeycodeName(Input.KEY_APPS, "Apps");
	putKeycodeName(Input.KEY_POWER, "Power");
	putKeycodeName(Input.KEY_SLEEP, "Sleep");
    }
    
    /**
     * Returns a short, descriptive, and unique String name for the specified
     * Control. The name will be no more than 6 characters long and contain only
     * ASCII characters and no whitespace. Two distinct Control objects will
     * have the same name if and only if they are equal. This method will return
     * a null name for certain malformed or highly unusual Control objects, but
     * any Control associated with an actual physical control device is almost
     * guaranteed to have a non-null name.
     * @param control The Control to name
     * @return The name of the specified Control
     */
    public final String getControlName(Control control) {
        if (control instanceof KeyControl) {
            if (KEYCODE_NAMES == null) {
                initKeycodeData();
            }
            return KEYCODE_NAMES.get(control.hashCode());
        } else if (control instanceof MouseButtonControl) {
            int hashCode = control.hashCode();
            switch (hashCode) {
                case Input.MOUSE_LEFT_BUTTON:
                    return "LMB";
                case Input.MOUSE_RIGHT_BUTTON:
                    return "RMB";
                case Input.MOUSE_MIDDLE_BUTTON:
                    return "MMB";
            }
        } else if (control instanceof ControllerDirectionControl) {
            for (int i = 0; i < MAX_CONTROLLERS; i++) {
                if (control.equals(new ControllerDirectionControl(i, ControllerDirectionControl.UP))) {
                    return "C" + i + "Up";
                } else if (control.equals(new ControllerDirectionControl(i, ControllerDirectionControl.DOWN))) {
                    return "C" + i + "Dwn";
                } else if (control.equals(new ControllerDirectionControl(i, ControllerDirectionControl.LEFT))) {
                    return "C" + i + "Lft";
                } else if (control.equals(new ControllerDirectionControl(i, ControllerDirectionControl.RIGHT))) {
                    return "C" + i + "Rgt";
                }
            }
        } else if (control instanceof ControllerButtonControl) {
            for (int i = 0; i < MAX_CONTROLLERS; i++) {
                for (int j = 0; j < NORMAL_CTLR_BUTTONS; j++) {
                    if (control.equals(new ControllerButtonControl(i, j))) {
                        return "C" + i + "B" + j;
                    }
                }
            }
            for (int i = 0; i < MAX_CONTROLLERS; i++) {
                for (int j = NORMAL_CTLR_BUTTONS; j < MAX_CTLR_BUTTONS; j++) {
                    if (control.equals(new ControllerButtonControl(i, j))) {
                        return "C" + i + "B" + j;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the Control whose name according to getControlName() is the
     * specified String, or null if no Control has that String as a name.
     * @param controlName The name of the Control to be returned
     * @return The Control whose name is the specified String
     */
    public final Control getControl(String controlName) {
        if (NAME_KEYCODES == null) {
            initKeycodeData();
        }
        Integer keycode = NAME_KEYCODES.get(controlName);
        if (keycode != null) {
            return new KeyControl(keycode);
        } else if (controlName.equals("LMB")) {
            return new MouseButtonControl(Input.MOUSE_LEFT_BUTTON);
        } else if (controlName.equals("RMB")) {
            return new MouseButtonControl(Input.MOUSE_RIGHT_BUTTON);
        } else if (controlName.equals("MMB")) {
            return new MouseButtonControl(Input.MOUSE_MIDDLE_BUTTON);
        } else if (controlName.length() > 0 && controlName.charAt(0) == 'C') {
            int i = 1;
            char c;
            while (true) {
                c = controlName.charAt(i);
                if (!Character.isDigit(c)) {
                    break;
                }
                i++;
                if (i == controlName.length()) {
                    return null;
                }
            }
            String controllerNumStr = controlName.substring(1, i);
            int controllerNum;
            try {
                controllerNum = Integer.parseInt(controllerNumStr);
            } catch (NumberFormatException e) {
                return null;
            }
            if (controllerNum < 0 || controllerNum >= MAX_CONTROLLERS
                    || (controllerNumStr.charAt(0) == '0' && controllerNumStr.length() > 1)) {
                return null;
            }
            if (c == 'B') {
                i++;
                int j = i;
                while (j < controlName.length()) {
                    c = controlName.charAt(j);
                    if (!Character.isDigit(c)) {
                        return null;
                    }
                    j++;
                }
                String buttonNumStr = controlName.substring(i, j);
                int buttonNum;
                try {
                    buttonNum = Integer.parseInt(buttonNumStr);
                } catch (NumberFormatException e) {
                    return null;
                }
                if (buttonNum < 0 || buttonNum >= MAX_CTLR_BUTTONS
                        || (buttonNumStr.charAt(0) == '0' && buttonNumStr.length() > 1)) {
                    return null;
                }
                return new ControllerButtonControl(controllerNum, buttonNum);
            }
            switch (controlName.substring(i)) {
                case "Up":
                    return new ControllerDirectionControl(controllerNum, ControllerDirectionControl.UP);
                case "Dwn":
                    return new ControllerDirectionControl(controllerNum, ControllerDirectionControl.DOWN);
                case "Lft":
                    return new ControllerDirectionControl(controllerNum, ControllerDirectionControl.LEFT);
                case "Rgt":
                    return new ControllerDirectionControl(controllerNum, ControllerDirectionControl.RIGHT);
            }
        }
        return null;
    }
    
    /**
     * Returns whether the specified command was pressed this frame; that is,
     * whether it transitioned from not being held to being held.
     * @param commandNum The number of the command to examine
     * @return Whether the specified command was pressed this frame
     */
    public final boolean commandPressed(int commandNum) {
        return commandStates[commandNum] == CommandState.PRESSED
                || commandStates[commandNum] == CommandState.TAPPED
                || commandStates[commandNum] == CommandState.UNTAPPED;
    }
    
    /**
     * Returns whether the specified command is being held this frame.
     * @param commandNum The number of the command to examine
     * @return Whether the specified command is being held this frame
     */
    public final boolean commandHeld(int commandNum) {
        return commandStates[commandNum] == CommandState.PRESSED
                || commandStates[commandNum] == CommandState.HELD
                || commandStates[commandNum] == CommandState.TAPPED;
    }
    
    /**
     * Returns whether the specified command was released this frame; that is,
     * whether it transitioned from being held to not being held.
     * @param commandNum The number of the command to examine
     * @return Whether the specified command was released this frame
     */
    public final boolean commandReleased(int commandNum) {
        return commandStates[commandNum] == CommandState.RELEASED
                || commandStates[commandNum] == CommandState.TAPPED
                || commandStates[commandNum] == CommandState.UNTAPPED;
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
     * Returns the maximum length in characters of the String that this CellGame
     * is being used to type, or 0 if this CellGame is not being used to type a
     * String.
     * @return The maximum length of the String that this CellGame is being used
     * to type
     */
    public final int getMaxTypingStringLength() {
        return maxTypingStringLength;
    }
    
    /**
     * Instructs this CellGame to interpret all inputs as typing a String with a
     * specified maximum length until further notice. While typing, the
     * Backspace key deletes individual characters, the Delete key resets the
     * String to the empty string, the Escape key calls cancelTypingString(),
     * and the Enter key finishes the typing and calls the
     * stringFinishedActions() method of this CellGame's current CellGameState.
     * This method will throw an Exception if this CellGame has already been
     * instructed to bind the next control pressed to a specified command.
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
     * cancelTypingString(), and the Enter key finishes the typing and calls the
     * stringFinishedActions() method of this CellGame's current CellGameState.
     * This method will throw an Exception if this CellGame has already been
     * instructed to bind the next control pressed to a specified command.
     * @param initialString The initial value of the String to be typed
     * @param maxLength The maximum length in characters of the String to be
     * typed
     */
    public final void beginTypingString(String initialString, int maxLength) {
        if (maxLength <= 0) {
            throw new RuntimeException("Attempted to begin typing to a String with non-positive maximum length " + maxLength);
        }
        if (bindingCommandNum >= 0) {
            throw new RuntimeException("Attempted to begin typing to a String while already binding to command number " + bindingCommandNum);
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
        if (currentState != null) {
            currentState.stringBeganActions(currentState.game, initialString);
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
            if (currentState != null) {
                currentState.stringCanceledActions(currentState.game, s);
            }
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
     * @param fps The new number of frames per second
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
     * Returns whether this CellGame displays on its screen the number of frames
     * that it executes per second.
     * @return Whether this CellGame displays the number of frames that it
     * executes per second
     */
    public final boolean isShowingFPS() {
        return showFPS;
    }
    
    /**
     * Sets whether this CellGame displays on its screen the number of frames
     * that it executes per second.
     * @param showFPS Whether this CellGame should display the number of frames
     * that it executes per second
     */
    public final void setShowFPS(boolean showFPS) {
        this.showFPS = showFPS;
        if (game.getContainer() != null) {
            game.getContainer().setShowFPS(showFPS);
        }
    }
    
    /**
     * Returns the width in pixels of this CellGame's screen.
     * @return The width in pixels of this CellGame's screen
     */
    public final int getScreenWidth() {
        return screenWidth;
    }
    
    /**
     * Sets the width in pixels of this CellGame's screen to the specified
     * value.
     * @param screenWidth The new screen width
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
     * @param screenHeight The new screen height
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
     * @param scaleFactor The new screen scale factor
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
    
    /**
     * Returns the greatest priority in this CellGame's music stack to which any
     * Music tracks are assigned, or 0 if the music stack is empty. The Music
     * tracks assigned to this priority are those that are currently playing.
     * @return The greatest music stack priority to which any Music tracks are
     * assigned
     */
    public final int getMusicStackTop() {
        return (musicStack.isEmpty() ? 0 : musicStack.lastKey());
    }
    
    /**
     * Returns the Set of Music tracks that are assigned to the specified
     * priority in this CellGame's music stack, or an empty Set if the music
     * stack is empty. Changes to the returned Set will not be reflected in the
     * music stack.
     * @param priority The priority of the Music tracks to return
     * @return The Set of Music tracks assigned to the specified priority
     */
    public final Set<Music> getMusicTracks(int priority) {
        Map<Music,MusicInstance> musics = musicStack.get(priority);
        return (musics == null ? new HashSet<>() : new HashSet<>(musics.keySet()));
    }
    
    /**
     * Returns the Set of Music tracks that are assigned to the greatest
     * priority in this CellGame's music stack, or an empty Set if the music
     * stack is empty. These Music tracks are those that are currently playing.
     * Changes to the returned Set will not be reflected in the music stack.
     * @return The Set of Music tracks assigned to the music stack's greatest
     * priority
     */
    public final Set<Music> getMusicTracks() {
        return getMusicTracks(getMusicStackTop());
    }
    
    /**
     * Returns the Music track assigned to the specified priority in this
     * CellGame's music stack, if there is exactly one such Music track, or null
     * otherwise.
     * @param priority The priority of the Music track to return
     * @return The Music track assigned to the specified priority
     */
    public final Music getMusic(int priority) {
        Map<Music,MusicInstance> musics = musicStack.get(priority);
        if (musics != null && musics.size() == 1) {
            for (Music music : musics.keySet()) {
                return music;
            }
        }
        return null;
    }
    
    /**
     * Returns the Music track assigned to the greatest priority in this
     * CellGame's music stack, if the music stack is not empty and there is
     * exactly one such Music track, or null otherwise.
     * @return The Music track assigned to the music stack's greatest priority
     */
    public final Music getMusic() {
        return getMusic(getMusicStackTop());
    }
    
    /**
     * Returns whether the specified Music track is assigned to the specified
     * priority in this CellGame's music stack.
     * @param priority The priority to examine
     * @param music The Music track to search for
     * @return Whether the specified Music track is assigned to the specified
     * priority in the music stack
     */
    public final boolean musicIsAtPriority(int priority, Music music) {
        Map<Music,MusicInstance> musics = musicStack.get(priority);
        return musics != null && musics.containsKey(music);
    }
    
    /**
     * Adds the specified Music track to the specified priority in this
     * CellGame's music stack. If that priority is the music stack's greatest,
     * the Music track will automatically begin playing.
     * @param priority The priority to assign the specified Music track to
     * @param music The Music track to add
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     * @param replace If true, all other Music tracks assigned to the specified
     * priority will be removed from it
     */
    public final void addMusic(int priority, Music music, boolean loop, boolean replace) {
        addMusic(priority, music, 1, 1, loop, replace);
    }
    
    /**
     * Adds the specified Music track to the specified priority in this
     * CellGame's music stack. If that priority is the music stack's greatest,
     * the Music track will automatically begin playing.
     * @param priority The priority to assign the specified Music track to
     * @param music The Music track to add
     * @param speed The speed at which to play the specified Music track when it
     * plays, with 1 representing no speed change
     * @param volume The volume at which to play the specified Music track when
     * it plays, with 1 representing no volume change
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     * @param replace If true, all other Music tracks assigned to the specified
     * priority will be removed from it
     */
    public final void addMusic(int priority, Music music,
            double speed, double volume, boolean loop, boolean replace) {
        if (!musicStack.isEmpty()) {
            int top = musicStack.lastKey();
            if (priority > top || (replace && priority == top)) {
                Map<Music,MusicInstance> topMusics = musicStack.get(top);
                for (Music topMusic : topMusics.keySet()) {
                    topMusic.stop();
                }
            }
        }
        Map<Music,MusicInstance> currentMusics = musicStack.get(priority);
        if (currentMusics == null) {
            currentMusics = new HashMap<>();
            musicStack.put(priority, currentMusics);
        } else if (replace) {
            currentMusics.clear();
        }
        MusicInstance instance = new MusicInstance(speed, volume, loop);
        currentMusics.put(music, instance);
        if (priority == musicStack.lastKey()) {
            music.play(instance);
        }
    }
    
    /**
     * Adds the specified Music track to the specified priority in this
     * CellGame's music stack, replacing and removing all other Music tracks
     * assigned to that priority. If that priority is the music stack's
     * greatest, the Music track will automatically begin playing.
     * @param priority The priority to assign the specified Music track to
     * @param music The Music track to add
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     */
    public final void addMusic(int priority, Music music, boolean loop) {
        addMusic(priority, music, 1, 1, loop, true);
    }
    
    /**
     * Adds the specified Music track to the specified priority in this
     * CellGame's music stack, replacing and removing all other Music tracks
     * assigned to that priority. If that priority is the music stack's
     * greatest, the Music track will automatically begin playing.
     * @param priority The priority to assign the specified Music track to
     * @param music The Music track to add
     * @param speed The speed at which to play the specified Music track when it
     * plays, with 1 representing no speed change
     * @param volume The volume at which to play the specified Music track when
     * it plays, with 1 representing no volume change
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     */
    public final void addMusic(int priority, Music music, double speed, double volume, boolean loop) {
        addMusic(priority, music, speed, volume, loop, true);
    }
    
    /**
     * Adds the specified Music track to the greatest priority in this
     * CellGame's music stack, or to priority 0 if the music stack is empty. The
     * Music track will automatically begin playing.
     * @param music The Music track to add
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     * @param replace If true, all other Music tracks assigned to the specified
     * priority will be removed from it
     */
    public final void addMusic(Music music, boolean loop, boolean replace) {
        addMusic(getMusicStackTop(), music, 1, 1, loop, replace);
    }
    
    /**
     * Adds the specified Music track to the greatest priority in this
     * CellGame's music stack, or to priority 0 if the music stack is empty. The
     * Music track will automatically begin playing.
     * @param music The Music track to add
     * @param speed The speed at which to play the specified Music track when it
     * plays, with 1 representing no speed change
     * @param volume The volume at which to play the specified Music track when
     * it plays, with 1 representing no volume change
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     * @param replace If true, all other Music tracks assigned to the specified
     * priority will be removed from it
     */
    public final void addMusic(Music music, double speed, double volume, boolean loop, boolean replace) {
        addMusic(getMusicStackTop(), music, speed, volume, loop, replace);
    }
    
    /**
     * Adds the specified Music track to the greatest priority in this
     * CellGame's music stack, replacing and removing all other Music tracks
     * assigned to that priority, or to priority 0 if the music stack is empty.
     * The Music track will automatically begin playing.
     * @param music The Music track to add
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     */
    public final void addMusic(Music music, boolean loop) {
        addMusic(getMusicStackTop(), music, 1, 1, loop, true);
    }
    
    /**
     * Adds the specified Music track to the greatest priority in this
     * CellGame's music stack, replacing and removing all other Music tracks
     * assigned to that priority, or to priority 0 if the music stack is empty.
     * The Music track will automatically begin playing.
     * @param music The Music track to add
     * @param speed The speed at which to play the specified Music track when it
     * plays, with 1 representing no speed change
     * @param volume The volume at which to play the specified Music track when
     * it plays, with 1 representing no volume change
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     */
    public final void addMusic(Music music, double speed, double volume, boolean loop) {
        addMusic(getMusicStackTop(), music, speed, volume, loop, true);
    }
    
    private void removeMusicPriority(int priority) {
        int oldTop = musicStack.lastKey();
        musicStack.remove(priority);
        if (priority == oldTop && !musicStack.isEmpty()) {
            for (Map.Entry<Music,MusicInstance> entry : musicStack.get(musicStack.lastKey()).entrySet()) {
                entry.getKey().play(entry.getValue());
            }
        }
    }
    
    /**
     * Removes the specified Music track from the specified priority in this
     * CellGame's music stack if it is currently assigned to that priority. If
     * that priority is the music stack's greatest, the specified Music track
     * will automatically stop.
     * @param priority The priority from which to remove the specified Music
     * track
     * @param music The Music track to remove
     */
    public final void removeMusic(int priority, Music music) {
        if (!musicStack.isEmpty()) {
            Map<Music,MusicInstance> musics = musicStack.get(priority);
            if (musics != null && musics.containsKey(music)) {
                music.stop();
                musics.remove(music);
                if (musics.isEmpty()) {
                    removeMusicPriority(priority);
                }
            }
        }
    }
    
    /**
     * Removes from the specified priority in this CellGame's music stack all of
     * the Music tracks currently assigned to it. If that priority is the music
     * stack's greatest, the removed Music tracks will automatically stop.
     * @param priority The priority from which to remove all Music tracks
     */
    public final void removeMusic(int priority) {
        if (!musicStack.isEmpty()) {
            Map<Music,MusicInstance> musics = musicStack.get(priority);
            if (musics != null) {
                for (Music music : musics.keySet()) {
                    music.stop();
                }
                removeMusicPriority(priority);
            }
        }
    }
    
    /**
     * Removes the specified Music track from the greatest priority in this
     * CellGame's music stack if it is currently assigned to that priority. The
     * specified Music track will automatically stop.
     * @param music The Music track to remove
     */
    public final void removeMusic(Music music) {
        removeMusic(getMusicStackTop(), music);
    }
    
    /**
     * Removes from the greatest priority in this CellGame's music stack all of
     * the Music tracks currently assigned to it, if the music stack is not
     * empty. The removed Music tracks will automatically stop.
     */
    public final void removeMusic() {
        removeMusic(getMusicStackTop());
    }
    
    /**
     * Removes from this CellGame's music stack all Music tracks assigned to any
     * priority.
     */
    public final void removeAllMusic() {
        if (!musicStack.isEmpty()) {
            Map<Music,MusicInstance> musics = musicStack.get(getMusicStackTop());
            for (Music music : musics.keySet()) {
                music.stop();
            }
            musicStack.clear();
        }
    }
    
}
