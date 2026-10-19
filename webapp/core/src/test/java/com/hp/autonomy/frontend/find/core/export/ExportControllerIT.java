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
package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class ExportControllerIT extends AbstractFindIT {
    private static final String TOPIC_MAP_DATA = "/com/hp/autonomy/frontend/find/core/export/topic-map-data.json";
    private static final String SUNBURST_DATA = "/com/hp/autonomy/frontend/find/core/export/sunburst-data.json";
    private static final String MAP_DATA = "/com/hp/autonomy/frontend/find/core/export/map-data.json";
    private static final String LIST_DATA = "/com/hp/autonomy/frontend/find/core/export/list-data.json";
    private static final String DATE_GRAPH_DATA = "/com/hp/autonomy/frontend/find/core/export/date-graph-data.json";
    private static final String REPORT_DATA = "/com/hp/autonomy/frontend/find/core/export/report-data.json";

    @Test
    public void exportAsCsv() throws Exception {
        final String json = "{\"queryRestrictions\": {" +
                "\"text\": \"*\", \"indexes\": " + mvcIntegrationTestUtils.getDatabasesAsJson() +
                "}," +
                "\"max_results\": 5," +
                "\"summary\": \"off\"}";

        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/csv").with(authentication(biAuth()));
        requestBuilder.param("queryRequest", json);
        final String selectedField1 = "dateCreated";
        requestBuilder.param("selectedFieldIds", selectedField1);
        final String selectedField2 = "WEIGHT";
        requestBuilder.param("selectedFieldIds", selectedField2);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.CSV.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportTopicMapToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/pptx/topic-map").with(authentication(biAuth()));
        requestBuilder.param("data", getData(TOPIC_MAP_DATA));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportSunburstToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/pptx/sunburst").with(authentication(biAuth()));
        requestBuilder.param("data", getData(SUNBURST_DATA));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Ignore
    @Test
    public void exportTableToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/pptx/table").with(authentication(biAuth()));
        //TODO determine good test data
        requestBuilder.param("data", "{}");
        requestBuilder.param("title", "");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportMapToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/pptx/map").with(authentication(biAuth()));
        requestBuilder.param("data", getData(MAP_DATA));
        requestBuilder.param("title", "Test");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportListToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/pptx/list").with(authentication(biAuth()));
        requestBuilder.param("data", getData(LIST_DATA));
        requestBuilder.param("results", "");
        requestBuilder.param("sortBy", "");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportDateGraphToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/pptx/date-graph").with(authentication(biAuth()));
        requestBuilder.param("data", getData(DATE_GRAPH_DATA));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportReportToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/pptx/report").with(authentication(biAuth()));
        requestBuilder.param("data", getData(REPORT_DATA));
        requestBuilder.param("multiPage", "false");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportReportToPptxMultiPage() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post("/api/bi/export/pptx/report").with(authentication(biAuth()));
        requestBuilder.param("data", getData(REPORT_DATA));
        requestBuilder.param("multiPage", "true");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    private String getData(final String resource) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(resource));
    }
}
