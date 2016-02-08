/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.searchcomponents.idol.configuration.QueryManipulation;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentServiceTest;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.types.idol.QueryResponseData;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.when;

public class FindIdolDocumentServiceTest extends IdolDocumentServiceTest {
    @Override
    @Before
    public void setUp() {
        when(havenSearchConfig.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().build());
        when(configService.getConfig()).thenReturn(havenSearchConfig);
        idolDocumentService = new FindIdolDocumentService(configService, parameterHandler, contentAciService, qmsAciService, aciResponseProcessorFactory);
    }

    @Test
    public void queryQmsButNoBlackList() {
        when(havenSearchConfig.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().setEnabled(true).build());
        final QueryResponseData responseData = mockQueryResponse();
        final AciErrorException blacklistError = new AciErrorException();
        blacklistError.setErrorString(FindIdolDocumentService.MISSING_RULE_ERROR);
        when(qmsAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenThrow(blacklistError).thenReturn(responseData);

        final Documents<IdolSearchResult> results = idolDocumentService.queryTextIndex(mockQueryParams());
        assertThat(results.getDocuments(), is(not(empty())));
    }

    @Test(expected = AciErrorException.class)
    public void queryQmsButUnexpectedError() {
        when(havenSearchConfig.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().setEnabled(true).build());
        when(qmsAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenThrow(new AciErrorException());

        idolDocumentService.queryTextIndex(mockQueryParams());
    }
}
