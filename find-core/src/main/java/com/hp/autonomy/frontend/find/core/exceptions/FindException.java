/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.exceptions;

/**
 * Any error
 */
public class FindException extends RuntimeException {
    public FindException(final String message) {
        super(message);
    }

    public FindException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
