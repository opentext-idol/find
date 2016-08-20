/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IdolLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final String configUrl;
    private final String applicationUrl;
    private final String roleDefault;
    private final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever;

    public IdolLoginSuccessHandler(
            final String configUrl,
            final String applicationUrl,
            final String roleDefault,
            final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever
    ) {
        this.configUrl = configUrl;
        this.applicationUrl = applicationUrl;
        this.roleDefault = roleDefault;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
    }

    @Override
    protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response) {
        final Authentication authentication = authenticationInformationRetriever.getAuthentication();

        for (final GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            final String authority = grantedAuthority.getAuthority();

            if (roleDefault.equalsIgnoreCase(authority)) {
                return configUrl;
            }
        }

        return applicationUrl;
    }
}
