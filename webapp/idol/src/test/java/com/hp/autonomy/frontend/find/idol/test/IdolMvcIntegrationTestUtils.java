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

package com.hp.autonomy.frontend.find.idol.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import com.hp.autonomy.searchcomponents.idol.test.IdolTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
@ConditionalOnProperty(value = "mock.configuration", matchIfMissing = true)
public class IdolMvcIntegrationTestUtils extends MvcIntegrationTestUtils {
    private final Environment environment;

    @Autowired
    public IdolMvcIntegrationTestUtils(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public String[] getDatabases() {
        final String testDatabase = environment.getProperty(IdolTestUtils.TEST_DATABASE_PROPERTY, IdolTestUtils.DEFAULT_TEST_DATABASE);
        return new String[]{testDatabase};
    }

    @Override
    public String getDatabasesAsJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(getDatabases());
    }

    @Override
    public EmbeddableIndex getEmbeddableIndex() {
        return new EmbeddableIndex(getDatabases()[0], null);
    }

    @Override
    protected Authentication createAuthentication(final Collection<GrantedAuthority> authorities) {
        final CommunityPrincipal communityPrincipal = new CommunityPrincipal(1L, "user", null, Collections.emptySet(), null);

        final UsernamePasswordAuthenticationToken authentication = mock(UsernamePasswordAuthenticationToken.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(communityPrincipal);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(authentication.getName()).thenReturn("user");

        return authentication;
    }

    @Override
    protected void addFieldRequestParams(final MockHttpServletRequestBuilder requestBuilder) {
    }
}
