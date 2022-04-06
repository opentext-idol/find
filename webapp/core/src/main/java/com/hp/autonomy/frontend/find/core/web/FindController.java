/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigBuilder;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
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
    private final FieldDisplayNameGenerator fieldDisplayNameGenerator;
    private final ConfigService<StyleConfiguration> styleConfigService;

    @Value(AppConfiguration.GIT_COMMIT_PROPERTY)
    private String gitCommit;

    @Value(AppConfiguration.APPLICATION_RELEASE_VERSION_PROPERTY)
    private String releaseVersion;

    @Value(FIND_METRICS_ENABLED_PROPERTY)
    private boolean metricsEnabled;

    @Value("${find.community.username.field:}")
    private String usernameField;

    protected FindController(final ControllerUtils controllerUtils,
                             final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                             final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                             final ConfigService<C> configService,
                             final FieldDisplayNameGenerator fieldDisplayNameGenerator,
                             final ConfigService<StyleConfiguration> styleConfigService) {
        this.controllerUtils = controllerUtils;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.authenticationConfigService = authenticationConfigService;
        this.configService = configService;
        this.fieldDisplayNameGenerator = fieldDisplayNameGenerator;
        this.styleConfigService = styleConfigService;
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
        final Authentication auth = authenticationInformationRetriever.getAuthentication();
        final String username = auth.getName();
        String userLabel = username;

        final Object principal = auth.getPrincipal();
        if (principal instanceof CommunityPrincipal) {
            final Map<String, String> fields = ((CommunityPrincipal) principal).getFields();
            if (fields != null) {
                final String givenName = fields.get(usernameField);
                if (StringUtils.isNotBlank(givenName)) {
                    userLabel = givenName;
                }
            }
        }

        final Collection<String> roles = auth
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toCollection(LinkedList::new));

        final FindConfig<C, B> findConfig = configService.getConfig();

        final Map<String, Object> config = new HashMap<>();
        config.put(MvcConstants.APPLICATION_PATH.value(), APP_PATH);
        config.put(MvcConstants.USERNAME.value(), username);
        config.put(MvcConstants.USERLABEL.value(), userLabel);
        config.put(MvcConstants.ROLES.value(), roles);
        config.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        config.put(MvcConstants.RELEASE_VERSION.value(), releaseVersion);
        config.put(MvcConstants.METRICS_ENABLED.value(), metricsEnabled);
        config.put(MvcConstants.SUNBURST.value(), findConfig.getSunburst());
        config.put(MvcConstants.MAP.value(), findConfig.getMap());
        config.put(MvcConstants.UI_CUSTOMIZATION.value(), findConfig.getUiCustomization());
        config.put(MvcConstants.SAVED_SEARCH_CONFIG.value(), findConfig.getSavedSearchConfig());
        config.put(MvcConstants.MIN_SCORE.value(), findConfig.getMinScore());
        config.put(MvcConstants.FIELDS_INFO.value(), getFieldConfigWithDisplayNames(findConfig));
        config.put(MvcConstants.TOPIC_MAP_MAX_RESULTS.value(), findConfig.getTopicMapMaxResults());
        config.put(MvcConstants.METADATA_FIELD_INFO.value(), getMetadataNodeInfo());
        config.put(MvcConstants.SEARCH_CONFIG.value(), findConfig.getSearch());
        config.put(MvcConstants.RELATED_USERS_CONFIG.value(), findConfig.getUsers().getRelatedUsers());

        final StyleConfiguration styleConfig = styleConfigService.getConfig();
        config.put(MvcConstants.TERM_HIGHLIGHT_COLOR.value(), styleConfig.getTermHighlightColor());
        config.put(MvcConstants.TERM_HIGHLIGHT_BACKGROUND.value(), styleConfig.getTermHighlightBackground());

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
    public ModelAndView config(final HttpServletRequest request) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(MvcConstants.GIT_COMMIT.value(), gitCommit);
        attributes.put(MvcConstants.BASE_URL.value(), RequestUtils.buildBaseUrl(request));
        return new ModelAndView(ViewNames.CONFIG.viewName(), attributes);
    }

    private Map<String, FieldInfo<?>> getMetadataNodeInfo() {
        return getMetadataNodes().stream()
            .collect(toLinkedMap(MetadataNode::getName, node -> FieldInfo.builder()
                .id(node.getName())
                .displayName(node.getDisplayName())
                .type(node.getFieldType())
                .build()));
    }

    private Map<String, FieldInfo<?>> getFieldConfigWithDisplayNames(final FindConfig<C, B> findConfig) {
        return findConfig.getFieldsInfo().getFieldConfig().entrySet().stream()
            .collect(toLinkedMap(Map.Entry::getKey, entry -> entry.getValue().toBuilder()
                .displayName(Optional.ofNullable(entry.getValue().getDisplayName())
                                 .orElseGet(() -> fieldDisplayNameGenerator.prettifyFieldName(entry.getValue().getId())))
                .build()));
    }

    private <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
        final Function<? super T, ? extends K> keyMapper,
        final Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper,
                                (u, v) -> {
                                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                                },
                                LinkedHashMap::new);
    }
}
