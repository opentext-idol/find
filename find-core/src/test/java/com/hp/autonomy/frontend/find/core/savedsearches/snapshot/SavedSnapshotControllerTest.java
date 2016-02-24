/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class SavedSnapshotControllerTest<S extends Serializable, R extends SearchResult, E extends Exception> {

    @Mock
    protected SavedSnapshotService savedSnapshotService;

    @Mock
    protected DocumentsService<S, R, E> documentsService;

    private SavedSnapshotController<S, R, E> savedSnapshotController;

    private final SavedSnapshot savedSnapshot = new SavedSnapshot.Builder()
            .setTitle("Any old saved search")
            .setIndexes(Collections.singleton(new EmbeddableIndex("index", "domain")))
            .build();

    protected abstract SavedSnapshotController<S, R, E> getControllerInstance();

    @Before
    public void setUp() {
        savedSnapshotController = getControllerInstance();
    }

    @Test
    public void create() throws Exception {
        final StateTokenAndResultCount stateTokenAndResultCount = new StateTokenAndResultCount("mock-state-token", 123);
        when(documentsService.getStateTokenAndResultCount(any(QueryRestrictions.class), any(Integer.class))).thenReturn(stateTokenAndResultCount);

        savedSnapshotController.create(savedSnapshot);
        verify(savedSnapshotService).create(Matchers.any(SavedSnapshot.class));
    }

    @Test
    public void update() throws E {
        when(savedSnapshotService.update(any(SavedSnapshot.class))).then(returnsFirstArg());

        final SavedSnapshot updatedQuery = savedSnapshotController.update(42, savedSnapshot);
        verify(savedSnapshotService).update(Matchers.isA(SavedSnapshot.class));
        assertEquals(updatedQuery.getId(), new Long(42L));
    }

    @Test
    public void getAll() {
        savedSnapshotController.getAll();
        verify(savedSnapshotService).getAll();
    }

    @Test
    public void delete() {
        savedSnapshotController.delete(42L);
        verify(savedSnapshotService).deleteById(eq(42L));
    }
}
