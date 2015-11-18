package com.hp.autonomy.frontend.find.core.exceptions;

/**
 * Any error
 */
public class FindException extends Exception{
    public FindException(final String message) {
        super(message);
    }

    public FindException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
