/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.CustomErrorController;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * Hosted error page handling
 */
@Controller
public class HodErrorController extends CustomErrorController {
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
}
