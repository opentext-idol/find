/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.io.Serializable;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class AbstractComparisonServiceIT<S extends Serializable, R extends SearchResult, E extends Exception> extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected DocumentsService<S, R, E> documentsService;

    protected ObjectMapper mapper = new ObjectMapper();

    protected String twoDocStateToken;
    protected String sixDocStateToken;

    protected abstract QueryRestrictions<S> buildQueryRestrictions();

    @Before
    public void createStateTokens() throws E {
        final QueryRestrictions<S> queryRestrictions = buildQueryRestrictions();

        twoDocStateToken = documentsService.getStateToken(queryRestrictions, 2);
        sixDocStateToken = documentsService.getStateToken(queryRestrictions, 6);
    }

    @Test
    public void compareQueryStateTokens() throws Exception {
        final ComparisonRequest<S> comparisonRequest = new ComparisonRequest.Builder<S>()
                .setFirstQueryStateToken(twoDocStateToken)
                .setSecondQueryStateToken(sixDocStateToken)
                .build();

        mockMvc.perform(post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", isEmptyOrNullString()))
                    .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", not(isEmptyOrNullString())));
    }

    @Test
    public void compareRestrictionsAndToken() throws Exception {
        final ComparisonRequest<S> comparisonRequest = new ComparisonRequest.Builder<S>()
                .setFirstRestrictions(buildQueryRestrictions())
                .setSecondQueryStateToken(sixDocStateToken)
                .build();

        mockMvc.perform(post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", isEmptyOrNullString()));
    }

    @Test
    public void compareTokenAndRestrictions() throws Exception {
        final ComparisonRequest<S> comparisonRequest = new ComparisonRequest.Builder<S>()
                .setFirstQueryStateToken(twoDocStateToken)
                .setSecondRestrictions(buildQueryRestrictions())
                .build();

        mockMvc.perform(post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", isEmptyOrNullString()))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", not(isEmptyOrNullString())));
    }

    @Test
    public void compareRestrictions() throws Exception {
        final ComparisonRequest<S> comparisonRequest = new ComparisonRequest.Builder<S>()
                .setFirstRestrictions(buildQueryRestrictions())
                .setSecondRestrictions(buildQueryRestrictions())
                .build();

        mockMvc.perform(post(ComparisonController.BASE_PATH + '/' + ComparisonController.COMPARE_PATH + '/')
                .content(mapper.writeValueAsString(comparisonRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documentsOnlyInFirstStateToken", isEmptyOrNullString()))
                .andExpect(jsonPath("$.documentsOnlyInSecondStateToken", isEmptyOrNullString()));
    }

    @Test
    public void getResults() throws Exception {
        final String[] stateMatchIds = {sixDocStateToken};
        final String[] stateDontMatchIds = {twoDocStateToken};

        mockMvc.perform(get(ComparisonController.BASE_PATH + '/' + ComparisonController.RESULTS_PATH + '/')
                .param(ComparisonController.STATE_MATCH_PARAM, stateMatchIds)
                .param(ComparisonController.STATE_DONT_MATCH_PARAM, stateDontMatchIds)
                .param(ComparisonController.RESULTS_START_PARAM, "1")
                .param(ComparisonController.MAX_RESULTS_PARAM, "6")
                .param(ComparisonController.SUMMARY_PARAM, "context")
                .param(ComparisonController.SORT_PARAM, "relevance")
                .param(ComparisonController.HIGHLIGHT_PARAM, "false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.documents", hasSize(4)));
    }
}
