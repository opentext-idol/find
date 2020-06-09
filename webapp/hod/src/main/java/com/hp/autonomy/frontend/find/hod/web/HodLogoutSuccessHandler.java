/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.hod.sso.HodTokenLogoutSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HodLogoutSuccessHandler implements LogoutSuccessHandler {

    private final String settingsLogoutSuccessUrl;
    private final HodTokenLogoutSuccessHandler hodTokenLogoutSuccessHandler;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public HodLogoutSuccessHandler(final HodTokenLogoutSuccessHandler hodTokenLogoutSuccessHandler, final String settingsLogoutSuccessUrl) {
        this.hodTokenLogoutSuccessHandler = hodTokenLogoutSuccessHandler;
        this.settingsLogoutSuccessUrl = settingsLogoutSuccessUrl;
    }

    @Override
    public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof HodAuthentication) {
            hodTokenLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
        }
        else {
            redirectStrategy.sendRedirect(request, response, settingsLogoutSuccessUrl);
        }
    }
}
