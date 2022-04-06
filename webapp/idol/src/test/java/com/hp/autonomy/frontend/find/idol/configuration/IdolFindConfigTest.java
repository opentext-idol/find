/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.server.ProductType;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.core.configuration.*;
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
    @Mock private CommunityAgentStoreConfig communityAgentStore;

    @Mock
    private TrendingConfiguration trending;

    @Mock
    private ThemeTrackerConfig themeTracker;

    @Mock
    private SearchConfig search;
    @Mock private UsersConfig users;

    private IdolFindConfig idolFindConfig;

    @Before
    public void setUp() {
        idolFindConfig = IdolFindConfig.builder()
                .content(serverConfig)
                .login(communityAuthentication)
                .queryManipulation(queryManipulation)
                .savedSearchConfig(savedSearchConfig)
                .trending(trending)
                .themeTracker(themeTracker)
                .view(viewConfig)
                .communityAgentStore(communityAgentStore)
                .export(export)
                .search(search)
                .users(users)
                .build();
    }

    @Test
    public void basicValidate_valid() throws ConfigException {
        idolFindConfig.basicValidate(null);
    }

    @Test(expected = ConfigException.class)
    public void basicValidate_invalidSection() throws ConfigException {
        doThrow(new ConfigException("QMS", "Bad Config")).when(queryManipulation).basicValidate(anyString());
        idolFindConfig.basicValidate(null);
    }

    @Test(expected = ConfigException.class)
    public void basicValidate_relatedUsersWithoutCommunityAgentStore() throws ConfigException {
        final RelatedUsersConfig relatedUsers = mock(RelatedUsersConfig.class);
        when(users.getRelatedUsers()).thenReturn(relatedUsers);
        when(relatedUsers.getEnabled()).thenReturn(true);
        idolFindConfig.basicValidate(null);
    }

    public void basicValidate_relatedUsersWithCommunityAgentStore() throws ConfigException {
        final RelatedUsersConfig relatedUsers = mock(RelatedUsersConfig.class);
        when(users.getRelatedUsers()).thenReturn(relatedUsers);
        when(relatedUsers.getEnabled()).thenReturn(true);
        when(communityAgentStore.getEnabled()).thenReturn(true);
        idolFindConfig.basicValidate(null);
    }

    @Test
    public void merge() {
        when(serverConfig.merge(any(ServerConfig.class))).thenReturn(serverConfig);
        when(communityAuthentication.merge(any(CommunityAuthentication.class))).thenReturn(communityAuthentication);
        when(queryManipulation.merge(any(QueryManipulation.class))).thenReturn(queryManipulation);
        when(savedSearchConfig.merge(any(SavedSearchConfig.class))).thenReturn(savedSearchConfig);
        when(viewConfig.merge(any(ViewConfig.class))).thenReturn(viewConfig);
        when(communityAgentStore.merge(any())).thenReturn(communityAgentStore);
        when(export.merge(any(ExportConfig.class))).thenReturn(export);
        when(trending.merge(any(TrendingConfiguration.class))).thenReturn(trending);
        when(themeTracker.merge(any(ThemeTrackerConfig.class))).thenReturn(themeTracker);


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
        assertEquals(themeTracker, mergedConfig.getThemeTracker());
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
