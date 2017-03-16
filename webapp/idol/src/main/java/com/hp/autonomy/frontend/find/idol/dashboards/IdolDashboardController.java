/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo.Builder;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.logging.Markers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@Slf4j
@Controller
public class IdolDashboardController {
    static final String DASHBOARD_CONFIG_RELOAD_PATH = "/api/admin/dashboards/reload";
    private final ControllerUtils controllerUtils;
    private final IdolDashboardConfigService dashConfig;

    @Autowired
    public IdolDashboardController(final IdolDashboardConfigService dashConfig,
                                   final ControllerUtils controllerUtils) {
        this.dashConfig = dashConfig;
        this.controllerUtils = controllerUtils;
    }

    @RequestMapping(method = RequestMethod.GET, value = DASHBOARD_CONFIG_RELOAD_PATH)
    public void reloadConfig(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        log.info(Markers.AUDIT, "Reloading dashboards configuration file {}",
                 dashConfig.getConfigResponse().getConfigPath());
        dashConfig.init();
        response.sendRedirect(request.getHeader(HttpHeaders.REFERER));
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView errorHandler(
            final HttpServletRequest request,
            final Exception e
    ) {
        return controllerUtils.buildErrorModelAndView(
                new Builder()
                        .setRequest(request)
                        .setMainMessageCode(null)
                        .setSubMessageCode(null)
                        .setMainMessage(e.getMessage())
                        .setSubMessage(e.getCause().getMessage())
                        .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .setButtonHref(URI.create(FindController.APP_PATH))
                        .setAuthError(false)
                        .build()
        );
    }
}
