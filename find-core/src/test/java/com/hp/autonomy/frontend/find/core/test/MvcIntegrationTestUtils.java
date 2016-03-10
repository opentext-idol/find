/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.test;

import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class MvcIntegrationTestUtils {
    public abstract String[] getDatabases();

    public abstract String[] getParametricFields();

    public String getValidReference(final MockMvc mockMvc) throws Exception {
        final String[] reference = new String[1];
        mockMvc.perform(get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH).param(DocumentsController.TEXT_PARAM, "*").param(DocumentsController.RESULTS_START_PARAM, "1").param(DocumentsController.MAX_RESULTS_PARAM, "50").param(DocumentsController.SUMMARY_PARAM, "context").param(DocumentsController.INDEXES_PARAM, getDatabases()))
                .andDo(new ResultHandler() {
                    @SuppressWarnings("InnerClassTooDeeplyNested")
                    @Override
                    public void handle(final MvcResult result) throws Exception {
                        final Pattern pattern = Pattern.compile(".+\"reference\"\\s*:\\s*\"(?<reference>[^\"]+)\".+");
                        final Matcher matcher = pattern.matcher(result.getResponse().getContentAsString());
                        if (matcher.find()) {
                            reference[0] = matcher.group("reference");
                        }
                    }
                });
        return reference[0];
    }
}
