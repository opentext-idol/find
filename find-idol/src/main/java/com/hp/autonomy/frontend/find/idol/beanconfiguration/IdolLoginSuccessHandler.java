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
    private final String adminUrl;
    private final String roleDefault;
    private final String roleAdmin;
    private final AuthenticationInformationRetriever<?> authenticationInformationRetriever;

    public IdolLoginSuccessHandler(
        final String configUrl,
        final String applicationUrl,
        final String adminUrl,
        final String roleDefault,
        final String roleAdmin,
        final AuthenticationInformationRetriever<?> authenticationInformationRetriever
    ) {
        this.configUrl = configUrl;
        this.applicationUrl = applicationUrl;
        this.adminUrl = adminUrl;
        this.roleDefault = roleDefault;
        this.roleAdmin = roleAdmin;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
    }

    @Override
    protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response) {
        final Authentication authentication = authenticationInformationRetriever.getAuthentication();

        for(final GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            final String authority = grantedAuthority.getAuthority();

            if(roleDefault.equalsIgnoreCase(authority)) {
                return configUrl;
            }
            else if (roleAdmin.equalsIgnoreCase(authority)) {
                return adminUrl;
            }
        }

        return applicationUrl;
    }
}
