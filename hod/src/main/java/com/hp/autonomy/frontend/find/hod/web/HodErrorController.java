/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.CustomErrorController;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * Hosted error page handling
 */
@Controller
public class HodErrorController extends CustomErrorController {
    static final String STATUS_CODE_PARAM = "statusCode";
    static final String MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_MAIN = "error.clientAuthenticationErrorMain";
    static final String MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_SUB = "error.clientAuthenticationErrorSub";

    private final ConfigService<HodFindConfig> configService;

    @Autowired
    public HodErrorController(final ControllerUtils controllerUtils, final ConfigService<HodFindConfig> configService) {
        super(controllerUtils);
        this.configService = configService;
    }

    @Override
    protected URI getAuthenticationErrorUrl(final HttpServletRequest request) {
        return URI.create(configService.getConfig().getHsod().getLandingPageUrl().toString());
    }

    @Override
    protected URI getErrorUrl(final HttpServletRequest request) {
        return URI.create(configService.getConfig().getHsod().getFindAppUrl().toString());
    }

    @RequestMapping(DispatcherServletConfiguration.CLIENT_AUTHENTICATION_ERROR_PATH)
    public ModelAndView clientAuthenticationErrorPage(
            @RequestParam(STATUS_CODE_PARAM) final int statusCode,
            final HttpServletRequest request
    ) {
        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_MAIN)
                .setSubMessageCode(MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_SUB)
                .setStatusCode(statusCode)
                .setButtonHref(getAuthenticationErrorUrl(request))
                .build());
    }
}
