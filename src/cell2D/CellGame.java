package cell2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javafx.util.Pair;
import javax.imageio.ImageIO;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.Color;
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

public abstract class CellGame {
    
    private static enum CommandState {
        NOTHELD, PRESSED, HELD, RELEASED, TAPPED, UNTAPPED
    }
    
    private static final Color transparent = new Color(0, 0, 0, 0);
    private static final int transparentInt = colorToInt(transparent);
    
    private boolean closeRequested = false;
    private final StateBasedGame game;
    private final Map<Integer,CellGameState> states = new HashMap<>();
    private int currentID = -2;
    private boolean negativeIDsOffLimits = false;
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
    private int mouseWheel = 0;
    private int newMouseWheel = 0;
    private String typingString = null;
    private int maxTypingStringLength = 0;
    private String typedString = null;
    private int updateFPS;
    private double msPerStep;
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
    private boolean autoLoadAssets;
    private boolean assetsInitialized = false;
    private Image loadingImage = null;
    private boolean loadingScreenRenderedOnce = false;
    private final Map<String,Filter> filters = new HashMap<>();
    private final Map<String,Sprite> sprites = new HashMap<>();
    private final Map<String,SpriteSheet> spriteSheets = new HashMap<>();
    private final Map<String,Animation> animations = new HashMap<>();
    private final Map<String,Sound> sounds = new HashMap<>();
    private final Map<String,Music> musics = new HashMap<>();
    private MusicInstance currentMusic = new MusicInstance(null, 0, 0, false);
    private final SortedMap<Integer,MusicInstance> musicStack = new TreeMap<>();
    private boolean stackOverridden = false;
    private boolean musicPaused = false;
    private float musicPosition = 0;
    private int musicFadeType = 0;
    private double fadeStartVolume = 0;
    private double fadeEndVolume = 0;
    private double fadeDuration = 0;
    private double msFading = 0;
    
    public CellGame(String gamename, int numCommands, int updateFPS,
            int screenWidth, int screenHeight, double scaleFactor,
            boolean fullscreen, boolean autoLoadAssets, String loadingImagePath) throws SlickException {
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
        setUpdateFPS(updateFPS);
        msToRun = 0;
        try {
            displayModes = Display.getAvailableDisplayModes();
        } catch (LWJGLException e) {
            throw new RuntimeException(e.toString());
        }
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        setScaleFactor(scaleFactor);
        this.fullscreen = fullscreen;
        this.autoLoadAssets = autoLoadAssets;
        if (loadingImagePath != null) {
            loadingImage = new Image(loadingImagePath);
        }
    }
    
    public static final void startGame(CellGame game) throws SlickException {
        AppGameContainer container = new AppGameContainer(game.game);
        game.updateScreen(container);
        container.setTargetFrameRate(game.getUpdateFPS());
        container.setShowFPS(false);
        container.start();
    }
    
    private static int colorToInt(Color color) {
        return (color.getAlphaByte() << 24) | (color.getRedByte() << 16) | (color.getGreenByte() << 8) | color.getBlueByte();
    }
    
    private static Color intToColor(int color) {
        return new Color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF);
    }
    
    static final Pair<Image,BufferedImage> getTransparentImage(String path, Color transColor) throws SlickException {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (bufferedImage.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            java.awt.Graphics bufferedGraphics = newImage.getGraphics();
            bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
            bufferedGraphics.dispose();
            bufferedImage = newImage;
        }
        Image image;
        if (transColor == null) {
            image = new Image(path);
        } else {
            image = new Image(width, height);
            Graphics graphics = image.getGraphics();
            Color color;
            int transR = transColor.getRedByte();
            int transG = transColor.getGreenByte();
            int transB = transColor.getBlueByte();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    color = intToColor(bufferedImage.getRGB(x, y));
                    if (color.getRedByte() == transR
                            && color.getGreenByte() == transG
                            && color.getBlueByte() == transB) {
                        color = transparent;
                        bufferedImage.setRGB(x, y, transparentInt);
                    }
                    graphics.setColor(color);
                    graphics.fillRect(x, y, 1, 1);
                }
            }
            graphics.flush();
        }
        image.setFilter(Image.FILTER_NEAREST);
        return new Pair<>(image, bufferedImage);
    }
    
    static final Pair<Image,BufferedImage> getRecoloredImage(
            BufferedImage bufferedImage, Map<Color,Color> colorMap) throws SlickException {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        java.awt.Graphics bufferedGraphics = newImage.getGraphics();
        bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
        bufferedGraphics.dispose();
        Image image = new Image(width, height);
        image.setFilter(Image.FILTER_NEAREST);
        Graphics graphics = image.getGraphics();
        int size = colorMap.size();
        int[] oldR = new int[size];
        int[] oldG = new int[size];
        int[] oldB = new int[size];
        int[] newR = new int[size];
        int[] newG = new int[size];
        int[] newB = new int[size];
        int i = 0;
        Color key, value;
        for (Map.Entry<Color,Color> entry : colorMap.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            oldR[i] = key.getRedByte();
            oldG[i] = key.getGreenByte();
            oldB[i] = key.getBlueByte();
            newR[i] = value.getRedByte();
            newG[i] = value.getGreenByte();
            newB[i] = value.getBlueByte();
            i++;
        }
        Color color;
        int colorR, colorG, colorB;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                color = intToColor(newImage.getRGB(x, y));
                colorR = color.getRedByte();
                colorG = color.getGreenByte();
                colorB = color.getBlueByte();
                for (int j = 0; j < size; j++) {
                    if (oldR[j] == colorR && oldG[j] == colorG && oldB[j] == colorB) {
                        color = new Color(newR[j], newG[j], newB[j], color.getAlphaByte());
                        newImage.setRGB(x, y, colorToInt(color));
                        break;
                    }
                }
                graphics.setColor(color);
                graphics.fillRect(x, y, 1, 1);
            }
        }
        graphics.flush();
        return new Pair<>(image, newImage);
    }
    
    static final Pair<Image,BufferedImage> getRecoloredImage(
            BufferedImage bufferedImage, Color newColor) throws SlickException {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        java.awt.Graphics bufferedGraphics = newImage.getGraphics();
        bufferedGraphics.drawImage(bufferedImage, 0, 0, null);
        bufferedGraphics.dispose();
        Image image = new Image(width, height);
        image.setFilter(Image.FILTER_NEAREST);
        Graphics graphics = image.getGraphics();
        int newColorR = newColor.getRedByte();
        int newColorG = newColor.getGreenByte();
        int newColorB = newColor.getBlueByte();
        Color color;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                color = new Color(newColorR, newColorG, newColorB, (newImage.getRGB(x, y) >> 24) & 0xFF);
                newImage.setRGB(x, y, colorToInt(color));
                graphics.setColor(color);
                graphics.fillRect(x, y, 1, 1);
            }
        }
        graphics.flush();
        return new Pair<>(image, newImage);
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
    
    public final void close() {
        closeRequested = true;
    }
    
    private class Game extends StateBasedGame implements InputProviderListener {

        private Game(String name) {
            super(name);
        }

        @Override
        public final void initStatesList(GameContainer container) throws SlickException {
            new LoadingState(-2);
            negativeIDsOffLimits = true;
            initStates();
            currentID = -2;
            enterState(-2);
        }
        
        @Override
        public final void preUpdateState(GameContainer container, int delta) {}
        
        @Override
        public final void postUpdateState(GameContainer container, int delta) throws SlickException {
            if (loadingScreenRenderedOnce) {
                if (assetsInitialized) {
                    double timeElapsed = Math.min(delta, msPerStep);
                    msToRun += timeElapsed;
                    if (currentMusic.music != null && !musicPaused) {
                        if (musicFadeType != 0) {
                            msFading = Math.min(msFading + timeElapsed, fadeDuration);
                            if (msFading == fadeDuration) {
                                currentMusic.music.setVolume((float)fadeEndVolume);
                                if (musicFadeType == 2) {
                                    currentMusic.music.stop();
                                }
                                musicFadeType = 0;
                            } else {
                                currentMusic.music.setVolume((float)(fadeStartVolume + (msFading/fadeDuration)*(fadeEndVolume - fadeStartVolume)));
                            }
                        }
                        if (!currentMusic.music.playing()) {
                            stopMusic();
                        }
                    }
                    if (msToRun >= msPerStep) {
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
                        mouseWheel = newMouseWheel;
                        newMouseWheel = 0;
                        CellGame.this.getCurrentState().doStep();
                        msToRun -= msPerStep;
                        if (closeRequested) {
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
                    initAssets();
                    assetsInitialized = true;
                    initActions();
                    CellGame.this.enterState(0);
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
            if (assetsInitialized) {
                renderActions(container, g, screenXOffset, screenYOffset, screenXOffset + screenWidth, screenYOffset + screenHeight);
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
            newMouseWheel += delta;
        }
        
        @Override
        public final void keyPressed(int key, char c) {
            if (typingString != null) {
                if (key == Input.KEY_ESCAPE) {
                    cancelTypingToString();
                } else if (key == Input.KEY_BACK) {
                    if (typingString.length() > 0) {
                        char toDelete = typingString.charAt(typingString.length() - 1);
                        typingString = typingString.substring(0, typingString.length() - 1);
                        CellGame.this.getCurrentState().charDeleted(toDelete);
                    }
                } else if (key == Input.KEY_DELETE) {
                    String s = typingString;
                    typingString = "";
                    CellGame.this.getCurrentState().stringDeleted(s);
                } else if (key == Input.KEY_ENTER) {
                    finishTypingToString();
                } else if (c != '\u0000' && typingString.length() < maxTypingStringLength) {
                    typingString += c;
                    CellGame.this.getCurrentState().charTyped(c);
                }
            } else if (commandToBind >= 0) {
                finishBindingToCommand(new KeyControl(key));
            }
        }
        
        @Override
        public final void mouseClicked(int button, int x, int y, int clickCount) {
            if (commandToBind >= 0) {
                finishBindingToCommand(new MouseButtonControl(button));
            }
        }
        
        @Override
        public final void controllerUpPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindingToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.UP));
            }
        }
        
        @Override
        public final void controllerDownPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindingToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.DOWN));
            }
        }
        
        @Override
        public final void controllerLeftPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindingToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.LEFT));
            }
        }
        
        @Override
        public final void controllerRightPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindingToCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.RIGHT));
            }
        }
        
        @Override
        public final void controllerButtonPressed(int controller, int button) {
            if (commandToBind >= 0) {
                finishBindingToCommand(new ControllerButtonControl(controller, button));
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
            state.renderActions(CellGame.this, g, screenXOffset, screenYOffset, screenXOffset + screenWidth, screenYOffset + screenHeight);
        }
        
        @Override
        public final void enter(GameContainer container, StateBasedGame game) {
            state.active = true;
            state.enteredActions(CellGame.this);
        }
        
        @Override
        public final void leave(GameContainer container, StateBasedGame game) {
            state.leftActions(CellGame.this);
            state.active = false;
        }
        
    }
    
    private class LoadingState extends cell2D.BasicGameState {
        
        private LoadingState(int id) {
            super(CellGame.this, id);
        }
        
        @Override
        public final void renderActions(CellGame game, Graphics g, int x1, int y1, int x2, int y2) {
            if (loadingImage != null) {
                loadingImage.draw((x1 + x2 - loadingImage.getWidth())/2, (y1 + y2 - loadingImage.getHeight())/2);
            }
            loadingScreenRenderedOnce = true;
        }
        
    }
    
    public final CellGameState getState(int id) {
        return (id < 0 && negativeIDsOffLimits ? null : states.get(id));
    }
    
    public final CellGameState getCurrentState() {
        return getState(currentID);
    }
    
    public final int getCurrentStateID() {
        return currentID;
    }
    
    final void addState(CellGameState state) {
        int id = state.getID();
        if (id < 0 && negativeIDsOffLimits) {
            throw new RuntimeException("Attempted to add a CellGameState with negative ID " + id);
        }
        if (states.get(id) != null) {
            throw new RuntimeException("Attempted to add multiple CellGameStates with ID " + id);
        }
        states.put(id, state);
        game.addState(new State(state));
    }
    
    public abstract void initStates() throws SlickException;
    
    public final void enterState(int id) {
        enterState(id, null, null);
    }
    
    public final void enterState(int id, Transition leave, Transition enter) {
        if (id < 0 && negativeIDsOffLimits) {
            throw new RuntimeException("Attempted to enter a CellGameState with negative ID " + id);
        }
        currentID = id;
        game.enterState(id, leave, enter);
    }
    
    public void initAssets() throws SlickException {}
    
    public void initActions() throws SlickException {}
    
    public void renderActions(GameContainer container, Graphics g, int x1, int y1, int x2, int y2) {}
    
    private void finishBindingToCommand(Control control) {
        bindControl(commandToBind, control);
        commandToBind = -1;
    }
    
    public final List<Control> getControlsFor(int commandNum) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to get the controls for nonexistent command number " + commandNum);
        }
        return (provider == null ? new ArrayList<>() : provider.getControlsFor(commands[commandNum]));
    }
    
    public final void bindControl(int commandNum, Control control) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to bind nonexistent command number " + commandNum);
        }
        if (provider != null) {
            provider.bindCommand(control, commands[commandNum]);
        }
    }
    
    public final void unbindControl(Control control) {
        if (provider != null) {
            provider.unbindCommand(control);
        }
    }
    
    public final void clearControls(int commandNum) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to clear nonexistent command number " + commandNum);
        }
        if (provider != null) {
            provider.clearCommand(commands[commandNum]);
        }
    }
    
    public final int getBindingCommand() {
        return commandToBind;
    }
    
    public final void waitToBindToCommand(int commandNum) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to begin waiting to bind nonexistent command number " + commandNum);
        }
        if (typingString != null) {
            throw new RuntimeException("Attempted to begin waiting to bind command number " + commandNum + " while already typing to a String");
        }
        commandToBind = commandNum;
        for (int i = 0; i < commandChanges.length; i++) {
            commandChanges[i] = CommandState.NOTHELD;
        }
    }
    
    public final void cancelBindToCommand() {
        commandToBind = -1;
    }
    
    public final boolean commandPressed(int commandNum) {
        return commandStates[commandNum] == CommandState.PRESSED
                || commandStates[commandNum] == CommandState.TAPPED
                || commandStates[commandNum] == CommandState.UNTAPPED;
    }
    
    public final boolean commandHeld(int commandNum) {
        return commandStates[commandNum] == CommandState.PRESSED
                || commandStates[commandNum] == CommandState.HELD
                || commandStates[commandNum] == CommandState.TAPPED;
    }
    
    public final boolean commandReleased(int commandNum) {
        return commandStates[commandNum] == CommandState.RELEASED
                || commandStates[commandNum] == CommandState.TAPPED
                || commandStates[commandNum] == CommandState.UNTAPPED;
    }
    
    public final int getMouseX() {
        return adjustedMouseX;
    }
    
    public final int getMouseY() {
        return adjustedMouseY;
    }
    
    public final int getMouseWheelMoved() {
        return mouseWheel;
    }
    
    public final String getTypingString() {
        return typingString;
    }
    
    public final String getTypedString() {
        String s = typedString;
        typedString = null;
        return s;
    }
    
    public final void beginTypingToString(String initialString, int maxLength) {
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
        typedString = null;
        for (int i = 0; i < commandChanges.length; i++) {
            commandChanges[i] = CommandState.NOTHELD;
        }
        getCurrentState().stringBegan(initialString);
    }
    
    public final void beginTypingToString(int maxLength) {
        beginTypingToString("", maxLength);
    }
    
    public final void finishTypingToString() {
        if (typingString != null) {
            typedString = typingString;
            typingString = null;
            maxTypingStringLength = 0;
            getCurrentState().stringTyped(typedString);
        }
    }
    
    public final void cancelTypingToString() {
        if (typingString != null) {
            String s = typingString;
            typingString = null;
            maxTypingStringLength = 0;
            typedString = null;
            getCurrentState().stringCanceled(s);
        }
    }
    
    public final int getUpdateFPS() {
        return updateFPS;
    }
    
    public final void setUpdateFPS(int updateFPS) {
        if (updateFPS <= 0) {
            throw new RuntimeException("Attempted to run a CellGame at a non-positive FPS");
        }
        this.updateFPS = updateFPS;
        msPerStep = 1000/((double)updateFPS);
    }
    
    public final int getScreenWidth() {
        return screenWidth;
    }
    
    public final void setScreenWidth(int screenWidth) {
        if (screenWidth <= 0) {
            throw new RuntimeException("Attempted to give a CellGame a non-positive screen width");
        }
        this.screenWidth = screenWidth;
        updateScreen = true;
    }
    
    public final int getScreenHeight() {
        return screenHeight;
    }
    
    public final void setScreenHeight(int screenHeight) {
        if (screenHeight <= 0) {
            throw new RuntimeException("Attempted to give a CellGame a non-positive screen height");
        }
        this.screenHeight = screenHeight;
        updateScreen = true;
    }
    
    public final double getScaleFactor() {
        return scaleFactor;
    }
    
    public final void setScaleFactor(double scaleFactor) {
        if (scaleFactor <= 0) {
            throw new RuntimeException("Attempted to give a CellGame a non-positive scale factor");
        }
        this.scaleFactor = scaleFactor;
        updateScreen = true;
    }
    
    public final boolean isFullscreen() {
        return fullscreen;
    }
    
    public final void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        updateScreen = true;
    }
    
    public final boolean getAutoLoadAssets() {
        return autoLoadAssets;
    }
    
    public final void setAutoLoadAssets(boolean autoLoadAssets) {
        this.autoLoadAssets = autoLoadAssets;
    }
    
    public final void add(String name, Sprite sprite) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a Sprite with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to add a Sprite with the empty string as a name");
        }
        if (sprites.put(name, sprite) != null) {
            throw new RuntimeException("Attempted to add multiple Sprites with the name " + name);
        }
    }
    
    public final void add(String name, SpriteSheet spriteSheet) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a SpriteSheet with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to add a SpriteSheet with the empty string as a name");
        }
        if (spriteSheets.put(name, spriteSheet) != null) {
            throw new RuntimeException("Attempted to add multiple SpriteSheets with the name " + name);
        }
    }
    
    public final void add(String name, Animation animation) {
        if (name == null) {
            throw new RuntimeException("Attempted to add an Animation with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to add an Animation with the empty string as a name");
        }
        if (animations.put(name, animation) != null) {
            throw new RuntimeException("Attempted to add multiple Animations with the name " + name);
        }
    }
    
    public final void add(String name, Sound sound) throws SlickException {
        if (name == null) {
            throw new RuntimeException("Attempted to add a Sound with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to add a Sound with the empty string as a name");
        }
        if (sounds.put(name, sound) != null) {
            throw new RuntimeException("Attempted to add multiple Sounds with the name " + name);
        }
    }
    
    public final void createFilter(String name, Map<Color,Color> colorMap) {
        createFilter(name, new ColorMapFilter(name, colorMap));
    }
    
    public final void createFilter(String name, int colorR, int colorG, int colorB) {
        createFilter(name, new ColorFilter(name, new Color(colorR, colorG, colorB)));
    }
    
    public final void createFilter(String name, Color color) {
        createFilter(name, new ColorFilter(name, color));
    }
    
    private void createFilter(String name, Filter filter) {
        if (name == null) {
            throw new RuntimeException("Attempted to create a filter with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to create a filter with the empty string as a name");
        }
        if (filters.put(name, filter) != null) {
            throw new RuntimeException("Attempted to add multiple filters with the name " + name);
        }
    }
    
    public final Sprite createSprite(String path, int originX, int originY, List<String> filters) throws SlickException {
        return createSprite(path, originX, originY, null, filters);
    }
    
    public final Sprite createSprite(String path, int originX, int originY,
            int transR, int transG, int transB, List<String> filters) throws SlickException {
        return createSprite(path, originX, originY,  new Color(transR, transG, transB), filters);
    }
    
    public final Sprite createSprite(String path, int originX,
            int originY, Color transColor, List<String> filters) throws SlickException {
        Set<Filter> filterSet = new HashSet<>();
        if (filters != null) {
            for (String name : filters) {
                Filter filter = this.filters.get(name);
                if (filter != null) {
                    filterSet.add(filter);
                }
            }
        }
        Sprite sprite = new Sprite(false, null, null, null, path, transColor, filterSet, originX, originY);
        if (autoLoadAssets) {
            sprite.load();
        }
        return sprite;
    }
    
    public final Sprite getSprite(String name) {
        return sprites.get(name);
    }
    
    public final SpriteSheet createSpriteSheet(String path, int width, int height, int spriteWidth,
            int spriteHeight, int spriteSpacing, int originX, int originY, String[] filters) throws SlickException {
        return createSpriteSheet(path, width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, null, filters);
    }
    
    public final SpriteSheet createSpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX,
            int originY, int transR, int transG, int transB, String[] filters) throws SlickException {
        return createSpriteSheet(path, width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY, new Color(transR, transG, transB), filters);
    }
    
    public final SpriteSheet createSpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int originX,
            int originY, Color transColor, String[] filters) throws SlickException {
        Set<Filter> filterSet = new HashSet<>();
        if (filters != null) {
            for (String name : filters) {
                Filter filter = this.filters.get(name);
                if (filter != null) {
                    filterSet.add(filter);
                }
            }
        }
        SpriteSheet spriteSheet = new SpriteSheet(null, null, path, transColor, filterSet,
                width, height, spriteWidth, spriteHeight, spriteSpacing, originX, originY);
        if (autoLoadAssets) {
            spriteSheet.load();
        }
        return spriteSheet;
    }
    
    public final SpriteSheet getSpriteSheet(String name) {
        return spriteSheets.get(name);
    }
    
    public final Animation createAnimation(Animatable frame) {
        Animatable[] frames = new Sprite[1];
        frames[0] = frame;
        return createAnimation(frames);
    }
    
    public final Animation createAnimation(Animatable[] frames) {
        if (frames == null || frames.length == 0) {
            throw new RuntimeException("Attempted to create an empty Animation");
        }
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == null) {
                frames[i] = Sprite.BLANK;
            }
        }
        double[] frameDurations = new double[frames.length];
        Arrays.fill(frameDurations, 1);
        return new Animation(frames, frameDurations);
    }
    
    public final Animation createAnimation(Animatable[] frames, double[] frameDurations) {
        if (frames == null || frames.length == 0) {
            throw new RuntimeException("Attempted to create an empty Animation");
        }
        if (frameDurations == null) {
            throw new RuntimeException("Attempted to create an Animation with absent frame durations");
        }
        if (frames.length != frameDurations.length) {
            throw new RuntimeException("Attempted to create an Animation with different numbers of frames and frame durations");
        }
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == null) {
                frames[i] = Sprite.BLANK;
            }
        }
        return new Animation(frames, frameDurations);
    }
    
    private Animatable[] spriteSheetToAnimation(SpriteSheet spriteSheet,
            int x1, int y1, int x2, int y2, boolean columns) {
        if (x2 < x1 || y2 < y1) {
            throw new RuntimeException("Attempted to create an Animation from a part of a sprite sheet defined by invalid coordinates");
        }
        Animatable[] frames = new Animatable[(x2 - x1 + 1)*(y2 - y1 + 1)];
        int i = 0;
        if (columns) {
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    frames[i] = spriteSheet.getSprite(x, y);
                    i++;
                }
            }
        } else {
            for (int y = y1; y <= y2; y++) {
                for (int x = x1; x <= x2; x++) {
                    frames[i] = spriteSheet.getSprite(x, y);
                    i++;
                }
            }
        }
        return frames;
    }
    
    public final Animation createAnimation(SpriteSheet spriteSheet,
            int x1, int y1, int x2, int y2, boolean columns) {
        return createAnimation(spriteSheetToAnimation(spriteSheet, x1, y1, x2, y2, columns));
    }
    
    public final Animation createAnimation(SpriteSheet spriteSheet,
            int x1, int y1, int x2, int y2, boolean columns, double[] frameDurations) {
        return createAnimation(spriteSheetToAnimation(spriteSheet, x1, y1, x2, y2, columns), frameDurations);
    }
    
    public final Animation getAnimation(String name) {
        return animations.get(name);
    }
    
    public final Sprite createRecolor(Sprite sprite, Map<Color,Color> colorMap) throws SlickException {
        if (sprite == Sprite.BLANK) {
            return sprite;
        }
        if (sprite.getSpriteSheet() != null) {
            throw new RuntimeException("Attempted to individually recolor a Sprite that is part of a SpriteSheet. Try recoloring the SpriteSheet itself instead.");
        }
        Sprite recolor = sprite.getRecolor(new ColorMapFilter(null, colorMap));
        if (autoLoadAssets && sprite.isLoaded()) {
            recolor.load();
        }
        return recolor;
    }
    
    public final Sprite createRecolor(Sprite sprite, int colorR, int colorG, int colorB) throws SlickException {
        return createRecolor(sprite, new Color(colorR, colorG, colorB));
    }
    
    public final Sprite createRecolor(Sprite sprite, Color newColor) throws SlickException {
        if (sprite == Sprite.BLANK) {
            return sprite;
        }
        if (sprite.getSpriteSheet() != null) {
            throw new RuntimeException("Attempted to individually recolor a Sprite that is part of a SpriteSheet. Try recoloring the SpriteSheet itself instead.");
        }
        Sprite recolor = sprite.getRecolor(new ColorFilter(null, newColor));
        if (autoLoadAssets && sprite.isLoaded()) {
            recolor.load();
        }
        return recolor;
    }
    
    public final SpriteSheet createRecolor(SpriteSheet spriteSheet, Map<Color,Color> colorMap) throws SlickException {
        SpriteSheet recolor = spriteSheet.getRecolor(new ColorMapFilter(null, colorMap));
        if (autoLoadAssets && spriteSheet.isLoaded()) {
            recolor.load();
        }
        return recolor;
    }
    
    public final SpriteSheet createRecolor(SpriteSheet spriteSheet, int colorR, int colorG, int colorB) throws SlickException {
        return createRecolor(spriteSheet, new Color(colorR, colorG, colorB));
    }
    
    public final SpriteSheet createRecolor(SpriteSheet spriteSheet, Color newColor) throws SlickException {
        SpriteSheet recolor = spriteSheet.getRecolor(new ColorFilter(null, newColor));
        if (autoLoadAssets && spriteSheet.isLoaded()) {
            recolor.load();
        }
        return recolor;
    }
    
    public final Sound createSound(String path) throws SlickException {
        Sound sound = new Sound(path);
        if (autoLoadAssets) {
            sound.load();
        }
        return sound;
    }
    
    public final Sound getSound(String name) {
        return sounds.get(name);
    }
    
    private class Music {
        
        private boolean isLoaded = false;
        private final String path;
        private org.newdawn.slick.Music music = null;
        
        private Music(String path) {
            this.path = path;
        }
        
        private boolean load() throws SlickException {
            if (!isLoaded) {
                isLoaded = true;
                music = new org.newdawn.slick.Music(path);
                return true;
            }
            return false;
        }
        
        private boolean unload() {
            if (isLoaded) {
                isLoaded = false;
                music = null;
                return true;
            }
            return false;
        }
        
    }
    
    public final void createMusic(String name, String path) throws SlickException {
        if (name == null) {
            throw new RuntimeException("Attempted to create a music track with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to create a music track with the empty string as a name");
        }
        Music music = new Music(path);
        if (musics.put(name, music) != null) {
            throw new RuntimeException("Attempted to create multiple music tracks with the name " + name);
        }
        if (autoLoadAssets) {
            music.load();
        }
    }
    
    public final boolean isMusicLoaded(String name) {
        Music music = musics.get(name);
        return (music == null ? false : music.isLoaded);
    }
    
    public final boolean loadMusic(String name) throws SlickException {
        Music music = musics.get(name);
        if (music != null) {
            return music.load();
        }
        return false;
    }
    
    public final boolean unloadMusic(String name) {
        Music music = musics.get(name);
        if (music != null) {
            return music.unload();
        }
        return false;
    }
    
    private class MusicInstance {
        
        private final String name;
        private final org.newdawn.slick.Music music;
        private final double pitch;
        private double volume;
        private final boolean loop;
        
        private MusicInstance(String name, double pitch, double volume, boolean loop) {
            this.name = name;
            Music musicData = musics.get(name);
            this.music = (musicData == null ? null : musicData.music);
            this.pitch = pitch;
            this.volume = volume;
            this.loop = loop;
        }
        
    }
    
    public final String getCurrentMusic() {
        return currentMusic.name;
    }
    
    public final String getMusic(int priority) {
        MusicInstance instance = musicStack.get(priority);
        return (instance == null ? null : instance.name);
    }
    
    private void changeMusic(MusicInstance instance) {
        if (!musicPaused) {
            if (currentMusic.music != null) {
                currentMusic.music.stop();
            }
            if (instance.music != null) {
                if (instance.loop) {
                    instance.music.loop((float)instance.pitch, (float)instance.volume);
                } else {
                    instance.music.play((float)instance.pitch, (float)instance.volume);
                }
            }
        }
        currentMusic = instance;
        musicPosition = 0;
        musicFadeType = 0;
    }
    
    private void startMusic(String name, double pitch, double volume, boolean loop) {
        if (name == null) {
            stopMusic();
            return;
        }
        if (musicFadeType == 2 && !musicStack.isEmpty() && !stackOverridden) {
            musicStack.remove(musicStack.lastKey());
        }
        changeMusic(new MusicInstance(name, pitch, volume, loop));
        stackOverridden = true;
    }
    
    private void addMusicToStack(int priority, String name, double pitch, double volume, boolean loop) {
        if (name == null) {
            stopMusic(priority);
            return;
        }
        MusicInstance instance = new MusicInstance(name, pitch, volume, loop);
        if ((musicStack.isEmpty() || priority >= musicStack.lastKey()) && !stackOverridden) {
            if (musicFadeType == 2 && !musicStack.isEmpty() && priority > musicStack.lastKey()) {
                musicStack.remove(musicStack.lastKey());
            }
            changeMusic(instance);
        }
        musicStack.put(priority, instance);
    }
    
    public final void playMusic(String name) {
        startMusic(name, 1, 1, false);
    }
    
    public final void playMusic(String name, double pitch, double volume) {
        startMusic(name, pitch, volume, false);
    }
    
    public final void playMusic(int priority, String name) {
        addMusicToStack(priority, name, 1, 1, false);
    }
    
    public final void playMusic(int priority, String name, double pitch, double volume) {
        addMusicToStack(priority, name, pitch, volume, false);
    }
    
    public final void loopMusic(String name) {
        startMusic(name, 1, 1, true);
    }
    
    public final void loopMusic(String name, double pitch, double volume) {
        startMusic(name, pitch, volume, true);
    }
    
    public final void loopMusic(int priority, String name) {
        addMusicToStack(priority, name, 1, 1, true);
    }
    
    public final void loopMusic(int priority, String name, double pitch, double volume) {
        addMusicToStack(priority, name, pitch, volume, true);
    }
    
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
    
    public final void stopMusic(String name) {
        if (currentMusic.name != null && currentMusic.name.equals(name)) {
            stopMusic();
        }
    }
    
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
    
    public final void stopMusic(int priority, String name) {
        if (musicStack.isEmpty()) {
            return;
        }
        MusicInstance instance = musicStack.get(priority);
        if (instance != null && instance.name != null && instance.name.equals(name)) {
            if (priority == musicStack.lastKey() && !stackOverridden) {
                stopMusic();
            } else {
                musicStack.remove(priority);
            }
        }
    }
    
    public final boolean musicIsPaused() {
        return musicPaused;
    }
    
    public final void pauseMusic() {
        if (currentMusic.name != null && !musicPaused) {
            if (currentMusic.music != null) {
                musicPosition = currentMusic.music.getPosition();
                currentMusic.music.stop();
            }
            musicPaused = true;
        }
    }
    
    public final void resumeMusic() {
        if (currentMusic.name != null && musicPaused) {
            if (currentMusic.music != null) {
                if (currentMusic.loop) {
                    currentMusic.music.loop((float)currentMusic.pitch, currentMusic.music.getVolume());
                } else {
                    currentMusic.music.play((float)currentMusic.pitch, currentMusic.music.getVolume());
                }
                currentMusic.music.setPosition(musicPosition);
            }
            musicPaused = false;
        }
    }
    
    public final double getMusicPosition() {
        return (currentMusic.music == null ? 0 : currentMusic.music.getPosition());
    }
    
    public final void setMusicPosition(double position) {
        if (currentMusic.music != null) {
            currentMusic.music.setPosition((float)position);
        }
    }
    
    public final double getMusicVolume() {
        return (currentMusic.music == null ? 0 : currentMusic.music.getVolume());
    }
    
    public final void setMusicVolume(double volume) {
        if (currentMusic.name != null) {
            currentMusic.volume = volume;
            if (currentMusic.music != null) {
                currentMusic.music.setVolume((float)volume);
                musicFadeType = 0;
            }
        }
    }
    
    public final void fadeMusicVolume(double volume, double duration) {
        if (currentMusic.name != null) {
            if (currentMusic.music != null) {
                musicFadeType = 1;
                fadeStartVolume = currentMusic.music.getVolume();
                fadeEndVolume = volume;
                fadeDuration = duration*1000;
                msFading = 0;
            }
            currentMusic.volume = volume;
        }
    }
    
    public final void fadeMusicOut(double duration) {
        if (currentMusic.name != null) {
            if (currentMusic.music == null) {
                stopMusic();
            } else {
                musicFadeType = 2;
                fadeStartVolume = currentMusic.music.getVolume();
                fadeEndVolume = 0;
                fadeDuration = duration*1000;
                msFading = 0;
            }
        }
    }
    
}
