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

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesServiceIT;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HodParametricValuesServiceIT extends AbstractParametricValuesServiceIT {
    @Override
    public void getDependentParametricValues() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(ParametricValuesController.PARAMETRIC_PATH + ParametricValuesController.DEPENDENT_VALUES_PATH)
                .param(ParametricValuesController.FIELD_NAMES_PARAM, mvcIntegrationTestUtils.getFields(mockMvc, FieldsController.GET_PARAMETRIC_FIELDS_PATH))
                .param(ParametricValuesController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(ParametricValuesController.QUERY_TEXT_PARAM, "*")
                .param(ParametricValuesController.FIELD_TEXT_PARAM, "")
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isInternalServerError()) // not implemented yt
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }
}
