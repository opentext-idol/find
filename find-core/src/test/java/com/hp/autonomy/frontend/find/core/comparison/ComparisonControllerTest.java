/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;

import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ComparisonControllerTest<S extends Serializable, R extends SearchResult, E extends Exception> {
    protected static final String MOCK_STATE_TOKEN_1 = "abc";
    private static final String MOCK_STATE_TOKEN_2 = "def";

    @Mock
    protected ComparisonService<R, E> comparisonService;
    @Mock
    protected DocumentsService<S, R, E> documentsService;

    protected ComparisonController<S, R, E> comparisonController;

    @Mock
    protected QueryRestrictions<S> queryRestrictions;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        comparisonController = new ComparisonController<>(comparisonService, documentsService);
        when(documentsService.getStateToken(any(QueryRestrictions.class), anyInt()))
                .thenReturn(MOCK_STATE_TOKEN_1);
    }

    @Test
    public void compareStateTokens() throws E {
        final ComparisonRequest<S> comparisonRequest = new ComparisonRequest.Builder<S>()
                .setFirstQueryStateToken(MOCK_STATE_TOKEN_1)
                .setSecondQueryStateToken(MOCK_STATE_TOKEN_2)
                .build();

        comparisonController.getCompareStateTokens(comparisonRequest);
        verify(comparisonService).getCompareStateTokens(eq(MOCK_STATE_TOKEN_1), eq(MOCK_STATE_TOKEN_2));
    }

    @Test
    public void compareTokenAndRestriction() throws E {
        final ComparisonRequest<S> comparisonRequest = new ComparisonRequest.Builder<S>()
                .setFirstQueryStateToken(MOCK_STATE_TOKEN_1)
                .setSecondRestrictions(queryRestrictions)
                .build();

        comparisonController.getCompareStateTokens(comparisonRequest);
        verify(comparisonService).getCompareStateTokens(eq(MOCK_STATE_TOKEN_1), eq(MOCK_STATE_TOKEN_1));
    }

    @Test
    public void compareRestrictionAndToken() throws E {
        final ComparisonRequest<S> comparisonRequest = new ComparisonRequest.Builder<S>()
                .setFirstRestrictions(queryRestrictions)
                .setSecondQueryStateToken(MOCK_STATE_TOKEN_2)
                .build();

        comparisonController.getCompareStateTokens(comparisonRequest);
        verify(comparisonService).getCompareStateTokens(eq(MOCK_STATE_TOKEN_1), eq(MOCK_STATE_TOKEN_2));
    }

    @Test
    public void compareRestrictions() throws E {
        final ComparisonRequest<S> comparisonRequest = new ComparisonRequest.Builder<S>()
                .setFirstRestrictions(queryRestrictions)
                .setSecondRestrictions(queryRestrictions)
                .build();

        comparisonController.getCompareStateTokens(comparisonRequest);
        verify(comparisonService).getCompareStateTokens(eq(MOCK_STATE_TOKEN_1), eq(MOCK_STATE_TOKEN_1));
    }

    @Test
    public void getResults() throws E {
        final List<String> stateMatchIds = Collections.singletonList(MOCK_STATE_TOKEN_1);
        final List<String> stateDontMatchIds = Collections.singletonList(MOCK_STATE_TOKEN_2);
        final String text = "*";
        final int start = 3;
        final int maxResults = 6;
        final String summary = "context";
        final String sort = "relevance";
        final boolean highlight = true;

        comparisonController.getResults(stateMatchIds, stateDontMatchIds, text, start, maxResults, summary, sort, highlight);
        verify(comparisonService).getResults(eq(stateMatchIds), eq(stateDontMatchIds), eq(text), eq(start), eq(maxResults), eq(summary), eq(sort), eq(highlight));
    }
}
