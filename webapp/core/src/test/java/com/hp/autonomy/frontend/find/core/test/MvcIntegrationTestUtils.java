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

package com.hp.autonomy.frontend.find.core.test;

import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.jayway.jsonpath.JsonPath;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.core.JacksonException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public abstract class MvcIntegrationTestUtils {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(".*\"reference\"\\s*:\\s*\"(?<reference>[^\"]+)\".*");

    public abstract String[] getDatabases();

    public abstract String getDatabasesAsJson() throws JacksonException;

    public abstract EmbeddableIndex getEmbeddableIndex();

    protected abstract Authentication createAuthentication(Collection<GrantedAuthority> authorities);

    protected abstract void addFieldRequestParams(MockHttpServletRequestBuilder requestBuilder);

    public String getValidReference(final MockMvc mockMvc) throws Exception {
        final MockHttpServletRequestBuilder request = get("/api/public/search/query-text-index/results").param("text", "*")
                .param("start", "1")
                .param("max_results", "50")
                .param("summary", "context")
                .param("indexes", getDatabases())
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

    public String[] getFields(final MockMvc mockMvc, final String subPath, final String... fieldTypes) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get("/api/public/fields" + subPath)
                .with(authentication(userAuth()));
        requestBuilder.param("fieldTypes", fieldTypes);
        addFieldRequestParams(requestBuilder);

        final MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andReturn();
        final Collection<Map<String, String>> tagNames = JsonPath.compile("$.fields").read(mvcResult.getResponse().getContentAsString());
        return tagNames.stream().map(tagName -> tagName.get("id")).toArray(String[]::new);
    }

    public Authentication userAuth() {
        final Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(FindRole.USER.toString()));
        return createAuthentication(authorities);
    }

    Authentication adminAuth() {
        final Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(FindRole.ADMIN.toString()));
        authorities.add(new SimpleGrantedAuthority(FindRole.USER.toString()));
        return createAuthentication(authorities);
    }

    Authentication configAuth() {
        final Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(FindRole.CONFIG.toString()));
        return createAuthentication(authorities);
    }

    Authentication biAuth() {
        final Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(FindRole.BI.toString()));
        authorities.add(new SimpleGrantedAuthority(FindRole.USER.toString()));
        return createAuthentication(authorities);
    }
}
