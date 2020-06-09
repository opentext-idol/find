/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.comparison;


import com.autonomy.aci.client.services.AciErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.idol.requests.IdolQueryRestrictionsMixin;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class ComparisonServiceIT extends AbstractFindIT {
    private static final String EMPTY_RESULT_SET_TOKEN = "NULL-0";
    private final ObjectMapper mapper = new ObjectMapper();

    private IdolQueryRestrictions queryRestrictions;

    @Autowired
    private IdolDocumentsService documentsService;
    @Autowired
    private IdolQueryRestrictionsBuilder idolQueryRestrictionsBuilder;

    private String twoDocStateToken;
    private String sixDocStateToken;

    @Before
    public void createStateTokens() throws AciErrorException {
        mapper.addMixIn(IdolQueryRestrictions.class, IdolQueryRestrictionsMixin.class);

        queryRestrictions = idolQueryRestrictionsBuilder
                .queryText("*")
                .fieldText("")
                .minScore(0)
                .build();
        twoDocStateToken = documentsService.getStateToken(queryRestrictions, 2, false);
        sixDocStateToken = documentsService.getStateToken(queryRestrictions, 6, false);
    }

    @Test
    public void basicUserNotAuthorised() throws Exception {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new ComparisonRequest.Builder<IdolQueryRestrictions>()
                .setFirstQueryStateToken(twoDocStateToken)
                .setSecondQueryStateToken(sixDocStateToken)
                .build();

        final MockHttpServletRequestBuilder requestBuilder = post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(userAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403));
    }

    @Test
    public void compareQueryStateTokens() throws Exception {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new ComparisonRequest.Builder<IdolQueryRestrictions>()
                .setFirstQueryStateToken(twoDocStateToken)
                .setSecondQueryStateToken(sixDocStateToken)
                .build();

        final MockHttpServletRequestBuilder requestBuilder = post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", is(EMPTY_RESULT_SET_TOKEN)))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", not(isEmptyOrNullString())));
    }

    @Test
    public void compareRestrictionsAndToken() throws Exception {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new ComparisonRequest.Builder<IdolQueryRestrictions>()
                .setFirstRestrictions(queryRestrictions)
                .setSecondQueryStateToken(sixDocStateToken)
                .build();

        final MockHttpServletRequestBuilder requestBuilder = post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", is(EMPTY_RESULT_SET_TOKEN)));
    }

    @Test
    public void compareTokenAndRestrictions() throws Exception {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new ComparisonRequest.Builder<IdolQueryRestrictions>()
                .setFirstQueryStateToken(twoDocStateToken)
                .setSecondRestrictions(queryRestrictions)
                .build();

        final MockHttpServletRequestBuilder requestBuilder = post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", is(EMPTY_RESULT_SET_TOKEN)))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", not(isEmptyOrNullString())));
    }

    @Test
    public void compareRestrictions() throws Exception {
        final ComparisonRequest<IdolQueryRestrictions> comparisonRequest = new ComparisonRequest.Builder<IdolQueryRestrictions>()
                .setFirstRestrictions(queryRestrictions)
                .setSecondRestrictions(queryRestrictions)
                .build();

        final MockHttpServletRequestBuilder requestBuilder = post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", is(EMPTY_RESULT_SET_TOKEN)))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", is(EMPTY_RESULT_SET_TOKEN)));
    }

    @Test
    public void getResults() throws Exception {
        final String[] stateMatchIds = {sixDocStateToken};
        final String[] stateDontMatchIds = {twoDocStateToken};

        final MockHttpServletRequestBuilder requestBuilder = get(ComparisonController.BASE_PATH + '/' + ComparisonController.RESULTS_PATH + '/')
                .param(ComparisonController.STATE_MATCH_PARAM, stateMatchIds)
                .param(ComparisonController.STATE_DONT_MATCH_PARAM, stateDontMatchIds)
                .param(ComparisonController.RESULTS_START_PARAM, "1")
                .param(ComparisonController.MAX_RESULTS_PARAM, "6")
                .param(ComparisonController.SUMMARY_PARAM, "context")
                .param(ComparisonController.SORT_PARAM, "Relevance")
                .param(ComparisonController.HIGHLIGHT_PARAM, "false")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", hasSize(4)));
    }
}
