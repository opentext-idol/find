/*
 * Copyright 2015-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.databases;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.databases.AbstractDatabasesControllerTest;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequest;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesService;
import com.opentext.idol.types.responses.Database;
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
