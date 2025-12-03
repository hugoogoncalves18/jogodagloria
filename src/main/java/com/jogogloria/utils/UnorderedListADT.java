package com.jogogloria.utils;


/**
 * An unordered list provides insertions at front or rear and the ability to
 * insert a new element after a target element in the list.
 *
 * @param <T> element type
 */
public interface UnorderedListADT<T> extends ListADT<T> {
    void addToFront(T element);

    void addToRear(T element);

    /**
     * Inserts {@code element} immediately after {@code target} in the list.
     *
     * @param element element to insert
     * @param target existing element after which the insertion should occur
     * @throws NoElementFoundException if {@code target} is not present
     */
    void addAfter(T element, T target) throws NoElementFoundException;
}
