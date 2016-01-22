package com.slightlyloony.blog.handlers;

import java.security.PrivilegedActionException;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HandlerIllegalStateException extends IllegalStateException {
    /**
     * Constructs an IllegalStateException with no detail message. A detail message is a String that describes this particular exception.
     */
    public HandlerIllegalStateException() {
    }


    /**
     * Constructs an IllegalStateException with the specified detail message.  A detail message is a String that describes this particular exception.
     *
     * @param s the String that contains a detailed message
     */
    public HandlerIllegalStateException( final String s ) {
        super( s );
    }


    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>
     * <p>Note that the detail message associated with <code>cause</code> is <i>not</i> automatically incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).  (A <tt>null</tt> value is permitted, and
     *                indicates that the cause is nonexistent or unknown.)
     * @since 1.5
     */
    public HandlerIllegalStateException( final String message, final Throwable cause ) {
        super( message, cause );
    }


    /**
     * Constructs a new exception with the specified cause and a detail message of <tt>(cause==null ? null : cause.toString())</tt> (which typically
     * contains the class and detail message of <tt>cause</tt>). This constructor is useful for exceptions that are little more than wrappers for other
     * throwables (for example, {@link PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).  (A <tt>null</tt> value is permitted, and
     *              indicates that the cause is nonexistent or unknown.)
     * @since 1.5
     */
    public HandlerIllegalStateException( final Throwable cause ) {
        super( cause );
    }
}
