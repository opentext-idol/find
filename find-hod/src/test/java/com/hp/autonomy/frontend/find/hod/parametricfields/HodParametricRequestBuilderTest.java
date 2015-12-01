/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.QueryManipulationConfig;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricRequestBuilderTest;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodParametricRequestBuilderTest extends AbstractParametricRequestBuilderTest<HodParametricRequest, ResourceIdentifier> {
    @Mock
    private ConfigService<HodFindConfig> configService;

    public HodParametricRequestBuilderTest() {
        super(new HodParametricRequestBuilder());
    }

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(parametricRequestBuilder, "configService", configService, ConfigService.class);

        final QueryManipulationConfig queryManipulationConfig = mock(QueryManipulationConfig.class);
        when(queryManipulationConfig.getProfile()).thenReturn("SomeProfileName");
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setQueryManipulation(queryManipulationConfig).build());

        final SecurityContext securityContext = mock(SecurityContext.class);
        final HodAuthentication authentication = mock(HodAuthentication.class);
        final HodAuthenticationPrincipal principal = mock(HodAuthenticationPrincipal.class);
        when(principal.getApplication()).thenReturn(new ResourceIdentifier("SomeDomain", "SomeName"));
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
