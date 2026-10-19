/*
 * Copyright 2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
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
        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/search/query-text-index/results")
                .param("text", "*")
                .param("start", "1")
                .param("max_results", "50")
                .param("summary", "context")
                .param("indexes", mvcIntegrationTestUtils.getDatabases())
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void queryWithPagination() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/search/query-text-index/results")
                .param("text", "*")
                .param("start", "10")
                .param("max_results", "20")
                .param("auto_correct", "false")
                .param("summary", "context")
                .param("indexes", mvcIntegrationTestUtils.getDatabases())
                .param("queryType", QueryRequest.QueryType.MODIFIED.name())
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void queryForPromotions() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/search/query-text-index/results")
                .param("text", "*")
                .param("start", "1")
                .param("max_results", "50")
                .param("summary", "context")
                .param("indexes", mvcIntegrationTestUtils.getDatabases())
                .param("queryType", QueryRequest.QueryType.PROMOTIONS.name())
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documents", empty()));
    }

    @Test
    public void findSimilar() throws Exception {
        final String reference = mvcIntegrationTestUtils.getValidReference(mockMvc);

        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/search/similar-documents")
                .param("reference", reference)
                .param("indexes", mvcIntegrationTestUtils.getDatabases())
                .param("max_results", "50")
                .param("summary", "context")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documents", not(empty())));
    }

    @Test
    public void getDocumentContent() throws Exception {
        final String reference = mvcIntegrationTestUtils.getValidReference(mockMvc);

        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/search/get-document-content")
                .param("reference", reference)
                .param("database", mvcIntegrationTestUtils.getDatabases()[0])
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(nullValue())));
    }
}
