/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
@ConditionalOnProperty("server.reverseProxy")
public class ReverseProxyIdolSecurityCustomizer implements IdolSecurityCustomizer {

    private final AuthenticationProvider idolPreAuthenticatedAuthenticationProvider;

    @Autowired
    public ReverseProxyIdolSecurityCustomizer(final AuthenticationProvider idolPreAuthenticatedAuthenticationProvider) {
        this.idolPreAuthenticatedAuthenticationProvider = idolPreAuthenticatedAuthenticationProvider;
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void customize(final HttpSecurity http, final AuthenticationManager authenticationManager) throws Exception {
        final J2eePreAuthenticatedProcessingFilter filter = new J2eePreAuthenticatedProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);

        http.addFilter(filter);
    }

    @Override
    public Collection<AuthenticationProvider> getAuthenticationProviders() {
        return Collections.singleton(idolPreAuthenticatedAuthenticationProvider);
    }
}
