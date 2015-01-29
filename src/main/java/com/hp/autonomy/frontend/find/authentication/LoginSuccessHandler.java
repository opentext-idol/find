/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

@Service
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        for(final GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if("ROLE_DEFAULT".equalsIgnoreCase(grantedAuthority.getAuthority())) {
                return "/config/";
            }
        }

        return "/p/";
    }
}