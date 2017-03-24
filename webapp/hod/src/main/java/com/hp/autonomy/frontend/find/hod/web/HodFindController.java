/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig.HodFindConfigBuilder;
import com.hp.autonomy.frontend.find.hod.export.service.HodMetadataNode;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class HodFindController extends FindController<HodFindConfig, HodFindConfigBuilder> {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public HodFindController(final ControllerUtils controllerUtils,
                             final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                             final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                             final ConfigService<HodFindConfig> configService) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService);
    }

    @Override
    protected Map<String, Object> getPublicConfig() {
        final HodFindConfig config = configService.getConfig();

        return Collections.singletonMap(MvcConstants.PUBLIC_INDEXES_ENABLED.value(), config.getHod().getPublicIndexesEnabled());
    }

    @Override
    protected List<MetadataNode> getMetadataNodes() {
        return Arrays.asList(HodMetadataNode.values());
    }
}
