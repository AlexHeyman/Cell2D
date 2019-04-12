package org.cell2d.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An ArrayMap is a Map implemented as a resizable array, just like that of an
 * ArrayList. An ArrayMap takes up less memory than other Map types, and offers
 * better performance when the Map size is very small. For best results, the
 * initial capacity of an ArrayMap should be set to the smallest number that the
 * ArrayMap's size will almost certainly never exceed.
 * @param <K> The type of keys maintained by this ArrayMap
 * @param <V> The type of mapped values
 * @see java.util.ArrayList
 * @author Andrew Heyman
 */
public class ArrayMap<K,V> implements Map<K,V> {
    
    private static class Entry<K,V> implements Map.Entry<K,V> {
        
        private final K key;
        private V value;
        
        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public final K getKey() {
            return key;
        }
        
        @Override
        public final V getValue() {
            return value;
        }
        
        @Override
        public final V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        
    }
    
    private final ArrayList<Entry<K,V>> list;
    private final KeySet keySet;
    private final Values values;
    private final EntrySet entrySet;
    
    /**
     * Constructs an empty ArrayMap with the specified initial capacity.
     * @param initialCapacity The initial capacity of the ArrayMap
     */
    public ArrayMap(int initialCapacity) {
        list = new ArrayList<>(initialCapacity);
        keySet = new KeySet();
        values = new Values();
        entrySet = new EntrySet();
    }
    
    /**
     * Constructs an empty ArrayMap with an initial capacity of 10.
     */
    public ArrayMap() {
        list = new ArrayList<>();
        keySet = new KeySet();
        values = new Values();
        entrySet = new EntrySet();
    }
    
    /**
     * Constructs an ArrayMap with the same mappings as the specified Map. The
     * ArrayMap is created with enough initial capacity to hold these mappings.
     * @param m The Map whose mappings are to be placed into this ArrayMap
     */
    public ArrayMap(Map<? extends K, ? extends V> m) {
        list = new ArrayList<>(m.size());
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            list.add(new Entry<>(entry.getKey(), entry.getValue()));
        }
        keySet = new KeySet();
        values = new Values();
        entrySet = new EntrySet();
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
    public boolean containsKey(Object key) {
        for (Entry<K,V> entry : list) {
            if (entry.key == key) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean containsValue(Object value) {
        for (Entry<K,V> entry : list) {
            if (entry.value == value) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public V get(Object key) {
        for (Entry<K,V> entry : list) {
            if (entry.key == key) {
                return entry.value;
            }
        }
        return null;
    }
    
    @Override
    public V put(K key, V value) {
        for (Entry<K,V> entry : list) {
            if (entry.key == key) {
                return entry.setValue(value);
            }
        }
        list.add(new Entry<>(key, value));
        return null;
    }
    
    @Override
    public V remove(Object key) {
        Iterator<Entry<K,V>> iterator = list.iterator();
        while (iterator.hasNext()) {
            Entry<K,V> entry = iterator.next();
            if (entry.key == key) {
                iterator.remove();
                return entry.value;
            }
        }
        return null;
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void clear() {
        list.clear();
    }
    
    private class KeySetIterator implements Iterator<K> {
        
        private int index = 0;
        private int lastIndex = -1;
        
        @Override
        public final boolean hasNext() {
            return index < list.size();
        }
        
        @Override
        public final K next() {
            if (index < list.size()) {
                K next = list.get(index).key;
                lastIndex = index;
                index++;
                return next;
            } else {
                throw new NoSuchElementException();
            }
        }
        
        @Override
        public final void remove() {
            if (lastIndex == -1) {
                throw new IllegalStateException();
            }
            list.remove(lastIndex);
            index--;
            lastIndex = -1;
        }
        
    }
    
    private class KeySet implements Set<K> {
        
        @Override
        public final int size() {
            return list.size();
        }
        
        @Override
        public final boolean isEmpty() {
            return list.isEmpty();
        }
        
        @Override
        public final boolean contains(Object o) {
            for (Entry<K,V> entry : list) {
                if (entry.key == o) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final Iterator<K> iterator() {
            return new KeySetIterator();
        }
        
        @Override
        public final Object[] toArray() {
            int size = list.size();
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = list.get(i).key;
            }
            return array;
        }
        
        @Override
        public final <T> T[] toArray(T[] a) {
            int size = list.size();
            if (size <= a.length) {
                for (int i = 0; i < size; i++) {
                    a[i] = (T)list.get(i).key;
                }
                if (size < a.length) {
                    a[size] = null;
                }
                return a;
            }
            T[] newArray = (T[])a[size];
            for (int i = 0; i < size; i++) {
                newArray[i] = (T)list.get(i).key;
            }
            return newArray;
        }
        
        @Override
        public final boolean add(K e) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean remove(Object o) {
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (entry.key == o) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final boolean containsAll(Collection<?> c) {
            for (Object element : c) {
                if (!containsKey(element)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public final boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean retainAll(Collection<?> c) {
            boolean removedAny = false;
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (!c.contains(entry.key)) {
                    iterator.remove();
                    removedAny = true;
                }
            }
            return removedAny;
        }
        
        @Override
        public final boolean removeAll(Collection<?> c) {
            boolean removedAny = false;
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (c.contains(entry.key)) {
                    iterator.remove();
                    removedAny = true;
                }
            }
            return removedAny;
        }
        
        @Override
        public final void clear() {
            list.clear();
        }
        
    }
    
    @Override
    public Set<K> keySet() {
        return keySet;
    }
    
    private class ValuesIterator implements Iterator<V> {
        
        private int index = 0;
        private int lastIndex = -1;
        
        @Override
        public final boolean hasNext() {
            return index < list.size();
        }
        
        @Override
        public final V next() {
            if (index < list.size()) {
                V next = list.get(index).value;
                lastIndex = index;
                index++;
                return next;
            } else {
                throw new NoSuchElementException();
            }
        }
        
        @Override
        public final void remove() {
            if (lastIndex == -1) {
                throw new IllegalStateException();
            }
            list.remove(lastIndex);
            index--;
            lastIndex = -1;
        }
        
    }
    
    private class Values implements Collection<V> {
        
        @Override
        public final int size() {
            return list.size();
        }
        
        @Override
        public final boolean isEmpty() {
            return list.isEmpty();
        }
        
        @Override
        public final boolean contains(Object o) {
            for (Entry<K,V> entry : list) {
                if (entry.value == o) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final Iterator<V> iterator() {
            return new ValuesIterator();
        }
        
        @Override
        public final Object[] toArray() {
            int size = list.size();
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = list.get(i).value;
            }
            return array;
        }
        
        @Override
        public final <T> T[] toArray(T[] a) {
            int size = list.size();
            if (size <= a.length) {
                for (int i = 0; i < size; i++) {
                    a[i] = (T)list.get(i).value;
                }
                if (size < a.length) {
                    a[size] = null;
                }
                return a;
            }
            T[] newArray = (T[])a[size];
            for (int i = 0; i < size; i++) {
                newArray[i] = (T)list.get(i).value;
            }
            return newArray;
        }
        
        @Override
        public final boolean add(V e) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean remove(Object o) {
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (entry.value == o) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final boolean containsAll(Collection<?> c) {
            for (Object element : c) {
                if (!containsValue(element)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public final boolean addAll(Collection<? extends V> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean removeAll(Collection<?> c) {
            boolean removedAny = false;
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (!c.contains(entry.value)) {
                    iterator.remove();
                    removedAny = true;
                }
            }
            return removedAny;
        }
        
        @Override
        public final boolean retainAll(Collection<?> c) {
            boolean removedAny = false;
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (c.contains(entry.value)) {
                    iterator.remove();
                    removedAny = true;
                }
            }
            return removedAny;
        }
        
        @Override
        public final void clear() {
            list.clear();
        }
        
    }
    
    @Override
    public Collection<V> values() {
        return values;
    }
    
    private class EntrySetIterator implements Iterator<Map.Entry<K,V>> {
        
        private int index = 0;
        private int lastIndex = -1;
        
        @Override
        public final boolean hasNext() {
            return index < list.size();
        }
        
        @Override
        public final Map.Entry<K,V> next() {
            if (index < list.size()) {
                Map.Entry<K,V> next = list.get(index);
                lastIndex = index;
                index++;
                return next;
            } else {
                throw new NoSuchElementException();
            }
        }
        
        @Override
        public final void remove() {
            if (lastIndex == -1) {
                throw new IllegalStateException();
            }
            list.remove(lastIndex);
            index--;
            lastIndex = -1;
        }
        
    }
    
    private class EntrySet implements Set<Map.Entry<K,V>> {
        
        @Override
        public final int size() {
            return list.size();
        }
        
        @Override
        public final boolean isEmpty() {
            return list.isEmpty();
        }
        
        @Override
        public final boolean contains(Object o) {
            for (Entry<K,V> entry : list) {
                if (entry == o) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntrySetIterator();
        }
        
        @Override
        public final Object[] toArray() {
            int size = list.size();
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = list.get(i);
            }
            return array;
        }
        
        @Override
        public final <T> T[] toArray(T[] a) {
            int size = list.size();
            if (size <= a.length) {
                for (int i = 0; i < size; i++) {
                    a[i] = (T)list.get(i);
                }
                if (size < a.length) {
                    a[size] = null;
                }
                return a;
            }
            T[] newArray = (T[])a[size];
            for (int i = 0; i < size; i++) {
                newArray[i] = (T)list.get(i);
            }
            return newArray;
        }
        
        @Override
        public final boolean add(Map.Entry<K,V> e) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean remove(Object o) {
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (entry == o) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final boolean containsAll(Collection<?> c) {
            for (Object element : c) {
                if (!contains(element)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public final boolean addAll(Collection<? extends Map.Entry<K,V>> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public final boolean retainAll(Collection<?> c) {
            boolean removedAny = false;
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (!c.contains(entry)) {
                    iterator.remove();
                    removedAny = true;
                }
            }
            return removedAny;
        }
        
        @Override
        public final boolean removeAll(Collection<?> c) {
            boolean removedAny = false;
            Iterator<Entry<K,V>> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entry<K,V> entry = iterator.next();
                if (c.contains(entry)) {
                    iterator.remove();
                    removedAny = true;
                }
            }
            return removedAny;
        }
        
        @Override
        public final void clear() {
            list.clear();
        }
        
    }
    
    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        return entrySet;
    }
    
    /**
     * Trims the capacity of this ArrayMap to be equal to its current size. This
     * operation runs in time proportional to the ArrayMap's size, but it
     * minimizes the amount of memory taken up by the ArrayMap.
     */
    public final void trimToSize() {
        list.trimToSize();
    }
    
    /**
     * Increases the capacity of this ArrayMap, if necessary, to be at least the
     * specified minimum capacity.
     * @param minCapacity The minimum capacity that this ArrayMap should have
     */
    public final void ensureCapacity(int minCapacity) {
        list.ensureCapacity(minCapacity);
    }
    
}
