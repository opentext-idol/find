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

package com.hp.autonomy.frontend.find.core.databases;

import com.hp.autonomy.searchcomponents.core.databases.DatabasesRequest;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.types.IdolDatabase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDatabasesControllerTest<D extends IdolDatabase, R extends DatabasesRequest, E extends Exception> {
    private DatabasesService<D, R, E> databasesService;
    private DatabasesController<D, R, E> databasesController;

    protected abstract DatabasesService<D, R, E> constructDatabasesService();

    protected abstract DatabasesController<D, R, E> constructDatabasesController();

    @Before
    public void setUp() {
        databasesService = constructDatabasesService();
        databasesController = constructDatabasesController();
    }

    @Test
    public void getParametricValues() throws E {
        databasesController.getDatabases();
        verify(databasesService).getDatabases(any());
    }
}
