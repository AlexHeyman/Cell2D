package org.cell2d;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import org.cell2d.celick.openal.AiffData;
import org.cell2d.celick.openal.OggData;
import org.cell2d.celick.openal.OggDecoder;
import org.cell2d.celick.openal.WaveData;
import org.cell2d.celick.util.ResourceLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

/**
 * @author Andrew Heyman
 * @author Kevin Glass
 * @author Nathan Sweet <misc@n4te.com>
 */
class Audio {
    
    private static final int NUM_SOURCES = 64;
    private static boolean initialized = false;
    private static IntBuffer sources = null;
    private static int[] timesSourcesPlayed = null;
    
    private final int buffer;
    private int index = -1;
    private int timesSourcePlayed = -1;
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
            AL10.alBufferData(buffer, ogg.channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16,
                    ogg.data, ogg.rate);
        } else if (lowerPath.endsWith(".aif") || lowerPath.endsWith(".aiff")) {
            AiffData data = AiffData.create(
                    new BufferedInputStream(ResourceLoader.getResourceAsStream(path)));
            AL10.alBufferData(buffer, data.format, data.data, data.samplerate);
        } else {
            throw new RuntimeException("Attempted to load an audio file with an unsupported format: "
                    + path);
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
        sources = BufferUtils.createIntBuffer(NUM_SOURCES);
        for (int i = 0; i < NUM_SOURCES; i++) {
            IntBuffer buf = BufferUtils.createIntBuffer(1);
            AL10.alGenSources(buf);
            if (AL10.alGetError() != AL10.AL_NO_ERROR) {
                break;
            }
            sources.put(buf.get(0));
        }
        timesSourcesPlayed = new int[NUM_SOURCES];
    }
    
    final void unload() {
        stop();
        AL10.alDeleteBuffers(buffer);
    }
    
    final double getLength() {
        return length;
    }
    
    final boolean isPlaying() {
        return index >= 0 && timesSourcePlayed == timesSourcesPlayed[index]
                && AL10.alGetSourcei(sources.get(index), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }
    
    final void play(double speed, double volume, boolean loop) {
        int freeIndex = -1;
        for (int i = 0; i < NUM_SOURCES; i++) {
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
    
    final void stop() {
        if (isPlaying()) {
            AL10.alSourceStop(sources.get(index));
        }
        index = -1;
    }
    
    final double getPosition() {
        return (isPlaying() ? AL10.alGetSourcef(sources.get(index), AL11.AL_SEC_OFFSET) : 0);
    }
    
    final void setPosition(double position) {
        if (isPlaying()) {
            position %= length;
            if (position < 0) {
                position += length;
            }
            AL10.alSourcef(sources.get(index), AL11.AL_SEC_OFFSET, (float)position);
        }
    }
    
    final void setSpeed(double speed) {
        if (isPlaying()) {
            AL10.alSourcef(sources.get(index), AL10.AL_PITCH, (float)speed);
        }
    }
    
    final void setVolume(double volume) {
        if (isPlaying()) {
            AL10.alSourcef(sources.get(index), AL10.AL_GAIN, (float)volume);
        }
    }
    
}
