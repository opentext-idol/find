/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    @Override
    public String[] getDatabases() {
        return new String[]{"Wookiepedia"};
    }

    @Override
    public String getDatabasesAsJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(getDatabases());
    }

    @Override
    public EmbeddableIndex getEmbeddableIndex() {
        return new EmbeddableIndex("Wookiepedia", null);
    }

    @Override
    protected Authentication createAuthentication(final Collection<GrantedAuthority> authorities) {
        final CommunityPrincipal communityPrincipal = new CommunityPrincipal(1L, "user", Collections.emptyList(), null);

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
