/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

/**
 * When calling a ControlPoint API, there were connection issues or the server behaved unexpectedly.
 */
public class ControlPointServiceException extends RuntimeException {

    public ControlPointServiceException(final String message) {
        super(message);
    }

    public ControlPointServiceException(final Throwable cause) {
        super(cause);
    }

}
