/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.configuration.TemplatesConfig;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.idol.applications.CustomApplication;
import com.hp.autonomy.frontend.find.idol.applications.CustomApplicationsConfig;
import com.hp.autonomy.frontend.find.idol.authentication.FindCommunityRole;
import com.hp.autonomy.frontend.find.idol.configuration.EntitySearchConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig.IdolFindConfigBuilder;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import com.hp.autonomy.frontend.find.idol.customization.AssetConfig;
import com.hp.autonomy.frontend.find.idol.dashboards.DashboardConfig;
import com.hp.autonomy.frontend.find.idol.export.service.IdolMetadataNode;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang.BooleanUtils.isTrue;

@Controller
public class IdolFindController extends FindController<IdolFindConfig, IdolFindConfigBuilder> {
    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;
    private final ConfigService<DashboardConfig> dashConfig;
    private final ConfigService<CustomApplicationsConfig> appsConfig;
    private final ConfigService<TemplatesConfig> templatesConfig;
    private final ConfigService<AssetConfig> assetsConfigService;

    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    protected IdolFindController(
            final ControllerUtils controllerUtils,
            final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
            final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
            final ConfigService<IdolFindConfig> configService,
            final ConfigService<DashboardConfig> dashConfig,
            final ConfigService<CustomApplicationsConfig> appsConfig,
            final FieldDisplayNameGenerator fieldDisplayNameGenerator,
            final ConfigService<TemplatesConfig> templatesConfig,
            final ConfigService<AssetConfig> assetsConfigService,
            final ConfigService<StyleConfiguration> styleSheetService
    ) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService, fieldDisplayNameGenerator, styleSheetService);
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.dashConfig = dashConfig;
        this.appsConfig = appsConfig;
        this.templatesConfig = templatesConfig;
        this.assetsConfigService = assetsConfigService;
    }

    @Override
    protected Map<String, Object> getPublicConfig() {
        final Map<String, Object> publicConfig = new HashMap<>();
        final IdolFindConfig config = configService.getConfig();
        final List<CustomApplication> enabledApps = appsConfig.getConfig()
                .getApplications()
                .stream()
                .filter(CustomApplication::getEnabled)
                .collect(Collectors.toList());

        final MMAP mmap = config.getMmap();

        if(isTrue(mmap.getEnabled())) {
            publicConfig.put(IdolMvcConstants.MMAP_BASE_URL.getName(), mmap.getBaseUrl());
        }
        final Set<String> roles = authenticationInformationRetriever.getPrincipal().getIdolRoles();

        publicConfig.put(IdolMvcConstants.TRENDING.getName(), config.getTrending());
        publicConfig.put(IdolMvcConstants.VIEW_HIGHLIGHTING.getName(), config.getViewConfig().getHighlighting());
        publicConfig.put(IdolMvcConstants.DASHBOARDS.getName(), dashConfig.getConfig().getDashboards().stream()
                .filter(dashboard ->
                        roles.contains(FindCommunityRole.ADMIN.value()) ||
                        dashboard.getRoles() == null || dashboard.getRoles().isEmpty() ||
                        dashboard.getRoles().stream().anyMatch(roles::contains))
                .collect(Collectors.toList()));
        publicConfig.put(IdolMvcConstants.APPLICATIONS.getName(), enabledApps);
        publicConfig.put(IdolMvcConstants.REFERENCE_FIELD.getName(), config.getReferenceField());

        final Boolean answerServerEnabled = config.getAnswerServer().getEnabled();
        publicConfig.put(MvcConstants.ANSWER_SERVER_ENABLED.value(), answerServerEnabled);
        publicConfig.put(MvcConstants.CONVERSATION_ENABLED.value(), Boolean.TRUE.equals(answerServerEnabled)
                && StringUtils.isNotEmpty(config.getAnswerServer().getConversationSystemName()));

        final EntitySearchConfig entitySearch = config.getEntitySearch();
        final Boolean entitySearchEnabled = entitySearch.getEnabled();
        publicConfig.put(MvcConstants.ENTITY_SEARCH_ENABLED.value(), entitySearchEnabled);
        publicConfig.put(MvcConstants.ENTITY_SEARCH_ANSWER_SERVER_ENABLED.value(),
            isTrue(entitySearchEnabled) && isTrue(entitySearch.getAnswerServer().getEnabled())
        );
        publicConfig.put(MvcConstants.ENTITY_SEARCH_OPTIONS.value(),
            isTrue(entitySearchEnabled) && isTrue(entitySearch.getDatabaseChoicesVisible()) ? entitySearch.getDatabaseChoices() : null
        );

        publicConfig.put(MvcConstants.TEMPLATES_CONFIG.value(), templatesConfig.getConfig());
        publicConfig.put(MvcConstants.ASSETS_CONFIG.value(), assetsConfigService.getConfig());
        publicConfig.put(MvcConstants.MESSAGE_OF_THE_DAY_CONFIG.value(), config.getMessageOfTheDay());

        return publicConfig;
    }

    @Override
    protected List<MetadataNode> getMetadataNodes() {
        return Arrays.asList(IdolMetadataNode.values());
    }

    private enum IdolMvcConstants {
        MMAP_BASE_URL("mmapBaseUrl"),
        VIEW_HIGHLIGHTING("viewHighlighting"),
        DASHBOARDS("dashboards"),
        APPLICATIONS("applications"),
        TRENDING("trending"),
        REFERENCE_FIELD("referenceField");

        @Getter
        private final String name;

        IdolMvcConstants(final String name) {
            this.name = name;
        }
    }
}
