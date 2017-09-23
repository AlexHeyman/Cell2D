package cell2d;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.newdawn.slick.openal.AiffData;
import org.newdawn.slick.openal.OggData;
import org.newdawn.slick.openal.OggDecoder;
import org.newdawn.slick.openal.WaveData;
import org.newdawn.slick.util.ResourceLoader;

/**
 * @author Andrew Heyman
 * @author Kevin Glass
 * @author Nathan Sweet <misc@n4te.com>
 */
class Audio {
    
    private static final int MAX_SOURCES = 64;
    private static boolean initialized = false;
    private static int numSources = 0;
    private static IntBuffer sources = null;
    private static long[] timesSourcesPlayed = null;
    
    private final int buffer;
    private int index = -1;
    private long timesSourcePlayed = -1;
    private final float length;
    
    Audio(String path) {
        if (!initialized) {
            init();
        }
        IntBuffer buf = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(buf);
        buffer = buf.get(0);
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".wav")) {
            WaveData data = WaveData.create(ResourceLoader.getResourceAsStream(path));
            AL10.alBufferData(buffer, data.format, data.data, data.samplerate);
        } else if (lowerPath.endsWith(".ogg")) {
            OggData ogg;
            try {
                ogg = new OggDecoder().getData(ResourceLoader.getResourceAsStream(path));
            } catch (IOException e) {
                throw new RuntimeException("Failed to load an OGG-format audio file: " + path);
            }
            AL10.alBufferData(buffer, ogg.channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16, ogg.data, ogg.rate);
        } else if (lowerPath.endsWith(".aif") || lowerPath.endsWith(".aiff")) {
            AiffData data = AiffData.create(new BufferedInputStream(ResourceLoader.getResourceAsStream(path)));
            AL10.alBufferData(buffer, data.format, data.data, data.samplerate);
        } else {
            throw new RuntimeException("Attempted to load an audio file with an unsupported format: " + path);
        }
        int bytes = AL10.alGetBufferi(buffer, AL10.AL_SIZE);
        int bits = AL10.alGetBufferi(buffer, AL10.AL_BITS);
        int channels = AL10.alGetBufferi(buffer, AL10.AL_CHANNELS);
        int freq = AL10.alGetBufferi(buffer, AL10.AL_FREQUENCY);
        int samples = bytes/(bits/8);
        length = ((float)samples/freq)/channels;
    }
    
    private static void init() {
        initialized = true;
        try {
            AL.create();
        } catch (LWJGLException e) {
            throw new RuntimeException(e.toString());
        }
        sources = BufferUtils.createIntBuffer(MAX_SOURCES);
        while (numSources < MAX_SOURCES) {
            IntBuffer buf = BufferUtils.createIntBuffer(1);
            AL10.alGenSources(buf);
            if (AL10.alGetError() != AL10.AL_NO_ERROR) {
                break;
            }
            sources.put(buf.get(0));
            numSources++;
        }
        timesSourcesPlayed = new long[numSources];
    }
    
    static void close() {
        if (initialized) {
            AL.destroy();
        }
    }
    
    void unload() {
        stop();
        AL10.alDeleteBuffers(buffer);
    }
    
    double getLength() {
        return length;
    }
    
    boolean isPlaying() {
        return (index < 0 ? false : timesSourcePlayed == timesSourcesPlayed[index]
                && AL10.alGetSourcei(sources.get(index), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING);
    }
    
    void play(double speed, double volume, boolean loop) {
        int freeIndex = -1;
        for (int i = 0; i < numSources; i++) {
            int state = AL10.alGetSourcei(sources.get(i), AL10.AL_SOURCE_STATE);
            if (state != AL10.AL_PLAYING && state != AL10.AL_PAUSED) {
                freeIndex = i;
                break;
            }
        }
        if (freeIndex >= 0) {
            stop();
            index = freeIndex;
            timesSourcesPlayed[index]++;
            timesSourcePlayed = timesSourcesPlayed[index];
            int source = sources.get(freeIndex);
            AL10.alSourceStop(source);
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
            AL10.alSourcef(source, AL10.AL_PITCH, (float)speed);
            AL10.alSourcef(source, AL10.AL_GAIN, (float)volume); 
            AL10.alSourcei(source, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
            AL10.alSourcePlay(source);
        }
    }
    
    void stop() {
        if (isPlaying()) {
            AL10.alSourceStop(sources.get(index));
        }
        index = -1;
    }
    
    double getPosition() {
        return (index < 0 ? 0 : AL10.alGetSourcef(sources.get(index), AL11.AL_SEC_OFFSET));
    }
    
    void setPosition(double position) {
        if (index >= 0) {
            position %= length;
            if (position < 0) {
                position += length;
            }
            AL10.alSourcef(sources.get(index), AL11.AL_SEC_OFFSET, (float)position);
        }
    }
    
    double getSpeed() {
        return (index < 0 ? 0 : AL10.alGetSourcef(sources.get(index), AL10.AL_PITCH));
    }
    
    void setSpeed(double speed) {
        if (index >= 0) {
            AL10.alSourcef(sources.get(index), AL10.AL_PITCH, (float)Math.max(speed, 0));
        }
    }
    
    double getVolume() {
        return (index < 0 ? 0 : AL10.alGetSourcef(sources.get(index), AL10.AL_GAIN));
    }
    
    void setVolume(double volume) {
        if (index >= 0) {
            AL10.alSourcef(sources.get(index), AL10.AL_GAIN, (float)Math.min(Math.max(volume, 0), 1));
        }
    }
    
}
