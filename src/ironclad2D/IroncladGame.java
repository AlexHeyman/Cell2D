package ironclad2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.newdawn.slick.Music;
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

public abstract class IroncladGame {
    
    private static enum CommandState {
        NOTHELD, PRESSED, HELD, RELEASED, TAPPED, UNTAPPED
    }
    
    private static final Color transparent = new Color(0, 0, 0, 0);
    private static final int transparentInt = colorToInt(transparent);
    
    private boolean closeRequested = false;
    private final StateBasedGame game;
    private final Map<Integer,IroncladGameState> states = new HashMap<>();
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
    private Path assetsPath;
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
    private MusicData currentMusic = new MusicData(null, 0, 0, false);
    private final SortedMap<Integer,MusicData> musicStack = new TreeMap<>();
    private boolean stackOverridden = false;
    private boolean musicPaused = false;
    private float musicPosition = 0;
    private int musicFadeType = 0;
    private double fadeStartVolume = 0;
    private double fadeEndVolume = 0;
    private double fadeDuration = 0;
    private double msFading = 0;
    
    public IroncladGame(String gamename, int numCommands, int updateFPS,
            int screenWidth, int screenHeight, double scaleFactor, boolean fullscreen,
            String assetsPath, boolean autoLoadAssets, String loadingImagePath) throws SlickException {
        game = new Game(gamename);
        if (numCommands < 0) {
            throw new RuntimeException("Attempted to create a game with a negative number of controls");
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
        this.assetsPath = Paths.get(assetsPath);
        this.autoLoadAssets = autoLoadAssets;
        if (loadingImagePath != null) {
            loadingImage = new Image(getSubFolderPath("graphics") + loadingImagePath);
        }
    }
    
    public static final void startGame(IroncladGame game) throws SlickException {
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
    
    private String getSubFolderPath(String subFolder) {
        return assetsPath.toString() + File.separator + subFolder + File.separator;
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
                        IroncladGame.this.getCurrentState().doStep();
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
                    IroncladGame.this.enterState(0);
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
                        IroncladGame.this.getCurrentState().charDeleted(toDelete);
                    }
                } else if (key == Input.KEY_DELETE) {
                    String s = typingString;
                    typingString = "";
                    IroncladGame.this.getCurrentState().stringDeleted(s);
                } else if (key == Input.KEY_ENTER) {
                    finishTypingToString();
                } else if (c != '\u0000' && typingString.length() < maxTypingStringLength) {
                    typingString += c;
                    IroncladGame.this.getCurrentState().charTyped(c);
                }
            } else if (commandToBind >= 0) {
                finishBindingCommand(new KeyControl(key));
            }
        }
        
        @Override
        public final void mouseClicked(int button, int x, int y, int clickCount) {
            if (commandToBind >= 0) {
                finishBindingCommand(new MouseButtonControl(button));
            }
        }
        
        @Override
        public final void controllerUpPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindingCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.UP));
            }
        }
        
        @Override
        public final void controllerDownPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindingCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.DOWN));
            }
        }
        
        @Override
        public final void controllerLeftPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindingCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.LEFT));
            }
        }
        
        @Override
        public final void controllerRightPressed(int controller) {
            if (commandToBind >= 0) {
                finishBindingCommand(new ControllerDirectionControl(controller, ControllerDirectionControl.RIGHT));
            }
        }
        
        @Override
        public final void controllerButtonPressed(int controller, int button) {
            if (commandToBind >= 0) {
                finishBindingCommand(new ControllerButtonControl(controller, button));
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
        
        private final IroncladGameState state;
        
        private State(IroncladGameState state) {
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
            state.renderActions(IroncladGame.this, g, screenXOffset, screenYOffset, screenXOffset + screenWidth, screenYOffset + screenHeight);
        }
        
        @Override
        public final void enter(GameContainer container, StateBasedGame game) {
            state.isActive = true;
            state.enteredActions(IroncladGame.this);
        }
        
        @Override
        public final void leave(GameContainer container, StateBasedGame game) {
            state.leftActions(IroncladGame.this);
            state.isActive = false;
        }
        
    }
    
    private class LoadingState extends ironclad2D.BasicGameState {
        
        private LoadingState(int id) {
            super(IroncladGame.this, id);
        }
        
        @Override
        public final void renderActions(IroncladGame game, Graphics g, int x1, int y1, int x2, int y2) {
            if (loadingImage != null) {
                loadingImage.draw((x1 + x2 - loadingImage.getWidth())/2, (y1 + y2 - loadingImage.getHeight())/2);
            }
            loadingScreenRenderedOnce = true;
        }
        
    }
    
    public final IroncladGameState getState(int id) {
        if (id < 0 && negativeIDsOffLimits) {
            return null;
        }
        return states.get(id);
    }
    
    public final IroncladGameState getCurrentState() {
        if (currentID < 0 && negativeIDsOffLimits) {
            return null;
        }
        return states.get(currentID);
    }
    
    public final int getCurrentStateID() {
        return currentID;
    }
    
    final void addState(IroncladGameState state) {
        int id = state.getID();
        if (id < 0 && negativeIDsOffLimits) {
            throw new RuntimeException("Attempted to add an Ironclad game state with negative ID " + id);
        }
        if (states.get(id) != null) {
            throw new RuntimeException("Attempted to add multiple Ironclad game states with ID " + id);
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
            throw new RuntimeException("Attempted to enter a game state with negative ID " + id);
        }
        currentID = id;
        game.enterState(id, leave, enter);
    }
    
    public void initAssets() throws SlickException {}
    
    public void initActions() throws SlickException {}
    
    public void renderActions(GameContainer container, Graphics g, int x1, int y1, int x2, int y2) {}
    
    private void finishBindingCommand(Control control) {
        bindControl(commandToBind, control);
        commandToBind = -1;
    }
    
    public final List<Control> getControlsFor(int commandNum) {
        if (commandNum < 0 || commandNum >= commands.length) {
            throw new RuntimeException("Attempted to get the controls for nonexistent command number " + commandNum);
        }
        if (provider != null) {
            return provider.getControlsFor(commands[commandNum]);
        }
        return new ArrayList<>();
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
    
    public final void waitToBindCommand(int commandNum) {
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
    
    public final void cancelBindCommand() {
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
            throw new RuntimeException("Attempted to run an Ironclad game at a non-positive FPS");
        }
        this.updateFPS = updateFPS;
        msPerStep = 1000/((double)updateFPS);
    }
    
    public final int getScreenWidth() {
        return screenWidth;
    }
    
    public final void setScreenWidth(int screenWidth) {
        if (screenWidth <= 0) {
            throw new RuntimeException("Attempted to give an Ironclad game a non-positive screen width");
        }
        this.screenWidth = screenWidth;
        updateScreen = true;
    }
    
    public final int getScreenHeight() {
        return screenHeight;
    }
    
    public final void setScreenHeight(int screenHeight) {
        if (screenHeight <= 0) {
            throw new RuntimeException("Attempted to give an Ironclad game a non-positive screen height");
        }
        this.screenHeight = screenHeight;
        updateScreen = true;
    }
    
    public final double getScaleFactor() {
        return scaleFactor;
    }
    
    public final void setScaleFactor(double scaleFactor) {
        if (scaleFactor <= 0) {
            throw new RuntimeException("Attempted to give an Ironclad game a non-positive scale factor");
        }
        this.scaleFactor = scaleFactor;
        updateScreen = true;
    }
    
    public final boolean getFullscreen() {
        return fullscreen;
    }
    
    public final void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        updateScreen = true;
    }
    
    public final String getAssetsPath() {
        return assetsPath.toString();
    }
    
    public final boolean getAutoLoadAssets() {
        return autoLoadAssets;
    }
    
    public final void add(String name, Sprite sprite) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a sprite with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to add a sprite with the empty string as a name");
        }
        if (sprites.put(name, sprite) != null) {
            throw new RuntimeException("Attempted to add multiple sprites with the name " + name);
        }
    }
    
    public final void add(String name, SpriteSheet spriteSheet) {
        if (name == null) {
            throw new RuntimeException("Attempted to add a sprite sheet with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to add a sprite sheet with the empty string as a name");
        }
        if (spriteSheets.put(name, spriteSheet) != null) {
            throw new RuntimeException("Attempted to add multiple sprite sheets with the name " + name);
        }
    }
    
    public final void add(String name, Animation animation) {
        if (name == null) {
            throw new RuntimeException("Attempted to add an animation with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to add an animation with the empty string as a name");
        }
        if (animations.put(name, animation) != null) {
            throw new RuntimeException("Attempted to add multiple animations with the name " + name);
        }
    }
    
    public final void add(String name, Sound sound) throws SlickException {
        if (name == null) {
            throw new RuntimeException("Attempted to add a sound effect with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to add a sound effect with the empty string as a name");
        }
        if (sounds.put(name, sound) != null) {
            throw new RuntimeException("Attempted to add multiple sounds with the name " + name);
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
    
    public final Sprite createSprite(String path, int centerX, int centerY, List<String> filters) throws SlickException {
        return createSprite(path, centerX, centerY, null, filters);
    }
    
    public final Sprite createSprite(String path, int centerX, int centerY,
            int transR, int transG, int transB, List<String> filters) throws SlickException {
        return createSprite(path, centerX, centerY,  new Color(transR, transG, transB), filters);
    }
    
    public final Sprite createSprite(String path, int centerX,
            int centerY, Color transColor, List<String> filters) throws SlickException {
        Set<Filter> filterSet = new HashSet<>();
        if (filters != null) {
            for (String name : filters) {
                Filter filter = this.filters.get(name);
                if (filter != null) {
                    filterSet.add(filter);
                }
            }
        }
        Sprite sprite = new Sprite(false, null, null, null, getSubFolderPath("graphics") + path, transColor, filterSet, centerX, centerY);
        if (autoLoadAssets) {
            sprite.load();
        }
        return sprite;
    }
    
    public final Sprite getSprite(String name) {
        return sprites.get(name);
    }
    
    public final SpriteSheet createSpriteSheet(String path, int width, int height, int spriteWidth,
            int spriteHeight, int spriteSpacing, int centerX, int centerY, String[] filters) throws SlickException {
        return createSpriteSheet(path, width, height, spriteWidth, spriteHeight, spriteSpacing, centerX, centerY, null, filters);
    }
    
    public final SpriteSheet createSpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int centerX,
            int centerY, int transR, int transG, int transB, String[] filters) throws SlickException {
        return createSpriteSheet(path, width, height, spriteWidth, spriteHeight, spriteSpacing, centerX, centerY, new Color(transR, transG, transB), filters);
    }
    
    public final SpriteSheet createSpriteSheet(String path, int width, int height,
            int spriteWidth, int spriteHeight, int spriteSpacing, int centerX,
            int centerY, Color transColor, String[] filters) throws SlickException {
        Set<Filter> filterSet = new HashSet<>();
        if (filters != null) {
            for (String name : filters) {
                Filter filter = this.filters.get(name);
                if (filter != null) {
                    filterSet.add(filter);
                }
            }
        }
        SpriteSheet spriteSheet = new SpriteSheet(null, null, getSubFolderPath("graphics") + path,
                transColor, filterSet, width, height, spriteWidth, spriteHeight, spriteSpacing, centerX, centerY);
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
            throw new RuntimeException("Attempted to create an empty sprite animation");
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
            throw new RuntimeException("Attempted to create an empty animation");
        }
        if (frameDurations == null) {
            throw new RuntimeException("Attempted to create a animation with absent frame durations");
        }
        if (frames.length != frameDurations.length) {
            throw new RuntimeException("Attempted to create a animation with different numbers of frames and frame durations");
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
            throw new RuntimeException("Attempted to create a animation from a part of a sprite sheet defined by invalid coordinates");
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
            throw new RuntimeException("Attempted to individually recolor a sprite that is part of a sprite sheet. Try recoloring the sprite sheet itself instead.");
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
            throw new RuntimeException("Attempted to individually recolor a sprite that is part of a sprite sheet. Try recoloring the sprite sheet itself instead.");
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
        return new Sound(getSubFolderPath("sounds") + path);
    }
    
    public final Sound getSound(String name) {
        return sounds.get(name);
    }
    
    public final void createMusic(String name, String path) throws SlickException {
        if (name == null) {
            throw new RuntimeException("Attempted to create a music track with a null name");
        }
        if (name.equals("")) {
            throw new RuntimeException("Attempted to create a music track with the empty string as a name");
        }
        if (musics.put(name, new Music(getSubFolderPath("music") + path)) != null) {
            throw new RuntimeException("Attempted to create multiple music tracks with the name " + name);
        }
    }
    
    private class MusicData {
        
        private final String name;
        private final Music music;
        private final double pitch;
        private double volume;
        private final boolean loop;
        
        private MusicData(String name, double pitch, double volume, boolean loop) {
            this.name = name;
            this.music = musics.get(name);
            this.pitch = pitch;
            this.volume = volume;
            this.loop = loop;
        }
        
    }
    
    public final String getCurrentMusic() {
        return currentMusic.name;
    }
    
    public final String getMusic(int priority) {
        MusicData data = musicStack.get(priority);
        if (data == null) {
            return null;
        }
        return data.name;
    }
    
    private void changeMusic(MusicData data) {
        if (!musicPaused) {
            if (currentMusic.music != null) {
                currentMusic.music.stop();
            }
            if (data.music != null) {
                if (data.loop) {
                    data.music.loop((float)data.pitch, (float)data.volume);
                } else {
                    data.music.play((float)data.pitch, (float)data.volume);
                }
            }
        }
        currentMusic = data;
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
        changeMusic(new MusicData(name, pitch, volume, loop));
        stackOverridden = true;
    }
    
    private void addMusicToStack(int priority, String name, double pitch, double volume, boolean loop) {
        if (name == null) {
            stopMusic(priority);
            return;
        }
        MusicData data = new MusicData(name, pitch, volume, loop);
        if ((musicStack.isEmpty() || priority >= musicStack.lastKey()) && !stackOverridden) {
            if (musicFadeType == 2 && !musicStack.isEmpty() && priority > musicStack.lastKey()) {
                musicStack.remove(musicStack.lastKey());
            }
            changeMusic(data);
        }
        musicStack.put(priority, data);
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
            changeMusic(new MusicData(null, 0, 0, false));
            stackOverridden = false;
            return;
        }
        if (!stackOverridden) {
            musicStack.remove(musicStack.lastKey());
            if (musicStack.isEmpty()) {
                changeMusic(new MusicData(null, 0, 0, false));
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
        MusicData data = musicStack.get(priority);
        if (data != null && data.name != null && data.name.equals(name)) {
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
        if (currentMusic.music == null) {
            return 0;
        }
        return currentMusic.music.getPosition();
    }
    
    public final void setMusicPosition(double position) {
        if (currentMusic.music != null) {
            currentMusic.music.setPosition((float)position);
        }
    }
    
    public final double getMusicVolume() {
        if (currentMusic.music == null) {
            return 0;
        }
        return currentMusic.music.getVolume();
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
