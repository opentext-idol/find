/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig.IdolFindConfigBuilder;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import com.hp.autonomy.frontend.find.idol.export.IdolMetadataNode;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IdolFindController extends FindController<IdolFindConfig, IdolFindConfigBuilder> {
    private static final String MMAP_BASE_URL = "mmapBaseUrl";
    private static final String VIEW_HIGHLIGHTING = "viewHighlighting";

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    protected IdolFindController(final ControllerUtils controllerUtils,
                                 final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                                 final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                                 final ConfigService<IdolFindConfig> configService) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService);
    }

    @Override
    protected Map<String, Object> getPublicConfig() {
        final Map<String, Object> publicConfig = new HashMap<>();
        final IdolFindConfig config = configService.getConfig();

        final MMAP mmap = config.getMmap();

        if(BooleanUtils.isTrue(mmap.getEnabled())) {
            publicConfig.put(MMAP_BASE_URL, mmap.getBaseUrl());
        }

        publicConfig.put(VIEW_HIGHLIGHTING, config.getViewConfig().getHighlighting());
        publicConfig.put(MvcConstants.ANSWER_SERVER_ENABLED.value(), config.getAnswerServer().getEnabled());

        return publicConfig;
    }

    @Override
    protected List<MetadataNode> getMetadataNodes() {
        return Arrays.asList(IdolMetadataNode.values());
    }
}
