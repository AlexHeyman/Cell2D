package cell2D;

import java.util.Arrays;

public class Animation implements Animatable {
    
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
    
    public Animation(Animatable frame) {
        this(arrayOf(frame), arrayOfOnes(1));
    }
    
    public Animation(Animatable[] frames) {
        this(frames, arrayOfOnes(frames.length));
    }
    
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
    
    public Animation(SpriteSheet spriteSheet, int x1, int y1, int x2, int y2, boolean columns) {
        this(spriteSheetToFrames(spriteSheet, x1, y1, x2, y2, columns));
    }
    
    public Animation(SpriteSheet spriteSheet, int x1, int y1, int x2, int y2, boolean columns, double[] frameDurations) {
        this(spriteSheetToFrames(spriteSheet, x1, y1, x2, y2, columns), frameDurations);
    }
    
    private boolean checkCompatibility(Animatable frame1, Animatable frame2) {
        if (frame1.getLevel() == 0 && frame2.getLevel() == 0) {
            return true;
        } else if (frame1.getLevel() != frame2.getLevel() || frame1.getLength() != frame2.getLength()) {
            return false;
        }
        for (int i = 0; i < frame1.getLength(); i++) {
            if (!checkCompatibility(frame1.getFrame(i), frame2.getFrame(i))) {
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
    public final int getLength() {
        return frames.length;
    }
    
    @Override
    public final Animatable getFrame(int index) {
        return (index >= 0 && index < frames.length ? frames[index] : Sprite.BLANK);
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
    
    @Override
    public final double getFrameDuration(int index) {
        return (index >= 0 && index < frameDurations.length ? frameDurations[index] : 0);
    }
    
}
