/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.find.core.web.ErrorResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
class IdolErrorResponse extends ErrorResponse {

    private final String backendErrorCode;

    IdolErrorResponse(final String message, final String backendErrorCode) {
        super(message);
        this.backendErrorCode = backendErrorCode;
    }
}
