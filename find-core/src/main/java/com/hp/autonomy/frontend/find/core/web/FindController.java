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
import com.hp.autonomy.frontend.find.core.configuration.MapConfig;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public abstract class FindController {

    public static final String PUBLIC_PATH = "/public/";
    public static final String PRIVATE_PATH = "/private/";
    public static final String LOGIN_PATH = "/login";
    private static final String DEFAULT_LOGIN_PAGE = "/loginPage";
    private static final String CONFIG_PATH = "/config";

    private static final String PUBLIC_JS = "public.js";
    private static final String ADMIN_JS = "admin.js";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService;

    @Autowired
    private ConfigService<? extends MapConfig> mapConfigService;

    @Value(AppConfiguration.GIT_COMMIT_PROPERTY)
    private String gitCommit;

    @Value(AppConfiguration.APPLICATION_RELEASE_VERSION_PROPERTY)
    private String releaseVersion;

    @Autowired
    private ControllerUtils controllerUtils;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private AuthenticationInformationRetriever<? extends Principal> authenticationInformationRetriever;

    protected abstract Map<String, Object> getPublicConfig();

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
        return getPageModelAndView(PUBLIC_JS);
    }

    @RequestMapping(value = PRIVATE_PATH, method = RequestMethod.GET)
    public ModelAndView adminPage() throws JsonProcessingException {
        return getPageModelAndView(ADMIN_JS);
    }

    private ModelAndView getPageModelAndView(final String mainJs) throws JsonProcessingException {
        final String username = authenticationInformationRetriever.getAuthentication().getName();
        final Map<String, Object> config = new HashMap<>();
        config.put(MvcConstants.USERNAME.value(), username);
        config.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        config.put(MvcConstants.RELEASE_VERSION.value(), releaseVersion);
        config.put(MvcConstants.MAP.value(), mapConfigService.getConfig().getMap());
        config.putAll(getPublicConfig());

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        attributes.put(MvcConstants.CONFIG.value(), controllerUtils.convertToJson(config));
        attributes.put(MvcConstants.MAIN_JS.value(), mainJs);

        return new ModelAndView(ViewNames.APP.viewName(), attributes);
    }

    @RequestMapping(value = LOGIN_PATH, method = RequestMethod.GET)
    public ModelAndView login() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        return new ModelAndView(ViewNames.LOGIN.viewName(), attributes);
    }

    @RequestMapping(value = CONFIG_PATH, method = RequestMethod.GET)
    public ModelAndView config() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        return new ModelAndView(ViewNames.CONFIG.viewName(), attributes);
    }
}
