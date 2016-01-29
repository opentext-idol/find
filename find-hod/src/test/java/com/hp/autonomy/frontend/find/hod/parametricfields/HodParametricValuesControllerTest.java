/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.frontend.find.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.frontend.find.hod.test.HodUnitTestUtils;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationCapable;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.when;

public class HodParametricValuesControllerTest extends AbstractParametricValuesControllerTest<HodParametricRequest, ResourceIdentifier, HodErrorException> {
    private static SecurityContext existingSecurityContext;

    @BeforeClass
    public static void init() {
        existingSecurityContext = SecurityContextHolder.getContext();
        HodUnitTestUtils.mockSpringSecurityContext();
    }

    @AfterClass
    public static void destroy() {
        SecurityContextHolder.setContext(existingSecurityContext);
    }

    @Mock
    private ConfigService<? extends QueryManipulationCapable> configService;

    @Mock
    private QueryManipulationCapable config;

    @Before
    public void setUp() {
        parametricValuesController = new HodParametricValuesController(parametricValuesService, configService, new HodQueryRestrictionsBuilder());

        when(config.getQueryManipulation()).thenReturn(new QueryManipulationConfig("SomeProfile", "SomeIndex"));
        when(configService.getConfig()).thenReturn(config);
    }
}
