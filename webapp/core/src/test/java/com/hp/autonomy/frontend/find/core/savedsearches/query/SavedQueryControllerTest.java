/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class SavedQueryControllerTest<RQ extends QueryRequest<Q>, S extends Serializable, Q extends QueryRestrictions<S>, D extends SearchResult, E extends Exception, C extends SavedQueryController<RQ, S, Q, D, E>> {
    @Mock
    protected SavedQueryService savedQueryService;
    @Mock
    protected FieldTextParser fieldTextParser;
    protected C savedQueryController;
    @Mock
    private Documents<D> searchResults;
    private DocumentsService<RQ, ?, ?, Q, D, E> documentsService;
    private SavedQuery savedQuery;

    protected abstract DocumentsService<RQ, ?, ?, Q, D, E> constructDocumentsService();

    protected abstract C constructController();

    @Before
    public void setUp() {
        documentsService = constructDocumentsService();
        savedQueryController = constructController();
        savedQuery = new SavedQuery.Builder()
                .setTitle("Any old saved search")
                .build();
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
        assertEquals(42L, (long)updatedQuery.getId());
    }

    @Test
    public void getAll() {
        savedQueryController.getAll(false);
        verify(savedQueryService).getOwned();
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
        final ZonedDateTime lastFetchTime = ZonedDateTime.now();
        final SavedQuery savedQuery = new SavedQuery.Builder()
                .setDateDocsLastFetched(lastFetchTime)
                .setId(id)
                .setMaxDate(lastFetchTime.minusMinutes(1))
                .build();
        when(savedQueryService.get(id)).thenReturn(savedQuery);
        final int numberOfResults = 0;

        assertEquals(numberOfResults, savedQueryController.checkForNewQueryResults(id));
        verify(documentsService, never()).queryTextIndex(any());
    }
}
