package org.cell2d.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * An ArraySet is a Set implemented as a resizable array, just like that of an
 * ArrayList. An ArraySet takes up less memory than other Set types, and offers
 * at-least-comparable performance to a HashSet when the Set size is very small.
 * For best results, the initial capacity of an ArraySet should be set to the
 * smallest number that the ArraySet's size will almost certainly never exceed.
 * @see java.util.ArrayList
 * @param <E> The type of elements in this ArraySet
 * @author Andrew Heyman
 */
public class ArraySet<E> implements Set<E> {
    
    private final ArrayList<E> list;
    
    /**
     * Constructs an empty ArraySet with the specified initial capacity.
     * @param initialCapacity The initial capacity of the ArraySet
     */
    public ArraySet(int initialCapacity) {
        list = new ArrayList<>(initialCapacity);
    }
    
    /**
     * Constructs an empty ArraySet with an initial capacity of 10.
     */
    public ArraySet() {
        list = new ArrayList<>();
    }
    
    /**
     * Constructs an ArraySet containing the elements of the specified
     * Collection. The ArraySet is created with enough initial capacity to hold
     * these elements.
     * @param c The Collection whose elements are to be placed into this
     * ArraySet
     */
    public ArraySet(Collection<? extends E> c) {
        list = new ArrayList<>(c);
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }
    
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }
    
    @Override
    public Object[] toArray() {
        return list.toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }
    
    @Override
    public boolean add(E e) {
        if (list.contains(e)) {
            return false;
        }
        list.add(e);
        return true;
    }
    
    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean setChanged = false;
        for (E element : c) {
            if (!list.contains(element)) {
                list.add(element);
                setChanged = true;
            }
        }
        return setChanged;
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }
    
    @Override
    public void clear() {
        list.clear();
    }
    
    /**
     * Trims the capacity of this ArraySet to be equal to its current size. This
     * operation runs in time proportional to the ArraySet's size, but it
     * minimizes the amount of memory taken up by the ArraySet.
     */
    public final void trimToSize() {
        list.trimToSize();
    }
    
    /**
     * Increases the capacity of this ArraySet, if necessary, to be at least the
     * specified minimum capacity.
     * @param minCapacity The minimum capacity that this ArraySet should have
     */
    public final void ensureCapacity(int minCapacity) {
        list.ensureCapacity(minCapacity);
    }
    
}
