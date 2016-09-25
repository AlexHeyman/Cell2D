package ironclad2D;

public class Animation implements Animatable {
    
    public static final Animation BLANK = new Animation();
    
    private final Animatable[] frames;
    private final double[] frameDurations;
    private final boolean[][] compatibilities;
    private final int level;
    
    Animation(Animatable[] frames, double[] frameDurations) {
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
    
    private Animation() {
        this.frames = new Animatable[1];
        this.frames[0] = Sprite.BLANK;
        this.frameDurations = new double[1];
        this.frameDurations[0] = 0;
        compatibilities = new boolean[1][1];
        compatibilities[0][0] = true;
        level = 1;
    }
    
    private boolean checkCompatibility(Animatable frame1, Animatable frame2) {
        int level1 = frame1.getLevel();
        int level2 = frame2.getLevel();
        if (level1 == 0 && level2 == 0) {
            return true;
        }
        int length1 = frame1.getLength();
        if (level1 != level2 || length1 != frame2.getLength()) {
            return false;
        }
        for (int i = 0; i < length1; i++) {
            if (!checkCompatibility(frame1.getFrame(i), frame2.getFrame(i))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int getLevel() {
        return level;
    }
    
    @Override
    public int getLength() {
        return frames.length;
    }
    
    @Override
    public Animatable getFrame(int index) {
        if (index >= 0 && index < frames.length) {
            return frames[index];
        }
        return Sprite.BLANK;
    }
    
    @Override
    public boolean framesAreCompatible(int index1, int index2) {
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
    public double getFrameDuration(int index) {
        if (index >= 0 && index < frameDurations.length) {
            return frameDurations[index];
        }
        return 0;
    }
    
}
