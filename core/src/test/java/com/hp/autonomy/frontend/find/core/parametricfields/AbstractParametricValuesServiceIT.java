/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.empty;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class AbstractParametricValuesServiceIT extends AbstractFindIT {
    @Test
    public void getParametricValues() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_VALUES_PATH)
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.FIELD_TEXT_PARAM, "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", CoreMatchers.not(empty())));
    }
}
