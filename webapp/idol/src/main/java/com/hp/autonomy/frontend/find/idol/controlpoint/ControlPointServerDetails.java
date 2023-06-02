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

import com.hp.autonomy.frontend.configuration.authentication.UsernameAndPassword;
import lombok.Builder;
import lombok.Getter;

// username and password are optional because we currently only use APIs which don't require
// authentication
/**
 * Configuration used when building ControlPoint API requests.  Username and password are optional.
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
