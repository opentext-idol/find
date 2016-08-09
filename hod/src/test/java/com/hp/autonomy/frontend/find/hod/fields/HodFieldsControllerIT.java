/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.frontend.find.core.fields.FieldsControllerIT;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.empty;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HodFieldsControllerIT extends FieldsControllerIT {
    @Override
    protected void addParams(final MockHttpServletRequestBuilder requestBuilder) {
        requestBuilder.param("databases", mvcIntegrationTestUtils.getDatabases());
    }

    @Override
    public void getParametricDateFields() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(FieldsController.FIELDS_PATH + FieldsController.GET_PARAMETRIC_DATE_FIELDS_PATH).with(authentication(userAuth()));
        addParams(requestBuilder);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", empty()));
    }
}
