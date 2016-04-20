/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.databases;

import com.hp.autonomy.searchcomponents.core.databases.DatabasesRequest;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.types.IdolDatabase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDatabasesControllerTest<D extends IdolDatabase, R extends DatabasesRequest, E extends Exception> {
    @Mock
    protected DatabasesService<D, R, E> databasesService;

    protected DatabasesController<D, R, E> databasesController;

    @Test
    public void getParametricValues() throws E {
        databasesController.getDatabases();
        verify(databasesService).getDatabases(Matchers.<R>any());
    }
}
