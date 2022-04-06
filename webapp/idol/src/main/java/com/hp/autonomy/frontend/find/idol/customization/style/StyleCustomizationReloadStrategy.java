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

package com.hp.autonomy.frontend.find.idol.customization.style;

import com.hp.autonomy.frontend.find.core.beanconfiguration.AppConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import com.hp.autonomy.frontend.find.core.customization.ReloadableCustomizationComponent;
import com.hp.autonomy.frontend.find.core.customization.style.StyleSheetService;
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
