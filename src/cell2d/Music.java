package cell2d;

/**
 * <p>A Music track is a piece of music that can be played in the background of
 * a CellGame. All playing and stopping of Music tracks is controlled by methods
 * of a CellGame so that the CellGame can keep track of them in its music stack.
 * </p>
 * 
 * <p>Like Sprites, SpriteSheets, and Sounds, Music tracks can be manually
 * loaded and unloaded into and out of memory. Loading may take a moment, but
 * while a Music track is not loaded, it cannot play.</p>
 * @author Andrew Heyman
 */
public class Music {
    
    /**
     * A blank Music track that produces no sound and plays indefinitely. It is
     * considered to be always looping, never paused, and with a position,
     * speed, and volume of 0.
     */
    public static final Music BLANK = new Music();
    
    private static enum FadeType {
        NONE, TO, OUT
    }
    
    private final boolean blank;
    private boolean loaded = false;
    private final String path;
    private final double loopStart, loopEnd;
    private Audio audio = null;
    private MusicInstance instance = null;
    private double speed;
    private double volume;
    private double pausePosition;
    private FadeType speedFadeType;
    private double speedFadeStart = 0;
    private double speedFadeDuration = 0;
    private double speedMSFading = 0;
    private FadeType volumeFadeType;
    private double volumeFadeStart = 0;
    private double volumeFadeDuration = 0;
    private double volumeMSFading = 0;
    
    private Music() {
        blank = true;
        loopStart = 0;
        loopEnd = -1;
        loaded = true;
        path = null;
        resetPlayData();
    }
    
    /**
     * Creates a new Music track from an audio file. Files of WAV, OGG, and
     * AIF(F) formats are supported. When this Music track is looping, it will
     * return to the start of the track once it reaches the end.
     * @param path The relative path to the audio file
     * @param load Whether this Music track should load upon creation
     */
    public Music(String path, boolean load) {
        this(path, 0, -1, load);
    }
    
    /**
     * Creates a new Music track from an audio file. Files of WAV, OGG, and
     * AIF(F) formats are supported.
     * @param path The relative path to the audio file
     * @param loopStart The position in seconds in this Music track to which it
     * should return at the start of a loop
     * @param loopEnd The position in seconds in this Music track that, when
     * reached, causes it to start a new loop if it is looping. If this is
     * negative, this Music track will start a new loop when it reaches the end
     * of the track.
     * @param load Whether this Music track should load upon creation
     */
    public Music(String path, double loopStart, double loopEnd, boolean load) {
        if (loopStart < 0) {
            throw new RuntimeException("Attempted to give a Music track a negative loop start position");
        } else if (loopEnd >= 0 && loopStart >= loopEnd) {
            throw new RuntimeException("Attempted to give a Music track a loop start position after or "
                    + "equal to its loop end position");
        }
        blank = false;
        this.path = path;
        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
        if (load) {
            load();
        }
        resetPlayData();
    }
    
    final void resetPlayData() {
        speed = 0;
        volume = 0;
        pausePosition = -1;
        speedFadeType = FadeType.NONE;
        volumeFadeType = FadeType.NONE;
    }
    
    /**
     * Returns whether this Music track is loaded.
     * @return Whether this Music track is loaded
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Loads this Music track if it is not already loaded.
     * @return Whether the loading occurred
     */
    public final boolean load() {
        if (loaded) {
            return false;
        }
        loaded = true;
        audio = new Audio(path);
        return true;
    }
    
    /**
     * Unloads this Music track if it is currently loaded.
     * @return Whether the unloading occurred
     */
    public final boolean unload() {
        if (blank || !loaded) {
            return false;
        }
        stop();
        audio.unload();
        audio = null;
        loaded = false;
        return true;
    }
    
    /**
     * Returns whether this Music track is currently playing. A Music track
     * counts as playing when it is paused.
     * @return Whether this Music track is currently playing
     */
    public final boolean isPlaying() {
        return instance != null;
    }
    
    final boolean play(MusicInstance instance) {
        if (loaded) {
            this.instance = instance;
            pausePosition = -1;
            speedFadeType = FadeType.NONE;
            volumeFadeType = FadeType.NONE;
            if (!blank) {
                speed = instance.destSpeed;
                volume = instance.destVolume;
                audio.play(speed, volume, false);
            }
            return true;
        }
        return false;
    }
    
    final void stop() {
        if (instance != null) {
            instance = null;
            resetPlayData();
            if (!blank) {
                audio.stop();
            }
        }
    }
    
    /**
     * Returns whether this Music track is currently paused.
     * @return Whether this Music track is currently paused
     */
    public final boolean isPaused() {
        return pausePosition >= 0;
    }
    
    /**
     * Pauses this Music track if it is currently playing.
     */
    public final void pause() {
        if (!blank && instance != null && pausePosition < 0) {
            pausePosition = audio.getPosition();
            audio.stop();
        }
    }
    
    /**
     * Resumes playing this Music track if it currently paused.
     */
    public final void resume() {
        if (!blank && instance != null && pausePosition >= 0) {
            audio.play(speed, volume, instance.loop);
            audio.setPosition(pausePosition);
            pausePosition = -1;
        }
    }
    
    /**
     * Returns the music player's position in seconds in this Music track, or 0
     * if it is not currently playing.
     * @return The music player's position in seconds in this Music track
     */
    public final double getPosition() {
        return (audio == null ? 0 : (pausePosition < 0 ? audio.getPosition() : pausePosition));
    }
    
    /**
     * Sets the music player's position in seconds in this Music track, if it is
     * currently playing.
     * @param position The music player's new position in seconds in this Music
     * track
     */
    public final void setPosition(double position) {
        if (!blank && instance != null) {
            if (pausePosition < 0) {
                audio.setPosition(position);
            } else {
                double length = audio.getLength();
                pausePosition = position % length;
                if (pausePosition < 0) {
                    pausePosition += length;
                }
            }
        }
    }
    
    /**
     * Returns the speed at which this Music track is playing, with 1
     * representing no speed change, or 0 if it is not currently playing.
     * @return The speed at which this Music track is playing
     */
    public final double getSpeed() {
        return speed;
    }
    
    /**
     * Sets the speed at which this Music track is playing, with 1 representing
     * no speed change, if it is currently playing. If this Music track's speed
     * is fading, the fade will be interrupted.
     * @param speed The speed at which this Music track should play
     */
    public final void setSpeed(double speed) {
        if (!blank && instance != null) {
            speedFadeType = FadeType.NONE;
            instance.destSpeed = speed;
            this.speed = speed;
            if (pausePosition < 0) {
                audio.setSpeed(speed);
            }
        }
    }
    
    /**
     * Instructs the music player to gradually fade this Music track's speed to
     * the specified speed over the specified duration, if this Music track is
     * currently playing.
     * @param speed This Music track's eventual speed
     * @param duration The fade's duration in seconds
     */
    public final void fadeSpeed(double speed, double duration) {
        if (!blank && instance != null) {
            speedFadeType = FadeType.TO;
            speedFadeStart = this.speed;
            instance.destSpeed = speed;
            speedFadeDuration = duration*1000;
            speedMSFading = 0;
        }
    }
    
    /**
     * Returns the volume at which this Music track is playing, with 1
     * representing no volume change, or 0 if it is not currently playing.
     * @return The volume at which this Music track is playing
     */
    public final double getVolume() {
        return volume;
    }
    
    /**
     * Sets the volume at which this Music track is playing, with 1 representing
     * no volume change, if it is currently playing. If this Music track's
     * volume is fading, the fade will be interrupted.
     * @param volume The volume at which this Music track should play
     */
    public final void setVolume(double volume) {
        if (!blank && instance != null) {
            volumeFadeType = FadeType.NONE;
            instance.destVolume = volume;
            this.volume = volume;
            if (pausePosition < 0) {
                audio.setVolume(volume);
            }
        }
    }
    
    /**
     * Instructs the music player to gradually fade this Music track's volume to
     * the specified volume over the specified duration, if this Music track is
     * currently playing.
     * @param volume This Music track's eventual volume
     * @param duration The fade's duration in seconds
     */
    public final void fadeVolume(double volume, double duration) {
        if (!blank && instance != null) {
            volumeFadeType = FadeType.TO;
            volumeFadeStart = this.volume;
            instance.destVolume = volume;
            volumeFadeDuration = duration*1000;
            volumeMSFading = 0;
        }
    }
    
    /**
     * Instructs the music player to gradually fade this Music track's volume to
     * 0 over the specified duration, stopping this Music track once it is
     * silent, if it is currently playing.
     * @param duration The duration in seconds of the fade-out
     */
    public final void fadeOut(double duration) {
        if (blank) {
            stop();
        } else if (instance != null) {
            volumeFadeType = FadeType.OUT;
            volumeFadeStart = this.volume;
            instance.destVolume = 0;
            volumeFadeDuration = duration*1000;
            volumeMSFading = 0;
        }
    }
    
    /**
     * Returns whether this Music track is currently looping indefinitely.
     * @return Whether this Music track is currently looping indefinitely
     */
    public final boolean isLooping() {
        return instance != null && (blank || instance.loop);
    }
    
    /**
     * Sets whether this Music track is looping indefinitely, if it is currently
     * playing.
     * @param loop Whether this Music track should loop indefinitely
     */
    public final void setLooping(boolean loop) {
        if (!blank && instance != null) {
            instance.loop = loop;
        }
    }
    
    final boolean update(double msElapsed) {
        if (!blank && pausePosition < 0) {
            if (!audio.isPlaying()) {
                if (instance.loop) {
                    audio.play(speed, volume, false);
                    audio.setPosition(loopStart);
                } else {
                    instance = null;
                    resetPlayData();
                    return true;
                }
            } else if (loopEnd >= 0 && instance.loop && audio.getPosition() >= loopEnd) {
                audio.setPosition(loopStart + ((audio.getPosition() - loopStart) % (loopEnd - loopStart)));
            }
            if (speedFadeType != FadeType.NONE) {
                speedMSFading = Math.min(speedMSFading + msElapsed, speedFadeDuration);
                if (speedMSFading == speedFadeDuration) {
                    speed = instance.destSpeed;
                    audio.setSpeed(speed);
                    speedFadeType = FadeType.NONE;
                } else {
                    speed = speedFadeStart
                            + (speedMSFading/speedFadeDuration)*(instance.destSpeed - speedFadeStart);
                    audio.setSpeed(speed);
                }
            }
            if (volumeFadeType != FadeType.NONE) {
                volumeMSFading = Math.min(volumeMSFading + msElapsed, volumeFadeDuration);
                if (volumeMSFading == volumeFadeDuration) {
                    volume = instance.destVolume;
                    audio.setVolume(volume);
                    if (volumeFadeType == FadeType.OUT) {
                        instance = null;
                        resetPlayData();
                        audio.stop();
                        return true;
                    }
                    volumeFadeType = FadeType.NONE;
                } else {
                    volume = volumeFadeStart
                            + (volumeMSFading/volumeFadeDuration)*(instance.destVolume - volumeFadeStart);
                    audio.setVolume(volume);
                }
            }
        }
        return false;
    }
    
}
