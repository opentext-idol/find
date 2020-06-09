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
