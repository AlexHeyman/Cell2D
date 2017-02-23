package cell2D;

import org.newdawn.slick.Graphics;

public class AnimationInstance implements Drawable {
    
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
    
    public AnimationInstance(Animation animation) {
        blank = false;
        this.animation = animation;
        level = animation.getLevel();
        indices = new int[level];
        indexChanges = new double[level];
        speeds = new double[level];
        updateCurrentSprite();
    }
    
    private AnimationInstance() {
        blank = true;
        this.animation = Animation.BLANK;
        level = 1;
        indices = new int[1];
        indexChanges = new double[1];
        speeds = new double[1];
        currentSprite = Sprite.BLANK;
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
    
    public final Sprite getCurrentSprite() {
        return currentSprite;
    }
    
    public final CellGameState getGameState() {
        return state;
    }
    
    public final void setGameState(CellGameState state) {
        if (this.state != null) {
            this.state.removeAnimInstance(this);
        }
        if (state != null) {
            state.addAnimInstance(this);
        }
    }
    
    public final boolean addTo(CellGameState state) {
        return state.addAnimInstance(this);
    }
    
    public final boolean remove() {
        return (state == null ? false : state.removeAnimInstance(this));
    }
    
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    public final double getEffectiveTimeFactor() {
        return (timeFactor < 0 ? (state == null ? 0 : state.getTimeFactor()) : timeFactor);
    }
    
    public final void setTimeFactor(double timeFactor) {
        if (!blank) {
            this.timeFactor = timeFactor;
        }
    }
    
    public final Animation getAnimation() {
        return animation;
    }
    
    public final int getIndex() {
        return indices[indices.length - 1];
    }
    
    public final int getIndex(int level) {
        return (level >= 0 && level < indices.length ? indices[level] : -1);
    }
    
    private double setIndex(int level, Animatable frame, int index, boolean resetLowerIndices) {
        int length = frame.getLength();
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
    
    public final void setIndex(int index) {
        setIndex(indices.length - 1, index, true);
    }
    
    public final void setIndex(int level, int index) {
        setIndex(level, index, true);
    }
    
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
    
    public final double getSpeed() {
        return speeds[speeds.length - 1];
    }
    
    public final double getSpeed(int level) {
        return (level >= 0 && level < speeds.length ? speeds[level] : 0);
    }
    
    public final void setSpeed(double speed) {
        setSpeed(indices.length - 1, speed);
    }
    
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
    public final void draw(Graphics g, int x, int y, boolean xFlip, boolean yFlip, double angle, double alpha, String filter) {
        currentSprite.draw(g, x, y, xFlip, yFlip, angle, alpha, filter);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, double scale, boolean xFlip, boolean yFlip, double alpha, String filter) {
        currentSprite.draw(g, x, y, scale, xFlip, yFlip, alpha, filter);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom) {
        currentSprite.draw(g, x, y, left, right, top, bottom);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom, boolean xFlip, boolean yFlip, double angle, double alpha, String filter) {
        currentSprite.draw(g, x, y, left, right, top, bottom, xFlip, yFlip, angle, alpha, filter);
    }
    
    @Override
    public final void draw(Graphics g, int x, int y, int left, int right, int top, int bottom, double scale, boolean xFlip, boolean yFlip, double alpha, String filter) {
        currentSprite.draw(g, x, y, left, right, top, bottom, scale, xFlip, yFlip, alpha, filter);
    }
    
}
