package cell2D;

/**
 * An Animatable object is an object that may be incorporated into an Animation
 * as one of its frames. For simplicity's sake, all Animatable objects, not just
 * Animations, may be treated as consisting of one or more Animatable frames,
 * indexed by the integers from 0 to getNumFrames() - 1 inclusive, each with its
 * own duration. Durations of 0 or less are interpreted as infinite. An
 * Animatable object that is not an Animation has exactly one frame, namely
 * itself, with a duration of 0.
 * @author Andrew Heyman
 */
public interface Animatable {
    
    /**
     * Returns how many levels of Animations this Animatable and its frames and
     * sub-frames comprise. For example, an Animation of Animations of
     * Animations has a level of 3.
     * @return This Animatable's level
     */
    public int getLevel();
    
    /**
     * Returns how many frames this Animatable has.
     * @return This Animatable's number of frames
     */
    public int getNumFrames();
    
    /**
     * Returns this Animatable's frame at the specified index. If the specified
     * index is not valid, this method will return Sprite.BLANK.
     * @param index The index of the frame to be returned
     * @return This Animatable's frame at the specified index
     */
    public Animatable getFrame(int index);
    
    /**
     * Returns the duration of the frame at the specified index. If the
     * specified index is not valid, this method will return 0.
     * @param index The index of the frame whose duration is to be returned
     * @return The duration of the frame at the specified index
     */
    public double getFrameDuration(int index);
    
    /**
     * Returns whether the two frames at the specified indices are compatible
     * for AnimationInstances' transitioning purposes. An AnimationInstance of
     * this Animatable, if it is an Animation, or of an Animation that
     * incorporates this Animatable may transition between the frames at the
     * specified indices without resetting all of its indices at its lower
     * levels to 0 if and only if this method returns true.
     * @param index1 The index of the first frame
     * @param index2 The index of the second frame
     * @return Whether or not an AnimationInstance may transition between the
     * two frames without resetting its lower indices to 0
     */
    public boolean framesAreCompatible(int index1, int index2);
    
}
