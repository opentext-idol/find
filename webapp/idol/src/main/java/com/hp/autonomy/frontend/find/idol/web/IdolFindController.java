/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.idol.applications.IdolCustomApplication;
import com.hp.autonomy.frontend.find.idol.applications.IdolCustomApplicationsConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig.IdolFindConfigBuilder;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import com.hp.autonomy.frontend.find.idol.dashboards.IdolDashboardConfig;
import com.hp.autonomy.frontend.find.idol.export.service.IdolMetadataNode;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import lombok.Getter;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class IdolFindController extends FindController<IdolFindConfig, IdolFindConfigBuilder> {
    private final ConfigService<IdolDashboardConfig> dashConfig;
    private final ConfigService<IdolCustomApplicationsConfig> appsConfig;

    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    protected IdolFindController(final ControllerUtils controllerUtils,
                                 final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                                 final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                                 final ConfigService<IdolFindConfig> configService,
                                 final ConfigService<IdolDashboardConfig> dashConfig,
                                 final ConfigService<IdolCustomApplicationsConfig> appsConfig,
                                 final FieldDisplayNameGenerator fieldDisplayNameGenerator) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService, fieldDisplayNameGenerator);
        this.dashConfig = dashConfig;
        this.appsConfig = appsConfig;
    }

    @Override
    protected Map<String, Object> getPublicConfig() {
        final Map<String, Object> publicConfig = new HashMap<>();
        final IdolFindConfig config = configService.getConfig();
        final List<IdolCustomApplication> enabledApps = appsConfig.getConfig()
                .getApplications()
                .stream()
                .filter(IdolCustomApplication::getEnabled)
                .collect(Collectors.toList());

        final MMAP mmap = config.getMmap();

        if(BooleanUtils.isTrue(mmap.getEnabled())) {
            publicConfig.put(IdolMvcConstants.MMAP_BASE_URL.getName(), mmap.getBaseUrl());
        }

        publicConfig.put(IdolMvcConstants.TRENDING.getName(), config.getTrending());
        publicConfig.put(IdolMvcConstants.VIEW_HIGHLIGHTING.getName(), config.getViewConfig().getHighlighting());
        publicConfig.put(IdolMvcConstants.DASHBOARDS.getName(), dashConfig.getConfig().getDashboards());
        publicConfig.put(IdolMvcConstants.APPLICATIONS.getName(), enabledApps);
        publicConfig.put(MvcConstants.ANSWER_SERVER_ENABLED.value(), config.getAnswerServer().getEnabled());

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
        TRENDING("trending");

        @Getter
        private final String name;

        IdolMvcConstants(final String name) {
            this.name = name;
        }
    }
}
