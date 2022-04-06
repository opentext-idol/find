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

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("ProhibitedExceptionDeclared")
public abstract class AbstractDocumentServiceIT extends AbstractFindIT {
    @Test
    public void query() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH)
                .param(DocumentsController.TEXT_PARAM, "*")
                .param(DocumentsController.RESULTS_START_PARAM, "1")
                .param(DocumentsController.MAX_RESULTS_PARAM, "50")
                .param(DocumentsController.SUMMARY_PARAM, "context")
                .param(DocumentsController.INDEXES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void queryWithPagination() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH)
                .param(DocumentsController.TEXT_PARAM, "*")
                .param(DocumentsController.RESULTS_START_PARAM, "10")
                .param(DocumentsController.MAX_RESULTS_PARAM, "20")
                .param(DocumentsController.AUTO_CORRECT_PARAM, "false")
                .param(DocumentsController.SUMMARY_PARAM, "context")
                .param(DocumentsController.INDEXES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(DocumentsController.QUERY_TYPE_PARAM, QueryRequest.QueryType.MODIFIED.name())
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void queryForPromotions() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH)
                .param(DocumentsController.TEXT_PARAM, "*")
                .param(DocumentsController.RESULTS_START_PARAM, "1")
                .param(DocumentsController.MAX_RESULTS_PARAM, "50")
                .param(DocumentsController.SUMMARY_PARAM, "context")
                .param(DocumentsController.INDEXES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(DocumentsController.QUERY_TYPE_PARAM, QueryRequest.QueryType.PROMOTIONS.name())
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", empty()));
    }

    @Test
    public void findSimilar() throws Exception {
        final String reference = mvcIntegrationTestUtils.getValidReference(mockMvc);

        final MockHttpServletRequestBuilder requestBuilder = get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.SIMILAR_DOCUMENTS_PATH)
                .param(DocumentsController.REFERENCE_PARAM, reference)
                .param(DocumentsController.INDEXES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(DocumentsController.MAX_RESULTS_PARAM, "50")
                .param(DocumentsController.SUMMARY_PARAM, "context")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void getDocumentContent() throws Exception {
        final String reference = mvcIntegrationTestUtils.getValidReference(mockMvc);

        final MockHttpServletRequestBuilder requestBuilder = get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.GET_DOCUMENT_CONTENT_PATH)
                .param(DocumentsController.REFERENCE_PARAM, reference)
                .param(DocumentsController.DATABASE_PARAM, mvcIntegrationTestUtils.getDatabases()[0])
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(nullValue())));
    }
}
