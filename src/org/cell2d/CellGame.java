package org.cell2d;

import org.cell2d.celick.GameContainer;
import org.cell2d.celick.Graphics;
import org.cell2d.celick.SlickException;
import org.cell2d.celick.state.BasicGameState;
import org.cell2d.celick.state.StateBasedGame;
import org.cell2d.celick.state.transition.Transition;
import org.cell2d.celick.util.Log;
import org.cell2d.control.Control;
import org.cell2d.control.ControllerButtonControl;
import org.cell2d.control.ControllerDirectionControl;
import org.cell2d.control.InvalidControlException;
import org.cell2d.control.KeyControl;
import org.cell2d.control.MouseButtonControl;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

/**
 * <p>A CellGame is a game made with Cell2D. A certain number of times per
 * second, a CellGame executes a <i>frame</i>, in which it processes input,
 * updates the logic of the game, and renders visuals.</p>
 * 
 * <p>A CellGame has one or more GameStates, each with a non-negative integer ID
 * that is unique within the CellGame. A CellGame is in exactly one of these
 * GameStates at any given time, and can transition between them. Each GameState
 * has its own actions to take every frame and in response to specific events,
 * but it only takes these actions while the CellGame is in it. If a GameState
 * is created with an ID that another GameState of the same CellGame already
 * has, the old GameState can no longer be entered.</p>
 * 
 * <p>A CellGame renders visuals on a rectangular grid of pixels called its
 * <i>screen</i>. Points on the screen have x-coordinates that increase from
 * left to right, as well as y-coordinates that increase from top to bottom. The
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
 * <i>commands</i>, numbered from 0 to getNumCommands() - 1 inclusive, as well
 * as the position of the mouse cursor on the screen and the movement of the
 * mouse wheel. Controls, which represent keys, controller buttons, etc. may be
 * bound to at most one command at a time so that when they are pressed, held,
 * and released, so too are the commands to which they are bound. If an attempt
 * is made to bind a Control that is already bound, it will be unbound from its
 * current command first. A CellGame considers Controls to be the same Control
 * if they are equal - that is, if they represent the same input. A CellGame
 * also allows for the temporary processing of input as assignments of Controls
 * to specific commands, or as the typing of text to a specific String.</p>
 * 
 * <p>A CellGame also controls the playing and stopping of Music tracks. It
 * contains a data structure called a <i>music stack</i> in which different
 * integer priority values may be assigned one or more Music tracks each. Only
 * the Music tracks assigned to the greatest priority in the stack will play at
 * any given time. If a currently playing Music track finishes, it will
 * automatically be removed from the top of the music stack.</p>
 * @see GameState
 * @see Control
 * @see Music
 * @author Andrew Heyman
 */
public abstract class CellGame {
    
    /**
     * The version number of Cell2D, currently 2.0.0.
     */
    public static final String VERSION = "2.0.0";
    
    private static class CommandState {
        
        private int numControlsHeld;
        private boolean pressed, released;
        
        private CommandState() {
            reset();
        }
        
        private void reset() {
            numControlsHeld = 0;
            pressed = false;
            released = false;
        }
        
    }
    
    /**
     * Loads the native libraries that are necessary for LWJGL 2, and thus
     * Slick2D, and thus Cell2D, to run. This method should be called exactly
     * once before any CellGames are created.
     * @param path The relative path to the folder containing the native
     * libraries
     */
    public static void loadNatives(String path) {
        System.setProperty("java.library.path", path);
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (IllegalAccessException | IllegalArgumentException
                | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException("Failed to load native libraries");
        }
        System.setProperty("org.lwjgl.librarypath", new File(path).getAbsolutePath());
    }
    
    private static void create() {
        try {
            if (!AL.isCreated()) {
                AL.create();
            }
            if (!Keyboard.isCreated()) {
                Keyboard.create();
            }
            if (!Mouse.isCreated()) {
                Mouse.create();
            }
            if (!Controllers.isCreated()) {
                Controllers.create();
            }
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void destroy() {
        if (AL.isCreated()) {
            AL.destroy();
        }
        if (Keyboard.isCreated()) {
            Keyboard.destroy();
        }
        if (Mouse.isCreated()) {
            Mouse.destroy();
        }
        if (Controllers.isCreated()) {
            Controllers.destroy();
        }
    }
    
    /**
     * Starts a CellGame. Once the started CellGame is closed, the program will
     * end.
     * @param game The CellGame to start
     */
    public static void startGame(CellGame game) {
        Log.info("Cell2D Version: " + VERSION);
        try {
            GameContainer container = new GameContainer(game.game);
            game.updateScreen(container);
            container.setTargetFrameRate(game.fps);
            container.setShowFPS(game.showFPS);
            if (game.iconPath != null) {
                container.setIcon(game.iconPath);
            }
            container.setAlwaysRender(true);
            container.start();
        } catch (SlickException e) {
            throw new RuntimeException("Failed to start a CellGame");
        }
    }
    
    private boolean closeRequested = false;
    private final StateBasedGame game;
    private final Map<Integer,GameState> states = new HashMap<>();
    private GameState currentState = null;
    private boolean loaded = false;
    private final CommandState[] commandStates;
    private final Set<Control> controlsHeld = new HashSet<>();
    private final Map<Integer,Set<Direction>> controllerDirections = new HashMap<>();
    private List<Set<Control>> commandControls;
    private final Map<Control,Integer> controlCommands = new HashMap<>();
    private int bindingCommandNum = -1;
    private int mouseX = 0;
    private int mouseY = 0;
    private int mouseWheelChange = 0;
    private String typingString = null;
    private int maxTypingStringLength = 0;
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
    private final String iconPath;
    private boolean loadingVisualsRendered = false;
    private final SortedMap<Integer,Map<Music,MusicInstance>> musicStack = new TreeMap<>();
    
    /**
     * Constructs a CellGame.
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
     * @param iconPath The relative path to the image file that this CellGame's
     * program window should use as its icon, or null if the window should use
     * the default LWJGL 2 icon
     */
    public CellGame(String name, int numCommands, int fps,
            int screenWidth, int screenHeight, double scaleFactor, boolean fullscreen, String iconPath) {
        game = new Game(name);
        if (numCommands < 0) {
            throw new RuntimeException("Attempted to create a CellGame with a negative number of commands");
        }
        commandStates = new CommandState[numCommands];
        commandControls = new ArrayList<>(numCommands);
        for (int i = 0; i < numCommands; i++) {
            commandStates[i] = new CommandState();
            commandControls.add(new HashSet<>());
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
        this.iconPath = iconPath;
    }
    
    /**
     * Instructs this CellGame to close itself the next time it finishes a game
     * logic update.
     */
    public final void close() {
        closeRequested = true;
    }
    
    private void resetCommands() {
        for (CommandState commandState : commandStates) {
            commandState.reset();
        }
        controlsHeld.clear();
        controllerDirections.clear();
    }
    
    private void updateScreen(GameContainer container) throws SlickException {
        updateScreen = false;
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
                container.setDisplayMode(newWidth, newHeight, true);
                resetCommands();
                return;
            }
        }
        effectiveScaleFactor = scaleFactor;
        screenXOffset = 0;
        screenYOffset = 0;
        container.setDisplayMode((int)(screenWidth*scaleFactor), (int)(screenHeight*scaleFactor), false);
        resetCommands();
    }
    
    private void updateControl(Control control, boolean pressed) {
        if (bindingCommandNum >= 0) {
            if (pressed) {
                finishBindToCommand(control);
            }
        } else if (typingString == null) {
            int commandNum = controlCommands.getOrDefault(control, -1);
            if (commandNum < 0) {
                return;
            }
            CommandState commandState = commandStates[commandNum];
            if (pressed) {
                if (controlsHeld.add(control)) {
                    if (commandState.numControlsHeld == 0) {
                        commandState.pressed = true;
                    }
                    commandState.numControlsHeld++;
                }
            } else {
                if (controlsHeld.remove(control)) {
                    commandState.numControlsHeld--;
                    if (commandState.numControlsHeld == 0) {
                        commandState.released = true;
                    }
                }
            }
        }
    }
    
    private void updateInput() {
        for (CommandState commandState : commandStates) {
            commandState.pressed = false;
            commandState.released = false;
        }
        Keyboard.poll();
        Mouse.poll();
        Controllers.poll();
        while (Keyboard.next()) {
            int keyCode = Keyboard.getEventKey();
            boolean keyPressed = Keyboard.getEventKeyState();
            if (typingString != null) {
                if (keyPressed) {
                    String s;
                    switch (keyCode) {
                        case KeyControl.KEY_ESCAPE:
                            cancelTypingString();
                            break;
                        case KeyControl.KEY_BACK:
                            if (typingString.length() > 0) {
                                char toDelete = typingString.charAt(typingString.length() - 1);
                                typingString = typingString.substring(0, typingString.length() - 1);
                                if (currentState != null) {
                                    currentState.charDeletedActions(currentState.game, toDelete);
                                }
                            }
                            break;
                        case KeyControl.KEY_DELETE:
                            s = typingString;
                            typingString = "";
                            if (currentState != null) {
                                currentState.stringDeletedActions(currentState.game, s);
                            }
                            break;
                        case KeyControl.KEY_ENTER:
                            s = typingString;
                            typingString = null;
                            maxTypingStringLength = 0;
                            if (currentState != null) {
                                currentState.stringFinishedActions(currentState.game, s);
                            }
                            break;
                        default:
                            char c = Keyboard.getEventCharacter();
                            if (c != '\u0000' && typingString.length() < maxTypingStringLength) {
                                typingString += c;
                                if (currentState != null) {
                                    currentState.charTypedActions(currentState.game, c);
                                }
                            }
                            break;
                    }
                }
            } else {
                try {
                    updateControl(new KeyControl(keyCode), keyPressed);
                } catch (InvalidControlException e) {}
            }
        }
        mouseWheelChange = 0;
        while (Mouse.next()) {
            mouseWheelChange += Mouse.getEventDWheel();
            int buttonNum = Mouse.getEventButton();
            if (buttonNum >= 0) {
                boolean buttonPressed = Mouse.getEventButtonState();
                try {
                    updateControl(new MouseButtonControl(buttonNum), buttonPressed);
                } catch (InvalidControlException e) {}
            }
        }
        mouseX = Math.min(Math.max(
                (int)(Mouse.getX()/effectiveScaleFactor) - screenXOffset, 0), screenWidth - 1);
        mouseY = Math.min(Math.max(
                (int)(Mouse.getY()/effectiveScaleFactor) - screenYOffset, 0), screenHeight - 1);
        while (Controllers.next()) {
            Controller controller = Controllers.getEventSource();
            int controllerNum = controller.getIndex();
            if (Controllers.isEventButton()) {
                int buttonNum = Controllers.getEventControlIndex();
                boolean buttonPressed = Controllers.getEventButtonState();
                try {
                    updateControl(new ControllerButtonControl(controllerNum, buttonNum), buttonPressed);
                } catch (InvalidControlException e) {}
            } else if (Controllers.isEventXAxis() || Controllers.isEventYAxis()) {
                float x = Controllers.getEventXAxisValue();
                float y = Controllers.getEventYAxisValue();
                double angle = Math.toDegrees(Math.atan2(-y, x)) % 360;
                if (angle < 0) {
                    angle += 360;
                }
                float deadZoneX = controller.getXAxisDeadZone();
                float deadZoneY = controller.getYAxisDeadZone();
                Set<Direction> directions = EnumSet.noneOf(Direction.class);
                if (x > deadZoneX && (angle < 67.5 || angle > 292.5)) {
                    directions.add(Direction.RIGHT);
                } else if (x < -deadZoneX && angle > 112.5 && angle < 247.5) {
                    directions.add(Direction.LEFT);
                }
                if (y > deadZoneY && angle > 202.5 && angle < 337.5) {
                    directions.add(Direction.DOWN);
                } else if (y < -deadZoneY && angle > 22.5 && angle < 157.5) {
                    directions.add(Direction.UP);
                }
                Set<Direction> oldDirections = controllerDirections.getOrDefault(
                        controllerNum, EnumSet.noneOf(Direction.class));
                controllerDirections.put(controllerNum, directions);
                for (Direction direction : Direction.values()) {
                    if (directions.contains(direction)) {
                        if (!oldDirections.contains(direction)) {
                            try {
                                updateControl(
                                        new ControllerDirectionControl(controllerNum, direction), true);
                            } catch (InvalidControlException e) {}
                        }
                    } else if (oldDirections.contains(direction)) {
                        try {
                            updateControl(new ControllerDirectionControl(controllerNum, direction), false);
                        } catch (InvalidControlException e) {}
                    }
                }
            }
        }
    }
    
    private class Game extends StateBasedGame {
        
        private Game(String name) {
            super(name);
        }
        
        @Override
        public final void initStatesList(GameContainer container) throws SlickException {
            game.addState(new LoadingState());
        }
        
        @Override
        public final void postUpdateState(GameContainer container, int delta) throws SlickException {
            if (loaded) {
                //Update music stack
                if (!musicStack.isEmpty()) {
                    int top = musicStack.lastKey();
                    Map<Music,MusicInstance> musics = musicStack.get(top);
                    Iterator<Music> iterator = musics.keySet().iterator();
                    while (iterator.hasNext()) {
                        Music music = iterator.next();
                        if (music.update(delta)) {
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
                //Execute a frame if it's been long enough since the last one
                msToRun += Math.min(delta, msPerFrame);
                if (msToRun >= msPerFrame) {
                    updateInput();
                    currentState.doFrame();
                    msToRun -= msPerFrame;
                    if (closeRequested) {
                        destroy();
                        container.exit();
                    } else if (updateScreen) {
                        updateScreen(container);
                    }
                }
            } else if (loadingVisualsRendered) {
                initActions();
                if (currentState == null) {
                    throw new RuntimeException("A CellGame did not enter any of its GameStates during"
                            + " initialization");
                }
                loaded = true;
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
        
    }
    
    private class LoadingState extends BasicGameState {
        
        private LoadingState() {}
        
        @Override
        public int getID() {
            return -1;
        }
        
        @Override
        public final void init(GameContainer container, StateBasedGame game) throws SlickException {
            create();
        }
        
        @Override
        public final void update(
                GameContainer container, StateBasedGame game, int delta) throws SlickException {}
        
        @Override
        public final void render(
                GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
            if (!loadingVisualsRendered) {
                renderLoadingVisuals(g, screenXOffset, screenYOffset,
                        screenXOffset + screenWidth, screenYOffset + screenHeight);
                loadingVisualsRendered = true;
            }
        }
        
    }
    
    private class State extends BasicGameState {
        
        private final GameState state;
        
        private State(GameState state) {
            this.state = state;
        }
        
        @Override
        public int getID() {
            return state.getID();
        }
        
        @Override
        public final void init(GameContainer container, StateBasedGame game) throws SlickException {}
        
        @Override
        public final void update(
                GameContainer container, StateBasedGame game, int delta) throws SlickException {}
        
        @Override
        public final void render(
                GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
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
    
    /**
     * Returns this CellGame's GameState with the specified ID, or null if there
     * is none.
     * @param id The ID of the GameState to return
     * @return The GameState with the specified ID
     */
    public final GameState getState(int id) {
        return (id < 0 ? null : states.get(id));
    }
    
    /**
     * Returns the GameState that this CellGame is currently in.
     * @return The GameState that this CellGame is currently in
     */
    public final GameState getCurrentState() {
        return currentState;
    }
    
    final void addState(GameState state) {
        int id = state.getID();
        if (id < 0) {
            throw new RuntimeException("Attempted to add a GameState with negative ID " + id);
        }
        states.put(id, state);
        game.addState(new State(state));
    }
    
    /**
     * Instructs this CellGame to enter its GameState with the specified ID at
     * the end of the current frame. If this CellGame has no GameState with the
     * specified ID, this method will do nothing.
     * @param id The ID of the GameState to enter
     */
    public final void enterState(int id) {
        enterState(id, null, null);
    }
    
    /**
     * Instructs this CellGame to enter its GameState with the specified ID,
     * using the specified Slick2D Transitions when leaving the current
     * GameState and entering the new one, at the end of the current frame.
     * If this CellGame has no GameState with the specified ID, this method will
     * do nothing.
     * @param id The ID of the GameState to enter
     * @param leave The Transition to use when leaving the current GameState
     * @param enter The Transition to use when entering the new GameState
     */
    public final void enterState(int id, Transition leave, Transition enter) {
        if (id < 0) {
            return;
        }
        GameState state = states.get(id);
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
     * entering its first GameState. This should include creating at least one
     * GameState for it, binding Controls to its commands, loading assets, etc.
     * enterState() must be called during this method to tell the CellGame which
     * GameState to start out in, or an Exception will be thrown.
     */
    public abstract void initActions();
    
    /**
     * Actions for this CellGame to take each frame to render visuals after its
     * current GameState has finished rendering.
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
        return commandStates.length;
    }
    
    /**
     * Returns an unmodifiable Set view of all of the Controls that are bound to
     * the specified command.
     * @param commandNum The number of the command whose controls are to be
     * returned
     * @return The Controls that are bound to the specified command
     */
    public final Set<Control> getControlsFor(int commandNum) {
        if (commandNum < 0 || commandNum >= commandStates.length) {
            throw new RuntimeException("Attempted to get the Controls for nonexistent command number "
                    + commandNum);
        }
        return Collections.unmodifiableSet(commandControls.get(commandNum));
    }
    
    /**
     * Binds the specified Control to the specified command. If the Control is
     * already bound to a command, it will be unbound first.
     * @param commandNum The command to bind the specified Control to
     * @param control The Control to bind to the specified command
     */
    public final void bindControl(int commandNum, Control control) {
        if (commandNum < 0 || commandNum >= commandStates.length) {
            throw new RuntimeException("Attempted to bind to nonexistent command number " + commandNum);
        }
        int oldCommandNum = controlCommands.getOrDefault(control, -1);
        if (oldCommandNum >= 0) {
            commandControls.get(oldCommandNum).remove(control);
        }
        commandControls.get(commandNum).add(control);
        controlCommands.put(control, commandNum);
    }
    
    /**
     * Unbinds the specified Control from its command, if it is bound to one.
     * @param control The Control to be unbound
     */
    public final void unbindControl(Control control) {
        int oldCommandNum = controlCommands.getOrDefault(control, -1);
        if (oldCommandNum >= 0) {
            commandControls.get(oldCommandNum).remove(control);
            controlCommands.remove(control);
        }
    }
    
    /**
     * Unbinds all of the specified command's Controls from it.
     * @param commandNum The number of the command whose Controls are to be
     * unbound
     */
    public final void clearControls(int commandNum) {
        if (commandNum < 0 || commandNum >= commandStates.length) {
            throw new RuntimeException("Attempted to clear nonexistent command number " + commandNum);
        }
        for (Control control : commandControls.get(commandNum)) {
            controlCommands.remove(control);
        }
        commandControls.get(commandNum).clear();
    }
    
    /**
     * Returns the number of the command to which this CellGame has been
     * instructed to bind the next valid Control pressed, or -1 if there is
     * none.
     * @return The number of the command to which this CellGame has been
     * instructed to bind the next valid Control pressed
     */
    public final int getBindingCommandNum() {
        return bindingCommandNum;
    }
    
    /**
     * Instructs this CellGame to bind the next valid Control pressed to the
     * specified command. After that Control is bound, the bindFinishedActions()
     * method of this CellGame's current GameState will be called. This method
     * will throw an Exception if this CellGame is already being used to type a
     * String.
     * @param commandNum The number of the command to which the next valid
     * Control pressed should be bound
     */
    public final void waitToBindToCommand(int commandNum) {
        if (commandNum < 0 || commandNum >= commandStates.length) {
            throw new RuntimeException("Attempted to begin waiting to bind to nonexistent command number " + commandNum);
        }
        if (typingString != null) {
            throw new RuntimeException("Attempted to begin waiting to bind to command number " + commandNum + " while already typing to a String");
        }
        bindingCommandNum = commandNum;
        resetCommands();
    }
    
    private void finishBindToCommand(Control control) {
        bindControl(bindingCommandNum, control);
        int boundCommandNum = bindingCommandNum;
        bindingCommandNum = -1;
        if (currentState != null) {
            currentState.bindFinishedActions(currentState.game, boundCommandNum);
        }
    }
    
    /**
     * Cancels this CellGame's instruction to bind the next valid Control
     * pressed to a specified command, if it has been instructed to do so.
     */
    public final void cancelBindToCommand() {
        bindingCommandNum = -1;
    }
    
    /**
     * Returns whether the specified command was pressed this frame. This is the
     * case if one of the command's bound Controls was pressed while none of its
     * other Controls were held.
     * @param commandNum The number of the command to examine
     * @return Whether the specified command was pressed this frame
     */
    public final boolean commandPressed(int commandNum) {
        return commandStates[commandNum].pressed;
    }
    
    /**
     * Returns whether the specified command is being held this frame. This is
     * the case if any of the command's bound Controls are being held.
     * @param commandNum The number of the command to examine
     * @return Whether the specified command is being held this frame
     */
    public final boolean commandHeld(int commandNum) {
        return commandStates[commandNum].numControlsHeld > 0;
    }
    
    /**
     * Returns whether the specified command was released this frame. This is
     * the case if one of the command's bound Controls was released while none
     * of its other Controls were held.
     * @param commandNum The number of the command to examine
     * @return Whether the specified command was released this frame
     */
    public final boolean commandReleased(int commandNum) {
        return commandStates[commandNum].released;
    }
    
    /**
     * Returns the x-coordinate in pixels of the mouse cursor on this CellGame's
     * screen.
     * @return The x-coordinate in pixels of the mouse cursor on this CellGame's
     * screen
     */
    public final int getMouseX() {
        return mouseX;
    }
    
    /**
     * Returns the y-coordinate in pixels of the mouse cursor on this CellGame's
     * screen.
     * @return The y-coordinate in pixels of the mouse cursor on this CellGame's
     * screen
     */
    public final int getMouseY() {
        return mouseY;
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
     * String to the empty String, the Escape key calls cancelTypingString(),
     * and the Enter key finishes the typing and calls the
     * stringFinishedActions() method of this CellGame's current GameState. This
     * method will throw an Exception if this CellGame has already been
     * instructed to bind the next Control pressed to a specified command.
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
     * resets the String to the empty String, the Escape key calls
     * cancelTypingString(), and the Enter key finishes the typing and calls the
     * stringFinishedActions() method of this CellGame's current GameState. This
     * method will throw an Exception if this CellGame has already been
     * instructed to bind the next Control pressed to a specified command.
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
        resetCommands();
        if (currentState != null) {
            currentState.stringBeganActions(currentState.game, initialString);
        }
    }
    
    /**
     * Instructs this CellGame to stop interpreting inputs as typing a String,
     * if it was doing so, and consider the typing canceled. This will call the
     * stringCanceledActions() method of this CellGame's current GameState.
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
     * Returns the relative path to the image file that this CellGame's program
     * window uses as its icon, or null if the window uses the default LWJGL 2
     * icon.
     * @return The relative path to the image file that this CellGame's program
     * window uses as its icon
     */
    public final String getIconPath() {
        return iconPath;
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
     * Returns an unmodifiable Set view of the Music tracks that are assigned to
     * the specified priority in this CellGame's music stack, or an unmodifiable
     * empty Set if the music stack is empty.
     * @param priority The priority of the Music tracks to return
     * @return The Set of Music tracks assigned to the specified priority
     */
    public final Set<Music> getMusicTracks(int priority) {
        Map<Music,MusicInstance> musics = musicStack.get(priority);
        return (musics == null ? Collections.emptySet() : Collections.unmodifiableSet(musics.keySet()));
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
     * plays
     * @param volume The volume at which to play the specified Music track when
     * it plays
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
     * plays
     * @param volume The volume at which to play the specified Music track when
     * it plays
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
     * @param replace If true, all other Music tracks assigned to the priority
     * to which the specified Music track is added will be removed from it
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
     * plays
     * @param volume The volume at which to play the specified Music track when
     * it plays
     * @param loop If true, the Music track will loop indefinitely until
     * stopped when it plays; otherwise, it will play once
     * @param replace If true, all other Music tracks assigned to the priority
     * to which the specified Music track is added will be removed from it
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
     * plays
     * @param volume The volume at which to play the specified Music track when
     * it plays
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
     * priority, leaving the stack empty.
     */
    public final void clearMusic() {
        if (!musicStack.isEmpty()) {
            Map<Music,MusicInstance> musics = musicStack.get(getMusicStackTop());
            for (Music music : musics.keySet()) {
                music.stop();
            }
            musicStack.clear();
        }
    }
    
}
