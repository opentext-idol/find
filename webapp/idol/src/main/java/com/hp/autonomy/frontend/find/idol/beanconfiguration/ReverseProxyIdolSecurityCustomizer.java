/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.authentication.IdolPreAuthenticatedAuthenticationProvider;
import com.hp.autonomy.frontend.find.idol.authentication.FindCommunityRole;
import com.hp.autonomy.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(ReverseProxyIdolSecurityCustomizer.REVERSE_PROXY_PROPERTY_KEY)
public class ReverseProxyIdolSecurityCustomizer implements IdolSecurityCustomizer {

    static final String REVERSE_PROXY_PROPERTY_KEY = "server.reverseProxy";
    static final String PRE_AUTHENTICATED_ROLES_PROPERTY_KEY = "find.reverse-proxy.pre-authenticated-roles";

    private final UserService userService;
    private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;
    private final String preAuthenticatedRoles;

    @Autowired
    public ReverseProxyIdolSecurityCustomizer(
            final UserService userService,
            final GrantedAuthoritiesMapper grantedAuthoritiesMapper,
            @Value("${find.reverse-proxy.pre-authenticated-roles}") final String preAuthenticatedRoles
    ) {

        this.userService = userService;
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
        this.preAuthenticatedRoles = preAuthenticatedRoles;
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
        return Collections.singleton(new IdolPreAuthenticatedAuthenticationProvider(
                userService,
                grantedAuthoritiesMapper,
                Arrays.stream(preAuthenticatedRoles.split(","))
                        .map(FindCommunityRole::fromValue)
                        .map(FindCommunityRole::value)
                        .collect(Collectors.toSet())
        ));
    }
}
