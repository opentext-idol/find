/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class AbstractParametricValuesServiceIT extends AbstractFindIT {
    @Test
    public void getParametricValues() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_VALUES_PATH)
                .param(ParametricValuesController.FIELD_NAMES_PARAM, mvcIntegrationTestUtils.getFields(mockMvc, FieldsController.GET_PARAMETRIC_FIELDS_PATH))
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
    public void getDateParametricValues() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_VALUES_PATH + ParametricValuesController.BUCKET_PARAMETRIC_PATH)
                .param(ParametricValuesController.FIELD_NAMES_PARAM, ParametricValuesService.AUTN_DATE_FIELD)
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
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
    public void getDateParametricValuesForField() throws Exception {
        final String url = ParametricValuesController.PARAMETRIC_VALUES_PATH + ParametricValuesController.BUCKET_PARAMETRIC_PATH + '/' + ParametricValuesService.AUTN_DATE_FIELD;

        final MockHttpServletRequestBuilder requestBuilder = get(url)
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.FIELD_TEXT_PARAM, "")
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
    public void getDependentParametricValues() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_VALUES_PATH + ParametricValuesController.DEPENDENT_VALUES_PATH)
                .param(ParametricValuesController.FIELD_NAMES_PARAM, mvcIntegrationTestUtils.getFields(mockMvc, FieldsController.GET_PARAMETRIC_FIELDS_PATH))
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.FIELD_TEXT_PARAM, "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }
}
