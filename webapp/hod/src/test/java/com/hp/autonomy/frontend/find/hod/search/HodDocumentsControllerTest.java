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

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.AbstractDocumentsControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequestIndex;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequestIndexBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.searchcomponents.hod.search.HodSuggestRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodSuggestRequestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodDocumentsControllerTest extends AbstractDocumentsControllerTest<HodQueryRequest, HodSuggestRequest, HodGetContentRequest, ResourceName, HodQueryRestrictions, HodGetContentRequestIndex, HodSearchResult, HodErrorException> {
    @Mock
    private HodDocumentsService hodDocumentsService;

    @Mock
    private ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private HodQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ObjectFactory<HodQueryRequestBuilder> queryRequestBuilderFactory;

    @Mock
    private HodQueryRequestBuilder queryRequestBuilder;

    @Mock
    private ObjectFactory<HodSuggestRequestBuilder> suggestRequestBuilderFactory;

    @Mock
    private HodSuggestRequestBuilder suggestRequestBuilder;

    @Mock
    private ObjectFactory<HodGetContentRequestBuilder> getContentRequestBuilderFactory;

    @Mock
    private HodGetContentRequestBuilder getContentRequestBuilder;

    @Mock
    private ObjectFactory<HodGetContentRequestIndexBuilder> getContentRequestIndexBuilderFactory;

    @Mock
    private HodGetContentRequestIndexBuilder getContentRequestIndexBuilder;

    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.fieldText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.maxDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minScore(anyInt())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateMatchIds(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateDontMatchIds(any())).thenReturn(queryRestrictionsBuilder);

        when(queryRequestBuilderFactory.getObject()).thenReturn(queryRequestBuilder);
        mockSearchRequestBuilder(queryRequestBuilder);
        when(queryRequestBuilder.autoCorrect(anyBoolean())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryType(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.summary(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.sort(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.intentBasedRanking(anyBoolean())).thenReturn(queryRequestBuilder);

        when(suggestRequestBuilderFactory.getObject()).thenReturn(suggestRequestBuilder);
        when(suggestRequestBuilder.reference(any())).thenReturn(suggestRequestBuilder);
        mockSearchRequestBuilder(suggestRequestBuilder);
        when(suggestRequestBuilder.summary(any())).thenReturn(suggestRequestBuilder);
        when(suggestRequestBuilder.sort(any())).thenReturn(suggestRequestBuilder);

        when(getContentRequestBuilderFactory.getObject()).thenReturn(getContentRequestBuilder);
        when(getContentRequestIndexBuilderFactory.getObject()).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestIndexBuilder.index(any())).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestIndexBuilder.reference(any())).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestBuilder.indexAndReferences(any())).thenReturn(getContentRequestBuilder);
        when(getContentRequestBuilder.print(any())).thenReturn(getContentRequestBuilder);

        documentsController = new HodDocumentsController(hodDocumentsService, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, suggestRequestBuilderFactory, getContentRequestBuilderFactory, getContentRequestIndexBuilderFactory);
        documentsService = hodDocumentsService;
        databaseType = ResourceName.class;
    }

    @Override
    protected HodSearchResult sampleResult() {
        return HodSearchResult.builder().build();
    }

    @Override
    protected String getSort() {
        return "relevance";
    }

    @Test(expected = HodErrorException.class)
    public void getDocumentContentNotFound() throws HodErrorException {
        documentsController.getDocumentContent("Some Reference", null);
    }
}
