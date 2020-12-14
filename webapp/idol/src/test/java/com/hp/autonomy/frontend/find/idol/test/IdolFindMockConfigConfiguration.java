/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.test;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.BaseConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.core.configuration.MapConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.UsersConfig;
import com.hp.autonomy.frontend.find.idol.configuration.EntitySearchConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.MMAP;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import db.migration.AbstractMigrateUsersToIncludeUsernames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ConditionalOnProperty(value = "mock.configuration", matchIfMissing = true)
public class IdolFindMockConfigConfiguration {
    private static final String COMMUNITY_HOST_PROPERTY = "test.community.host";
    private static final String COMMUNITY_HOST = "ida-backend";
    private static final String COMMUNITY_PORT_PROPERTY = "test.community.port";
    private static final int COMMUNITY_PORT = 9030;

    @Autowired
    private Environment environment;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Primary
    @Bean
    public IdolFindConfig config() {
        final CommunityAuthentication loginConfig = CommunityAuthentication.builder()
                .method("autonomy")
                .community(ServerConfig.builder()
                        .host(getProperty(COMMUNITY_HOST_PROPERTY, COMMUNITY_HOST))
                        .port(getIntProperty(COMMUNITY_PORT_PROPERTY, COMMUNITY_PORT))
                        .build())
                .build();

        final MMAP mmapConfig = new MMAP.Builder()
                .setBaseUrl("")
                .setEnabled(false)
                .build();

        final IdolFindConfig config = mock(IdolFindConfig.class);
        // The rest of the fields are mocked in the haven-search-components IdolTestConfiguration class
        when(config.getLogin()).thenReturn(loginConfig);
        when(config.getAuthentication()).thenReturn((Authentication) loginConfig);
        when(config.getCommunityDetails()).thenReturn(new AciServerDetails(
                getProperty(COMMUNITY_HOST_PROPERTY, COMMUNITY_HOST),
                getIntProperty(COMMUNITY_PORT_PROPERTY, COMMUNITY_PORT)
        ));
        when(config.getMap()).thenReturn(new MapConfiguration("", false, "", null, 2, null));
        when(config.getMmap()).thenReturn(mmapConfig);
        final EntitySearchConfig entitySearchConfig = mock(EntitySearchConfig.class);
        final AnswerServerConfig entitySearchAnswerServerConfig = mock(AnswerServerConfig.class);
        when(entitySearchConfig.getEnabled()).thenReturn(false);
        when(entitySearchConfig.getAnswerServer()).thenReturn(entitySearchAnswerServerConfig);
        when(config.getEntitySearch()).thenReturn(entitySearchConfig);
        when(config.getUsers()).thenReturn(UsersConfig.builder().build());
        return config;
    }

    @SuppressWarnings({"CastToConcreteClass", "unchecked"})
    @Primary
    @Bean
    public BaseConfigFileService<IdolFindConfig> configService(
            @Qualifier("testConfigService") final ConfigService<IdolSearchCapable> testConfigService) {
        final BaseConfigFileService<IdolFindConfig> configService = mock(BaseConfigFileService.class);
        final IdolFindConfig config = (IdolFindConfig) testConfigService.getConfig();
        when(configService.getConfig()).thenReturn(config);
        return configService;
    }

    @Bean
    @Primary
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // terrible hack - using system properties to pass data to migration
            System.setProperty(AbstractMigrateUsersToIncludeUsernames.COMMUNITY_PROTOCOL, "HTTP");
            System.setProperty(AbstractMigrateUsersToIncludeUsernames.COMMUNITY_HOST, getProperty(COMMUNITY_HOST_PROPERTY, COMMUNITY_HOST));
            System.setProperty(AbstractMigrateUsersToIncludeUsernames.COMMUNITY_PORT, getProperty(COMMUNITY_PORT_PROPERTY, String.valueOf(COMMUNITY_PORT)));

            flyway.migrate();

            System.clearProperty(AbstractMigrateUsersToIncludeUsernames.COMMUNITY_PROTOCOL);
            System.clearProperty(AbstractMigrateUsersToIncludeUsernames.COMMUNITY_HOST);
            System.clearProperty(AbstractMigrateUsersToIncludeUsernames.COMMUNITY_PORT);
        };
    }

    private String getProperty(final String property, final String defaultValue) {
        return environment.getProperty(property, defaultValue);
    }

    private int getIntProperty(final String property, final int defaultValue) {
        return Integer.parseInt(environment.getProperty(property, String.valueOf(defaultValue)));
    }
}
