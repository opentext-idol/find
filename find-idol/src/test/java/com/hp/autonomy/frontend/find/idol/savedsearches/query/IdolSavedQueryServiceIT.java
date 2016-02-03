/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.query;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.savedsearches.query.AbstractSavedQueryServiceIT;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class IdolSavedQueryServiceIT extends AbstractSavedQueryServiceIT {

    @PostConstruct
    public void setUp() {
        final UsernamePasswordAuthenticationToken authentication = mock(UsernamePasswordAuthenticationToken.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        final CommunityPrincipal communityPrincipal = mock(CommunityPrincipal.class);
        when(communityPrincipal.getId()).thenReturn(1L);
        when(communityPrincipal.getUsername()).thenReturn("user");
        when(authentication.getPrincipal()).thenReturn(communityPrincipal);

        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
