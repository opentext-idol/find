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

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class FieldsControllerIT extends AbstractFindIT {
    protected abstract void addParams(MockHttpServletRequestBuilder requestBuilder);

    @Test
    public void getParametricFields() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(FieldsController.FIELDS_PATH + FieldsController.GET_PARAMETRIC_FIELDS_PATH).with(authentication(userAuth()));
        requestBuilder.param(FieldsController.FIELD_TYPES_PARAM, FieldTypeParam.Parametric.name(), FieldTypeParam.Numeric.name(), FieldTypeParam.NumericDate.name());
        addParams(requestBuilder);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }
}
