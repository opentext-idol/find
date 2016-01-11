/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractDocumentServiceIT<S extends Serializable, R extends SearchResult, E extends Exception> extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected DocumentsController<S, R, E> documentsController;

    protected final List<S> indexes;
    protected final String[] indexesArray;

    protected AbstractDocumentServiceIT(final List<S> indexes) {
        this.indexes = new ArrayList<>(indexes);

        indexesArray = new String[indexes.size()];
        int i = 0;
        for (final S index : indexes) {
            indexesArray[i++] = index.toString();
        }
    }

    @Test
    public void query() throws Exception {
        mockMvc.perform(get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH).param(DocumentsController.TEXT_PARAM, "*").param(DocumentsController.MAX_RESULTS_PARAM, "50").param(DocumentsController.SUMMARY_PARAM, "context").param(DocumentsController.INDEX_PARAM, indexesArray))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void queryForPromotions() throws Exception {
        mockMvc.perform(get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.PROMOTIONS_PATH).param(DocumentsController.TEXT_PARAM, "*").param(DocumentsController.MAX_RESULTS_PARAM, "50").param(DocumentsController.SUMMARY_PARAM, "context").param(DocumentsController.INDEX_PARAM, indexesArray))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", empty()));
    }

    @Test
    public void findSimilar() throws Exception {
        //TODO currently not making (many) assumptions about content we are querying so we don't know a valid reference in advance...
        final Documents<R> documents = documentsController.query("*", 50, null, indexes, null, null, null, null, false);
        final String reference = documents.getDocuments().get(0).getReference();
        mockMvc.perform(get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.SIMILAR_DOCUMENTS_PATH).param(DocumentsController.REFERENCE_PARAM, reference).param(DocumentsController.INDEXES_PARAM, indexesArray))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }
}
