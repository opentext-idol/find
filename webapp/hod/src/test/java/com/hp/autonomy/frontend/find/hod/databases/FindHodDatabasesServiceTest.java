/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindHodDatabasesServiceTest {
    @Mock
    private HodDatabasesService databasesService;
    @Mock
    private HodDatabasesRequest databasesRequest;
    @Mock
    private ConfigService<HodFindConfig> configService;

    private HodDatabasesService findDatabasesService;

    @Before
    public void setUp() {
        findDatabasesService = new FindHodDatabasesServiceImpl(databasesService, configService);

        final HodConfig hodConfig = HodConfig.builder().build();
        when(configService.getConfig()).thenReturn(HodFindConfig.builder().hod(hodConfig).build());
    }

    @Test
    public void listActiveIndexes() throws HodErrorException {
        final ResourceName activeIndex = ResourceName.WIKI_ENG;
        final HodConfig hodConfig = HodConfig.builder()
                .activeIndex(activeIndex)
                .build();
        when(configService.getConfig()).thenReturn(HodFindConfig.builder().hod(hodConfig).build());
        when(databasesRequest.isPublicIndexesEnabled()).thenReturn(true);
        assertThat(findDatabasesService.getDatabases(databasesRequest), hasSize(1));
    }
}
