/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ListResourcesRequestBuilder;
import com.hp.autonomy.hod.client.api.resource.Resource;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.resource.ResourceType;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.api.resource.ResourcesService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FindHodDatabasesServiceImpl extends HodDatabasesService implements FindHodDatabasesService {
    private final ConfigService<HodFindConfig> configService;

    @Autowired
    public FindHodDatabasesServiceImpl(
            final ResourcesService resourcesService,
            final ConfigService<HodFindConfig> configService,
            final AuthenticationInformationRetriever<HodAuthenticationPrincipal> authenticationInformationRetriever
    ) {
        super(resourcesService, authenticationInformationRetriever);
        this.configService = configService;
    }

    @Override
    public Resources getAllIndexes(final TokenProxy<?, TokenType.Simple> tokenProxy) throws HodErrorException {
        final Set<ResourceType> types = new HashSet<>();
        types.add(ResourceType.CONTENT);

        final ListResourcesRequestBuilder params = new ListResourcesRequestBuilder().setTypes(types);

        final Resources indexes = resourcesService.list(tokenProxy, params);

        final List<Resource> resources = new ArrayList<>(indexes.getResources().size());

        for (final Resource resource : indexes.getResources()) {
            if (CONTENT_FLAVOURS.contains(resource.getFlavour())) {
                resources.add(resource);
            }
        }

        return new Resources(resources, indexes.getPublicResources());
    }

    @Override
    public Set<Database> getDatabases(final HodDatabasesRequest request) throws HodErrorException {
        final List<ResourceIdentifier> activeIndexes = configService.getConfig().getIod().getActiveIndexes();
        return activeIndexes.isEmpty() ? super.getDatabases(request) : listActiveIndexes(activeIndexes);
    }

    private Set<Database> listActiveIndexes(final Iterable<ResourceIdentifier> activeIndexes) {
        final Set<Database> activeDatabases = new TreeSet<>();

        for (final ResourceIdentifier index : activeIndexes) {
            activeDatabases.add(new Database.Builder()
                    .setDomain(index.getDomain())
                    .setName(index.getName())
                    .build());
        }

        return activeDatabases;
    }
}
