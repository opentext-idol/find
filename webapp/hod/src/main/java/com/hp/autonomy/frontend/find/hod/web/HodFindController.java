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

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.find.core.configuration.TemplatesConfig;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.core.web.MvcConstants;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig.HodFindConfigBuilder;
import com.hp.autonomy.frontend.find.hod.export.service.HodMetadataNode;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class HodFindController extends FindController<HodFindConfig, HodFindConfigBuilder> {

    private final ConfigService<TemplatesConfig> templatesConfig;

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public HodFindController(final ControllerUtils controllerUtils,
                             final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                             final ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService,
                             final ConfigService<HodFindConfig> configService,
                             final ConfigService<TemplatesConfig> templatesConfig,
                             final FieldDisplayNameGenerator fieldDisplayNameGenerator,
                             final ConfigService<StyleConfiguration> styleSheetService) {
        super(controllerUtils, authenticationInformationRetriever, authenticationConfigService, configService, fieldDisplayNameGenerator, styleSheetService);
        this.templatesConfig = templatesConfig;
    }

    @Override
    protected Map<String, Object> getPublicConfig() {
        final HodFindConfig config = configService.getConfig();

        final Map<String, Object> publicConfig = new HashMap<>();
        publicConfig.put(MvcConstants.PUBLIC_INDEXES_ENABLED.value(), config.getHod().getPublicIndexesEnabled());
        publicConfig.put(MvcConstants.TEMPLATES_CONFIG.value(), templatesConfig.getConfig());

        return publicConfig;
    }

    @Override
    protected List<MetadataNode> getMetadataNodes() {
        return Arrays.asList(HodMetadataNode.values());
    }
}
