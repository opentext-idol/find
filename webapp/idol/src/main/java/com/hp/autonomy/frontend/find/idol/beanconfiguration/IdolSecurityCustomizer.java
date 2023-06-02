/*
 * Copyright 2014-2016 Open Text.
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

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.Collection;

public interface IdolSecurityCustomizer {

    @SuppressWarnings("ProhibitedExceptionDeclared")
    void customize(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception;

    Collection<AuthenticationProvider> getAuthenticationProviders();

}
