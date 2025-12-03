package com.jogogloria.utils;

/**
 * Thrown when an expected element cannot be located inside a collection.
 * This can happen when invoking removal or search operations that require
 * the element to be present.
 *
 * Example usage:
 * - attempting to remove an element from an ordered/unordered list when
 *   the element is not present
 * - searching for a value by key in a search tree when it doesn't exist
 *
 * @author HG
 * @since 1.0
 */
public class NoElementFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new NoElementFoundException with a default message.
     */
    public NoElementFoundException() {
        super("No such element found in the collection");
    }

    /**
     * Constructs a new NoElementFoundException with the specified detail message.
     *
     * @param message descriptive message explaining why the element was not found
     */
    public NoElementFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new NoElementFoundException with a message and cause.
     *
     * @param message descriptive message explaining why the element was not found
     * @param cause underlying cause of this exception
     */
    public NoElementFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new NoElementFoundException wrapping another throwable.
     *
     * @param cause the underlying cause
     */
    public NoElementFoundException(Throwable cause) {
        super(cause);
    }
}
