package org.cell2d;

import java.util.Set;

/**
 * <p>An Animatable object is one that may be incorporated into an Animation as
 * one of its frames. Every Animatable object is either a Sprite or an
 * Animation. For simplicity's sake, both Sprites and Animations may be treated
 * as consisting of one or more Animatable frames, indexed by the integers from
 * 0 to getNumFrames() - 1 inclusive, each with its own duration in fracunits.
 * Durations of 0 or less are interpreted as infinite. A Sprite has exactly one
 * frame, namely itself, with a duration of 0.</p>
 * @see Animation
 * @author Alex Heyman
 */
public interface Animatable {
    
    /**
     * Returns how many levels of Animations this Animatable and its frames and
     * sub-frames comprise. For example, an Animation of Animations of
     * Animations has a level of 3.
     * @return This Animatable's level
     */
    int getLevel();
    
    /**
     * Returns how many frames this Animatable has.
     * @return This Animatable's number of frames
     */
    int getNumFrames();
    
    /**
     * Returns this Animatable's frame at the specified index.
     * @param index The index of the frame to be returned
     * @return This Animatable's frame at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    Animatable getFrame(int index);
    
    /**
     * Returns the duration in fracunits of this Animatable's frame at the
     * specified index.
     * @param index The index of the frame whose duration is to be returned
     * @return The duration of this Animatable's frame at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    long getFrameDuration(int index);
    
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
     * @throws IndexOutOfBoundsException if either of the two indices are out of
     * range
     */
    boolean framesAreCompatible(int index1, int index2);
    
    /**
     * Returns an unmodifiable Set view of all of the Sprites that ultimately
     * compose this Animatable. If this Animatable is a Sprite, the returned Set
     * will only contain the Sprite itself. If this Animatable is an Animation,
     * the returned Set will be equal to the union of the return values of
     * getSprites() for each of the Animation's frames.
     * @return The Sprites that ultimately compose this Animatable
     */
    Set<Sprite> getSprites();
    
    /**
     * Returns a new Animatable created by applying a Filter to all of the
     * Sprites of which this Animatable is ultimately composed. The returned
     * Animatable will have the same hierarchical Animation structure as this
     * Animatable. In particular, if this Animatable is a Sprite, the returned
     * Animatable will also be a Sprite. To avoid redundant Sprite creation,
     * Sprites keep track of the filtered copies of themselves that have been
     * created, so applying this method to the same Sprite twice will cause the
     * same filtered Sprite to be returned twice.
     * @param filter The Filter to apply to this Animatable's Sprites to create
     * the new Animatable
     * @param load Whether the new Animatable should load its Sprites upon its
     * creation
     * @return A filtered copy of this Animatable
     */
    Animatable getFilteredCopy(Filter filter, boolean load);
    
    /**
     * Returns a Drawable instantiation of this Animatable.
     * @return A Drawable instantiation of this Animatable
     */
    Drawable getInstance();
    
}
