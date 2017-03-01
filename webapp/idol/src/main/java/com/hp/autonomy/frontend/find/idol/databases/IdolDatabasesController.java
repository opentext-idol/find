/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.databases;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.databases.DatabasesController;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequest;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesService;
import com.hp.autonomy.types.idol.responses.Database;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
class IdolDatabasesController extends DatabasesController<Database, IdolDatabasesRequest, AciErrorException> {
    private final ObjectFactory<IdolDatabasesRequestBuilder> databasesRequestBuilderFactory;

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolDatabasesController(final IdolDatabasesService databasesService,
                                   final ObjectFactory<IdolDatabasesRequestBuilder> databasesRequestBuilderFactory) {
        super(databasesService);
        this.databasesRequestBuilderFactory = databasesRequestBuilderFactory;
    }

    @Override
    protected IdolDatabasesRequest buildDatabasesRequest() {
        return databasesRequestBuilderFactory.getObject().build();
    }
}
