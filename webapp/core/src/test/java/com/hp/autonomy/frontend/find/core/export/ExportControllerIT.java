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

        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.CSV_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.QUERY_REQUEST_PARAM, json);
        final String selectedField1 = "dateCreated";
        requestBuilder.param(ExportController.SELECTED_EXPORT_FIELDS_PARAM, selectedField1);
        final String selectedField2 = "WEIGHT";
        requestBuilder.param(ExportController.SELECTED_EXPORT_FIELDS_PARAM, selectedField2);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.CSV.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportTopicMapToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.PPTX_PATH + ExportController.TOPIC_MAP_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.DATA_PARAM, getData(TOPIC_MAP_DATA));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportSunburstToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.PPTX_PATH + ExportController.SUNBURST_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.DATA_PARAM, getData(SUNBURST_DATA));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Ignore
    @Test
    public void exportTableToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.PPTX_PATH + ExportController.TABLE_PATH).with(authentication(biAuth()));
        //TODO determine good test data
        requestBuilder.param(ExportController.DATA_PARAM, "{}");
        requestBuilder.param(ExportController.TITLE_PARAM, "");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportMapToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.PPTX_PATH + ExportController.MAP_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.DATA_PARAM, getData(MAP_DATA));
        requestBuilder.param(ExportController.TITLE_PARAM, "Test");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportListToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.PPTX_PATH + ExportController.LIST_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.DATA_PARAM, getData(LIST_DATA));
        requestBuilder.param(ExportController.RESULTS_PARAM, "");
        requestBuilder.param(ExportController.SORT_BY_PARAM, "");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportDateGraphToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.PPTX_PATH + ExportController.DATE_GRAPH_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.DATA_PARAM, getData(DATE_GRAPH_DATA));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportReportToPptx() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.PPTX_PATH + ExportController.REPORT_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.DATA_PARAM, getData(REPORT_DATA));
        requestBuilder.param(ExportController.MULTI_PAGE_PARAM, "false");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    @Test
    public void exportReportToPptxMultiPage() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = post(ExportController.EXPORT_PATH + ExportController.PPTX_PATH + ExportController.REPORT_PATH).with(authentication(biAuth()));
        requestBuilder.param(ExportController.DATA_PARAM, getData(REPORT_DATA));
        requestBuilder.param(ExportController.MULTI_PAGE_PARAM, "true");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ExportFormat.PPTX.getMimeType()))
                .andExpect(content().string(notNullValue()));
    }

    private String getData(final String resource) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(resource));
    }
}
