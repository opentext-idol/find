/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDocumentsControllerTest<S extends Serializable, R extends SearchResult, E extends Exception> {
    @Mock
    protected DocumentsService<S, R, E> documentsService;

    protected DocumentsController<S, R, E> documentsController;
    protected Class<S> databaseType;

    protected abstract R sampleResult();

    @Test
    public void query() throws E {
        documentsController.query("Some query text", 1, 30, null, Collections.<S>emptyList(), null, null, null, null, true, false);
        verify(documentsService).queryTextIndex(Matchers.<SearchRequest<S>>any());
    }

    @Test
    public void queryForPromotions() throws E {
        documentsController.queryForPromotions("Some query text", 1, 30, null, Collections.<S>emptyList(), null, null, null, null, true, false);
        verify(documentsService).queryTextIndexForPromotions(Matchers.<SearchRequest<S>>any());
    }

    @Test
    public void queryPaginationTest() throws E {
        documentsController.query("Some query text", 30, 60, null, Collections.<S>emptyList(), null, null, null, null, true, false);
        verify(documentsService).queryTextIndex(Matchers.<SearchRequest<S>>any());
    }

    @Test
    public void findSimilar() throws E {
        final String reference = "SomeReference";
        documentsController.findSimilar(reference, Collections.<S>emptyList());
        verify(documentsService).findSimilar(Matchers.<SuggestRequest<S>>any());
    }

    @Test
    public void getDocumentContent() throws E {
        when(documentsService.getDocumentContent(Matchers.<GetContentRequest<S>>any())).thenReturn(Collections.singletonList(sampleResult()));
        final String reference = "SomeReference";
        assertNotNull(documentsController.getDocumentContent(reference, null));
    }
}
