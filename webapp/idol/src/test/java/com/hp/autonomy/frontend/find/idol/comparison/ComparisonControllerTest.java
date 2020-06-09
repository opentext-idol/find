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

package com.hp.autonomy.frontend.find.idol.comparison;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.idol.comparison.ComparisonRequest.Builder;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComparisonControllerTest {
    private static final String MOCK_STATE_TOKEN_1 = "abc";
    private static final String MOCK_STATE_TOKEN_2 = "def";

    @Mock
    private ComparisonService<IdolSearchResult, AciErrorException> comparisonService;

    @Mock
    private IdolDocumentsService documentsService;

    private ComparisonController<IdolQueryRestrictions, IdolSearchResult, AciErrorException> comparisonController;

    @Mock
    private IdolQueryRestrictions queryRestrictions;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        comparisonController = new ComparisonController<>(comparisonService, documentsService);
        when(documentsService.getStateToken(any(), anyInt(), anyBoolean()))
                .thenReturn(MOCK_STATE_TOKEN_1);
    }

    @Test
    public void compareStateTokens() throws AciErrorException {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new Builder<IdolQueryRestrictions>()
                .setFirstQueryStateToken(MOCK_STATE_TOKEN_1)
                .setSecondQueryStateToken(MOCK_STATE_TOKEN_2)
                .build();

        comparisonController.getCompareStateTokens(comparisonRequest);
        verify(comparisonService).getCompareStateTokens(eq(MOCK_STATE_TOKEN_1), eq(MOCK_STATE_TOKEN_2));
    }

    @Test
    public void compareTokenAndRestriction() throws AciErrorException {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new Builder<IdolQueryRestrictions>()
                .setFirstQueryStateToken(MOCK_STATE_TOKEN_1)
                .setSecondRestrictions(queryRestrictions)
                .build();

        comparisonController.getCompareStateTokens(comparisonRequest);
        verify(comparisonService).getCompareStateTokens(eq(MOCK_STATE_TOKEN_1), eq(MOCK_STATE_TOKEN_1));
    }

    @Test
    public void compareRestrictionAndToken() throws AciErrorException {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new Builder<IdolQueryRestrictions>()
                .setFirstRestrictions(queryRestrictions)
                .setSecondQueryStateToken(MOCK_STATE_TOKEN_2)
                .build();

        comparisonController.getCompareStateTokens(comparisonRequest);
        verify(comparisonService).getCompareStateTokens(eq(MOCK_STATE_TOKEN_1), eq(MOCK_STATE_TOKEN_2));
    }

    @Test
    public void compareRestrictions() throws AciErrorException {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new Builder<IdolQueryRestrictions>()
                .setFirstRestrictions(queryRestrictions)
                .setSecondRestrictions(queryRestrictions)
                .build();

        comparisonController.getCompareStateTokens(comparisonRequest);
        verify(comparisonService).getCompareStateTokens(eq(MOCK_STATE_TOKEN_1), eq(MOCK_STATE_TOKEN_1));
    }

    @Test
    public void getResults() throws AciErrorException {
        final List<String> stateMatchIds = Collections.singletonList(MOCK_STATE_TOKEN_1);
        final List<String> stateDontMatchIds = Collections.singletonList(MOCK_STATE_TOKEN_2);
        final String text = "*";
        final String fieldText = "EXISTS{}:LATITUDE AND EXISTS{}:LONGITUDE";
        final int start = 3;
        final int maxResults = 6;
        final String summary = "context";
        final String sort = "relevance";
        final boolean highlight = true;

        comparisonController.getResults(stateMatchIds, stateDontMatchIds, text, fieldText, start, maxResults, summary, sort, highlight, false);
        verify(comparisonService).getResults(eq(stateMatchIds), eq(stateDontMatchIds), eq(text), eq(fieldText), eq(start), eq(maxResults), eq(summary), eq(sort), eq(highlight));
    }
}
