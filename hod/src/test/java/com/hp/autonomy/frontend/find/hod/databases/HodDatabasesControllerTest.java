/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.databases.AbstractDatabasesControllerTest;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.IodConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodDatabasesControllerTest extends AbstractDatabasesControllerTest<Database, HodDatabasesRequest, HodErrorException> {
    @Mock
    private ConfigService<HodFindConfig> configService;

    @Before
    public void setUp() {
        final IodConfig iodConfig = new IodConfig.Builder().setPublicIndexesEnabled(true).build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setIod(iodConfig).build());

        databasesController = new HodDatabasesController(databasesService, configService);
    }
}
