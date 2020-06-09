/*
 * (c) Copyright 2014-2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.authentication.IdolPreAuthenticatedAuthenticationProvider;
import com.hp.autonomy.frontend.find.idol.authentication.FindCommunityRole;
import com.hp.autonomy.user.UserService;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
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
    static final String PRE_AUTHENTICATED_USERNAME_PROPERTY_KEY = "find.reverse-proxy.pre-authenticated-username";
    static final String PRE_AUTHENTICATED_ROLES_PROPERTY_KEY = "find.reverse-proxy.pre-authenticated-roles";

    private final UserService userService;
    private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;
    private final String preAuthenticatedRoles;

    private final String preAuthenticatedUsername;

    @Autowired
    public ReverseProxyIdolSecurityCustomizer(
            final UserService userService,
            final GrantedAuthoritiesMapper grantedAuthoritiesMapper,
            @Value("${find.reverse-proxy.pre-authenticated-roles}") final String preAuthenticatedRoles,
            @Value("${find.reverse-proxy.pre-authenticated-username}") final String preAuthenticatedUsername
    ) {

        this.userService = userService;
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
        this.preAuthenticatedRoles = preAuthenticatedRoles;
        this.preAuthenticatedUsername = preAuthenticatedUsername;
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void customize(final HttpSecurity http, final AuthenticationManager authenticationManager) throws Exception {
        final J2eePreAuthenticatedProcessingFilter filter = new J2eePreAuthenticatedProcessingFilter() {
            @Override
            protected Object getPreAuthenticatedPrincipal(final HttpServletRequest httpRequest) {
                return StringUtils.isNotBlank(preAuthenticatedUsername) ? preAuthenticatedUsername
                    : super.getPreAuthenticatedPrincipal(httpRequest);
            }
        };
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
