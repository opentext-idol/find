/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class ExportControllerIT extends AbstractFindIT {
    @Test
    public void exportAsCsv() throws Exception {
        final String json = '{' +
                "\"queryRestrictions\": { \"text\": \"*\", \"indexes\": " + mvcIntegrationTestUtils.getDatabasesAsJson() + "}," +
                "\"max_results\": 5," +
                "\"summary\": \"off\" " +
                '}';

        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.CSV_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.POST_DATA_PARAM, json);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.CSV.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }
}
