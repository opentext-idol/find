/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.test;

import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class MvcIntegrationTestUtils {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(".*\"reference\"\\s*:\\s*\"(?<reference>[^\"]+)\".*");

    public abstract String[] getDatabases();
    public abstract String[] getParametricFields();
    public abstract EmbeddableIndex getEmbeddableIndex();

    public String getValidReference(final MockMvc mockMvc) throws Exception {
        final MockHttpServletRequestBuilder request = get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH).param(DocumentsController.TEXT_PARAM, "*")
                .param(DocumentsController.RESULTS_START_PARAM, "1")
                .param(DocumentsController.MAX_RESULTS_PARAM, "50")
                .param(DocumentsController.SUMMARY_PARAM, "context")
                .param(DocumentsController.INDEXES_PARAM, getDatabases());

        final MvcResult mvcResult = mockMvc.perform(request)
                .andReturn();

        final Matcher matcher = REFERENCE_PATTERN.matcher(mvcResult.getResponse().getContentAsString());

        if (matcher.find()) {
            return matcher.group("reference");
        } else {
            throw new IllegalStateException("Could not resolve valid reference for integration tests");
        }
    }
}
