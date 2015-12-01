/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Collections;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractRelatedConceptsControllerTest<Q extends QuerySummaryElement, S extends Serializable, E extends Exception> {
    @Mock
    protected RelatedConceptsService<Q, S, E> relatedConceptsService;

    protected final RelatedConceptsController<Q, S, E> relatedConceptsController;
    protected final Class<S> databaseType;

    protected AbstractRelatedConceptsControllerTest(final RelatedConceptsController<Q, S, E> relatedConceptsController, final Class<S> databaseType) {
        this.relatedConceptsController = relatedConceptsController;
        this.databaseType = databaseType;
    }

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(relatedConceptsController, "relatedConceptsService", relatedConceptsService, RelatedConceptsService.class);
    }

    @Test
    public void query() throws E {
        final String text = "Some query text";
        relatedConceptsController.findRelatedConcepts(text, Collections.<S>emptyList(), null);
        verify(relatedConceptsService).findRelatedConcepts(eq(text), anyListOf(databaseType), anyString());
    }
}
