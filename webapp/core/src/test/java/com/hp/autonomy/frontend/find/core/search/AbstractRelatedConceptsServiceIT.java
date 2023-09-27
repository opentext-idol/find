/*
 * Copyright 2015 Open Text.
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

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractRelatedConceptsServiceIT extends AbstractFindIT {
    @Test
    public void findRelatedConcepts() throws Exception {
        final MockHttpServletRequestBuilder request = get(RelatedConceptsController.RELATED_CONCEPTS_PATH)
                .param(RelatedConceptsController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(RelatedConceptsController.QUERY_TEXT_PARAM, "*")
                .param(RelatedConceptsController.FIELD_TEXT_PARAM, "")
                .with(authentication(userAuth()));

        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())));
    }
}
