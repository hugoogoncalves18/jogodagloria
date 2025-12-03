package com.jogogloria.utils;

/**
 * Queue abstract data type describing common queue operations. Implementations
 * should throw {@link EmptyCollectionException} when attempting to access
 * elements from an empty queue.
 *
 * @param <T> element type
 */
public interface QueueADT<T> {

    /**
     * Adds an element to the end of the queue.
     *
     * @param element element to add
     */
    void enqueue(T element);

    /**
     * Removes and returns the element at the front of the queue.
     *
     * @return the dequeued element
     * @throws EmptyCollectionException if the queue is empty
     */
    T dequeue() throws EmptyCollectionException;

    /**
     * Returns, but does not remove, the front element of the queue.
     *
     * @return the element at front
     * @throws EmptyCollectionException if the queue is empty
     */
    T first() throws EmptyCollectionException;

    /**
     * Tests whether the queue is empty.
     *
     * @return true if queue contains no elements
     */
    boolean isEmpty();

    /**
     * Returns the number of elements in the queue.
     *
     * @return size of queue
     */
    int size();

    @Override
    String toString();
}