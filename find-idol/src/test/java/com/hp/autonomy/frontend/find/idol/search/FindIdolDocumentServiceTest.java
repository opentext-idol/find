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
import com.hp.autonomy.searchcomponents.idol.configuration.QueryManipulation;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentService;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentServiceTest;
import com.hp.autonomy.types.idol.QueryResponseData;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
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
        when(havenSearchConfig.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().build());
        when(configService.getConfig()).thenReturn(havenSearchConfig);
        idolDocumentService = new FindIdolDocumentService(configService, parameterHandler, queryResponseParser, contentAciService, qmsAciService, aciResponseProcessorFactory);
    }

    @Test
    public void queryQmsButNoBlackList() {
        when(havenSearchConfig.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().setEnabled(true).build());
        final QueryResponseData responseData = new QueryResponseData();
        final AciErrorException blacklistError = new AciErrorException();
        blacklistError.setErrorString(FindIdolDocumentService.MISSING_RULE_ERROR);
        when(qmsAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenThrow(blacklistError).thenReturn(responseData);

        idolDocumentService.queryTextIndex(mockQueryParams());
        verify(queryResponseParser).parseQueryResults(Matchers.<AciSearchRequest<String>>any(), any(AciParameters.class), eq(responseData), any(IdolDocumentService.QueryExecutor.class));
    }

    @Test(expected = AciErrorException.class)
    public void queryQmsButUnexpectedError() {
        when(havenSearchConfig.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().setEnabled(true).build());
        when(qmsAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenThrow(new AciErrorException());

        idolDocumentService.queryTextIndex(mockQueryParams());
    }
}
