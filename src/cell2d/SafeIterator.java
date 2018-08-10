package cell2d;

import java.util.Iterator;

/**
 * <p>A SafeIterator is an Iterator whose Collection can be safely modified by
 * some means other than the Iterator's own remove() method while the Iterator
 * is active. Changes to the Collection will be delayed until after all of its
 * SafeIterators stop iterating over it.</p>
 * @param <E> The type of element that this SafeIterator returns
 * @author Andrew Heyman
 */
public interface SafeIterator<E> extends Iterator<E> {
    
    /**
     * Instructs this SafeIterator to stop iterating over its Collection. Once
     * this method is called, hasNext() will always return false and remove()
     * will do nothing. This method must be called for every SafeIterator that
     * does not remain in use until it naturally runs out of elements to return,
     * or else its Collection will not be notified that it is no longer being
     * iterated over and that it is safe for it to update.
     */
    void stop();
    
}
