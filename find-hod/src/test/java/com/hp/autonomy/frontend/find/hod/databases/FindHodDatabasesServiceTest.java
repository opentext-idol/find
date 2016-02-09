/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.IodConfig;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindHodDatabasesServiceTest extends HodDatabasesServiceTest {
    @Mock
    private ConfigService<HodFindConfig> configService;

    private FindHodDatabasesService findDatabasesService;

    @Override
    @Before
    public void setUp() {
        findDatabasesService = new FindHodDatabasesServiceImpl(resourcesService, configService, authenticationInformationRetriever);
        databasesService = findDatabasesService;

        final IodConfig iodConfig = new IodConfig.Builder().build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setIod(iodConfig).build());
    }

    @Test
    public void getAllIndexesViaTokenProxy() throws HodErrorException {
        final Resources resources = findDatabasesService.getAllIndexes(new TokenProxy<>(mock(EntityType.class), TokenType.Simple.INSTANCE));
        assertThat(resources.getResources(), hasSize(1));
        assertThat(resources.getPublicResources(), hasSize(1));
    }

    @Test
    public void listActiveIndexes() throws HodErrorException {
        final ResourceIdentifier activeIndex = ResourceIdentifier.WIKI_ENG;
        final IodConfig iodConfig = new IodConfig.Builder().setActiveIndexes(Collections.singletonList(activeIndex)).build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setIod(iodConfig).build());
        final HodDatabasesRequest databasesRequest = new HodDatabasesRequest.Builder()
                .setPublicIndexesEnabled(true)
                .build();
        assertThat(databasesService.getDatabases(databasesRequest), hasSize(1));
    }
}
