/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration.style;

import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import org.springframework.stereotype.Service;

@Service
public class IdolStyleConfigurationService extends CustomizationConfigService<StyleConfiguration> {
    public IdolStyleConfigurationService() {
        super(
            "style.json",
            "defaultStyleConfigFile.json",
            StyleConfiguration.class,
            StyleConfiguration.builder().build()
        );
    }
}
