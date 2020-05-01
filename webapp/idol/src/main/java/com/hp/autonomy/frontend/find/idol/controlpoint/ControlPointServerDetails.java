/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.hp.autonomy.frontend.configuration.authentication.UsernameAndPassword;
import lombok.Builder;
import lombok.Getter;

// username and password are optional because we currently only use APIs which don't require
// authentication
/**
 * Configuration used when building Control API requests.  Username and password are optional.
 */
@Getter
@Builder(toBuilder = true)
public class ControlPointServerDetails {
    private final String protocol;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    @Builder.Default
    private final String basePath = "/WebApi/api";
}
