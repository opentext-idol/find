/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
