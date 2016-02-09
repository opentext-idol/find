/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.test;

import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.IodConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
public class HodFindMockConfigConfiguration {
    @Bean
    @Primary
    public BaseConfigFileService<?> baseConfigFileService() {
        @SuppressWarnings("unchecked") final BaseConfigFileService<HodFindConfig> baseConfigFileService = mock(BaseConfigFileService.class);
        final IodConfig iodConfig = new IodConfig.Builder().setApiKey("").setApplication("").setDomain("").setActiveIndexes(Collections.<ResourceIdentifier>emptyList()).setPublicIndexesEnabled(true).build();
        when(baseConfigFileService.getConfig()).thenReturn(new HodFindConfig.Builder().setIod(iodConfig).build());

        return baseConfigFileService;
    }
}
