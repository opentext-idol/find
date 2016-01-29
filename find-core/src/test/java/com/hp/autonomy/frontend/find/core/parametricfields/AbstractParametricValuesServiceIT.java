/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class AbstractParametricValuesServiceIT extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected MvcIntegrationTestUtils mvcIntegrationTestUtils;

    @Test
    public void getParametricValues() throws Exception {
        mockMvc.perform(
                get(ParametricValuesController.PARAMETRIC_VALUES_PATH)
                        .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                        .param(ParametricValuesController.FIELD_NAMES_PARAM, mvcIntegrationTestUtils.getParametricFields())
                        .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                        .param(ParametricValuesController.FIELD_TEXT_PARAM, ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", CoreMatchers.not(empty())));
    }
}
