/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.ServerConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QmsAciServiceTest {
    @Mock
    private AciServerDetails aciServerDetails;

    @Mock
    private ServerConfig serverConfig;

    @Mock
    private QueryManipulation queryManipulation;

    @Mock
    private IdolFindConfig idolFindConfig;

    @Mock
    private ConfigService<IdolFindConfig> configService;

    @Mock
    private AciService aciService;

    private QmsAciService qmsAciService;

    @Before
    public void setUp() {
        when(serverConfig.toAciServerDetails()).thenReturn(aciServerDetails);
        when(queryManipulation.isEnabled()).thenReturn(true);
        when(queryManipulation.getServer()).thenReturn(serverConfig);
        when(idolFindConfig.getQueryManipulation()).thenReturn(queryManipulation);
        when(configService.getConfig()).thenReturn(idolFindConfig);
        qmsAciService = new QmsAciService(aciService, configService);
    }

    @Test
    public void getServerDetails() {
        assertNotNull(qmsAciService.getServerDetails());
    }

    @Test
    public void enabled() {
        assertTrue(qmsAciService.isEnabled());
    }

    @Test
    public void configNotProvided() {
        when(idolFindConfig.getQueryManipulation()).thenReturn(null);
        assertFalse(qmsAciService.isEnabled());
    }

    @Test
    public void disabled() {
        when(queryManipulation.isEnabled()).thenReturn(false);
        assertFalse(qmsAciService.isEnabled());
    }
}
