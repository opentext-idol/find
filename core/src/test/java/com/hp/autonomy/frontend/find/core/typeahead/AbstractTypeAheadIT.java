/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.typeahead;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractTypeAheadIT extends AbstractFindIT {
    private static final TypeReference<List<String>> RESPONSE_TYPE = new TypeReference<List<String>>() {};

    private final String inputText;
    private final String expectedSuggestion;
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected AbstractTypeAheadIT(final String inputText, final String expectedSuggestion) {
        this.inputText = inputText;
        this.expectedSuggestion = expectedSuggestion;
    }

    @Test
    public void getsSuggestions() throws Exception {
        final RequestBuilder request = get(TypeAheadController.URL).param(TypeAheadController.TEXT_PARAMETER, inputText);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(final MvcResult result) throws Exception {
                        final List<String> output = objectMapper.readValue(result.getResponse().getContentAsString(), RESPONSE_TYPE);
                        assertThat(output, hasItem(expectedSuggestion));
                    }
                });
    }
}
