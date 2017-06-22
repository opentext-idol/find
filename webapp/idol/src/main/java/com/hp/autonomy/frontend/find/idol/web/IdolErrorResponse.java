/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.find.core.web.ErrorResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(callSuper = true)
class IdolErrorResponse extends ErrorResponse {
    private final String backendErrorCode;

    @Setter
    private Boolean isUserError = false;

    IdolErrorResponse(final String message, final String backendErrorCode) {
        super(message);
        this.backendErrorCode = backendErrorCode;
    }
}
