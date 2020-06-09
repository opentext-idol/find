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

package com.hp.autonomy.frontend.find.idol.search;

import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsServiceIT;
import com.hp.autonomy.frontend.find.core.search.RelatedConceptsController;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class IdolRelatedConceptsServiceIT extends AbstractRelatedConceptsServiceIT {
    @Autowired
    private IdolDocumentsService documentsService;

    @Autowired
    private ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilder;

    @Test
    public void findRelatedConceptsWithStateToken() throws Exception {
        final IdolQueryRestrictions queryRestrictions = queryRestrictionsBuilder.getObject()
                .queryText("*")
                .fieldText("")
                .databases(Arrays.asList(mvcIntegrationTestUtils.getDatabases()))
                .minScore(0)
                .build();

        final String stateToken = documentsService.getStateToken(queryRestrictions, Integer.MAX_VALUE, false);

        final MockHttpServletRequestBuilder request = get(RelatedConceptsController.RELATED_CONCEPTS_PATH)
                .param(RelatedConceptsController.DATABASES_PARAM, mvcIntegrationTestUtils.getDatabases())
                .param(RelatedConceptsController.QUERY_TEXT_PARAM, "*")
                .param(RelatedConceptsController.FIELD_TEXT_PARAM, "")
                .param(RelatedConceptsController.STATE_MATCH_TOKEN_PARAM, stateToken)
                .with(authentication(userAuth()));

        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", not(empty())));
    }
}
