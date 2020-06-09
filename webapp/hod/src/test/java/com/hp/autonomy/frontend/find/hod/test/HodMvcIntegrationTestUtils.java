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

package com.hp.autonomy.frontend.find.hod.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.sso.HodApplicationGrantedAuthority;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import com.hp.autonomy.searchcomponents.hod.test.HodTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collection;
import java.util.HashSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
@ConditionalOnProperty(value = "mock.configuration", matchIfMissing = true)
public class HodMvcIntegrationTestUtils extends MvcIntegrationTestUtils {
    private final HodAuthenticationPrincipal testPrincipal;
    private final TokenProxy<EntityType.Application, TokenType.Simple> testTokenProxy;
    private final Environment environment;

    @Autowired
    public HodMvcIntegrationTestUtils(final HodAuthenticationPrincipal testPrincipal, final TokenProxy<EntityType.Application, TokenType.Simple> testTokenProxy, final Environment environment) {
        this.testPrincipal = testPrincipal;
        this.testTokenProxy = testTokenProxy;
        this.environment = environment;
    }

    @Override
    public String[] getDatabases() {
        return new String[]{ResourceName.WIKI_ENG.toString()};
    }

    @Override
    public String getDatabasesAsJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(new ResourceName[]{ResourceName.WIKI_ENG});
    }

    @Override
    public EmbeddableIndex getEmbeddableIndex() {
        return new EmbeddableIndex(ResourceName.WIKI_ENG.getName(), ResourceName.WIKI_ENG.getDomain());
    }

    @Override
    protected Authentication createAuthentication(final Collection<GrantedAuthority> baseAuthorities) {
        final ResourceName application = new ResourceName(
                environment.getProperty(HodTestConfiguration.DOMAIN_PROPERTY),
                environment.getProperty(HodTestConfiguration.APPLICATION_PROPERTY)
        );

        final Collection<GrantedAuthority> hodAuthorities = new HashSet<>(baseAuthorities);
        hodAuthorities.add(new HodApplicationGrantedAuthority(application));

        @SuppressWarnings("unchecked") final HodAuthentication<EntityType.Application> authentication = mock(HodAuthentication.class);
        when(authentication.getPrincipal()).thenReturn(testPrincipal);
        when(authentication.getTokenProxy()).thenReturn(testTokenProxy);
        when(authentication.getAuthorities()).thenReturn(hodAuthorities);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user");
        return authentication;
    }

    @Override
    protected void addFieldRequestParams(final MockHttpServletRequestBuilder requestBuilder) {
        requestBuilder.param("databases", getDatabases());
    }
}
