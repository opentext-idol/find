/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsRequest;
import com.hp.autonomy.searchcomponents.core.search.RelatedConceptsService;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
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
public abstract class AbstractRelatedConceptsControllerTest<Q extends QuerySummaryElement, R extends QueryRestrictions<S>, S extends Serializable, E extends Exception> {
    @Mock
    protected RelatedConceptsService<Q, S, E> relatedConceptsService;
    @Mock
    protected ObjectFactory<QueryRestrictions.Builder<R, S>> queryRestrictionsBuilderFactory;

    protected RelatedConceptsController<Q, R, S, E> relatedConceptsController;

    @Test
    public void query() throws E {
        relatedConceptsController.findRelatedConcepts("Some query text", null, Collections.<S>emptyList(), null, null, 0, null);
        verify(relatedConceptsService).findRelatedConcepts(Matchers.<RelatedConceptsRequest<S>>any());
    }
}
