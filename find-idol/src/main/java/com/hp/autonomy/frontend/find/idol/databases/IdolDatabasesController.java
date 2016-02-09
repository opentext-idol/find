/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.databases;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.databases.DatabasesController;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequest;
import com.hp.autonomy.types.idol.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class IdolDatabasesController extends DatabasesController<Database, IdolDatabasesRequest, AciErrorException> {
    @Autowired
    public IdolDatabasesController(final DatabasesService<Database, IdolDatabasesRequest, AciErrorException> databasesService) {
        super(databasesService);
    }

    @Override
    protected IdolDatabasesRequest buildDatabasesRequest() {
        return new IdolDatabasesRequest();
    }
}
