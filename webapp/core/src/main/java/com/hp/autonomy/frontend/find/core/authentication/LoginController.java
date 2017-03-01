/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.authentication;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.find.core.web.FindController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class LoginController {

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @RequestMapping(FindController.DEFAULT_LOGIN_PAGE)
    @ResponseBody
    public void login(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        String queryString = request.getQueryString();
        final Authentication<?> authentication = configService.getConfig().getAuthentication();

        if(LoginTypes.DEFAULT.equalsIgnoreCase(authentication.getMethod())) {
            final String defaultUsername = authentication.getDefaultLogin().getUsername();

            queryString = queryString != null ? "defaultLogin=" + defaultUsername + '&' + queryString : "defaultLogin=" + defaultUsername;
        }

        String redirectUrl = request.getContextPath() + FindController.LOGIN_PATH;

        if(queryString != null) {
            redirectUrl += '?' + queryString;
        }

        response.sendRedirect(redirectUrl);
    }
}
