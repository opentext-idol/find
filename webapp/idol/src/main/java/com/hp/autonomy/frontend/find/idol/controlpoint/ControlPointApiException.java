/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import lombok.Getter;
import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A ControlPoint API responded with an error.
 */
@Getter
public class ControlPointApiException extends Exception {
    /**
     * ControlPoint error code.
     */
    private final ErrorId errorId;
    /**
     * HTTP response code.
     */
    private final int statusCode;

    public ControlPointApiException(final int statusCode, final ControlPointErrorResponse error) {
        super(error.getDescription());
        this.statusCode = statusCode;
        errorId = ErrorId.fromValue(error.getId());
    }

    public ControlPointApiException(final String message) {
        super(message);
        statusCode = HttpStatus.SC_OK;
        errorId = ErrorId.UNKNOWN;
    }

    public enum ErrorId {
        INVALID_GRANT("Invalid Grant"),
        /**
         * Any error code not yet mapped here.
         */
        UNKNOWN("Unknown");

        // mapping from the value we receive in the API response
        private final static Map<String, ErrorId> byValue =
            Arrays.stream(ErrorId.values())
                .collect(Collectors.toMap(errorId -> errorId.value, errorId -> errorId));

        private final String value;

        ErrorId(final String value) {
            this.value = value;
        }

        /**
         * Lookup an error code received in an API response.  Unknown values map to {@link UNKNOWN}.
         */
        public static ErrorId fromValue(final String value) {
            return byValue.getOrDefault(value, UNKNOWN);
        }

    }

}
