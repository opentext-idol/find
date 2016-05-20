/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.test;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.test.MvcIntegrationTestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

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
    public EmbeddableIndex getEmbeddableIndex() {
        return new EmbeddableIndex("Wookiepedia", null);
    }

    @Override
    protected Authentication createAuthentication(final Collection<GrantedAuthority> authorities) {
        final CommunityPrincipal communityPrincipal = mock(CommunityPrincipal.class);
        when(communityPrincipal.getId()).thenReturn(1L);
        when(communityPrincipal.getUsername()).thenReturn("user");

        final UsernamePasswordAuthenticationToken authentication = mock(UsernamePasswordAuthenticationToken.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(communityPrincipal);
        when(authentication.getAuthorities()).thenReturn(authorities);

        return authentication;
    }
}
