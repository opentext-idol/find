/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.applications;

import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import org.springframework.stereotype.Service;

@Service
public class IdolCustomApplicationsConfigService extends CustomizationConfigService<IdolCustomApplicationsConfig> {
    public IdolCustomApplicationsConfigService() {
        super(
                "applications.json",
                "defaultApplicationsConfigFile.json",
                IdolCustomApplicationsConfig.class,
                IdolCustomApplicationsConfig.builder().build()
        );
    }
}
