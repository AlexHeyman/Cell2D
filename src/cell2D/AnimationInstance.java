package cell2D;

public class AnimationInstance {
    
    public static final AnimationInstance BLANK = new AnimationInstance();
    
    private final boolean isBlank;
    CellGameState state = null;
    private double timeFactor = 1;
    private final Animation animation;
    private final int level;
    private final int[] indices;
    private final double[] indexChanges;
    private final double[] speeds;
    private Sprite currentSprite;
    
    public AnimationInstance(Animation animation) {
        isBlank = false;
        this.animation = animation;
        level = animation.getLevel();
        indices = new int[level];
        indexChanges = new double[level];
        speeds = new double[level];
        updateCurrentSprite();
    }
    
    private AnimationInstance() {
        isBlank = true;
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
    
    public final boolean addTo(CellGameState state) {
        return state.addAnimInstance(this);
    }
    
    public final boolean remove() {
        if (state != null) {
            return state.removeAnimInstance(this);
        }
        return false;
    }
    
    public final double getTimeFactor() {
        return timeFactor;
    }
    
    public final void setTimeFactor(double timeFactor) {
        if (isBlank) {
            return;
        }
        if (timeFactor < 0) {
            throw new RuntimeException("Attempted to give an animation instance a negative time factor");
        }
        this.timeFactor = timeFactor;
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
        if (!isBlank && level >= 0 && level < indices.length) {
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
    
    public final void setSpeed(int level, double speed) {
        if (isBlank) {
            return;
        }
        if (level >= 0 && level < speeds.length) {
            speeds[level] = speed;
        }
    }
    
    final void update(double time) {
        if (isBlank || timeFactor == 0) {
            return;
        }
        time *= timeFactor;
        boolean spriteChanged = false;
        Animatable frame = animation;
        double duration;
        for (int i = indices.length - 1; i >= 0; i--) {
            if (speeds[i] != 0) {
                duration = frame.getFrameDuration(indices[i]);
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
    
}
