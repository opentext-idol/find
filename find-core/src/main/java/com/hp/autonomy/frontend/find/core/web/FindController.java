/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public static final String PUBLIC_PATH = "/public/";
    public static final String LOGIN_PATH = "/login";
    private static final String DEFAULT_LOGIN_PAGE = "/loginPage";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService;

    @Value(AppConfiguration.APPLICATION_VERSION_PROPERTY)
    private String applicationVersion;

    @Autowired
    private ControllerUtils controllerUtils;

    @RequestMapping("/")
    public void index(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String contextPath = request.getContextPath();

        if (LoginTypes.DEFAULT.equals(authenticationConfigService.getConfig().getAuthentication().getMethod())) {
            response.sendRedirect(contextPath + DEFAULT_LOGIN_PAGE);
        } else {
            response.sendRedirect(contextPath + PUBLIC_PATH);
        }
    }

    @RequestMapping(value = PUBLIC_PATH, method = RequestMethod.GET)
    public ModelAndView mainPage() throws JsonProcessingException {
        final String username = SecurityContextHolder.getContext().getAuthentication().getName();
        final Map<String, Object> config = new HashMap<>();
        config.put(MvcConstants.USERNAME.value(), username);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.APPLICATION_VERSION.value(), applicationVersion);
        attributes.put(MvcConstants.CONFIG.value(), controllerUtils.convertToJson(config));

        return new ModelAndView(ViewNames.PUBLIC.value(), attributes);
    }

    @RequestMapping(value = LOGIN_PATH, method = RequestMethod.GET)
    public ModelAndView login() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.APPLICATION_VERSION.value(), applicationVersion);
        return new ModelAndView(ViewNames.LOGIN.value(), attributes);
    }
}
