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
