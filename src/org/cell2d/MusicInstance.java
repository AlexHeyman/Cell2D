package org.cell2d;

/**
 * @author Alex Heyman
 */
class MusicInstance {
    
    private double destSpeed;
    private double destVolume;
    private boolean loop;
    
    MusicInstance(double destSpeed, double destVolume, boolean loop) {
        setDestSpeed(destSpeed);
        setDestVolume(destVolume);
        this.loop = loop;
    }
    
    final double getDestSpeed() {
        return destSpeed;
    }
    
    final void setDestSpeed(double destSpeed) {
        this.destSpeed = Math.max(destSpeed, 0);
    }
    
    final double getDestVolume() {
        return destVolume;
    }
    
    final void setDestVolume(double destVolume) {
        this.destVolume = Math.min(Math.max(destVolume, 0), 1);
    }
    
    final boolean isLooping() {
        return loop;
    }
    
    final void setLooping(boolean loop) {
        this.loop = loop;
    }
    
}
