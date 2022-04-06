/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void getNumericParametricValuesInBuckets() throws Exception {
        final String[] fields = mvcIntegrationTestUtils.getFields(mockMvc, FieldsController.GET_PARAMETRIC_FIELDS_PATH, FieldTypeParam.Numeric.name());

        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_PATH + ParametricValuesController.NUMERIC_PATH + ParametricValuesController.BUCKET_PARAMETRIC_PATH + '/' + fields[0])
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.TARGET_NUMBER_OF_BUCKETS_PARAM, "35")
                .param(ParametricValuesController.BUCKET_MIN_PARAM, "0")
                .param(ParametricValuesController.BUCKET_MAX_PARAM, String.valueOf(Integer.MAX_VALUE))
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void getDateParametricValuesInBuckets() throws Exception {
        // Note: this requires content 11.4 GA or above due to changes in parametric range support.
        final String url = ParametricValuesController.PARAMETRIC_PATH + ParametricValuesController.DATE_PATH + ParametricValuesController.BUCKET_PARAMETRIC_PATH + '/' + ParametricValuesService.AUTN_DATE_FIELD;

        final MockHttpServletRequestBuilder requestBuilder = get(url)
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.TARGET_NUMBER_OF_BUCKETS_PARAM, "35")
                .param(ParametricValuesController.BUCKET_MIN_PARAM, "1970-01-01T00:00:00Z")
                .param(ParametricValuesController.BUCKET_MAX_PARAM, "2038-01-01T00:00:00Z")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void getDependentParametricValues() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(parametricValuesRequest()).andReturn();
        final byte[] contentBytes = mvcResult.getResponse().getContentAsByteArray();
        final JsonNode contentTree = new ObjectMapper().readTree(contentBytes);

        final List<String> fields = new LinkedList<>();

        // Only ask for dependent parametric values in fields which have values
        for (final JsonNode fieldNode : contentTree) {
            if (fieldNode.get("totalValues").asInt() > 0) {
                fields.add(fieldNode.get("id").asText());
            }
        }

        if (fields.isEmpty()) {
            throw new IllegalStateException("No parametric fields have values");
        }

        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_PATH + ParametricValuesController.DEPENDENT_VALUES_PATH)
                .param(ParametricValuesController.FIELD_NAMES_PARAM, fields.toArray(new String[]{}))
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.FIELD_TEXT_PARAM, "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void getNumericValueDetails() throws Exception {
        final String[] fields = mvcIntegrationTestUtils.getFields(mockMvc, FieldsController.GET_PARAMETRIC_FIELDS_PATH, FieldTypeParam.Numeric.name());

        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_PATH + ParametricValuesController.NUMERIC_PATH + ParametricValuesController.VALUE_DETAILS_PATH)
                .param(ParametricValuesController.FIELD_NAME_PARAM, fields[0])
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.FIELD_TEXT_PARAM, "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void getDateValueDetails() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_PATH + ParametricValuesController.DATE_PATH + ParametricValuesController.VALUE_DETAILS_PATH)
                .param(ParametricValuesController.FIELD_NAME_PARAM, ParametricValuesService.AUTN_DATE_FIELD)
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.FIELD_TEXT_PARAM, "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }

    private RequestBuilder parametricValuesRequest() throws Exception {
        return get(ParametricValuesController.PARAMETRIC_PATH + ParametricValuesController.VALUES_PATH)
                .param(ParametricValuesController.FIELD_NAMES_PARAM, mvcIntegrationTestUtils.getFields(mockMvc, FieldsController.GET_PARAMETRIC_FIELDS_PATH, FieldTypeParam.Parametric.name()))
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.FIELD_TEXT_PARAM, "")
                .with(authentication(userAuth()));
    }
}
