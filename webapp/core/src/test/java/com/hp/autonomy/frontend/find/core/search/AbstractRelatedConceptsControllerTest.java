/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsRequest;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;

import java.io.Serializable;
import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractRelatedConceptsControllerTest<Q extends QuerySummaryElement, R extends QueryRestrictions<S>, L extends RelatedConceptsRequest<S>, S extends Serializable, E extends Exception> {
    @Mock
    protected RelatedConceptsService<Q, S, E> relatedConceptsService;

    @Mock
    protected QueryRestrictionsBuilderFactory<R, S> queryRestrictionsBuilderFactory;

    @Mock
    protected ObjectFactory<RelatedConceptsRequest.RelatedConceptsRequestBuilder<L, S>> relatedConceptsRequestBuilderFactory;

    private RelatedConceptsController<Q, R, L, S, E> relatedConceptsController;

    protected abstract RelatedConceptsController<Q, R, L, S, E> buildController(final RelatedConceptsService<Q, S, E> relatedConceptsService, final QueryRestrictionsBuilderFactory<R, S> queryRestrictionsBuilderFactory, final ObjectFactory<RelatedConceptsRequest.RelatedConceptsRequestBuilder<L, S>> relatedConceptsRequestBuilderFactory);

    @Before
    public void setUp() {
        relatedConceptsController = buildController(relatedConceptsService, queryRestrictionsBuilderFactory, relatedConceptsRequestBuilderFactory);
    }

    @Test
    public void query() throws E {
        relatedConceptsController.findRelatedConcepts("Some query text", null, Collections.emptyList(), null, null, 0, null, null, null);
        verify(relatedConceptsService).findRelatedConcepts(Matchers.any());
    }
}
