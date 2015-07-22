/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FindController {

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService;

    @Autowired
    private HodAuthenticationRequestService hodAuthenticationRequestService;

    @Autowired
    private ObjectMapper contextObjectMapper;

    @RequestMapping("/")
    public void index(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String contextPath = request.getContextPath();

        if(LoginTypes.DEFAULT.equals(authenticationConfigService.getConfig().getAuthentication().getMethod())) {
            response.sendRedirect(contextPath + "/loginPage");
        }
        else {
            response.sendRedirect(contextPath + "/public/");
        }
    }

    // TODO pull out strings
    @RequestMapping(value = "/sso", method = RequestMethod.GET)
    public ModelAndView sso() throws JsonProcessingException, HodErrorException {
        final Map<String, Object> ssoConfig = new HashMap<>();
        ssoConfig.put("authenticatePath", "/authenticate-sso");
        ssoConfig.put("combinedRequestApi", "/api/authentication/combined-request");
        ssoConfig.put("errorPage", "/client-authentication-error");
        ssoConfig.put("listApplicationRequest", hodAuthenticationRequestService.getListApplicationRequest());
        ssoConfig.put("listApplicationRequestApi", "/api/authentication/list-application-request");
        ssoConfig.put("ssoPage", System.getProperty("find.hod.sso", "https://www.idolondemand.com/sso.html"));
        ssoConfig.put("ssoEntryPage", "/sso");

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("configJson", contextObjectMapper.writeValueAsString(ssoConfig));

        return new ModelAndView("sso", attributes);
    }
}
