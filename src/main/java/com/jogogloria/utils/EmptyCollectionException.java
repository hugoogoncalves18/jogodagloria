package com.jogogloria.utils;

/**
 * Signals that an operation that requires a non-empty collection was invoked on an
 * empty collection instance. This exception is intended for checked-exception
 * semantics so clients are made aware of the potential error at compile time.
 *
 * Typical usage examples:
 * - calling {@code removeFirst()} on an empty list
 * - calling {@code pop()} on an empty stack
 *
 * This class provides standard constructors for passing message and cause.
 *
 * @author HG
 * @since 1.0
 */
public class EmptyCollectionException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new EmptyCollectionException without a message.
     */
    public EmptyCollectionException() {
        super("Collection is empty");
    }

    /**
     * Creates a new EmptyCollectionException with a detail message.
     *
     * @param message human-readable description of the problem
     */
    public EmptyCollectionException(String message) {
        super(message);
    }

    /**
     * Creates a new EmptyCollectionException with a cause. The message of the
     * cause will be used as the detail message if {@code message} is {@code null}.
     *
     * @param message human-readable description of the problem
     * @param cause the underlying cause of this exception
     */
    public EmptyCollectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new EmptyCollectionException wrapping an underlying cause.
     *
     * @param cause the underlying cause of this exception
     */
    public EmptyCollectionException(Throwable cause) {
        super(cause);
    }
}
