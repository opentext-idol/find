/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.core.web.ViewNames;
import com.hp.autonomy.frontend.find.hod.authentication.HodCombinedRequestController;
import com.hp.autonomy.frontend.find.hod.beanconfiguration.HodConfiguration;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthenticationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class SsoController {

    public static final String SSO_PAGE = "/sso";
    public static final String SSO_AUTHENTICATION_URI = "/authenticate-sso";
    public static final String SSO_LOGOUT_PAGE = "/sso-logout";

    @Autowired
    private HodAuthenticationRequestService hodAuthenticationRequestService;

    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Autowired
    private ControllerUtils controllerUtils;

    @Value(AppConfiguration.GIT_COMMIT_PROPERTY)
    private String gitCommit;

    @Value(HodConfiguration.SSO_PAGE_PROPERTY)
    private String ssoPage;

    @Value(HodConfiguration.HOD_API_URL_PROPERTY)
    private String logoutEndpoint;

    @RequestMapping(value = SSO_PAGE, method = RequestMethod.GET)
    public ModelAndView sso(final ServletRequest request) throws JsonProcessingException, HodErrorException {
        final Map<String, Object> ssoConfig = new HashMap<>();
        ssoConfig.put(SsoMvcConstants.AUTHENTICATE_PATH.value(), SSO_AUTHENTICATION_URI);
        ssoConfig.put(SsoMvcConstants.COMBINED_REQUEST_API.value(), HodCombinedRequestController.COMBINED_REQUEST);
        ssoConfig.put(SsoMvcConstants.ERROR_PAGE.value(), DispatcherServletConfiguration.CLIENT_AUTHENTICATION_ERROR_PATH);
        ssoConfig.put(SsoMvcConstants.LIST_APPLICATION_REQUEST.value(), hodAuthenticationRequestService.getListApplicationRequest());
        ssoConfig.put(SsoMvcConstants.LIST_APPLICATION_REQUEST_API.value(), HodCombinedRequestController.LIST_APPLICATION_REQUEST);
        ssoConfig.put(SsoMvcConstants.SSO_PAGE.value(), ssoPage);
        ssoConfig.put(SsoMvcConstants.SSO_ENTRY_PAGE.value(), SSO_PAGE);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        attributes.put(MvcConstants.CONFIG.value(), controllerUtils.convertToJson(ssoConfig));
        attributes.put(ControllerUtils.SPRING_CSRF_ATTRIBUTE, request.getAttribute(ControllerUtils.SPRING_CSRF_ATTRIBUTE));

        return new ModelAndView(ViewNames.SSO.viewName(), attributes);
    }

    @RequestMapping(value = SSO_LOGOUT_PAGE, method = RequestMethod.GET)
    public ModelAndView ssoLogoutPage(final ServletRequest request) throws JsonProcessingException {
        final HodFindConfig hodFindConfig = configService.getConfig();

        final Map<String, Object> ssoConfig = new HashMap<>();
        ssoConfig.put(SsoMvcConstants.LOGOUT_ENDPOINT.value(), logoutEndpoint);
        ssoConfig.put(SsoMvcConstants.LOGOUT_REDIRECT_URL.value(), hodFindConfig.getHsod().getExternalUrl() + FindController.PUBLIC_PATH);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        attributes.put(MvcConstants.CONFIG.value(), controllerUtils.convertToJson(ssoConfig));
        attributes.put(ControllerUtils.SPRING_CSRF_ATTRIBUTE, request.getAttribute(ControllerUtils.SPRING_CSRF_ATTRIBUTE));
        return new ModelAndView(ViewNames.SSO_LOGOUT.viewName(), attributes);
    }
}
