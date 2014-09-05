package com.hp.autonomy.frontend.find.authentication;

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

/*
 * $Id: //depot/products/frontend/site-admin/trunk/webapp/src/main/java/com/autonomy/controlcentre/authentication/LoginController.java#11 $
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author: luca.mandrioli $ on $Date: 2014/01/06 $
 */
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