/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigBuilder;
import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.find.core.metrics.MetricsConfiguration.FIND_METRICS_ENABLED_PROPERTY;

public abstract class FindController<C extends FindConfig<C, B>, B extends FindConfigBuilder<C, B>> {
    public static final String APP_PATH = "/public";
    public static final String LOGIN_PATH = "/login";
    public static final String DEFAULT_LOGIN_PAGE = "/loginPage";
    public static final String CONFIG_PATH = "/config";
    protected final ConfigService<C> configService;
    private final ControllerUtils controllerUtils;
    private final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever;
    private final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService;
    @Value(AppConfiguration.GIT_COMMIT_PROPERTY)
    private String gitCommit;

    @Value(AppConfiguration.APPLICATION_RELEASE_VERSION_PROPERTY)
    private String releaseVersion;

    @Value(FIND_METRICS_ENABLED_PROPERTY)
    private boolean metricsEnabled;

    protected FindController(final ControllerUtils controllerUtils,
                             final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                             final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                             final ConfigService<C> configService) {
        this.controllerUtils = controllerUtils;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.authenticationConfigService = authenticationConfigService;
        this.configService = configService;
    }

    protected abstract Map<String, Object> getPublicConfig();

    protected abstract List<MetadataNode> getMetadataNodes();

    @RequestMapping("/")
    public void index(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String contextPath = request.getContextPath();

        if(LoginTypes.DEFAULT.equals(authenticationConfigService.getConfig().getAuthentication().getMethod())) {
            response.sendRedirect(contextPath + DEFAULT_LOGIN_PAGE);
        } else {
            response.sendRedirect(contextPath + APP_PATH);
        }
    }

    @RequestMapping(value = APP_PATH + "/**", method = RequestMethod.GET)
    public ModelAndView mainPage(final HttpServletRequest request) throws JsonProcessingException {
        final String username = authenticationInformationRetriever.getAuthentication().getName();

        final Collection<String> roles = authenticationInformationRetriever.getAuthentication()
                .getAuthorities()
                .stream()
                .map((Function<GrantedAuthority, String>)GrantedAuthority::getAuthority)
                .collect(Collectors.toCollection(LinkedList::new));

        final FindConfig<C, B> findConfig = configService.getConfig();

        final Map<String, Object> config = new HashMap<>();
        config.put(MvcConstants.APPLICATION_PATH.value(), APP_PATH);
        config.put(MvcConstants.USERNAME.value(), username);
        config.put(MvcConstants.ROLES.value(), roles);
        config.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        config.put(MvcConstants.RELEASE_VERSION.value(), releaseVersion);
        config.put(MvcConstants.METRICS_ENABLED.value(), metricsEnabled);
        config.put(MvcConstants.MAP.value(), findConfig.getMap());
        config.put(MvcConstants.UI_CUSTOMIZATION.value(), findConfig.getUiCustomization());
        config.put(MvcConstants.SAVED_SEARCH_CONFIG.value(), findConfig.getSavedSearchConfig());
        config.put(MvcConstants.MIN_SCORE.value(), findConfig.getMinScore());
        config.put(MvcConstants.FIELDS_INFO.value(), findConfig.getFieldsInfo().getFieldConfig());
        config.put(MvcConstants.TOPIC_MAP_MAX_RESULTS.value(), findConfig.getTopicMapMaxResults());
        config.put(MvcConstants.METADATA_FIELD_IDS.value(), getMetadataNodes());
        config.putAll(getPublicConfig());

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        attributes.put(MvcConstants.CONFIG.value(), controllerUtils.convertToJson(config));
        attributes.put(MvcConstants.BASE_URL.value(), RequestUtils.buildBaseUrl(request));

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
