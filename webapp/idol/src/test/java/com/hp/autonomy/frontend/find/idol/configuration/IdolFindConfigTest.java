/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.server.ProductType;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.core.configuration.SavedSearchConfig;
import com.hp.autonomy.frontend.find.core.configuration.TrendingConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.export.ExportConfig;
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
    private ExportConfig export;

    @Mock
    private QueryManipulation queryManipulation;

    @Mock
    private SavedSearchConfig savedSearchConfig;

    @Mock
    private ViewConfig viewConfig;

    @Mock
    private TrendingConfiguration trending;

    private IdolFindConfig idolFindConfig;

    @Before
    public void setUp() {
        idolFindConfig = IdolFindConfig.builder()
                .content(serverConfig)
                .login(communityAuthentication)
                .queryManipulation(queryManipulation)
                .savedSearchConfig(savedSearchConfig)
                .trending(trending)
                .view(viewConfig)
                .export(export)
                .build();
    }

    @Test
    public void validateGoodConfig() throws ConfigException {
        idolFindConfig.basicValidate(null);
    }

    @Test(expected = ConfigException.class)
    public void validateBadConfig() throws ConfigException {
        doThrow(new ConfigException("QMS", "Bad Config")).when(queryManipulation).basicValidate(anyString());
        idolFindConfig.basicValidate(null);
    }

    @Test
    public void merge() {
        when(serverConfig.merge(any(ServerConfig.class))).thenReturn(serverConfig);
        when(communityAuthentication.merge(any(CommunityAuthentication.class))).thenReturn(communityAuthentication);
        when(queryManipulation.merge(any(QueryManipulation.class))).thenReturn(queryManipulation);
        when(savedSearchConfig.merge(any(SavedSearchConfig.class))).thenReturn(savedSearchConfig);
        when(viewConfig.merge(any(ViewConfig.class))).thenReturn(viewConfig);
        when(export.merge(any(ExportConfig.class))).thenReturn(export);
        when(trending.merge(any(TrendingConfiguration.class))).thenReturn(trending);

        final IdolFindConfig defaults = IdolFindConfig.builder().content(mock(ServerConfig.class)).build();
        final IdolFindConfig mergedConfig = idolFindConfig.merge(defaults);
        assertEquals(serverConfig, mergedConfig.getContent());
        assertEquals(communityAuthentication, mergedConfig.getLogin());
        assertEquals(queryManipulation, mergedConfig.getQueryManipulation());
        assertEquals(savedSearchConfig, mergedConfig.getSavedSearchConfig());
        assertEquals(viewConfig, mergedConfig.getViewConfig());
        assertEquals(export, mergedConfig.getExport());
        assertEquals(trending, mergedConfig.getTrending());
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
    public void getContentAciServerDetails() {
        when(serverConfig.toAciServerDetails()).thenReturn(mock(AciServerDetails.class));
        assertNotNull(idolFindConfig.getContentAciServerDetails());
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

    @Test
    public void lookupComponentNameByHostAndPort() {
        when(communityAuthentication.getMethod()).thenReturn("default");

        final String host = "localhost";
        when(serverConfig.getHost()).thenReturn(host);
        final int port = 1234;
        when(serverConfig.getPort()).thenReturn(port);
        final int servicePort = 5678;
        when(serverConfig.getServicePort()).thenReturn(servicePort);
        assertEquals(ProductType.AXE.getFriendlyName(), idolFindConfig.lookupComponentNameByHostAndPort(host, port));
        assertEquals(ProductType.AXE.getFriendlyName(), idolFindConfig.lookupComponentNameByHostAndPort(host, servicePort));
    }
}
