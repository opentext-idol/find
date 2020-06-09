/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.hod.test;

import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.find.hod.configuration.HodConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HsodConfig;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import com.hp.autonomy.searchcomponents.hod.test.HodTestConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
@ConditionalOnProperty(value = "mock.configuration", matchIfMissing = true)
public class HodFindMockConfigConfiguration {
    @Bean
    @Primary
    public BaseConfigFileService<?> baseConfigFileService(
            @Value("${test.hod.endpoint:https://api.havenondemand.com}") final URL endpoint
    ) throws MalformedURLException {
        @SuppressWarnings("unchecked") final BaseConfigFileService<HodFindConfig> baseConfigFileService = mock(BaseConfigFileService.class);

        final QueryManipulationConfig queryManipulationConfig = QueryManipulationConfig.builder()
                .profile(HodTestConfiguration.QUERY_PROFILE)
                .index(HodTestConfiguration.QUERY_MANIPULATION_INDEX)
                .build();

        final HsodConfig hsodConfig = HsodConfig.builder()
                .landingPageUrl(new URL("https://search.havenondemand.com"))
                .build();

        final HodConfig hodConfig = HodConfig.builder()
                .apiKey(new ApiKey("mock-api-key"))
                .publicIndexesEnabled(true)
                .ssoPageGetUrl(new URL("https://dev.havenondemand.com/sso.html"))
                .endpointUrl(endpoint)
                .build();

        final HodFindConfig config = HodFindConfig.builder()
                .queryManipulation(queryManipulationConfig)
                .hsod(hsodConfig)
                .hod(hodConfig)
                .fieldsInfo(FieldsInfo.builder().build())
                .build();

        when(baseConfigFileService.getConfig()).thenReturn(config);
        return baseConfigFileService;
    }
}
