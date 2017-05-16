/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.applications;

import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import com.hp.autonomy.frontend.find.core.customization.ReloadableCustomizationComponent;
import org.springframework.stereotype.Service;

@Service
public class CustomApplicationsConfigService extends CustomizationConfigService<CustomApplicationsConfig> implements ReloadableCustomizationComponent {
    public CustomApplicationsConfigService() {
        super(
            "applications.json",
            "defaultApplicationsConfigFile.json",
            CustomApplicationsConfig.class,
            CustomApplicationsConfig.builder().build()
        );
    }

    @Override
    public void reload() throws Exception {
        init();
    }
}
