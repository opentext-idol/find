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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    // Reinitialise dashboards config and attempt to redirect user to where they were according to the
    // request's "referer" header, or defaults to application root.
    // Detects case in which user renamed or deleted current dashboard, and redirects to app root.
    @RequestMapping(method = RequestMethod.GET, value = DASHBOARD_CONFIG_RELOAD_PATH)
    public void reloadConfig(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        log.info(Markers.AUDIT, "Reloading dashboards configuration file {}",
                 dashConfig.getConfigResponse().getConfigPath());

        // Pull in config changes
        dashConfig.init();

        // Get list of dashboard names.
        final Set<String> newDashboardsNames = dashConfig.getConfigResponse().getConfig().getDashboards()
                .stream()
                .map(Dashboard::getDashboardName)
                .collect(Collectors.toSet());

        final String newUrl = Optional
                // "referer" will be null if the config was reloaded by typing the endpoint URL
                // into the browser, rather than by clicking the reload button
                .ofNullable(request.getHeader(HttpHeaders.REFERER))
                // This is to handle the case if the user uploads a config in which the dashboard
                // they are currently on has been renamed or deleted, and cannot be redirected to
                .filter(s -> {
                    final String[] split = s.split("/public/dashboards/");
                    boolean dashboardExists;
                    try {
                        final String decodedName = URLDecoder.decode(split[split.length - 1], StandardCharsets.UTF_8.name());
                        dashboardExists = newDashboardsNames.contains(decodedName);
                    } catch(UnsupportedEncodingException e) {
                        throw new IllegalStateException("UTF-8 should be supported on all JVMs", e);
                    }

                    return split.length == 1 || dashboardExists;
                })
                .orElse("/");

        response.sendRedirect(newUrl);
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
