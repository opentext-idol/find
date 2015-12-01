/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Collections;

import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDocumentsControllerTest<S extends Serializable, D extends FindDocument, E extends Exception> {
    @Mock
    protected DocumentsService<S, D, E> documentsService;

    protected final DocumentsController<S, D, E> documentsController;
    protected final Class<S> databaseType;

    protected AbstractDocumentsControllerTest(final DocumentsController<S, D, E> documentsController, final Class<S> databaseType) {
        this.documentsController = documentsController;
        this.databaseType = databaseType;
    }

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(documentsController, "documentsService", documentsService, DocumentsService.class);
    }

    @Test
    public void query() throws E {
        documentsController.query("Some query text", 30, null, Collections.<S>emptyList(), null, null, null, null);
        verify(documentsService).queryTextIndex(Matchers.<FindQueryParams<S>>any());
    }

    @Test
    public void queryForPromotions() throws E {
        documentsController.queryForPromotions("Some query text", 30, null, Collections.<S>emptyList(), null, null, null, null);
        verify(documentsService).queryTextIndexForPromotions(Matchers.<FindQueryParams<S>>any());
    }

    @Test
    public void findSimilar() throws E {
        final String reference = "SomeReference";
        documentsController.findSimilar(reference, Collections.<S>emptySet());
        verify(documentsService).findSimilar(anySetOf(databaseType), eq(reference));
    }
}
