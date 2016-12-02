/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ListResourcesRequestBuilder;
import com.hp.autonomy.hod.client.api.resource.Resource;
import com.hp.autonomy.hod.client.api.resource.ResourceFlavour;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.resource.ResourceType;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.api.resource.ResourcesService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindHodDatabasesServiceTest {
    @Mock
    private HodDatabasesService databasesService;
    @Mock
    private HodDatabasesRequest databasesRequest;
    @Mock
    private ResourcesService resourcesService;
    @Mock
    private ConfigService<HodFindConfig> configService;

    private FindHodDatabasesService findDatabasesService;

    @Before
    public void setUp() {
        findDatabasesService = new FindHodDatabasesServiceImpl(databasesService, resourcesService, configService);

        final HodConfig hodConfig = HodConfig.builder().build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setHod(hodConfig).build());
    }

    @Test
    public void getAllIndexesViaTokenProxy() throws HodErrorException {
        mockListResourcesResponse();
        final Resources resources = findDatabasesService.getAllIndexes(new TokenProxy<>(mock(EntityType.class), TokenType.Simple.INSTANCE));
        assertThat(resources.getResources(), hasSize(1));
        assertThat(resources.getPublicResources(), hasSize(1));
    }

    @Test
    public void listActiveIndexes() throws HodErrorException {
        final ResourceIdentifier activeIndex = ResourceIdentifier.WIKI_ENG;
        final HodConfig hodConfig = HodConfig.builder()
                .activeIndex(activeIndex)
                .build();
        when(configService.getConfig()).thenReturn(new HodFindConfig.Builder().setHod(hodConfig).build());
        when(databasesRequest.isPublicIndexesEnabled()).thenReturn(true);
        assertThat(findDatabasesService.getDatabases(databasesRequest), hasSize(1));
    }

    private void mockListResourcesResponse() throws HodErrorException {
        final Resource resource1 = mockResource("PrivateResource1", ResourceFlavour.STANDARD);
        final Resource resource2 = mockResource("PrivateResource2", ResourceFlavour.WEB_CLOUD);
        final List<Resource> privateResources = Arrays.asList(resource1, resource2);
        final List<Resource> publicResources = Collections.singletonList(mockResource("PublicResource1", ResourceFlavour.STANDARD));
        when(resourcesService.list(any(ListResourcesRequestBuilder.class))).thenReturn(new Resources(privateResources, publicResources));
        when(resourcesService.list(any(), any(ListResourcesRequestBuilder.class))).thenReturn(new Resources(privateResources, publicResources));
    }

    private Resource mockResource(final String name, final ResourceFlavour resourceFlavour) {
        return new Resource(name, "Some Description", ResourceType.CONTENT, resourceFlavour, null, name);
    }
}
