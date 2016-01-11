/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.indexes;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.IodConfig;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ListResourcesRequestBuilder;
import com.hp.autonomy.hod.client.api.resource.Resource;
import com.hp.autonomy.hod.client.api.resource.ResourceFlavour;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.api.resource.ResourcesService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.databases.Database;
import com.hp.autonomy.hod.databases.DatabasesService;
import com.hp.autonomy.hod.fields.IndexFieldsService;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HodIndexesServiceTest {
    public static final String SAMPLE_DOMAIN = "SomeDomain";

    private static SecurityContext existingSecurityContext;

    @BeforeClass
    public static void init() {
        existingSecurityContext = SecurityContextHolder.getContext();

        final SecurityContext securityContext = mock(SecurityContext.class);
        final HodAuthentication hodAuthentication = mock(HodAuthentication.class);
        final HodAuthenticationPrincipal hodAuthenticationPrincipal = mock(HodAuthenticationPrincipal.class);
        final ResourceIdentifier resourceIdentifier = mock(ResourceIdentifier.class);
        when(resourceIdentifier.getDomain()).thenReturn(SAMPLE_DOMAIN);
        when(hodAuthenticationPrincipal.getApplication()).thenReturn(resourceIdentifier);
        when(hodAuthentication.getPrincipal()).thenReturn(hodAuthenticationPrincipal);
        when(securityContext.getAuthentication()).thenReturn(hodAuthentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterClass
    public static void destroy() {
        SecurityContextHolder.setContext(existingSecurityContext);
    }

    @Mock
    private ConfigService<HodFindConfig> configService;

    @Mock
    private ResourcesService resourcesService;

    @Mock
    private IndexFieldsService indexFieldsService;

    @Mock
    private DatabasesService databasesService;

    private HodIndexesService hodIndexesService;

    @Before
    public void setUp() throws HodErrorException {
        hodIndexesService = new HodIndexesServiceImpl(configService, resourcesService, indexFieldsService, databasesService);

        final Database privateDatabase = new Database.Builder().setName("Database1").build();
        final Database privateDatabase2 = new Database.Builder().setName("Database2").build();
        final Database publicDatabase = new Database.Builder().setName("PublicDatabase1").setIsPublic(true).build();
        when(databasesService.getDatabases(SAMPLE_DOMAIN)).thenReturn(new HashSet<>(Arrays.asList(privateDatabase, privateDatabase2, publicDatabase)));
    }

    @Test
    public void listAllVisibleIndexes() throws HodErrorException {
        final IodConfig iodConfig = new IodConfig.Builder().setActiveIndexes(Collections.<ResourceIdentifier>emptyList()).setPublicIndexesEnabled(true).build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setIod(iodConfig).build());
        assertThat(hodIndexesService.listVisibleIndexes(), hasSize(3));
    }

    @Test
    public void listAllPrivateVisibleIndexes() throws HodErrorException {
        final IodConfig iodConfig = new IodConfig.Builder().setActiveIndexes(Collections.<ResourceIdentifier>emptyList()).setPublicIndexesEnabled(false).build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setIod(iodConfig).build());
        assertThat(hodIndexesService.listVisibleIndexes(), hasSize(2));
    }

    @Test
    public void listActiveVisibleIndexes() throws HodErrorException {
        final ResourceIdentifier activeIndex = ResourceIdentifier.WIKI_ENG;
        final IodConfig iodConfig = new IodConfig.Builder().setActiveIndexes(Collections.singletonList(activeIndex)).build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setIod(iodConfig).build());
        assertThat(hodIndexesService.listVisibleIndexes(), hasSize(1));
        verify(indexFieldsService).getParametricFields(activeIndex);
    }

    @Test
    public void listActiveIndexes() {
        final IodConfig iodConfig = new IodConfig.Builder().setActiveIndexes(Collections.singletonList(ResourceIdentifier.WIKI_ENG)).build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setIod(iodConfig).build());
        assertThat(hodIndexesService.listActiveIndexes(), hasSize(1));
    }

    @Test
    public void listIndexResources() throws HodErrorException {
        final Resources resources = mock(Resources.class);
        final Resource resource1 = mock(Resource.class);
        when(resource1.getFlavour()).thenReturn(ResourceFlavour.EXPLORER);
        final Resource resource2 = mock(Resource.class);
        when(resource2.getFlavour()).thenReturn(ResourceFlavour.QUERY_MANIPULATION);
        when(resources.getResources()).thenReturn(Arrays.asList(resource1, resource2));
        when(resourcesService.list(Matchers.<TokenProxy<?, TokenType.Simple>>any(), any(ListResourcesRequestBuilder.class))).thenReturn(resources);
        assertThat(hodIndexesService.listIndexes(null).getResources(), hasSize(1));
    }
}
