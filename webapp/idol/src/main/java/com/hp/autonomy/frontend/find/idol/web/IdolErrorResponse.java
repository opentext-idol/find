/*
 * Copyright 2015-2017 Open Text.
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
