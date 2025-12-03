package com.jogogloria.utils;

import java.util.Iterator;

/**
 * A simple singly linked list implementation offering basic operations such as
 * add, remove by index, and in-place reversal. Now implements {@link Iterable}
 * to support foreach loops and external iteration.
 *
 * @param <T> the element type stored by the linked list
 */
public class LinkedList<T> implements Iterable<T> {

    /**
     * Node structure used internally by the LinkedList.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    /** Reference to the first node of the list. */
    private Node<T> head;

    /**
     * Returns true if the list is empty.
     *
     * @return {@code true} if the list contains no elements
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Appends an element to the end of the list.
     *
     * @param data element to append
     */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
            return;
        }
        Node<T> current = head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = newNode;
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index position of the element to remove (0-based)
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IllegalStateException if the list is empty
     */
    public void remove(int index) {
        if (head == null) {
            throw new IllegalStateException("List is empty");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index must be non-negative");
        }
        if (index == 0) {
            head = head.next;
            return;
        }

        Node<T> current = head;
        Node<T> previous = null;
        int position = 0;

        while (current != null && position < index) {
            previous = current;
            current = current.next;
            position++;
        }

        if (current == null) {
            throw new IndexOutOfBoundsException("Index is out of bounds");
        }

        previous.next = current.next;
    }

    /**
     * Replaces all occurrences of {@code existingElement} with {@code newElement}.
     *
     * @param existingElement element to be replaced
     * @param newElement replacement element
     * @return number of elements replaced
     * @throws IllegalStateException if list is empty
     */
    public int replace(T existingElement, T newElement) {
        if (head == null) {
            throw new IllegalStateException("List is empty");
        }
        return replaceRecursive(head, existingElement, newElement);
    }

    private int replaceRecursive(Node<T> node, T existingElement, T newElement) {
        if (node == null) return 0;

        int count = 0;
        if (node.data != null && node.data.equals(existingElement)) {
            node.data = newElement;
            count = 1;
        }
        return count + replaceRecursive(node.next, existingElement, newElement);
    }

    /**
     * Reverses the list in place using recursion.
     */
    public void reverseRecursive() {
        if (head == null || head.next == null) {
            return;
        }
        head = reverseRecursiveHelper(head);
    }

    private Node<T> reverseRecursiveHelper(Node<T> node) {
        if (node.next == null) {
            return node;
        }
        Node<T> newHead = reverseRecursiveHelper(node.next);
        node.next.next = node;
        node.next = null;
        return newHead;
    }

    /**
     * Returns a custom iterator for this LinkedList.
     *
     * @return iterator for sequential traversal
     */
    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    /**
     * Private iterator class enabling foreach iteration.
     */
    private class LinkedListIterator implements Iterator<T> {

        private Node<T> current = head;

        /**
         * Returns true if at least one more element exists.
         */
        @Override
        public boolean hasNext() {
            return current != null;
        }

        /**
         * Returns the next element and advances the iterator.
         */
        @Override
        public T next() {
            T data = current.data;
            current = current.next;
            return data;
        }
    }

    /**
     * Returns a string representation of the list.
     *
     * @return string in the form: a -> b -> c -> null
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node<T> current = head;

        while (current != null) {
            sb.append(current.data).append(" -> ");
            current = current.next;
        }

        sb.append("null");
        return sb.toString();
    }
}
