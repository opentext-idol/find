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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Controller
public class IdolDashboardController {
    static final String DASHBOARD_CONFIG_RELOAD_PATH = "/api/admin/dashboards/reload";
    private static final String DASHBOARD_KEEP_ALIVE = "/api/bi/dashboards/keep-alive";
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
    @SuppressWarnings("ProhibitedExceptionDeclared")
    @RequestMapping(method = RequestMethod.GET, value = DASHBOARD_CONFIG_RELOAD_PATH)
    public void reloadConfig(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        log.info(Markers.AUDIT, "Reloading dashboards configuration file {}",
                dashConfig.getConfigResponse().getConfigPath());

        // Pull in config changes
        dashConfig.init();

        final String newUrl = Optional
                // "referer" will be null if the config was reloaded by typing the endpoint URL
                // into the browser, rather than by clicking the reload button
                .ofNullable(request.getHeader(HttpHeaders.REFERER))
                // This is to handle the case if the user uploads a config in which the dashboard
                // they are currently on has been renamed or deleted, and cannot be redirected to
                .filter(url -> {
                    final Optional<String> decodedName = getDecodedDashboardNameFromUrl(url);
                    return !decodedName.isPresent() || dashboardExists(decodedName.get());
                })
                .orElse("/");

        response.sendRedirect(newUrl);
    }

    // Endpoint for fullscreen dashboards to poll when they want to extend the session [FIND-983].
    @RequestMapping(method = RequestMethod.POST, value = DASHBOARD_KEEP_ALIVE)
    @ResponseBody
    public int keepAlive(final HttpServletRequest request, final HttpSession session) {
        final String logMessage = Optional
                // "referer" will be null if the config was reloaded by typing the endpoint URL
                // into the browser, rather than by clicking the reload button
                .ofNullable(request.getHeader(HttpHeaders.REFERER))
                .flatMap(this::getDecodedDashboardNameFromUrl)
                .map(dashboardName -> "Session extended by fullscreen dashboard " + dashboardName)
                .orElse("Session extended by fullscreen dashboard.");

        log.info(logMessage);

        return session.getMaxInactiveInterval();
    }

    private Optional<String> getDecodedDashboardNameFromUrl(final String url) {
        final String[] split = url.split("/public/dashboards/");
        if (split.length == 1) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(URLDecoder.decode(split[split.length - 1], StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("UTF-8 should be supported on all JVMs", e);
            }
        }
    }

    private boolean dashboardExists(final String decodedName) {
        return dashConfig.getConfigResponse().getConfig().getDashboards()
                .stream()
                .map(Dashboard::getDashboardName)
                .anyMatch(name -> name.equals(decodedName));
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
