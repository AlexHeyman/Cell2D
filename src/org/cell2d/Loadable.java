package org.cell2d;

/**
 * <p>A Loadable object is an asset, such as an image or sound effect, that can
 * be manually loaded and unloaded into and out of memory. Loading may take a
 * moment, but while a Loadable is not loaded, it cannot be used.</p>
 * @author Alex Heyman
 */
public interface Loadable {
    
    /**
     * Returns whether this Loadable is loaded.
     * @return Whether this Loadable is loaded
     */
    boolean isLoaded();
    
    /**
     * Loads this Loadable if it is not already loaded.
     * @return Whether the loading occurred
     */
    boolean load();
    
    /**
     * Unloads this Loadable if it is currently loaded.
     * @return Whether the unloading occurred
     */
    boolean unload();
    
}
