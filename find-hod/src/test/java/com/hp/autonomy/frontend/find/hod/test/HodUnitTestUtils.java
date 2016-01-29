/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.test;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("UtilityClass")
public class HodUnitTestUtils {
    public static final String SAMPLE_DOMAIN = "SomeDomain";

    public static void mockSpringSecurityContext() {
        final SecurityContext securityContext = mock(SecurityContext.class);
        final HodAuthentication hodAuthentication = mock(HodAuthentication.class);
        final HodAuthenticationPrincipal hodAuthenticationPrincipal = mock(HodAuthenticationPrincipal.class);
        final ResourceIdentifier resourceIdentifier = mock(ResourceIdentifier.class);
        when(resourceIdentifier.getDomain()).thenReturn(SAMPLE_DOMAIN);
        when(hodAuthenticationPrincipal.getApplication()).thenReturn(resourceIdentifier);
        when(hodAuthentication.getPrincipal()).thenReturn(hodAuthenticationPrincipal);
        when(securityContext.getAuthentication()).thenReturn(hodAuthentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
