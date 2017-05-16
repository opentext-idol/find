/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration.style;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.style.StyleConfiguration;
import org.springframework.stereotype.Service;

@Service
public class HodStyleConfigurationService implements ConfigService<StyleConfiguration> {
    @Override
    public StyleConfiguration getConfig() {
        return StyleConfiguration.builder().build();
    }
}
