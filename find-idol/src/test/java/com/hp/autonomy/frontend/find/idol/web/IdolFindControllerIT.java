/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.web.AbstractFindControllerIT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class IdolFindControllerIT extends AbstractFindControllerIT {
    private static SecurityContext existingSecurityContext;

    @BeforeClass
    public static void initLocal() {
        existingSecurityContext = SecurityContextHolder.getContext();
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterClass
    public static void destroyLocal() {
        SecurityContextHolder.setContext(existingSecurityContext);
    }
}
