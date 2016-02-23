/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsServiceIT;
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class IdolRelatedConceptsServiceIT extends AbstractRelatedConceptsServiceIT {
    @Autowired
    private DocumentsService<String, IdolSearchResult, AciErrorException> documentsService;

    @Autowired
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Test
    public void findRelatedConceptsWithStateToken() throws Exception {
        final QueryRestrictions<String> queryRestrictions = queryRestrictionsBuilder.build(
                "*",
                "",
                Arrays.asList(mvcIntegrationTestUtils.getDatabases()),
                null,
                null,
                Collections.<String>emptyList(),
                Collections.<String>emptyList()
        );

        final String stateToken = documentsService.getStateToken(queryRestrictions, Integer.MAX_VALUE);

        final MockHttpServletRequestBuilder request = get(RelatedConceptsController.RELATED_CONCEPTS_PATH)
                .param(RelatedConceptsController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(RelatedConceptsController.QUERY_TEXT_PARAM, "*")
                .param(RelatedConceptsController.FIELD_TEXT_PARAM, "")
                .param(RelatedConceptsController.STATE_TOKEN_PARAM, stateToken);

        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }
}
