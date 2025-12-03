package com.jogogloria.utils;

/**
 * Node used internally by {@link LinkedQueue}. Stores an element of type {@code T}
 * and a reference to the next node in the queue.
 *
 * @param <T> the type of element stored in the node
 */
class LinearNode<T> {

    /** Reference to the next node in the queue. */
    LinearNode<T> next;

    /** The element stored in this node. */
    private T element;

    /**
     * Creates an empty node with no element and no next node.
     */
    public LinearNode() {
        this.element = null;
        this.next = null;
    }

    /**
     * Creates a node storing the given element. The {@code next} reference is initialized to {@code null}.
     *
     * @param elem the element to store
     */
    public LinearNode(T elem) {
        this.element = elem;
        this.next = null;
    }

    /**
     * Returns the element stored in this node.
     *
     * @return the stored element
     */
    public T getElement() {
        return element;
    }

    /**
     * Updates the element stored in this node.
     *
     * @param elem the element to store
     */
    public void setElement(T elem) {
        this.element = elem;
    }

    /**
     * Returns the next node in the queue.
     *
     * @return the next node, or {@code null} if this is the last node
     */
    public LinearNode<T> getNext() {
        return next;
    }

    /**
     * Sets the reference to the next node in the queue.
     *
     * @param node the node that should follow this one
     */
    public void setNext(LinearNode<T> node) {
        this.next = node;
    }
}

/**
 * Singly-linked queue implementation based on {@link LinearNode}.
 * Follows the FIFO (First In, First Out) principle and implements {@link QueueADT}.
 *
 * <p>
 * The queue maintains references to both the front (head) and rear (tail) nodes,
 * enabling constant-time enqueue and dequeue operations. When the queue is empty,
 * both references are {@code null}.
 * </p>
 *
 * @param <T> the type of elements stored in the queue
 */
public class LinkedQueue<T> implements QueueADT<T> {

    /** Front node of the queue (element to be dequeued next). */
    private LinearNode<T> front;

    /** Rear node of the queue (most recently enqueued element). */
    private LinearNode<T> rear;

    /** Number of elements in the queue. */
    private int count;

    /**
     * Creates an empty queue with no elements.
     */
    public LinkedQueue() {
        this.front = this.rear = null;
        count = 0;
    }

    /**
     * Inserts the specified element at the rear of the queue.
     *
     * @param element the element to add
     */
    @Override
    public void enqueue(T element) {
        LinearNode<T> newNode = new LinearNode<>(element);

        if (isEmpty()) {
            front = newNode;
        } else {
            rear.setNext(newNode);
        }
        rear = newNode;
        count++;
    }

    /**
     * Removes and returns the element at the front of the queue.
     *
     * @return the element removed
     * @throws EmptyCollectionException if the queue is empty
     */
    @Override
    public T dequeue() throws EmptyCollectionException {
        if (isEmpty()) {
            throw new EmptyCollectionException("Queue is empty");
        }
        T result = front.getElement();
        front = front.getNext();
        count--;
        if (isEmpty()) {
            rear = null;
        }
        return result;
    }

    /**
     * Returns the element at the front of the queue without removing it.
     *
     * @return the element at the front
     * @throws EmptyCollectionException if the queue is empty
     */
    @Override
    public T first() throws EmptyCollectionException {
        if (isEmpty()) {
            throw new EmptyCollectionException("Queue is empty");
        }
        return front.getElement();
    }

    /**
     * Checks whether the queue contains no elements.
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns the number of elements in the queue.
     *
     * @return the size of the queue
     */
    @Override
    public int size() {
        return count;
    }

    /**
     * Returns a string representation of the queue elements from front to rear.
     * The output is of the form:
     * <pre>
     * Topo - elem1-- elem2-- elem3-- null
     * </pre>
     *
     * @return string representation of the queue
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Topo - ");

        LinearNode<T> current = front;
        while (current != null) {
            sb.append(current.getElement()).append("-- ");
            current = current.getNext();
        }
        sb.append("null");
        return sb.toString();
    }
}
