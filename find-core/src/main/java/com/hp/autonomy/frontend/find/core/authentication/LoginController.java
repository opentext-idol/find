/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.authentication;

import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @RequestMapping(value = "/loginPage")
    @ResponseBody
    public void login(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws IOException {
        final String baseUrl = "/login";
        String queryString = request.getQueryString();
        final Authentication<?> authentication = this.configService.getConfig().getAuthentication();

        if(LoginTypes.DEFAULT.equalsIgnoreCase(authentication.getMethod())) {
            final String defaultUsername = authentication.getDefaultLogin().getUsername();

            if(queryString != null) {
                queryString = "defaultLogin=" + defaultUsername + '&' + queryString;
            }
            else {
                queryString = "defaultLogin=" + defaultUsername;
            }
        }

        String redirectUrl = request.getContextPath() + baseUrl;

        if(queryString != null) {
            redirectUrl += '?' + queryString;
        }

        response.sendRedirect(redirectUrl);
    }
}
