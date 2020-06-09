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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.core.web.ViewNames;
import com.hp.autonomy.frontend.find.hod.authentication.HodCombinedRequestController;
import com.hp.autonomy.frontend.find.hod.configuration.HodConfig;
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

    private final HodAuthenticationRequestService hodAuthenticationRequestService;
    private final HodErrorController hodErrorController;
    private final ConfigService<HodFindConfig> configService;
    private final ControllerUtils controllerUtils;
    private final String gitCommit;

    @Autowired
    public SsoController(
            final HodAuthenticationRequestService hodAuthenticationRequestService,
            final ConfigService<HodFindConfig> configService,
            final ControllerUtils controllerUtils,
            final HodErrorController hodErrorController,
            @Value(AppConfiguration.GIT_COMMIT_PROPERTY) final String gitCommit
    ) {
        this.hodAuthenticationRequestService = hodAuthenticationRequestService;
        this.configService = configService;
        this.controllerUtils = controllerUtils;
        this.hodErrorController = hodErrorController;
        this.gitCommit = gitCommit;
    }

    @RequestMapping(value = SSO_PAGE, method = RequestMethod.GET)
    public ModelAndView sso(final ServletRequest request) throws JsonProcessingException, HodErrorException {
        final HodConfig hodConfig = configService.getConfig().getHod();

        final Map<String, Object> ssoConfig = new HashMap<>();
        ssoConfig.put(SsoMvcConstants.AUTHENTICATE_PATH.value(), SSO_AUTHENTICATION_URI);
        ssoConfig.put(SsoMvcConstants.ERROR_PAGE.value(), DispatcherServletConfiguration.CLIENT_AUTHENTICATION_ERROR_PATH);
        ssoConfig.put(SsoMvcConstants.PATCH_REQUEST.value(), hodAuthenticationRequestService.getCombinedPatchRequest());
        ssoConfig.put(SsoMvcConstants.SSO_PATCH_TOKEN_API.value(), HodCombinedRequestController.COMBINED_PATCH_REQUEST_URL);
        ssoConfig.put(SsoMvcConstants.SSO_PAGE_GET_URL.value(), hodConfig.getSsoPageGetUrl());
        ssoConfig.put(SsoMvcConstants.SSO_PAGE_POST_URL.value(), hodConfig.getSsoPagePostUrl());
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
        ssoConfig.put(SsoMvcConstants.LOGOUT_ENDPOINT.value(), hodFindConfig.getHod().getEndpointUrl());
        ssoConfig.put(SsoMvcConstants.LOGOUT_REDIRECT_URL.value(), hodFindConfig.getHsod().getLandingPageUrl());

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        attributes.put(MvcConstants.CONFIG.value(), controllerUtils.convertToJson(ssoConfig));
        attributes.put(ControllerUtils.SPRING_CSRF_ATTRIBUTE, request.getAttribute(ControllerUtils.SPRING_CSRF_ATTRIBUTE));
        return new ModelAndView(ViewNames.SSO_LOGOUT.viewName(), attributes);
    }
}
