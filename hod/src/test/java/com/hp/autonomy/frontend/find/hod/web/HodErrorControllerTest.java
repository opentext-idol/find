/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.AbstractErrorControllerTest;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HsodConfig;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodErrorControllerTest extends AbstractErrorControllerTest {
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
}
