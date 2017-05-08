/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.databases.DatabasesController;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesService;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
class HodDatabasesController extends DatabasesController<Database, HodDatabasesRequest, HodErrorException> {
    private final ObjectFactory<HodDatabasesRequestBuilder> databasesRequestBuilderFactory;
    private final ConfigService<HodFindConfig> configService;

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public HodDatabasesController(final HodDatabasesService databasesService,
                                  final ObjectFactory<HodDatabasesRequestBuilder> databasesRequestBuilderFactory,
                                  final ConfigService<HodFindConfig> configService) {
        super(databasesService);
        this.databasesRequestBuilderFactory = databasesRequestBuilderFactory;
        this.configService = configService;
    }

    @Override
    protected HodDatabasesRequest buildDatabasesRequest() {
        final Boolean publicIndexesEnabled = configService.getConfig().getHod().getPublicIndexesEnabled();

        return databasesRequestBuilderFactory.getObject()
            .publicIndexesEnabled(publicIndexesEnabled)
            .build();
    }
}
