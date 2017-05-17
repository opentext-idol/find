/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class HodSavedQueryControllerTest extends SavedQueryControllerTest<HodQueryRequest, ResourceName, HodQueryRestrictions, HodSearchResult, HodErrorException, HodSavedQueryController> {
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

    @Override
    protected HodSavedQueryController constructController() {
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

        return new HodSavedQueryController(savedQueryService, hodDocumentsService, fieldTextParser, queryRestrictionsBuilderFactory, queryRequestBuilderFactory);
    }

    @Override
    protected DocumentsService<HodQueryRequest, ?, ?, HodQueryRestrictions, HodSearchResult, HodErrorException> constructDocumentsService() {
        return hodDocumentsService;
    }
}
