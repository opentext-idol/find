package com.hp.autonomy.frontend.find.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

/*
 * $Id: //depot/products/frontend/site-admin/trunk/webapp/src/main/java/com/autonomy/controlcentre/authentication/LoginController.java#11 $
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author: luca.mandrioli $ on $Date: 2014/01/06 $
 */
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