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

import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
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

    IdolLoginSuccessHandler(
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
