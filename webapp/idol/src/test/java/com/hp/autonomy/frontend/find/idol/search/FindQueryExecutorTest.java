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

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest.QueryType;
import com.hp.autonomy.searchcomponents.idol.search.QueryExecutor;
import com.hp.autonomy.types.idol.responses.GetQueryTagValuesResponseData;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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

        assertNotNull(findQueryExecutor.executeQuery(new AciParameters(), QueryType.MODIFIED));
    }

    @Test(expected = AciErrorException.class)
    public void queryQmsButUnexpectedError() {
        when(queryExecutor.executeQuery(any(), any())).thenThrow(new AciErrorException());
        findQueryExecutor.executeQuery(new AciParameters(), QueryType.MODIFIED);
    }

    @Test
    public void performQuery() {
        final QueryType queryType = QueryType.MODIFIED;
        findQueryExecutor.performQuery(queryType);
        verify(queryExecutor).performQuery(queryType);
    }

    @Test
    public void executeSuggest() {
        final AciParameters parameters = new AciParameters();
        final QueryType queryType = QueryType.RAW;
        findQueryExecutor.executeSuggest(parameters, queryType);
        verify(queryExecutor).executeSuggest(parameters, queryType);
    }

    @Test
    public void testParametricValuesWithNoBlacklist() {
        final GetQueryTagValuesResponseData responseData = new GetQueryTagValuesResponseData();

        final AciErrorException blacklistError = new AciErrorException();
        blacklistError.setErrorString(FindQueryExecutor.MISSING_RULE_ERROR);

        when(queryExecutor.executeGetQueryTagValues(any(), any())).thenThrow(blacklistError).thenReturn(responseData);

        assertThat(findQueryExecutor.executeGetQueryTagValues(new AciParameters(), QueryType.MODIFIED), is(responseData));
    }

    @Test(expected = AciErrorException.class)
    public void testParametricValuesWithUnexpectedError() {
        when(queryExecutor.executeGetQueryTagValues(any(), any())).thenThrow(new AciErrorException());
        findQueryExecutor.executeGetQueryTagValues(new AciParameters(), QueryType.MODIFIED);
    }
}
