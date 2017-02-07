/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.api.textindex.query.search.Entity;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.searchcomponents.hod.search.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodRelatedConceptsControllerTest extends AbstractRelatedConceptsControllerTest<Entity, HodQueryRestrictions, HodRelatedConceptsRequest, ResourceName, HodErrorException> {
    @Mock
    private HodRelatedConceptsService hodRelatedConceptsService;

    @Mock
    private ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private HodQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ObjectFactory<HodRelatedConceptsRequestBuilder> relatedConceptsRequestBuilderFactory;

    @Mock
    private HodRelatedConceptsRequestBuilder relatedConceptsRequestBuilder;

    @Override
    protected HodRelatedConceptsController buildController() {
        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.fieldText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.maxDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minScore(anyInt())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateMatchIds(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateDontMatchIds(any())).thenReturn(queryRestrictionsBuilder);

        when(relatedConceptsRequestBuilderFactory.getObject()).thenReturn(relatedConceptsRequestBuilder);
        when(relatedConceptsRequestBuilder.maxResults(anyInt())).thenReturn(relatedConceptsRequestBuilder);
        when(relatedConceptsRequestBuilder.querySummaryLength(anyInt())).thenReturn(relatedConceptsRequestBuilder);
        when(relatedConceptsRequestBuilder.queryRestrictions(any())).thenReturn(relatedConceptsRequestBuilder);

        return new HodRelatedConceptsController(hodRelatedConceptsService, queryRestrictionsBuilderFactory, relatedConceptsRequestBuilderFactory);
    }

    @Override
    protected RelatedConceptsService<HodRelatedConceptsRequest, Entity, HodQueryRestrictions, HodErrorException> buildService() {
        return hodRelatedConceptsService;
    }
}
