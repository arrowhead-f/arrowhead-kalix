package eu.arrowhead.kalix.collections;

import java.util.*;

/**
 * A {@link Set} optimized for small collections (e.g. less than 20 elements).
 * <p>
 * Internally, an unsorted list of elements are maintain, which are scanned
 * linearly every time a new element is added, removed or requested.
 *
 * @param <E> Element type.
 */
public class UnsortedListSet<E> implements Set<E> {
    final List<E> elements;

    /**
     * Creates new empty {@link UnsortedListSet} with {@code 0} capacity.
     * <p>
     * An internal buffer is allocated only when data is first inserted into
     * the set.
     */
    public UnsortedListSet() {
        this(0);
    }

    private UnsortedListSet(final int capacity) {
        elements = new ArrayList<>(capacity);
    }

    private UnsortedListSet(final List<E> elements) {
        this.elements = elements;
    }

    static <E> UnsortedListSet<E> fromListUnchecked(final List<E> elements) {
        return new UnsortedListSet<E>(elements);
    }

    /**
     * Creates new {@link UnsortedListSet} with specified minimum capacity.
     *
     * @param capacity Number of elements to allocate memory for.
     * @param <E>      Element type.
     * @return New {@link UnsortedListSet}.
     */
    public static <E> UnsortedListSet<E> withCapacity(final int capacity) {
        return new UnsortedListSet<>(capacity);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return elements.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public Object[] toArray() {
        return elements.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        //noinspection SuspiciousToArrayCall
        return elements.toArray(a);
    }

    @Override
    public boolean add(E e) {
        if (elements.contains(e)) {
            return false;
        }
        return elements.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        final var size = elements.size();
        for (var i = 0; i < size; ++i) {
            final var element = elements.get(i);
            if (Objects.equals(element, o)) {
                elements.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        var changed = false;
        for (final var e : c) {
            changed = add(e);
        }
        return changed;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return elements.retainAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return elements.removeAll(c);
    }

    @Override
    public void clear() {
        elements.clear();
    }
}
