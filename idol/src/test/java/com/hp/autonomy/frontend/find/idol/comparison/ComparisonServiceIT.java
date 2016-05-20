/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.comparison;


import com.autonomy.aci.client.services.AciErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.frontend.find.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class ComparisonServiceIT extends AbstractFindIT {
    private final ObjectMapper mapper = new ObjectMapper();
    private final QueryRestrictions<String> queryRestrictions = new IdolQueryRestrictionsBuilder().build("*", "", Collections.<String>emptyList(), null, null, 0, Collections.<String>emptyList(), Collections.<String>emptyList());

    @SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
    @Autowired
    private DocumentsService<String, IdolSearchResult, AciErrorException> documentsService;

    private String twoDocStateToken;
    private String sixDocStateToken;

    @Before
    public void createStateTokens() throws AciErrorException {
        twoDocStateToken = documentsService.getStateToken(queryRestrictions, 2, false);
        sixDocStateToken = documentsService.getStateToken(queryRestrictions, 6, false);
    }

    @Test
    public void basicUserNotAuthorised() throws Exception {
        final ComparisonRequest<String> comparisonRequest = new ComparisonRequest.Builder<String>()
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
        final ComparisonRequest<String> comparisonRequest = new ComparisonRequest.Builder<String>()
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
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", isEmptyOrNullString()))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", not(isEmptyOrNullString())));
    }

    @Test
    public void compareRestrictionsAndToken() throws Exception {
        final ComparisonRequest<String> comparisonRequest = new ComparisonRequest.Builder<String>()
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
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", isEmptyOrNullString()));
    }

    @Test
    public void compareTokenAndRestrictions() throws Exception {
        final ComparisonRequest<String> comparisonRequest = new ComparisonRequest.Builder<String>()
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
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", isEmptyOrNullString()))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", not(isEmptyOrNullString())));
    }

    @Test
    public void compareRestrictions() throws Exception {
        final ComparisonRequest<String> comparisonRequest = new ComparisonRequest.Builder<String>()
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
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", isEmptyOrNullString()))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", isEmptyOrNullString()));
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
                .param(ComparisonController.SORT_PARAM, "relevance")
                .param(ComparisonController.HIGHLIGHT_PARAM, "false")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(biAuth()));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", hasSize(4)));
    }
}
