/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * ControlPoint login API response.
 */
@Getter
public class ControlPointLoginResponse {
    private final String accessToken;
    private final int expiresInSeconds;

    public ControlPointLoginResponse(
        @JsonProperty(value = "access_token", required = true) final String accessToken,
        @JsonProperty("expires_in") final int expiresInSeconds
    ) {
        this.accessToken = accessToken;
        this.expiresInSeconds = expiresInSeconds;
    }

}
