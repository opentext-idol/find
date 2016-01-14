/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;

import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractParametricValuesServiceIT extends AbstractFindIT {
    protected final String[] databases;
    protected final String[] fieldNames;

    protected AbstractParametricValuesServiceIT(final String[] databases, final String[] fieldNames) {
        this.databases = Arrays.copyOf(databases, databases.length);
        this.fieldNames = Arrays.copyOf(fieldNames, fieldNames.length);
    }

    @Test
    public void getParametricValues() throws Exception {
        mockMvc.perform(get(ParametricValuesController.PARAMETRIC_VALUES_PATH).param(ParametricValuesController.DATABASES_PARAM, databases).param(ParametricValuesController.FIELD_NAMES_PARAM, fieldNames).param(ParametricValuesController.QUERY_TEXT_PARAM, "*").param(ParametricValuesController.FIELD_TEXT_PARAM, ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", CoreMatchers.not(empty())));
    }
}
