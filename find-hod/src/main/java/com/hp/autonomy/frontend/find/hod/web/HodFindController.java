/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.ErrorController;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.hod.authentication.HodCombinedRequestController;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HodFindController extends FindController {

    public static final String SSO_PAGE = "/sso";
    public static final String SSO_AUTHENTICATION_URI = "/authenticate-sso";
    public static final String SSO_LOGOUT_PAGE = "/sso-logout";
    private static final String HOD_ENDPOINT = System.getProperty("find.iod.api");

    @Autowired
    private HodAuthenticationRequestService hodAuthenticationRequestService;

    @Autowired
    private ConfigService<?> configService;

    @Autowired
    private ObjectMapper contextObjectMapper;

    @RequestMapping(value = SSO_PAGE, method = RequestMethod.GET)
    public ModelAndView sso() throws JsonProcessingException, HodErrorException {
        final Map<String, Object> ssoConfig = new HashMap<>();
        ssoConfig.put("authenticatePath", SSO_AUTHENTICATION_URI);
        ssoConfig.put("combinedRequestApi", HodCombinedRequestController.COMBINED_REQUEST);
        ssoConfig.put("errorPage", ErrorController.CLIENT_AUTHENTICATION_ERROR);
        ssoConfig.put("listApplicationRequest", hodAuthenticationRequestService.getListApplicationRequest());
        ssoConfig.put("listApplicationRequestApi", HodCombinedRequestController.LIST_APPLICATION_REQUEST);
        ssoConfig.put("ssoPage", System.getProperty("find.hod.sso", "https://www.idolondemand.com/sso.html"));
        ssoConfig.put("ssoEntryPage", SSO_PAGE);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("configJson", convertToJson(ssoConfig));

        return new ModelAndView("sso", attributes);
    }

    @RequestMapping(value = SSO_LOGOUT_PAGE, method = RequestMethod.GET)
    public ModelAndView ssoLogoutPage() throws JsonProcessingException {
        final HodFindConfig hodFindConfig = (HodFindConfig) configService.getConfig();

        final Map<String, Object> ssoConfig = new HashMap<>();
        ssoConfig.put("endpoint", HOD_ENDPOINT);
        ssoConfig.put("redirectUrl", hodFindConfig.getHsod().getLandingPageUrl());

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("configJson", contextObjectMapper.writeValueAsString(ssoConfig));
        return new ModelAndView("sso-logout", attributes);
    }

}
