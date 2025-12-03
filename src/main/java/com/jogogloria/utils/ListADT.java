package com.jogogloria.utils;

import java.util.Iterator;

/**
 * Abstract list data type describing a set of common list operations.
 * Implementations include linked or array-based lists, ordered or unordered.
 *
 * @param <T> element type stored in the list
 */
public interface ListADT<T> extends Iterable<T> {

    /**
     * Removes and returns the first element in the list.
     *
     * @return the removed element
     * @throws IllegalStateException if the list is empty
     */
    T removeFirst() throws com.jogogloria.utils.EmptyCollectionException;

    /**
     * Removes and returns the last element in the list.
     *
     * @return the removed last element
     * @throws IllegalStateException if the list is empty
     */
    T removeLast() throws com.jogogloria.utils.EmptyCollectionException;

    /**
     * Removes and returns the specified element from the list if present.
     * If the element is not present, {@code null} may be returned or an
     * implementation-specific exception thrown.
     *
     * @param element element to remove
     * @return the removed element or {@code null} if not present
     */
    T remove(T element) throws com.jogogloria.utils.NoElementFoundException, com.jogogloria.utils.EmptyCollectionException;

    /**
     * Returns the first element of the list without removing it.
     *
     * @return the first element, or {@code null} if the list is empty
     */
    T first() throws com.jogogloria.utils.EmptyCollectionException;

    /**
     * Returns the last element of the list without removing it.
     *
     * @return the last element, or {@code null} if the list is empty
     */
    T last() throws com.jogogloria.utils.EmptyCollectionException;

    /**
     * Returns {@code true} if the list contains the given element.
     *
     * @param target element to test
     * @return {@code true} if the element is present
     */
    boolean contains(T target);

    /**
     * Returns {@code true} if the list contains no elements.
     *
     * @return {@code true} if empty
     */
    boolean isEmpty();

    /**
     * Returns the number of elements in the list.
     *
     * @return size of list
     */
    int size();

    /**
     * Returns an iterator to traverse the elements in the list.
     *
     * @return iterator for list elements
     */
    Iterator<T> iterator();

    /**
     * Returns a string representation of the list. Implementations should
     * provide a human readable description of the elements in order.
     *
     * @return string representation
     */
    @Override
    String toString();
}
