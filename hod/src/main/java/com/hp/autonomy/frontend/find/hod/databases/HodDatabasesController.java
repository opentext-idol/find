/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.databases.DatabasesController;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class HodDatabasesController extends DatabasesController<Database, HodDatabasesRequest, HodErrorException> {
    private final ConfigService<HodFindConfig> configService;

    @Autowired
    public HodDatabasesController(final DatabasesService<Database, HodDatabasesRequest, HodErrorException> databasesService, final ConfigService<HodFindConfig> configService) {
        super(databasesService);
        this.configService = configService;
    }


    @Override
    protected HodDatabasesRequest buildDatabasesRequest() {
        final Boolean publicIndexesEnabled = configService.getConfig().getIod().getPublicIndexesEnabled();

        return new HodDatabasesRequest.Builder()
                .setPublicIndexesEnabled(publicIndexesEnabled)
                .build();
    }
}
