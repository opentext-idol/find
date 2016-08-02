/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.AbstractErrorControllerTest;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HsodConfig;
import org.apache.http.HttpStatus;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodErrorControllerTest extends AbstractErrorControllerTest<HodErrorController> {
    @Mock
    private ConfigService<HodFindConfig> configService;

    @Override
    @Before
    public void setUp() throws MalformedURLException {
        errorController = new HodErrorController(controllerUtils, configService);
        super.setUp();

        final HsodConfig hsodConfig = new HsodConfig.Builder()
                .setLandingPageUrl(new URL("https://search.havenondemand.com"))
                .setFindAppUrl(new URL("https://find.havenapps.io"))
                .build();
        final HodFindConfig config = new HodFindConfig.Builder().setHsod(hsodConfig).build();
        when(configService.getConfig()).thenReturn(config);
    }

    @Test
    public void clientAuthenticationErrorPage() {
        assertNotNull(errorController.clientAuthenticationErrorPage(HttpStatus.SC_GONE, request));
        verify(controllerUtils).buildErrorModelAndView(argThat(new HasPropertyWithValue<>("mainMessageCode", is(HodErrorController.MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_MAIN))));
    }
}
