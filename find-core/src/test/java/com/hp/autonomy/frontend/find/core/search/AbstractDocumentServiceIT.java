/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("ProhibitedExceptionDeclared")
public abstract class AbstractDocumentServiceIT extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected MvcIntegrationTestUtils mvcIntegrationTestUtils;

    @Test
    public void query() throws Exception {
        mockMvc.perform(
                get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH)
                        .param(DocumentsController.TEXT_PARAM, "*")
                        .param(DocumentsController.RESULTS_START_PARAM, "1")
                        .param(DocumentsController.MAX_RESULTS_PARAM, "50")
                        .param(DocumentsController.SUMMARY_PARAM, "context")
                        .param(DocumentsController.INDEXES_PARAM, mvcIntegrationTestUtils.getDatabases()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void queryWithPagination() throws Exception {
        mockMvc.perform(
                get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH)
                        .param(DocumentsController.TEXT_PARAM, "*")
                        .param(DocumentsController.RESULTS_START_PARAM, "51")
                        .param(DocumentsController.MAX_RESULTS_PARAM, "100")
                        .param(DocumentsController.AUTO_CORRECT_PARAM, "false")
                        .param(DocumentsController.SUMMARY_PARAM, "context")
                        .param(DocumentsController.INDEXES_PARAM, mvcIntegrationTestUtils.getDatabases()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void queryForPromotions() throws Exception {
        mockMvc.perform(
                get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.PROMOTIONS_PATH)
                        .param(DocumentsController.TEXT_PARAM, "*")
                        .param(DocumentsController.RESULTS_START_PARAM, "1")
                        .param(DocumentsController.MAX_RESULTS_PARAM, "50")
                        .param(DocumentsController.SUMMARY_PARAM, "context")
                        .param(DocumentsController.INDEXES_PARAM, mvcIntegrationTestUtils.getDatabases()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", empty()));
    }

    @Test
    public void findSimilar() throws Exception {
        final String reference = mvcIntegrationTestUtils.getValidReference(mockMvc);

        mockMvc.perform(
                get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.SIMILAR_DOCUMENTS_PATH)
                        .param(DocumentsController.REFERENCE_PARAM, reference)
                        .param(DocumentsController.INDEXES_PARAM, mvcIntegrationTestUtils.getDatabases())
                        .param(DocumentsController.MAX_RESULTS_PARAM, "50")
                        .param(DocumentsController.SUMMARY_PARAM, "context"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void getDocumentContent() throws Exception {
        final String reference = mvcIntegrationTestUtils.getValidReference(mockMvc);

        mockMvc.perform(
                get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.GET_DOCUMENT_CONTENT_PATH)
                        .param(DocumentsController.REFERENCE_PARAM, reference)
                        .param(DocumentsController.DATABASE_PARAM, mvcIntegrationTestUtils.getDatabases()[0]))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(nullValue())));
    }
}
