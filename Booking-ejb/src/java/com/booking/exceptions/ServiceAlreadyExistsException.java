package com.booking.exceptions;

/**
 *
 * @author Jesús Soriano
 */
public class ServiceAlreadyExistsException extends Exception {

    /**
     * Creates a new instance of <code>ServiceAlreadyExistsException</code>
     * without detail message.
     */
    public ServiceAlreadyExistsException() {
    }

    /**
     * Constructs an instance of <code>ServiceAlreadyExistsException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public ServiceAlreadyExistsException(String msg) {
        super(msg);
    }
}
