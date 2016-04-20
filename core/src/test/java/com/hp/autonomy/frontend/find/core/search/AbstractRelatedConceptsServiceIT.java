/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public abstract class AbstractRelatedConceptsServiceIT extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected MvcIntegrationTestUtils mvcIntegrationTestUtils;

    @Test
    public void findRelatedConcepts() throws Exception {
        final MockHttpServletRequestBuilder request = get(RelatedConceptsController.RELATED_CONCEPTS_PATH)
                .param(RelatedConceptsController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(RelatedConceptsController.QUERY_TEXT_PARAM, "*")
                .param(RelatedConceptsController.FIELD_TEXT_PARAM, "");

        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }
}
