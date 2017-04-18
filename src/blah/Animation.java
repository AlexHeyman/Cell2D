package blah;

import java.util.Arrays;

/**
 * <p>An Animation is a sequence of one or more Animatable frames that may be
 * instantiated in an AnimationInstance in order to be displayed one after
 * another and/or smoothly transitioned between. The frames are indexed by the
 * integers from 0 to getNumFrames() - 1 inclusive, and each has its own
 * duration that is measured in time units by default. Durations of 0 or less
 * are interpreted as infinite.</p>
 * 
 * <p>The frames of Animations may be other Animations in addition to single
 * Sprites, which allows for the creation of multi-dimensional Animations in a
 * similar manner to multi-dimensional arrays.</p>
 * 
 * <p>In order to be of use, Animations need not represent linear progressions
 * of frames; they may also be collections of counterpart Animations that need
 * to be switched between without an AnimationInstance losing its place in them.
 * </p>
 * 
 * <p>Once created, Animations are static and immutable, with all movement
 * through time happening in AnimationInstances.</p>
 * 
 * <p>All of Animation's constructors treat a null Animatable as equivalent to
 * Sprite.BLANK, and thus no frame of an Animation may be null.</p>
 * @author Andrew Heyman
 */
public class Animation implements Animatable {
    
    /**
     * A blank Animation with Sprite.BLANK, duration 0, as its only frame.
     */
    public static final Animation BLANK = new Animation();
    
    private final Animatable[] frames;
    private final double[] frameDurations;
    private final boolean[][] compatibilities;
    private final int level;
    
    private Animation() {
        this.frames = new Animatable[1];
        this.frames[0] = Sprite.BLANK;
        this.frameDurations = new double[1];
        this.frameDurations[0] = 0;
        compatibilities = new boolean[1][1];
        compatibilities[0][0] = true;
        level = 1;
    }
    
    private static Animatable[] arrayOf(Animatable animatable) {
        Animatable[] array = new Animatable[1];
        array[0] = animatable;
        return array;
    }
    
    private static double[] arrayOfOnes(int length) {
        double[] array = new double[length];
        Arrays.fill(array, 1);
        return array;
    }
    
    /**
     * Creates a new Animation with the specified Animatable object, duration 0,
     * as its only frame.
     * @param frame The Animatable object out of which to make the Animation
     */
    public Animation(Animatable frame) {
        this(arrayOf(frame), new double[1]);
    }
    
    /**
     * Creates a new Animation with the Animatable objects in the specified
     * array as its frames. Each frame will have a duration of 1.
     * @param frames The array of the Animation's frames
     */
    public Animation(Animatable[] frames) {
        this(frames, arrayOfOnes(frames.length));
    }
    
    /**
     * Creates a new Animation out of the Animatable objects in the specified
     * array of frames. Each frame will have a duration that is the value at its
     * corresponding index in the array of frame durations.
     * @param frames The array of the Animation's frames
     * @param frameDurations The array of the Animation's frame durations
     */
    public Animation(Animatable[] frames, double[] frameDurations) {
        if (frames.length == 0) {
            throw new RuntimeException("Attempted to create an empty Animation");
        }
        if (frames.length != frameDurations.length) {
            throw new RuntimeException("Attempted to create an Animation with different numbers of frames and frame durations");
        }
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == null) {
                frames[i] = Sprite.BLANK;
            }
        }
        this.frames = frames.clone();
        this.frameDurations = frameDurations.clone();
        compatibilities = new boolean[frames.length][0];
        for (int i = frames.length - 1; i >= 0; i--) {
            compatibilities[i] = new boolean[i + 1];
            compatibilities[i][i] = true;
            for (int j = 0; j < i; j++) {
                compatibilities[i][j] = checkCompatibility(frames[i], frames[j]);
            }
        }
        int maxLevel = 0;
        for (Animatable frame : frames) {
            int frameLevel = frame.getLevel();
            if (frameLevel > maxLevel) {
                maxLevel = frameLevel;
            }
        }
        level = maxLevel + 1;
    }
    
    private static Animatable[] spriteSheetToFrames(SpriteSheet spriteSheet, int x1, int y1, int x2, int y2, boolean columns) {
        if (x2 < x1 || y2 < y1) {
            throw new RuntimeException("Attempted to create an Animation from a region of a sprite sheet defined by invalid coordinates");
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
    
    /**
     * Creates an Animation with the Sprites in a rectangular region of a
     * SpriteSheet as its frames. Each frame will have a duration of 1.
     * @param spriteSheet The SpriteSheet out of which to make the Animation
     * @param x1 The x-coordinate, in Sprites, of the region's left edge
     * @param y1 The y-coordinate, in Sprites, of the region's top edge
     * @param x2 The x-coordinate, in Sprites, of the region's right edge
     * @param y2 The y-coordinate, in Sprites, of the region's bottom edge
     * @param columns If true, the Sprites will be read from the SpriteSheet in
     * columns from top to bottom, the columns going from left to right.
     * Otherwise, they will be read in rows from left to right, the rows going
     * from top to bottom. The Sprites will appear in the Animation in the order
     * in which they are read.
     */
    public Animation(SpriteSheet spriteSheet, int x1, int y1, int x2, int y2, boolean columns) {
        this(spriteSheetToFrames(spriteSheet, x1, y1, x2, y2, columns));
    }
    
    /**
     * Creates an Animation with the Sprites in a rectangular region of a
     * SpriteSheet as its frames. Each frame will have a duration that is the
     * value at its corresponding index in the array of frame durations.
     * @param spriteSheet The SpriteSheet out of which to make the Animation
     * @param x1 The x-coordinate, in Sprites, of the region's left edge
     * @param y1 The y-coordinate, in Sprites, of the region's top edge
     * @param x2 The x-coordinate, in Sprites, of the region's right edge
     * @param y2 The y-coordinate, in Sprites, of the region's bottom edge
     * @param columns If true, the Sprites will be read from the SpriteSheet in
     * columns from top to bottom, the columns going from left to right.
     * Otherwise, they will be read in rows from left to right, the rows going
     * from top to bottom. The Sprites will appear in the Animation in the order
     * in which they are read.
     * @param frameDurations The array of the Animation's frame durations
     */
    public Animation(SpriteSheet spriteSheet, int x1, int y1, int x2, int y2, boolean columns, double[] frameDurations) {
        this(spriteSheetToFrames(spriteSheet, x1, y1, x2, y2, columns), frameDurations);
    }
    
    private boolean checkCompatibility(Animatable frame1, Animatable frame2) {
        if (frame1.getLevel() == 0 && frame2.getLevel() == 0) {
            return true;
        } else if (frame1.getLevel() != frame2.getLevel() || frame1.getNumFrames() != frame2.getNumFrames()) {
            return false;
        }
        for (int i = 0; i < frame1.getNumFrames(); i++) {
            if (frame1.getFrameDuration(i) != frame2.getFrameDuration(i)
                    || !checkCompatibility(frame1.getFrame(i), frame2.getFrame(i))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public final int getLevel() {
        return level;
    }
    
    @Override
    public final int getNumFrames() {
        return frames.length;
    }
    
    @Override
    public final Animatable getFrame(int index) {
        return (index >= 0 && index < frames.length ? frames[index] : Sprite.BLANK);
    }
    
    @Override
    public final double getFrameDuration(int index) {
        return (index >= 0 && index < frameDurations.length ? frameDurations[index] : 0);
    }
    
    @Override
    public final boolean framesAreCompatible(int index1, int index2) {
        if (index1 < 0 || index1 >= frames.length
                || index2 < 0 || index2 >= frames.length) {
            return false;
        }
        if (index2 > index1) {
            return compatibilities[index2][index1];
        }
        return compatibilities[index1][index2];
    }
    
}
