/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.test;

import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.configuration.CommunityAuthentication;
import com.hp.autonomy.frontend.find.core.configuration.MapConfiguration;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ConditionalOnProperty(value = "mock.configuration", matchIfMissing = true)
public class IdolFindMockConfigConfiguration {
    @Bean
    @Primary
    public BaseConfigFileService<?> configService() {
        final CommunityAuthentication loginConfig = new CommunityAuthentication.Builder()
                .setMethod("autonomy")
                .build();

        final MMAP mmapConfig = new MMAP.Builder()
                .setBaseUrl("")
                .setEnabled(false)
                .build();

        // The rest of the fields are mocked in the haven-search-components IdolTestConfiguration class
        final IdolFindConfig config = new IdolFindConfig.Builder()
                .setLogin(loginConfig)
                .setMap(new MapConfiguration("", false, "", null, 2, null))
                .setMmap(mmapConfig)
                .build();

        @SuppressWarnings("unchecked")
        final BaseConfigFileService<IdolFindConfig> configService = mock(BaseConfigFileService.class);

        when(configService.getConfig()).thenReturn(config);
        return configService;
    }
}
