/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.find.core.web.ErrorResponse;
import com.hp.autonomy.hod.client.error.HodErrorCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class HodErrorResponse extends ErrorResponse {

    private final HodErrorCode hodErrorCode;

    public HodErrorResponse(final String message, final HodErrorCode hodErrorCode) {
        super(message);
        this.hodErrorCode = hodErrorCode;
    }
}
