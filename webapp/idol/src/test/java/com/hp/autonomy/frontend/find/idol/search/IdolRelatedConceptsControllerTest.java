/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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
import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsControllerTest;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolRelatedConceptsRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolRelatedConceptsRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolRelatedConceptsService;
import com.hp.autonomy.types.idol.responses.QsElement;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class IdolRelatedConceptsControllerTest extends AbstractRelatedConceptsControllerTest<QsElement, IdolQueryRestrictions, IdolRelatedConceptsRequest, String, AciErrorException> {
    @Mock
    private IdolRelatedConceptsService idolRelatedConceptsService;

    @Mock
    private ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ObjectFactory<IdolRelatedConceptsRequestBuilder> relatedConceptsRequestBuilderFactory;

    @Mock
    private IdolRelatedConceptsRequestBuilder relatedConceptsRequestBuilder;

    @Override
    protected IdolRelatedConceptsController buildController() {
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
        when(relatedConceptsRequestBuilder.queryType(any())).thenReturn(relatedConceptsRequestBuilder);

        return new IdolRelatedConceptsController(idolRelatedConceptsService, queryRestrictionsBuilderFactory, relatedConceptsRequestBuilderFactory);
    }

    @Override
    protected RelatedConceptsService<IdolRelatedConceptsRequest, QsElement, IdolQueryRestrictions, AciErrorException> buildService() {
        return idolRelatedConceptsService;
    }
}
