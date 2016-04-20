/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.test;

import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HsodConfig;
import com.hp.autonomy.frontend.find.hod.configuration.IodConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import com.hp.autonomy.searchcomponents.hod.test.HodTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
public class HodFindMockConfigConfiguration {
    @Bean
    @Primary
    public BaseConfigFileService<?> baseConfigFileService() throws MalformedURLException {
        @SuppressWarnings("unchecked") final BaseConfigFileService<HodFindConfig> baseConfigFileService = mock(BaseConfigFileService.class);

        final QueryManipulationConfig queryManipulationConfig = new QueryManipulationConfig(HodTestConfiguration.QUERY_PROFILE, HodTestConfiguration.QUERY_MANIPULATION_INDEX);

        final HsodConfig hsodConfig = new HsodConfig.Builder()
                .setLandingPageUrl(new URL("https://search.havenondemand.com"))
                .setFindAppUrl(new URL("https://find.havenapps.io"))
                .build();

        final IodConfig iodConfig = new IodConfig.Builder()
                .setApiKey("")
                .setApplication("")
                .setDomain("")
                .setActiveIndexes(Collections.<ResourceIdentifier>emptyList())
                .setPublicIndexesEnabled(true)
                .build();

        final HodFindConfig config = new HodFindConfig.Builder()
                .setQueryManipulation(queryManipulationConfig)
                .setHsod(hsodConfig)
                .setIod(iodConfig)
                .build();
        when(baseConfigFileService.getConfig()).thenReturn(config);

        return baseConfigFileService;
    }
}
