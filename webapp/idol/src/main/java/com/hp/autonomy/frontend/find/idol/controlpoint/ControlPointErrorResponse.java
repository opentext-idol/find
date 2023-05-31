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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Error response from a ControlPoint API call.
 */
@Getter
public class ControlPointErrorResponse {
    private final String id;
    private final String description;

    ControlPointErrorResponse(
        @JsonProperty(value = "error", required = true) final String id,
        @JsonProperty(value = "error_description", required = true) final String description
    ) {
        this.id = id;
        this.description = description;
    }

}
