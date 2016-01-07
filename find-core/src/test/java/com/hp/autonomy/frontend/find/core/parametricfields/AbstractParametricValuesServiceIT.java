/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.io.Serializable;
import java.util.Arrays;

import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractParametricValuesServiceIT<R extends ParametricRequest<S>, S extends Serializable> extends AbstractFindIT {
    protected final String[] indexes;
    protected final String[] fieldNames;

    protected AbstractParametricValuesServiceIT(final String[] indexes, final String[] fieldNames) {
        this.indexes = Arrays.copyOf(indexes, indexes.length);
        this.fieldNames = Arrays.copyOf(fieldNames, fieldNames.length);
    }

    @Test
    public void getParametricValues() throws Exception {
        mockMvc.perform(get(ParametricValuesController.PARAMETRIC_VALUES_PATH).param("databases", indexes).param("fieldNames", fieldNames).param("queryText", "*").param("fieldText", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", CoreMatchers.not(empty())));
    }
}
