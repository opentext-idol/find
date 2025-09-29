/*
 * Copyright 2020 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import lombok.Getter;
import org.apache.hc.core5.http.HttpStatus;

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
