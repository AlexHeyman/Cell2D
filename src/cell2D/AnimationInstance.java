package cell2D;

import org.newdawn.slick.Graphics;

/**
 * An AnimationInstance is an instance of an Animation that represents a point
 * in the Animation and rates at which that point progresses through the
 * Animation. An AnimationInstance has one level for each dimension of its
 * Animation, numbered 0 through the number of dimensions minus 1 inclusive. For
 * example, an instance of a level 2 Animation (an Animation of Animations) has
 * two levels: level 0 for the component Animations, and level 1 for the
 * Animation of Animations.
 * 
 * At each of its levels, an AnimationInstance has an index of its current
 * position in that level of the Animation, as well as a speed that represents
 * how fast, in frame duration units per time unit, it advances through that
 * level. If a level's speed is negative, the AnimationInstance will cycle
 * through the Animation at that level backward. If an AnimationInstance moves
 * forward past a level's end or backward past its beginning, it will loop back
 * to the beginning or end, respectively.
 * 
 * When an AnimationInstance is drawn, it will appear as the Sprite in its
 * Animation that its current indices specify. The Filters that can be
 * successfully applied to that Sprite when it is drawn through the
 * AnimationInstance are the same ones as when the Sprite is drawn by itself.
 * 
 * AnimationInstances keep track of time by being added to one CellGameState
 * each. An AnimationInstance's time factor represents how many time units the
 * AnimationInstance will experience every frame while assigned to an active
 * CellGameState. If its own time factor is negative, an AnimationInstance will
 * use its assigned CellGameState's time factor instead. If an AnimationInstance
 * is assigned to an inactive CellGameState or none at all, time will not pass
 * for it.
 * @author Andrew Heyman
 */
public class AnimationInstance implements Drawable {
    
    /**
     * A blank AnimationInstance of Animation.BLANK.
     */
    public static final AnimationInstance BLANK = new AnimationInstance();
    
    private final boolean blank;
    CellGameState state = null;
    private double timeFactor = -1;
    private final Animation animation;
    private final int level;
    private final int[] indices;
    private final double[] indexChanges;
    private final double[] speeds;
    private Sprite currentSprite;
    
    private AnimationInstance() {
        blank = true;
        this.animation = Animation.BLANK;
        level = 1;
        indices = new int[1];
        indexChanges = new double[1];
        speeds = new double[1];
        currentSprite = Sprite.BLANK;
    }
    
    /**
     * Creates a new AnimationInstance of the specified Animation. The
     * instance's indices and speeds at each level will start at 0.
     * @param animation The animation to make the AnimationInstance of
     */
    public AnimationInstance(Animation animation) {
        blank = false;
        this.animation = animation;
        level = animation.getLevel();
        indices = new int[level];
        indexChanges = new double[level];
        speeds = new double[level];
        updateCurrentSprite();
    }
    
    private void updateCurrentSprite() {
        Animatable frame = animation;
        for (int i = indices.length - 1; i >= 0; i--) {
            frame = frame.getFrame(indices[i]);
            if (frame instanceof Sprite) {
                currentSprite = (Sprite)frame;
                return;
            }
        }
        currentSprite = Sprite.BLANK;
    }
    
    /**
     * Returns the Sprite that is specified by this AnimationInstance's current
     * indices, and that the draw() methods will draw if called now.
     * @return This AnimationInstance's current Sprite
     */
    public final Sprite getCurrentSprite() {
        return currentSprite;
    }
    
    /**
     * Returns the CellGameState to which this AnimationInstance is currently
     * assigned, or null if it is assigned to none.
     * @return The CellGameState to which this AnimationInstance is currently
     * assigned
     */
    public final CellGameState getGameState() {
        return state;
    }
    
    /**
     * Sets the CellGameState to which this AnimationInstance is currently
     * assigned. If it is set to a null CellGameState, this AnimationInstance
     * will be removed from its current CellGameState if it has one.
     * @param state The CellGameState to add this AnimationInstance to
     */
    public final void setGameState(CellGameState state) {
        if (this.state != null) {
            this.state.removeAnimInstance(this);
        }
        if (state != null) {
            state.addAnimInstance(this);
        }
    }
    
    /**
     * Returns this AnimationInstance's time factor.
     * @return This AnimationInstance's time factor
     */
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    /**
     * Returns this AnimationInstance's effective time factor; that is, how many
     * time units it experiences every frame. If it is not assigned to a
     * CellGameState, this will be 0.
     * @return This AnimationInstance's effective time factor
     */
    public final double getEffectiveTimeFactor() {
        return (state == null ? 0 : (timeFactor < 0 ? state.getTimeFactor() : timeFactor));
    }
    
    /**
     * Sets this AnimationInstance's time factor to the specified value.
     * @param timeFactor The value to which the time factor will be set
     */
    public final void setTimeFactor(double timeFactor) {
        if (!blank) {
            this.timeFactor = timeFactor;
        }
    }
    
    /**
     * Returns the Animation that this AnimationInstance is an instance of.
     * @return The Animation that this AnimationInstance is an instance of
     */
    public final Animation getAnimation() {
        return animation;
    }
    
    /**
     * Returns this AnimationInstance's current index at its highest level. If
     * this AnimationInstance has only one level, this will be its only index.
     * @return This AnimationInstance's current index at its highest level
     */
    public final int getIndex() {
        return indices[indices.length - 1];
    }
    
    /**
     * Returns this AnimationInstance's current index at the specified level. If
     * the specified level is not valid, this method will return -1.
     * @param level The level of the index to be returned
     * @return The current index at the specified level
     */
    public final int getIndex(int level) {
        return (level >= 0 && level < indices.length ? indices[level] : -1);
    }
    
    private double setIndex(int level, Animatable frame, int index, boolean resetLowerIndices) {
        int length = frame.getNumFrames();
        index %= length;
        if (index < 0) {
            index += length;
        }
        if (level != 0 && (resetLowerIndices || !frame.framesAreCompatible(indices[level], index))) {
            for (int i = level - 1; i >= 0; i--) {
                indices[i] = 0;
                indexChanges[i] = 0;
            }
        }
        indices[level] = index;
        double duration = frame.getFrameDuration(index);
        if (duration <= 0) {
            indexChanges[level] = 0;
        }
        return duration;
    }
    
    /**
     * Sets this AnimationInstance's index at its highest level to the specified
     * value. If this AnimationInstance has only one level, this will be its
     * only index.
     * @param index The value to which the index will be set
     */
    public final void setIndex(int index) {
        setIndex(indices.length - 1, index, true);
    }
    
    /**
     * Sets this AnimationInstance's index at the specified level to the
     * specified value. All indices at lower levels will be reset to 0.
     * @param level The level of the index to be set
     * @param index The value to which the index will be set
     */
    public final void setIndex(int level, int index) {
        setIndex(level, index, true);
    }
    
    /**
     * Sets this AnimationInstance's index at the specified level to the
     * specified value.
     * @param level The level of the index to be set
     * @param index The value to which the index will be set
     * @param resetLowerIndices If this is false and the frame at the new index
     * is compatible with the frame at the current one, the indices at lower
     * levels will not be reset to 0. Otherwise, they will be.
     */
    public final void setIndex(int level, int index, boolean resetLowerIndices) {
        if (!blank && level >= 0 && level < indices.length) {
            Animatable frame = animation;
            for (int i = indices.length - 1; i > level; i++) {
                frame = frame.getFrame(indices[i]);
            }
            setIndex(level, frame, index, resetLowerIndices);
            indexChanges[level] = 0;
        }
    }
    
    /**
     * Returns this AnimationInstance's speed at its highest level. If this
     * AnimationInstance has only one level, this will be its only speed.
     * @return This AnimationInstance's speed at its highest level
     */
    public final double getSpeed() {
        return speeds[speeds.length - 1];
    }
    
    /**
     * Returns this AnimationInstance's speed at the specified level. If the
     * specified level is not valid, this method will return 0.
     * @param level The level of the speed to be returned
     * @return The speed at the specified level
     */
    public final double getSpeed(int level) {
        return (level >= 0 && level < speeds.length ? speeds[level] : 0);
    }
    
    /**
     * Sets this AnimationInstance's speed at its highest level to the specified
     * value. If this AnimationInstance has only one level, this will be its
     * only speed.
     * @param speed The value to which the speed will be set
     */
    public final void setSpeed(double speed) {
        if (!blank) {
            speeds[indices.length - 1] = speed;
        }
    }
    
    /**
     * Sets this AnimationInstance's speed at the specified level to the
     * specified value.
     * @param level The level of the speed to be set
     * @param speed The value to which the speed will be set
     */
    public final void setSpeed(int level, double speed) {
        if (!blank && level >= 0 && level < speeds.length) {
            speeds[level] = speed;
        }
    }
    
    final void update() {
        if (blank) {
            return;
        }
        double time = getEffectiveTimeFactor();
        if (time == 0) {
            return;
        }
        boolean spriteChanged = false;
        Animatable frame = animation;
        for (int i = indices.length - 1; i >= 0; i--) {
            if (speeds[i] != 0) {
                double duration = frame.getFrameDuration(indices[i]);
                if (duration > 0) {
                    indexChanges[i] += time*speeds[i];
                    if (speeds[i] > 0) {
                        while (indexChanges[i] >= duration) {
                            spriteChanged = true;
                            indexChanges[i] -= duration;
                            duration = setIndex(i, frame, indices[i] + 1, false);
                            if (duration <= 0) {
                                break;
                            }
                        }
                    } else {
                        while (indexChanges[i] < 0) {
                            spriteChanged = true;
                            duration = setIndex(i, frame, indices[i] - 1, false);
                            if (duration <= 0) {
                                break;
                            }
                            indexChanges[i] -= duration;
                        }
                    }
                }
            }
            frame = frame.getFrame(indices[i]);
        }
        if (spriteChanged) {
            updateCurrentSprite();
        }
    }
    
    @Override
    public final void draw(Graphics g, int x, int y) {
        currentSprite.draw(g, x, y);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter) {
        currentSprite.draw(g, x, y, xFlip, yFlip, angle, alpha, filter);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter) {
        currentSprite.draw(g, x, y, scale, xFlip, yFlip, alpha, filter);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom) {
        currentSprite.draw(g, x, y, left, right, top, bottom);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom, boolean xFlip, boolean yFlip, double angle, double alpha, Filter filter) {
        currentSprite.draw(g, x, y, left, right, top, bottom, xFlip, yFlip, angle, alpha, filter);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom, double scale, boolean xFlip, boolean yFlip, double alpha, Filter filter) {
        currentSprite.draw(g, x, y, left, right, top, bottom, scale, xFlip, yFlip, alpha, filter);
    }
    
}
