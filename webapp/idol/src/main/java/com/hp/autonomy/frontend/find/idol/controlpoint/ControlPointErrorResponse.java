/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
