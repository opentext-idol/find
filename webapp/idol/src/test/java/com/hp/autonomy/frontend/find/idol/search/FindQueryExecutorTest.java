/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.QueryExecutor;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindQueryExecutorTest {
    @Mock
    private QueryExecutor queryExecutor;

    private FindQueryExecutor findQueryExecutor;

    @Before
    public void setUp() {
        findQueryExecutor = new FindQueryExecutor(queryExecutor);
    }

    @Test
    public void queryQmsButNoBlackList() {
        final QueryResponseData responseData = new QueryResponseData();
        final AciErrorException blacklistError = new AciErrorException();
        blacklistError.setErrorString(FindQueryExecutor.MISSING_RULE_ERROR);
        when(queryExecutor.executeQuery(any(), any())).thenThrow(blacklistError).thenReturn(responseData);

        assertNotNull(findQueryExecutor.executeQuery(new AciParameters(), QueryRequest.QueryType.MODIFIED));
    }

    @Test(expected = AciErrorException.class)
    public void queryQmsButUnexpectedError() {
        when(queryExecutor.executeQuery(any(), any())).thenThrow(new AciErrorException());
        findQueryExecutor.executeQuery(new AciParameters(), QueryRequest.QueryType.MODIFIED);
    }

    @Test
    public void performQuery() {
        final QueryRequest.QueryType queryType = QueryRequest.QueryType.MODIFIED;
        findQueryExecutor.performQuery(queryType);
        verify(queryExecutor).performQuery(queryType);
    }

    @Test
    public void executeSuggest() {
        final AciParameters parameters = new AciParameters();
        final QueryRequest.QueryType queryType = QueryRequest.QueryType.RAW;
        findQueryExecutor.executeSuggest(parameters, queryType);
        verify(queryExecutor).executeSuggest(parameters, queryType);
    }
}
