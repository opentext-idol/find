/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ServerConfig;
import com.hp.autonomy.searchcomponents.idol.configuration.QueryManipulation;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdolFindConfigTest {
    @Mock
    private ServerConfig serverConfig;

    @Mock
    private CommunityAuthentication communityAuthentication;

    @Mock
    private QueryManipulation queryManipulation;

    @Mock
    private ViewConfig viewConfig;

    private IdolFindConfig idolFindConfig;

    @Before
    public void setUp() {
        idolFindConfig = new IdolFindConfig.Builder()
                .setContent(serverConfig)
                .setLogin(communityAuthentication)
                .setQueryManipulation(queryManipulation)
                .setView(viewConfig)
                .build();
    }

    @Test
    public void validateGoodConfig() throws ConfigException {
        idolFindConfig.basicValidate();
    }

    @Test(expected = ConfigException.class)
    public void validateBadConfig() throws ConfigException {
        doThrow(new ConfigException("QMS", "Bad Config")).when(queryManipulation).basicValidate();
        idolFindConfig.basicValidate();
    }

    @Test
    public void merge() {
        when(serverConfig.merge(any(ServerConfig.class))).thenReturn(serverConfig);
        when(communityAuthentication.merge(any(CommunityAuthentication.class))).thenReturn(communityAuthentication);
        when(queryManipulation.merge(any(QueryManipulation.class))).thenReturn(queryManipulation);
        when(viewConfig.merge(any(ViewConfig.class))).thenReturn(viewConfig);

        final IdolFindConfig defaults = new IdolFindConfig.Builder().setContent(mock(ServerConfig.class)).build();
        final IdolFindConfig mergedConfig = idolFindConfig.merge(defaults);
        assertEquals(serverConfig, mergedConfig.getContent());
        assertEquals(communityAuthentication, mergedConfig.getLogin());
        assertEquals(queryManipulation, mergedConfig.getQueryManipulation());
        assertEquals(viewConfig, mergedConfig.getViewConfig());
        assertEquals(idolFindConfig, mergedConfig);
    }

    @Test
    public void mergeWithNoDefaults() {
        assertNotNull(idolFindConfig.merge(null));
    }

    @Test
    public void getCommunityDetails() {
        final ServerConfig community = mock(ServerConfig.class);
        when(community.toAciServerDetails()).thenReturn(mock(AciServerDetails.class));
        when(communityAuthentication.getCommunity()).thenReturn(community);
        assertNotNull(idolFindConfig.getCommunityDetails());
    }

    @Test
    public void getAuthentication() {
        assertEquals(communityAuthentication, idolFindConfig.getAuthentication());
    }

    @Test
    public void withoutDefaultLogin() {
        idolFindConfig.withoutDefaultLogin();
        verify(communityAuthentication).withoutDefaultLogin();
    }

    @Test
    public void generateDefaultLogin() {
        idolFindConfig.generateDefaultLogin();
        verify(communityAuthentication).generateDefaultLogin();
    }

    @Test
    public void withHashedPasswords() {
        assertEquals(idolFindConfig, idolFindConfig.withHashedPasswords());
    }
}
