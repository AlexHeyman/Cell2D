package org.cell2d.celick;

import org.cell2d.celick.opengl.CursorLoader;
import org.cell2d.celick.opengl.ImageData;
import org.cell2d.celick.opengl.ImageIOImageData;
import org.cell2d.celick.opengl.InternalTextureLoader;
import org.cell2d.celick.opengl.LoadableImageData;
import org.cell2d.celick.opengl.TGAImageData;
import org.cell2d.celick.opengl.renderer.Renderer;
import org.cell2d.celick.opengl.renderer.SGL;
import org.cell2d.celick.util.Log;
import org.cell2d.celick.util.ResourceLoader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

/**
 * A generic game container that handles the game loop, fps recording and
 * managing the input system
 *
 * @author kevin
 */
public class GameContainer {

    /**
     * The original display mode before we tampered with things
     */
    protected DisplayMode originalDisplayMode;
    /**
     * The display mode we're going to try and use
     */
    protected DisplayMode targetDisplayMode;
    /**
     * True if we should update the game only when the display is visible
     */
    protected boolean updateOnlyOnVisible = true;
    /**
     * Alpha background supported
     */
    protected boolean alphaSupport = false;
    
    /**
     * The renderer to use for all GL operations
     */
    protected static SGL GL = Renderer.get();
    /**
     * The shared drawable if any
     */
    protected static Drawable SHARED_DRAWABLE;

    /**
     * The time the last frame was rendered
     */
    protected long lastFrame;
    /**
     * The last time the FPS recorded
     */
    protected long lastFPS;
    /**
     * The last recorded FPS
     */
    protected int recordedFPS;
    /**
     * The current count of FPS
     */
    protected int fps;
    /**
     * True if we're currently running the game loop
     */
    protected boolean running = true;

    /**
     * The width of the display
     */
    protected int width;
    /**
     * The height of the display
     */
    protected int height;
    /**
     * The game being managed
     */
    protected Game game;

    /**
     * The default font to use in the graphics context
     */
    private Font defaultFont;
    /**
     * The graphics context to be passed to the game
     */
    private Graphics graphics;

    /**
     * The FPS we want to lock to
     */
    protected int targetFPS = -1;
    /**
     * True if we should show the fps
     */
    private boolean showFPS = true;
    /**
     * The minimum logic update interval
     */
    protected long minimumLogicInterval = 1;
    /**
     * The stored delta
     */
    protected long storedDelta;
    /**
     * The maximum logic update interval
     */
    protected long maximumLogicInterval = 0;
    /**
     * The last game started
     */
    protected Game lastGame;
    /**
     * True if we should clear the screen each frame
     */
    protected boolean clearEachFrame = true;

    /**
     * True if the game is paused
     */
    protected boolean paused;
    /**
     * True if we should force exit
     */
    protected boolean forceExit = true;
    /**
     * True if vsync has been requested
     */
    protected boolean vsync;
    /**
     * Smoothed deltas requested
     */
    protected boolean smoothDeltas;
    /**
     * The number of samples we'll attempt through hardware
     */
    protected int samples;
    
    /**
     * True if this context supports multisample
     */
    protected boolean supportsMultiSample;

    /**
     * True if we should render when not focused
     */
    protected boolean alwaysRender;
    
    /**
     * Create a new container wrapping a game
     *
     * @param game The game to be wrapped
     * @throws SlickException Indicates a failure to initialise the display
     */
    public GameContainer(Game game) throws SlickException {
        this.game = game;
        lastFrame = getTime();

        getBuildVersion();
        Log.checkVerboseLogSetting();

        originalDisplayMode = Display.getDisplayMode();
    }

    /**
     * Set the default font that will be intialised in the graphics held in this
     * container
     *
     * @param font The font to use as default
     */
    public void setDefaultFont(Font font) {
        if (font != null) {
            this.defaultFont = font;
        } else {
            Log.warn("Please provide a non null font");
        }
    }

    /**
     * Indicate whether we want to try to use fullscreen multisampling. This
     * will give antialiasing across the whole scene using a hardware feature.
     *
     * @param samples The number of samples to attempt (2 is safe)
     */
    public void setMultiSample(int samples) {
        this.samples = samples;
    }

    /**
     * Check if this hardware can support multi-sampling
     *
     * @return True if the hardware supports multi-sampling
     */
    public boolean supportsMultiSample() {
        return supportsMultiSample;
    }

    /**
     * The number of samples we're attempting to performing using hardware
     * multisampling
     *
     * @return The number of samples requested
     */
    public int getSamples() {
        return samples;
    }

    /**
     * Indicate if we should force exitting the VM at the end of the game
     * (default = true)
     *
     * @param forceExit True if we should force the VM exit
     */
    public void setForceExit(boolean forceExit) {
        this.forceExit = forceExit;
    }

    /**
     * Indicate if we want to smooth deltas. This feature will report a delta
     * based on the FPS not the time passed. This works well with vsync.
     *
     * @param smoothDeltas True if we should report smooth deltas
     */
    public void setSmoothDeltas(boolean smoothDeltas) {
        this.smoothDeltas = smoothDeltas;
    }

    /**
     * Get the aspect ratio of the screen
     *
     * @return The aspect ratio of the display
     */
    public float getAspectRatio() {
        return getWidth() / getHeight();
    }

    /**
     * Enable shared OpenGL context. After calling this all containers created
     * will shared a single parent context
     *
     * @throws SlickException Indicates a failure to create the shared drawable
     */
    public static void enableSharedContext() throws SlickException {
        try {
            SHARED_DRAWABLE = new Pbuffer(64, 64, new PixelFormat(8, 0, 0), null);
        } catch (LWJGLException e) {
            throw new SlickException("Unable to create the pbuffer used for shard context, buffers not supported", e);
        }
    }

    /**
     * Get the context shared by all containers
     *
     * @return The context shared by all the containers or null if shared
     * context isn't enabled
     */
    public static Drawable getSharedContext() {
        return SHARED_DRAWABLE;
    }

    /**
     * Indicate if we should clear the screen at the beginning of each frame. If
     * you're rendering to the whole screen each frame then setting this to
     * false can give some performance improvements
     *
     * @param clear True if the the screen should be cleared each frame
     */
    public void setClearEachFrame(boolean clear) {
        this.clearEachFrame = clear;
    }

    /**
     * Pause the game - i.e. suspend updates
     */
    public void pause() {
        setPaused(true);
    }

    /**
     * Resumt the game - i.e. continue updates
     */
    public void resume() {
        setPaused(false);
    }

    /**
     * Check if the container is currently paused.
     *
     * @return True if the container is paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Indicates if the game should be paused, i.e. if updates should be
     * propogated through to the game.
     *
     * @param paused True if the game should be paused
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * True if this container should render when it has focus
     *
     * @return True if this container should render when it has focus
     */
    public boolean getAlwaysRender() {
        return alwaysRender;
    }

    /**
     * Indicate whether we want this container to render when it has focus
     *
     * @param alwaysRender True if this container should render when it has
     * focus
     */
    public void setAlwaysRender(boolean alwaysRender) {
        this.alwaysRender = alwaysRender;
    }

    /**
     * Get the build number of slick
     *
     * @return The build number of slick
     */
    public static int getBuildVersion() {
        try {
            Properties props = new Properties();
            props.load(ResourceLoader.getResourceAsStream("version"));

            int build = Integer.parseInt(props.getProperty("build"));
            Log.info("Slick Build #" + build);

            return build;
        } catch (Exception e) {
            Log.error("Unable to determine Slick build number");
            return -1;
        }
    }

    /**
     * Get the default system font
     *
     * @return The default system font
     */
    public Font getDefaultFont() {
        return defaultFont;
    }

    /**
     * Get the width of the game canvas
     *
     * @return The width of the game canvas
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the game canvas
     *
     * @return The height of the game canvas
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the accurate system time
     *
     * @return The system time in milliseconds
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    /**
     * Sleep for a given period
     *
     * @param milliseconds The period to sleep for in milliseconds
     */
    public void sleep(int milliseconds) {
        long target = getTime() + milliseconds;
        while (getTime() < target) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Get a cursor based on a image reference on the classpath. The image is
     * assumed to be a set/strip of cursor animation frames running from top to
     * bottom.
     *
     * @param ref The reference to the image to be loaded
     * @param x The x-coordinate of the cursor hotspot (left -> right)
     * @param y The y-coordinate of the cursor hotspot (bottom -> top)
     * @param width The x width of the cursor
     * @param height The y height of the cursor
     * @param cursorDelays image delays between changing frames in animation
     *
     * @throws SlickException Indicates a failure to load the image or a failure
     * to create the hardware cursor
     */
    public void setAnimatedMouseCursor(String ref, int x, int y, int width, int height, int[] cursorDelays) throws SlickException {
        try {
            Cursor cursor;
            cursor = CursorLoader.get().getAnimatedCursor(ref, x, y, width, height, cursorDelays);
            setMouseCursor(cursor, x, y);
        } catch (IOException | LWJGLException e) {
            throw new SlickException("Failed to set mouse cursor", e);
        }
    }

    /**
     * Get the current recorded FPS (frames per second)
     *
     * @return The current FPS
     */
    public int getFPS() {
        return recordedFPS;
    }

    /**
     * Retrieve the time taken to render the last frame, i.e. the change in time
     * - delta.
     *
     * @return The time taken to render the last frame
     */
    protected int getDelta() {
        long time = getTime();
        int delta = (int) (time - lastFrame);
        lastFrame = time;

        return delta;
    }

    /**
     * Updated the FPS counter
     */
    protected void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            lastFPS = getTime();
            recordedFPS = fps;
            fps = 0;
        }
        fps++;
    }

    /**
     * Set the minimum amount of time in milliseonds that has to pass before
     * update() is called on the container game. This gives a way to limit logic
     * updates compared to renders.
     *
     * @param interval The minimum interval between logic updates
     */
    public void setMinimumLogicUpdateInterval(int interval) {
        minimumLogicInterval = interval;
    }

    /**
     * Set the maximum amount of time in milliseconds that can passed into the
     * update method. Useful for collision detection without sweeping.
     *
     * @param interval The maximum interval between logic updates
     */
    public void setMaximumLogicUpdateInterval(int interval) {
        maximumLogicInterval = interval;
    }

    /**
     * Update and render the game
     *
     * @param delta The change in time since last update and render
     * @throws SlickException Indicates an internal fault to the game.
     */
    protected void updateAndRender(int delta) throws SlickException {
        if (smoothDeltas) {
            if (getFPS() != 0) {
                delta = 1000 / getFPS();
            }
        }

        if (!paused) {
            storedDelta += delta;

            if (storedDelta >= minimumLogicInterval) {
                try {
                    if (maximumLogicInterval != 0) {
                        long cycles = storedDelta / maximumLogicInterval;
                        for (int i = 0; i < cycles; i++) {
                            game.update(this, (int) maximumLogicInterval);
                        }

                        int remainder = (int) (storedDelta % maximumLogicInterval);
                        if (remainder > minimumLogicInterval) {
                            game.update(this, (int) (remainder % maximumLogicInterval));
                            storedDelta = 0;
                        } else {
                            storedDelta = remainder;
                        }
                    } else {
                        game.update(this, (int) storedDelta);
                        storedDelta = 0;
                    }

                } catch (Throwable e) {
                    Log.error(e);
                    throw new SlickException("Game.update() failure - check the game code.");
                }
            }
        } else {
            game.update(this, 0);
        }

        if (hasFocus() || getAlwaysRender()) {
            if (clearEachFrame) {
                GL.glClear(SGL.GL_COLOR_BUFFER_BIT | SGL.GL_DEPTH_BUFFER_BIT);
            }

            GL.glLoadIdentity();

            graphics.resetTransform();
            graphics.resetFont();
            graphics.resetLineWidth();
            graphics.setAntiAlias(false);
            try {
                game.render(this, graphics);
            } catch (Throwable e) {
                Log.error(e);
                throw new SlickException("Game.render() failure - check the game code.");
            }
            graphics.resetTransform();

            if (showFPS) {
                defaultFont.drawString(10, 10, "FPS: " + recordedFPS);
            }

            GL.flush();
        }

        if (targetFPS != -1) {
            Display.sync(targetFPS);
        }
    }

    /**
     * Initialise the GL context
     */
    protected void initGL() {
        Log.info("Starting display " + width + "x" + height);
        GL.initDisplay(width, height);
        if (graphics != null) {
            graphics.setDimensions(getWidth(), getHeight());
        }
        lastGame = game;
    }

    /**
     * Initialise the system components, OpenGL and OpenAL.
     *
     * @throws SlickException Indicates a failure to create a native handler
     */
    protected void initSystem() throws SlickException {
        initGL();

        graphics = new Graphics(width, height);
        defaultFont = graphics.getFont();
    }

    /**
     * Enter the orthographic mode
     */
    protected void enterOrtho() {
        enterOrtho(width, height);
    }

    /**
     * Indicate whether the container should show the FPS
     *
     * @param show True if the container should show the FPS
     */
    public void setShowFPS(boolean show) {
        showFPS = show;
    }

    /**
     * Check if the FPS is currently showing
     *
     * @return True if the FPS is showing
     */
    public boolean isShowingFPS() {
        return showFPS;
    }

    /**
     * Set the target fps we're hoping to get
     *
     * @param fps The target fps we're hoping to get
     */
    public void setTargetFrameRate(int fps) {
        targetFPS = fps;
    }

    /**
     * Indicate whether the display should be synced to the vertical refresh
     * (stops tearing)
     *
     * @param vsync True if we want to sync to vertical refresh
     */
    public void setVSync(boolean vsync) {
        this.vsync = vsync;
        Display.setVSyncEnabled(vsync);
    }

    /**
     * True if vsync is requested
     *
     * @return True if vsync is requested
     */
    public boolean isVSyncRequested() {
        return vsync;
    }

    /**
     * True if the game is running
     *
     * @return True if the game is running
     */
    protected boolean running() {
        return running;
    }

    /**
     * Inidcate we want verbose logging
     *
     * @param verbose True if we want verbose logging (INFO and DEBUG)
     */
    public void setVerbose(boolean verbose) {
        Log.setVerbose(verbose);
    }

    /**
     * Cause the game to exit and shutdown cleanly
     */
    public void exit() {
        running = false;
    }

    /**
     * Get the graphics context used by this container. Note that this value may
     * vary over the life time of the game.
     *
     * @return The graphics context used by this container
     */
    public Graphics getGraphics() {
        return graphics;
    }

    /**
     * Enter the orthographic mode
     *
     * @param xsize The size of the panel being used
     * @param ysize The size of the panel being used
     */
    protected void enterOrtho(int xsize, int ysize) {
        GL.enterOrtho(xsize, ysize);
    }

    /**
     * Check if the display created supported alpha in the back buffer
     *
     * @return True if the back buffer supported alpha
     */
    public boolean supportsAlphaInBackBuffer() {
        return alphaSupport;
    }

    /**
     * Set the title of the window
     *
     * @param title The title to set on the window
     */
    public void setTitle(String title) {
        Display.setTitle(title);
    }

    /**
     * Set the display mode to be used
     *
     * @param width The width of the display required
     * @param height The height of the display required
     * @param fullscreen True if we want fullscreen mode
     * @throws SlickException Indicates a failure to initialise the display
     */
    public void setDisplayMode(int width, int height, boolean fullscreen) throws SlickException {
        if ((this.width == width) && (this.height == height) && (isFullscreen() == fullscreen)) {
            return;
        }

        try {
            targetDisplayMode = null;
            if (fullscreen) {
                DisplayMode[] modes = Display.getAvailableDisplayModes();
                int freq = 0;

                for (int i = 0; i < modes.length; i++) {
                    DisplayMode current = modes[i];

                    if ((current.getWidth() == width) && (current.getHeight() == height)) {
                        if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
                            if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
                                targetDisplayMode = current;
                                freq = targetDisplayMode.getFrequency();
                            }
                        }

                        // if we've found a match for bpp and frequence against the 
                        // original display mode then it's probably best to go for this one
                        // since it's most likely compatible with the monitor
                        if ((current.getBitsPerPixel() == originalDisplayMode.getBitsPerPixel())
                                && (current.getFrequency() == originalDisplayMode.getFrequency())) {
                            targetDisplayMode = current;
                            break;
                        }
                    }
                }
            } else {
                targetDisplayMode = new DisplayMode(width, height);
            }

            if (targetDisplayMode == null) {
                throw new SlickException("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
            }

            this.width = width;
            this.height = height;

            Display.setDisplayMode(targetDisplayMode);
            Display.setFullscreen(fullscreen);

            if (Display.isCreated()) {
                initGL();
                enterOrtho();
            }

            if (targetDisplayMode.getBitsPerPixel() == 16) {
                InternalTextureLoader.get().set16BitMode();
            }
        } catch (LWJGLException e) {
            throw new SlickException("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen, e);
        }

        getDelta();
    }

    /**
     * Check if the display is in fullscreen mode
     *
     * @return True if the display is in fullscreen mode
     */
    public boolean isFullscreen() {
        return Display.isFullscreen();
    }

    /**
     * Indicate whether we want to be in fullscreen mode. Note that the current
     * display mode must be valid as a fullscreen mode for this to work
     *
     * @param fullscreen True if we want to be in fullscreen mode
     * @throws SlickException Indicates we failed to change the display mode
     */
    public void setFullscreen(boolean fullscreen) throws SlickException {
        if (isFullscreen() == fullscreen) {
            return;
        }

        if (!fullscreen) {
            try {
                Display.setFullscreen(fullscreen);
            } catch (LWJGLException e) {
                throw new SlickException("Unable to set fullscreen=" + fullscreen, e);
            }
        } else {
            setDisplayMode(width, height, fullscreen);
        }
        getDelta();
    }
    
    /**
     * Set the mouse cursor to be displayed - this is a hardware cursor and hence
     * shouldn't have any impact on FPS.
     * 
     * @param ref The location of the image to be loaded for the cursor
     * @param hotSpotX The x coordinate of the hotspot within the cursor image
     * @param hotSpotY The y coordinate of the hotspot within the cursor image
     * @throws SlickException Indicates a failure to load the cursor image or create the hardware cursor
     */
    public void setMouseCursor(String ref, int hotSpotX, int hotSpotY) throws SlickException {
        try {
            Cursor cursor = CursorLoader.get().getCursor(ref, hotSpotX, hotSpotY);
            Mouse.setNativeCursor(cursor);
        } catch (Throwable e) {
            Log.error("Failed to load and apply cursor.", e);
            throw new SlickException("Failed to set mouse cursor", e);
        }
    }
    
    /**
     * Set the mouse cursor to be displayed - this is a hardware cursor and hence
     * shouldn't have any impact on FPS.
     * 
     * @param data The image data from which the cursor can be constructed
     * @param hotSpotX The x coordinate of the hotspot within the cursor image
     * @param hotSpotY The y coordinate of the hotspot within the cursor image
     * @throws SlickException Indicates a failure to load the cursor image or create the hardware cursor
     */
    public void setMouseCursor(ImageData data, int hotSpotX, int hotSpotY) throws SlickException {
        try {
            Cursor cursor = CursorLoader.get().getCursor(data, hotSpotX, hotSpotY);
            Mouse.setNativeCursor(cursor);
        } catch (Throwable e) {
            Log.error("Failed to load and apply cursor.", e);
            throw new SlickException("Failed to set mouse cursor", e);
        }
    }
    
    /**
     * Set the mouse cursor to be displayed - this is a hardware cursor and hence
     * shouldn't have any impact on FPS.
     * 
     * @param cursor The cursor to use
     * @param hotSpotX The x coordinate of the hotspot within the cursor image
     * @param hotSpotY The y coordinate of the hotspot within the cursor image
     * @throws SlickException Indicates a failure to load the cursor image or create the hardware cursor
     */
    public void setMouseCursor(Cursor cursor, int hotSpotX, int hotSpotY) throws SlickException {
        try {
            Mouse.setNativeCursor(cursor);
        } catch (Throwable e) {
            Log.error("Failed to load and apply cursor.", e);
            throw new SlickException("Failed to set mouse cursor", e);
        }
    }
    
    /**
     * Get the closest greater power of 2 to the fold number
     *
     * @param fold The target number
     * @return The power of 2
     */
    private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    }
    
    /**
     * Set the mouse cursor to be displayed - this is a hardware cursor and hence
     * shouldn't have any impact on FPS.
     * 
     * @param image The image from which the cursor can be constructed
     * @param hotSpotX The x coordinate of the hotspot within the cursor image
     * @param hotSpotY The y coordinate of the hotspot within the cursor image
     * @throws SlickException Indicates a failure to load the cursor image or create the hardware cursor
     */
    public void setMouseCursor(Image image, int hotSpotX, int hotSpotY) throws SlickException {
        try {
            Image temp = new Image(get2Fold(image.getWidth()), get2Fold(image.getHeight()));
            Graphics g = temp.getGraphics();

            ByteBuffer buffer = BufferUtils.createByteBuffer(temp.getWidth() * temp.getHeight() * 4);
            g.drawImage(image.getFlippedCopy(false, true), 0, 0);
            g.flush();
            g.getArea(0, 0, temp.getWidth(), temp.getHeight(), buffer);

            Cursor cursor = CursorLoader.get().getCursor(buffer, hotSpotX, hotSpotY, temp.getWidth(), image.getHeight());
            Mouse.setNativeCursor(cursor);
        } catch (SlickException | IOException | LWJGLException e) {
            Log.error("Failed to load and apply cursor.", e);
            throw new SlickException("Failed to set mouse cursor", e);
        }
    }
    
    /**
     * Try creating a display with the given format
     *
     * @param format The format to attempt
     * @throws LWJGLException Indicates a failure to support the given format
     */
    private void tryCreateDisplay(PixelFormat format) throws LWJGLException {
        if (SHARED_DRAWABLE == null) {
            Display.create(format);
        } else {
            Display.create(format, SHARED_DRAWABLE);
        }
    }

    /**
     * Start running the game
     *
     * @throws SlickException Indicates a failure to initialise the system
     */
    public void start() throws SlickException {
        try {
            setup();

            getDelta();
            while (running()) {
                gameLoop();
            }
        } finally {
            destroy();
        }

        if (forceExit) {
            System.exit(0);
        }
    }

    /**
     * Setup the environment
     *
     * @throws SlickException Indicates a failure
     */
    protected void setup() throws SlickException {
        if (targetDisplayMode == null) {
            setDisplayMode(640, 480, false);
        }

        Display.setTitle(game.getTitle());

        Log.info("LWJGL Version: " + Sys.getVersion());
        Log.info("OriginalDisplayMode: " + originalDisplayMode);
        Log.info("TargetDisplayMode: " + targetDisplayMode);

        AccessController.doPrivileged(new PrivilegedAction() {
            
            @Override
            public Object run() {
                try {
                    PixelFormat format = new PixelFormat(8, 8, 0, samples);

                    tryCreateDisplay(format);
                    supportsMultiSample = true;
                } catch (Exception e) {
                    Display.destroy();

                    try {
                        PixelFormat format = new PixelFormat(8, 8, 0);

                        tryCreateDisplay(format);
                        alphaSupport = false;
                    } catch (Exception e2) {
                        Display.destroy();
                        // if we couldn't get alpha, let us know
                        try {
                            tryCreateDisplay(new PixelFormat());
                        } catch (Exception e3) {
                            Log.error(e3);
                        }
                    }
                }

                return null;
            }
            
        });

        if (!Display.isCreated()) {
            throw new SlickException("Failed to initialise the LWJGL display");
        }

        initSystem();
        enterOrtho();

        try {
            game.init(this);
        } catch (SlickException e) {
            Log.error(e);
            running = false;
        }
    }

    /**
     * Strategy for overloading game loop context handling
     *
     * @throws SlickException Indicates a game failure
     */
    protected void gameLoop() throws SlickException {
        int delta = getDelta();
        if (!Display.isVisible() && updateOnlyOnVisible) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        } else {
            try {
                updateAndRender(delta);
            } catch (SlickException e) {
                Log.error(e);
                running = false;
                return;
            }
        }

        updateFPS();

        Display.update();

        if (Display.isCloseRequested()) {
            if (game.closeRequested()) {
                running = false;
            }
        }
    }

    public void setUpdateOnlyWhenVisible(boolean updateOnlyWhenVisible) {
        updateOnlyOnVisible = updateOnlyWhenVisible;
    }

    public boolean isUpdatingOnlyWhenVisible() {
        return updateOnlyOnVisible;
    }

    public void setIcon(String ref) throws SlickException {
        setIcons(new String[]{ref});
    }

    public void setMouseGrabbed(boolean grabbed) {
        Mouse.setGrabbed(grabbed);
    }

    public boolean isMouseGrabbed() {
        return Mouse.isGrabbed();
    }

    public boolean hasFocus() {
        // hmm, not really the right thing, talk to the LWJGL guys
        return Display.isActive();
    }

    /**
     * Get the height of the standard screen resolution
     * 
     * @return The screen height
     */
    public int getScreenHeight() {
        return originalDisplayMode.getHeight();
    }

    /**
     * Get the width of the standard screen resolution
     * 
     * @return The screen width
     */
    public int getScreenWidth() {
        return originalDisplayMode.getWidth();
    }

    /**
     * Destroy the app game container
     */
    public void destroy() {
        Display.destroy();
        AL.destroy();
    }

    /**
     * A null stream to clear out those horrid errors
     *
     * @author kevin
     */
    private class NullOutputStream extends OutputStream {

        /**
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write(int b) throws IOException {
            // null implemetnation
        }

    }

    public void setIcons(String[] refs) throws SlickException {
        ByteBuffer[] bufs = new ByteBuffer[refs.length];
        for (int i = 0; i < refs.length; i++) {
            LoadableImageData data;
            boolean flip = true;

            if (refs[i].endsWith(".tga")) {
                data = new TGAImageData();
            } else {
                flip = false;
                data = new ImageIOImageData();
            }

            try {
                bufs[i] = data.loadImage(ResourceLoader.getResourceAsStream(refs[i]), flip, false, null);
            } catch (Exception e) {
                Log.error(e);
                throw new SlickException("Failed to set the icon");
            }
        }

        Display.setIcon(bufs);
    }
    
    /**
     * Set the default mouse cursor - i.e. the original cursor before any native 
     * cursor was set
     */
    public void setDefaultMouseCursor() {
        try {
            Mouse.setNativeCursor(null);
        } catch (LWJGLException e) {
            Log.error("Failed to reset mouse cursor", e);
        }
    }
    
}
