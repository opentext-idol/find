/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.view.idol.configuration.ViewConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ViewAciServiceTest {
    @Mock
    private AciServerDetails aciServerDetails;

    @Mock
    private ViewConfig viewConfig;

    @Mock
    private IdolFindConfig idolFindConfig;

    @Mock
    private ConfigService<IdolFindConfig> configService;

    @Mock
    private AciService aciService;

    private ViewAciService viewAciService;

    @Before
    public void setUp() {
        when(viewConfig.toAciServerDetails()).thenReturn(aciServerDetails);
        when(idolFindConfig.getViewConfig()).thenReturn(viewConfig);
        when(configService.getConfig()).thenReturn(idolFindConfig);
        viewAciService = new ViewAciService(aciService, configService);
    }

    @Test
    public void getServerDetails() {
        assertNotNull(viewAciService.getServerDetails());
    }
}
