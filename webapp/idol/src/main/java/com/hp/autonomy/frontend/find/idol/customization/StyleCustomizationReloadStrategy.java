/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customization;

import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import com.hp.autonomy.frontend.find.core.customization.ReloadableCustomizationComponent;
import com.hp.autonomy.frontend.find.core.customization.StyleSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StyleCustomizationReloadStrategy implements ReloadableCustomizationComponent {
    private final CustomizationConfigService<StyleConfiguration> configService;
    private final StyleSheetService styleSheetService;

    @Value(AppConfiguration.GIT_COMMIT_PROPERTY)
    private String gitCommitHash;

    @Autowired
    public StyleCustomizationReloadStrategy(final CustomizationConfigService<StyleConfiguration> configService,
                                            final StyleSheetService styleSheetService) {
        this.configService = configService;
        this.styleSheetService = styleSheetService;
    }

    @Override
    public void reload() throws Exception {
        configService.init();
        styleSheetService.generateCss();
    }
}
