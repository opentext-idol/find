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

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequest;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestIndex;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.searchcomponents.core.search.SuggestRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDocumentsControllerTest<RQ extends QueryRequest<Q>, RS extends SuggestRequest<Q>, RC extends GetContentRequest<T>, S extends Serializable, Q extends QueryRestrictions<S>, T extends GetContentRequestIndex<S>, R extends SearchResult, E extends Exception> {
    protected DocumentsService<RQ, RS, RC, Q, R, E> documentsService;

    protected DocumentsController<RQ, RS, RC, S, Q, T, R, E> documentsController;
    protected Class<S> databaseType;

    protected abstract R sampleResult();

    protected abstract String getSort();

    @Test
    public void query() throws E {
        documentsController.query("Some query text", 1, 30, null, Collections.emptyList(), null, null, null, null, true, 0, false, false, QueryRequest.QueryType.MODIFIED.name());
        verify(documentsService).queryTextIndex(any());
    }

    @Test
    public void queryForPromotions() throws E {
        documentsController.query("Some query text", 1, 30, null, Collections.emptyList(), null, null, null, null, true, 0, false, false, QueryRequest.QueryType.PROMOTIONS.name());
        verify(documentsService).queryTextIndex(any());
    }

    @Test
    public void queryPaginationTest() throws E {
        documentsController.query("Some query text", 30, 60, null, Collections.emptyList(), null, null, null, null, true, 0, false, false, QueryRequest.QueryType.MODIFIED.name());
        verify(documentsService).queryTextIndex(any());
    }

    @Test
    public void findSimilar() throws E {
        final String reference = "SomeReference";
        documentsController.findSimilar(reference, 1, 30, "context", Collections.emptyList(), "", getSort(), null, ZonedDateTime.now(), true, 0);
        verify(documentsService).findSimilar(any());
    }

    @Test
    public void getDocumentContent() throws E {
        when(documentsService.getDocumentContent(any())).thenReturn(Collections.singletonList(sampleResult()));
        final String reference = "SomeReference";
        assertNotNull(documentsController.getDocumentContent(reference, null));
    }

    protected <SR extends SearchRequest<Q>, B extends SearchRequestBuilder<SR, Q, B>> void mockSearchRequestBuilder(final B builder) {
        when(builder.queryRestrictions(any())).thenReturn(builder);
        when(builder.start(anyInt())).thenReturn(builder);
        when(builder.maxResults(anyInt())).thenReturn(builder);
        when(builder.summaryCharacters(anyInt())).thenReturn(builder);
        when(builder.highlight(anyBoolean())).thenReturn(builder);
    }
}
