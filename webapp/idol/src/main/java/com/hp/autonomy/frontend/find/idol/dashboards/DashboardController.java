/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.dashboards;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Controller
public class DashboardController {
    private static final String DASHBOARD_KEEP_ALIVE = "/api/bi/dashboards/keep-alive";

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
        if(split.length == 1) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(URLDecoder.decode(split[split.length - 1], StandardCharsets.UTF_8.name()));
            } catch(UnsupportedEncodingException e) {
                throw new IllegalStateException("UTF-8 should be supported on all JVMs", e);
            }
        }
    }
}
