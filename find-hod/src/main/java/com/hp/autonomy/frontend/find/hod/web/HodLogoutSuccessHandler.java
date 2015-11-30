/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
