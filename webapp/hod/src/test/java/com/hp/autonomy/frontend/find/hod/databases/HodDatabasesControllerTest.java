/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.databases.AbstractDatabasesControllerTest;
import com.hp.autonomy.frontend.find.hod.configuration.HodConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesService;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

public class HodDatabasesControllerTest extends AbstractDatabasesControllerTest<Database, HodDatabasesRequest, HodErrorException> {
    @Mock
    private HodDatabasesService hodDatabasesService;
    @Mock
    private ObjectFactory<HodDatabasesRequestBuilder> databasesRequestBuilderFactory;
    @Mock
    private HodDatabasesRequestBuilder databasesRequestBuilder;
    @Mock
    private ConfigService<HodFindConfig> configService;

    @Override
    protected DatabasesService<Database, HodDatabasesRequest, HodErrorException> constructDatabasesService() {
        return hodDatabasesService;
    }

    @Override
    protected HodDatabasesController constructDatabasesController() {
        when(databasesRequestBuilderFactory.getObject()).thenReturn(databasesRequestBuilder);
        when(databasesRequestBuilder.publicIndexesEnabled(anyBoolean())).thenReturn(databasesRequestBuilder);

        final HodConfig hodConfig = HodConfig.builder()
            .publicIndexesEnabled(true)
            .build();
        when(configService.getConfig()).thenReturn(HodFindConfig.builder().hod(hodConfig).build());

        return new HodDatabasesController(hodDatabasesService, databasesRequestBuilderFactory, configService);
    }
}
