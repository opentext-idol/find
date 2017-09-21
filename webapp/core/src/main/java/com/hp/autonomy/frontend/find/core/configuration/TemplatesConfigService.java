/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import org.springframework.stereotype.Service;

@Service
public class TemplatesConfigService extends CustomizationConfigService<TemplatesConfig> {
    public TemplatesConfigService() {
        super(
            "templates.json",
            "defaultTemplatesConfigFile.json",
            TemplatesConfig.class,
            TemplatesConfig.builder().build()
        );
    }
}
