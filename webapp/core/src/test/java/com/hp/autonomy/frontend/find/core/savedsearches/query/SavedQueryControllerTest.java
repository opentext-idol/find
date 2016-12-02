/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
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
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public abstract class SavedQueryControllerTest<RQ extends QueryRequest<Q>, S extends Serializable, Q extends QueryRestrictions<S>, D extends SearchResult, E extends Exception> {
    private final SavedQuery savedQuery = new SavedQuery.Builder()
            .setTitle("Any old saved search")
            .build();
    @Mock
    protected SavedQueryService savedQueryService;
    @Mock
    protected FieldTextParser fieldTextParser;
    @Mock
    private Documents<D> searchResults;
    private DocumentsService<RQ, ?, ?, Q, D, E> documentsService;
    private SavedQueryController<RQ, S, Q, D, E> savedQueryController;

    protected abstract DocumentsService<RQ, ?, ?, Q, D, E> constructDocumentsService();
    protected abstract SavedQueryController<RQ, S, Q, D, E> constructController();

    @Before
    public void setUp() {
        documentsService = constructDocumentsService();
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
                .setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("raccoons", true, 0)))
                .build();
        when(savedQueryService.get(id)).thenReturn(savedQuery);
        final int numberOfResults = 1;
        when(searchResults.getTotalResults()).thenReturn(numberOfResults);
        when(documentsService.queryTextIndex(any())).thenReturn(searchResults);
        assertEquals(numberOfResults, savedQueryController.checkForNewQueryResults(id));
    }

    @Test
    public void checkForNewQueryResultsNoNewResults() throws E {
        final long id = 123L;
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setId(id)
                .setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("raccoons", true, 0)))
                .build();
        when(savedQueryService.get(id)).thenReturn(savedQuery);
        when(documentsService.queryTextIndex(any())).thenReturn(searchResults);
        final int numberOfResults = 0;
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
        verify(documentsService, never()).queryTextIndex(any());
    }
}
