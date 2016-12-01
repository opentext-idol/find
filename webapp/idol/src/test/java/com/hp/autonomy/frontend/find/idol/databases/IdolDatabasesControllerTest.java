/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.databases;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.databases.AbstractDatabasesControllerTest;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequest;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesService;
import com.hp.autonomy.types.idol.responses.Database;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Mockito.when;

public class IdolDatabasesControllerTest extends AbstractDatabasesControllerTest<Database, IdolDatabasesRequest, AciErrorException> {
    @Mock
    private IdolDatabasesService idolDatabasesService;
    @Mock
    private ObjectFactory<IdolDatabasesRequestBuilder> databasesRequestBuilderFactory;
    @Mock
    private IdolDatabasesRequestBuilder databasesRequestBuilder;

    @Override
    protected IdolDatabasesService constructDatabasesService() {
        when(databasesRequestBuilderFactory.getObject()).thenReturn(databasesRequestBuilder);

        return idolDatabasesService;
    }

    @Override
    protected IdolDatabasesController constructDatabasesController() {
        return new IdolDatabasesController(idolDatabasesService, databasesRequestBuilderFactory);
    }
}
