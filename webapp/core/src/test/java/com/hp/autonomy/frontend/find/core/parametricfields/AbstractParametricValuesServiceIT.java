/*
 * Copyright 2015-2017 Open Text.
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

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("ProhibitedExceptionDeclared")
public abstract class AbstractParametricValuesServiceIT extends AbstractFindIT {
    @Test
    public void getParametricValues() throws Exception {
        mockMvc.perform(parametricValuesRequest())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.parametricValues", not(empty())));
    }

    @Test
    public void getNumericParametricValuesInBuckets() throws Exception {
        final String[] fields = mvcIntegrationTestUtils.getFields(mockMvc, "/parametric", FieldTypeParam.Numeric.name());

        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/parametric/numeric/buckets/" + fields[0])
                .param("databases", mvcIntegrationTestUtils.getDatabases())
                .param("queryText", "*")
                .param("targetNumberOfBuckets", "35")
                .param("bucketMin", "0")
                .param("bucketMax", String.valueOf(Integer.MAX_VALUE))
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void getDateParametricValuesInBuckets() throws Exception {
        // Note: this requires content 11.4 GA or above due to changes in parametric range support.
        final String url = "/api/public/parametric/date/buckets/" + ParametricValuesService.AUTN_DATE_FIELD;

        final MockHttpServletRequestBuilder requestBuilder = get(url)
                .param("databases", mvcIntegrationTestUtils.getDatabases())
                .param("queryText", "*")
                .param("targetNumberOfBuckets", "35")
                .param("bucketMin", "1970-01-01T00:00:00Z")
                .param("bucketMax", "2038-01-01T00:00:00Z")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void getDependentParametricValues() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(parametricValuesRequest()).andReturn();
        final byte[] contentBytes = mvcResult.getResponse().getContentAsByteArray();
        final JsonNode contentTree = new ObjectMapper().readTree(contentBytes);

        final List<String> fields = new LinkedList<>();

        // Only ask for dependent parametric values in fields which have values
        for (final JsonNode fieldNode : contentTree.get("parametricValues")) {
            if (fieldNode.get("totalValues").asInt() > 0) {
                fields.add(fieldNode.get("id").asText());
            }
        }

        if (fields.isEmpty()) {
            throw new IllegalStateException("No parametric fields have values");
        }

        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/parametric/dependent-values")
                .param("fieldNames", fields.toArray(new String[]{}))
                .param("databases", mvcIntegrationTestUtils.getDatabases())
                .param("queryText", "*")
                .param("fieldText", "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fields", not(empty())));
    }

    @Test
    public void getNumericValueDetails() throws Exception {
        final String[] fields = mvcIntegrationTestUtils.getFields(mockMvc, "/parametric", FieldTypeParam.Numeric.name());

        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/parametric/numeric/value-details")
                .param("fieldName", fields[0])
                .param("databases", mvcIntegrationTestUtils.getDatabases())
                .param("queryText", "*")
                .param("fieldText", "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void getDateValueDetails() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/parametric/date/value-details")
                .param("fieldName", ParametricValuesService.AUTN_DATE_FIELD)
                .param("databases", mvcIntegrationTestUtils.getDatabases())
                .param("queryText", "*")
                .param("fieldText", "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())));
    }

    private RequestBuilder parametricValuesRequest() throws Exception {
        return get("/api/public/parametric/values")
                .param("fieldNames", mvcIntegrationTestUtils.getFields(mockMvc, "/parametric", FieldTypeParam.Parametric.name()))
                .param("databases", mvcIntegrationTestUtils.getDatabases())
                .param("queryText", "*")
                .param("fieldText", "")
                .with(authentication(userAuth()));
    }
}
