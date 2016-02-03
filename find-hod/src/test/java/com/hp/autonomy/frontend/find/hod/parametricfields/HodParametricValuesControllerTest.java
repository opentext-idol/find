/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.frontend.find.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationCapable;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class HodParametricValuesControllerTest extends AbstractParametricValuesControllerTest<HodParametricRequest, ResourceIdentifier, HodErrorException> {
    @Mock
    private ConfigService<? extends QueryManipulationCapable> configService;

    @Mock
    private AuthenticationInformationRetriever<HodAuthentication> authenticationInformationRetriever;

    @Mock
    private HodAuthentication hodAuthentication;

    @Mock
    private HodAuthenticationPrincipal hodAuthenticationPrincipal;

    @Mock
    private QueryManipulationCapable config;

    @Before
    public void setUp() {
        parametricValuesController = new HodParametricValuesController(parametricValuesService, configService, new HodQueryRestrictionsBuilder(), authenticationInformationRetriever);

        when(config.getQueryManipulation()).thenReturn(new QueryManipulationConfig("SomeProfile", "SomeIndex"));
        when(configService.getConfig()).thenReturn(config);

        when(hodAuthenticationPrincipal.getApplication()).thenReturn(new ResourceIdentifier("SomeDomain", "SomeIndex"));
        when(hodAuthentication.getPrincipal()).thenReturn(hodAuthenticationPrincipal);
        when(authenticationInformationRetriever.getAuthentication()).thenReturn(hodAuthentication);
    }
}
