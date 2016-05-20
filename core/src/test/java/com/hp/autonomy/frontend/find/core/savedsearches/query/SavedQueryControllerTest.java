/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public abstract class SavedQueryControllerTest<S extends Serializable, D extends SearchResult, E extends Exception> {
    @Mock
    protected SavedQueryService savedQueryService;
    @Mock
    protected DocumentsService<S, D, E> documentsService;
    @Mock
    protected QueryRestrictionsBuilder<S> queryRestrictionsBuilder;
    @Mock
    private Documents<D> searchResults;

    private SavedQueryController<S, D, E> savedQueryController;

    private final SavedQuery savedQuery = new SavedQuery.Builder()
            .setTitle("Any old saved search")
            .build();

    protected abstract SavedQueryController<S, D, E> constructController();

    @Before
    public void setUp() {
        savedQueryController = constructController();
    }

    @Test
    public void create() {
        savedQueryController.create(savedQuery);
        verify(savedQueryService).create(Matchers.same(savedQuery));
    }

    @Test
    public void update() {
        when(savedQueryService.update(any(SavedQuery.class))).then(returnsFirstArg());

        final SavedQuery updatedQuery = savedQueryController.update(42, savedQuery);
        verify(savedQueryService).update(Matchers.isA(SavedQuery.class));
        assertEquals(42L, (long) updatedQuery.getId());
    }

    @Test
    public void getAll() {
        savedQueryController.getAll();
        verify(savedQueryService).getAll();
    }

    @Test
    public void delete() {
        savedQueryController.delete(42L);
        verify(savedQueryService).deleteById(eq(42L));
    }

    @Test
    public void checkForNewQueryResults() throws E {
        final long id = 123L;
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setId(id)
                .build();
        when(savedQueryService.get(id)).thenReturn(savedQuery);
        int numberOfResults = 1;
        when(searchResults.getTotalResults()).thenReturn(numberOfResults);
        when(documentsService.queryTextIndex(Matchers.<SearchRequest<S>>any())).thenReturn(searchResults);
        assertEquals(numberOfResults, savedQueryController.checkForNewQueryResults(id));
    }

    @Test
    public void checkForNewQueryResultsNoNewResults() throws E {
        final long id = 123L;
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setId(id)
                .build();
        when(savedQueryService.get(id)).thenReturn(savedQuery);
        final int numberOfResults = 0;
        when(documentsService.queryTextIndex(Matchers.<SearchRequest<S>>any())).thenReturn(searchResults);
        assertEquals(numberOfResults, savedQueryController.checkForNewQueryResults(id));
    }

    @Test
    public void checkForNewQueryResultsButIncompatibleRestrictions() throws E {
        final long id = 123L;
        final DateTime lastFetchTime = DateTime.now();
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setDateNewDocsLastFetched(lastFetchTime)
                .setId(id)
                .setMaxDate(lastFetchTime.minus(1))
                .build();
        when(savedQueryService.get(id)).thenReturn(savedQuery);
        final int numberOfResults = 0;

        assertEquals(numberOfResults, savedQueryController.checkForNewQueryResults(id));
        verify(documentsService, never()).queryTextIndex(Matchers.<SearchRequest<S>>any());
    }
}
