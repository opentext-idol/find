/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;

import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDocumentsControllerTest<S extends Serializable, R extends SearchResult, E extends Exception> {
    @Mock
    protected DocumentsService<S, R, E> documentsService;

    @Mock
    protected QueryRestrictionsBuilder<S> queryRestrictionsBuilder;

    protected DocumentsController<S, R, E> documentsController;
    protected Class<S> databaseType;

    @Test
    public void query() throws E {
        documentsController.query("Some query text", 30, null, Collections.<S>emptyList(), null, null, null, null, true, false);
        verify(documentsService).queryTextIndex(Matchers.<SearchRequest<S>>any());
    }

    @Test
    public void queryForPromotions() throws E {
        documentsController.queryForPromotions("Some query text", 30, null, Collections.<S>emptyList(), null, null, null, null, true, false);
        verify(documentsService).queryTextIndexForPromotions(Matchers.<SearchRequest<S>>any());
    }

    @Test
    public void findSimilar() throws E {
        final String reference = "SomeReference";
        documentsController.findSimilar(reference, Collections.<S>emptySet());
        verify(documentsService).findSimilar(anySetOf(databaseType), eq(reference));
    }
}
