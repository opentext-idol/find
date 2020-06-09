/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.AbstractErrorControllerTest;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HsodConfig;
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

        final HsodConfig hsodConfig = HsodConfig.builder()
                .landingPageUrl(new URL("https://search.havenondemand.com"))
                .build();

        final HodFindConfig config = HodFindConfig.builder().hsod(hsodConfig).build();
        when(configService.getConfig()).thenReturn(config);
    }

    @Test
    public void clientAuthenticationErrorPage() {
        assertNotNull(errorController.clientAuthenticationErrorPage(request));
        verify(controllerUtils).buildErrorModelAndView(argThat(new HasPropertyWithValue<>("mainMessageCode", is(HodErrorController.MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_MAIN))));
    }
}
