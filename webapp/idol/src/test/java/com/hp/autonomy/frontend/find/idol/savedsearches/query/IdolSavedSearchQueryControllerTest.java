/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.query;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryControllerTest;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class IdolSavedSearchQueryControllerTest extends SavedQueryControllerTest<IdolQueryRequest, String, IdolQueryRestrictions, IdolSearchResult, AciErrorException> {
    @Mock
    private IdolDocumentsService idolDocumentsService;

    @Mock
    private ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory;

    @Mock
    private IdolQueryRequestBuilder queryRequestBuilder;

    @Override
    protected IdolSavedQueryController constructController() {
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
        when(queryRequestBuilder.queryRestrictions(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.maxResults(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryType(any())).thenReturn(queryRequestBuilder);
        return new IdolSavedQueryController(savedQueryService, idolDocumentsService, fieldTextParser, queryRestrictionsBuilderFactory, queryRequestBuilderFactory);
    }

    @Override
    protected DocumentsService<IdolQueryRequest, ?, ?, IdolQueryRestrictions, IdolSearchResult, AciErrorException> constructDocumentsService() {
        return idolDocumentsService;
    }
}
