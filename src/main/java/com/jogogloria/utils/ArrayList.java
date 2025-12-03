package com.jogogloria.utils;
import java.util.Iterator;


/**
 * Abstract base class for array-based list implementations. This class provides
 * fundamental behavior shared by ordered and unordered array lists, such as
 * element removal, shifting operations, boundary checks, and basic queries.
 * <p>
 * Concrete subclasses are responsible for defining how elements are inserted
 * (e.g., preserving order or not), while this class handles the internal storage
 * mechanism and structural maintenance.
 * </p>
 *
 * <h2>Main characteristics:</h2>
 * <ul>
 *   <li>Uses an array as underlying storage</li>
 *   <li>Grows dynamically when needed (subclasses may trigger expansion)</li>
 *   <li>Provides constant-time access by index</li>
 *   <li>Implements common removal operations and iteration</li>
 * </ul>
 *
 * <h2>Error handling:</h2>
 * <ul>
 *   <li>Throws {@link EmptyCollectionException} when attempting operations
 *       that require at least one element</li>
 *   <li>Throws {@link NoElementFoundException} when attempting to remove a
 *       specific element not present in the list</li>
 * </ul>
 *
 * @param <T> the type of elements stored in the list
 * @author HG
 * @since 1.0
 */
public abstract class ArrayList<T> implements ListADT<T> {

    /** Default initial array capacity if none is specified. */
    private final static int DEFAULT_CAPACITY = 10;

    /** Logical size of the list, representing the index of the next free slot. */
    protected int rear;

    /** Underlying array storing elements of the list. */
    protected T[] list;

    /**
     * Creates an empty list with default capacity.
     */
    public ArrayList() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates an empty list with a specified initial capacity.
     *
     * @param initialCapacity the initial size of the backing array
     * @throws IllegalArgumentException if {@code initialCapacity} is negative
     */
    @SuppressWarnings("unchecked")
    public ArrayList(int initialCapacity) {
        rear = 0;
        list = (T[])(new Comparable[initialCapacity]);
    }

    /**
     * Removes and returns the first element of the list. All remaining elements
     * are shifted one position to the left.
     *
     * @return the first element originally at index 0
     * @throws EmptyCollectionException if the list contains no elements
     */
    @Override
    public T removeFirst() throws EmptyCollectionException {
        if (isEmpty())
            throw new EmptyCollectionException("List is empty");

        T result = list[0];

        for (int i = 1; i < rear; i++) {
            list[i - 1] = list[i];
        }

        rear--;
        list[rear] = null;
        return result;
    }

    /**
     * Removes and returns the last element of the list.
     *
     * @return the last element currently in the list
     * @throws EmptyCollectionException if the list contains no elements
     */
    @Override
    public T removeLast() throws EmptyCollectionException {
        if (isEmpty())
            throw new EmptyCollectionException("List is empty");

        T result = list[rear - 1];
        list[rear - 1] = null;
        rear--;
        return result;
    }

    /**
     * Removes the first occurrence of a specified element. Remaining elements
     * are shifted to preserve contiguous storage.
     *
     * @param element the element to remove (must not be {@code null})
     * @return the removed element
     * @throws NoElementFoundException if the element does not exist in the list
     * @throws IllegalArgumentException if {@code element} is {@code null}
     */
    @Override
    public T remove(T element) throws NoElementFoundException {
        int pos = findPosition(element);
        if (pos == -1)
            throw new NoElementFoundException("Element not found in list");

        T result = list[pos];

        for (int i = pos; i < rear - 1; i++) {
            list[i] = list[i + 1];
        }

        rear--;
        list[rear] = null;
        return result;
    }

    /**
     * Returns the index of the first occurrence of the specified element.
     *
     * @param element the element to search for (must not be {@code null})
     * @return its index, or {@code -1} if not found
     * @throws IllegalArgumentException if {@code element} is {@code null}
     */
    private int findPosition(T element) {
        if (element == null)
            throw new IllegalArgumentException("element cannot be null");

        for (int i = 0; i < rear; i++) {
            if (element.equals(list[i])) return i;
        }
        return -1;
    }

    /**
     * Returns the first element in the list.
     *
     * @return element at index 0
     * @throws EmptyCollectionException if the list is empty
     */
    @Override
    public T first() throws EmptyCollectionException {
        if (isEmpty())
            throw new EmptyCollectionException("List is empty");
        return list[0];
    }

    /**
     * Returns the last element in the list.
     *
     * @return element stored at {@code rear - 1}
     * @throws EmptyCollectionException if the list is empty
     */
    @Override
    public T last() throws EmptyCollectionException {
        if (isEmpty())
            throw new EmptyCollectionException("List is empty");
        return list[rear - 1];
    }

    /**
     * Checks if the list contains a given element.
     *
     * @param target element to search for
     * @return {@code true} if found, {@code false} otherwise
     */
    @Override
    public boolean contains(T target) {
        return findPosition(target) != -1;
    }

    /**
     * Indicates whether the list contains no elements.
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    @Override
    public boolean isEmpty() {
        return rear == 0;
    }

    /**
     * Returns the number of stored elements.
     *
     * @return current logical size of the list
     */
    @Override
    public int size() {
        return rear;
    }

    /**
     * Returns an iterator over elements of the list.
     *
     * @return an iterator traversing the list from first to last
     */
    @Override
    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    /**
     * Private iterator implementation for this array-based structure. It simply
     * walks the internal array from index {@code 0} to {@code rear - 1}.
     */
    private class ArrayIterator implements Iterator<T> {

        /** Current index of the iteration. */
        private int current = 0;

        /**
         * Checks if more elements remain in the iteration.
         *
         * @return {@code true} if next() will return a value, {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return current < rear;
        }

        /**
         * Returns the next element in sequence.
         *
         * @return the next stored element
         * @throws NoElementFoundException if no further elements exist
         */
        @Override
        public T next() {
            if (!hasNext())
                return null;
            return list[current++];
        }
    }

    /**
     * Returns a string representation of the list in a comma-separated format.
     *
     * @return a string in the form {@code [a, b, c]}
     */
    @Override
    public String toString() {
        if (isEmpty())
            return "[]";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rear - 1; i++) {
            sb.append(list[i]).append(", ");
        }
        sb.append(list[rear - 1]).append("]");
        return sb.toString();
    }

    /**
     * Retorna o elemento na posição especificada.
     * @param index A posição (0-based) do elemento a retornar.
     * @return O elemento na posição.
     * @throws IndexOutOfBoundsException Se o índice for inválido.
     */
    public T get(int index) {
        if (index < 0 || index >= this.rear) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        return this.list[index];
    }
}
