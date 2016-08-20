/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.core.search.AciSearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentService;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentServiceTest;
import com.hp.autonomy.types.idol.QueryResponseData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FindIdolDocumentServiceTest extends IdolDocumentServiceTest {
    @Override
    @Before
    public void setUp() {
        when(aciServiceRetriever.getAciService(any(SearchRequest.QueryType.class))).thenReturn(aciService);
        idolDocumentService = new FindIdolDocumentService(parameterHandler, queryResponseParser, aciServiceRetriever, aciResponseProcessorFactory);
    }

    @Test
    public void queryQmsButNoBlackList() {
        final QueryResponseData responseData = new QueryResponseData();
        final AciErrorException blacklistError = new AciErrorException();
        blacklistError.setErrorString(FindIdolDocumentService.MISSING_RULE_ERROR);
        when(aciService.executeAction(anySetOf(AciParameter.class), any())).thenThrow(blacklistError).thenReturn(responseData);

        idolDocumentService.queryTextIndex(mockQueryParams(SearchRequest.QueryType.MODIFIED));
        verify(queryResponseParser).parseQueryResults(any(), any(AciParameters.class), eq(responseData), any());
    }

    @Test(expected = AciErrorException.class)
    public void queryQmsButUnexpectedError() {
        when(aciService.executeAction(anySetOf(AciParameter.class), any())).thenThrow(new AciErrorException());

        idolDocumentService.queryTextIndex(mockQueryParams(SearchRequest.QueryType.MODIFIED));
    }
}
