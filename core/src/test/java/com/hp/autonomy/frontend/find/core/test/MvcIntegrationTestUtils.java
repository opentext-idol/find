/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class MvcIntegrationTestUtils {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(".*\"reference\"\\s*:\\s*\"(?<reference>[^\"]+)\".*");

    public abstract String[] getDatabases();

    public abstract EmbeddableIndex getEmbeddableIndex();

    protected abstract Authentication createAuthentication(Collection<GrantedAuthority> authorities);

    protected abstract void addFieldRequestParams(MockHttpServletRequestBuilder requestBuilder);

    public String getValidReference(final MockMvc mockMvc) throws Exception {
        final MockHttpServletRequestBuilder request = get(DocumentsController.SEARCH_PATH + '/' + DocumentsController.QUERY_PATH).param(DocumentsController.TEXT_PARAM, "*")
                .param(DocumentsController.RESULTS_START_PARAM, "1")
                .param(DocumentsController.MAX_RESULTS_PARAM, "50")
                .param(DocumentsController.SUMMARY_PARAM, "context")
                .param(DocumentsController.INDEXES_PARAM, getDatabases())
                .with(authentication(userAuth()));

        final MvcResult mvcResult = mockMvc.perform(request)
                .andReturn();

        final Matcher matcher = REFERENCE_PATTERN.matcher(mvcResult.getResponse().getContentAsString());

        if (matcher.find()) {
            return matcher.group("reference");
        } else {
            throw new IllegalStateException("Could not resolve valid reference for integration tests");
        }
    }

    public String[] getFields(final MockMvc mockMvc, final String subPath) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(FieldsController.FIELDS_PATH + subPath)
                .with(authentication(userAuth()));
        addFieldRequestParams(requestBuilder);

        final MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andReturn();
        return new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), String[].class);
    }

    public Authentication userAuth() {
        final Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(FindRole.USER.toString()));
        return createAuthentication(authorities);
    }

    public Authentication adminAuth() {
        final Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(FindRole.ADMIN.toString()));
        authorities.add(new SimpleGrantedAuthority(FindRole.USER.toString()));
        return createAuthentication(authorities);
    }

    public Authentication biAuth() {
        final Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(FindRole.BI.toString()));
        authorities.add(new SimpleGrantedAuthority(FindRole.USER.toString()));
        return createAuthentication(authorities);
    }
}
