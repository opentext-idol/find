/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.indexes;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
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
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.DatabasesService;
import com.hp.autonomy.searchcomponents.hod.fields.IndexFieldsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class HodIndexesServiceImpl implements HodIndexesService {

    private static final Set<ResourceFlavour> FLAVOURS_TO_REMOVE = ResourceFlavour.of(ResourceFlavour.QUERY_MANIPULATION, ResourceFlavour.CATEGORIZATION);

    private final ConfigService<HodFindConfig> configService;
    private final ResourcesService resourcesService;
    private final IndexFieldsService indexFieldsService;
    private final DatabasesService databasesService;

    @Autowired
    public HodIndexesServiceImpl(final ConfigService<HodFindConfig> configService, final ResourcesService resourcesService, final IndexFieldsService indexFieldsService, final DatabasesService databasesService) {
        this.configService = configService;
        this.resourcesService = resourcesService;
        this.indexFieldsService = indexFieldsService;
        this.databasesService = databasesService;
    }

    @Override
    @Cacheable(FindCacheNames.INDEXES)  // TODO: the caching here doesn't work from the settings page
    public Resources listIndexes(final TokenProxy<?, TokenType.Simple> tokenProxy) throws HodErrorException {
        final Set<ResourceType> types = new HashSet<>();
        types.add(ResourceType.CONTENT);

        final ListResourcesRequestBuilder params = new ListResourcesRequestBuilder()
                .setTypes(types);

        final Resources indexes = resourcesService.list(tokenProxy, params);

        final List<Resource> resources = new ArrayList<>(indexes.getResources().size());
        for (final Resource resource : indexes.getResources()) {
            if (!FLAVOURS_TO_REMOVE.contains(resource.getFlavour())) {
                resources.add(resource);
            }
        }

        return new Resources(resources, indexes.getPublicResources());
    }

    @Override
    public List<ResourceIdentifier> listActiveIndexes() {
        return configService.getConfig().getIod().getActiveIndexes();
    }

    @Override
    @Cacheable(value = FindCacheNames.VISIBLE_INDEXES, key = "#root.methodName")
    public List<Database> listVisibleIndexes() throws HodErrorException {
        final List<ResourceIdentifier> activeIndexes = configService.getConfig().getIod().getActiveIndexes();

        if (activeIndexes.isEmpty()) {
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            final String domain = ((HodAuthenticationPrincipal) auth.getPrincipal()).getApplication().getDomain();

            final Set<Database> validDatabases;
            final Set<Database> allDatabases = databasesService.getDatabases(domain);

            if (configService.getConfig().getIod().getPublicIndexesEnabled()) {
                validDatabases = allDatabases;
            } else {
                validDatabases = new HashSet<>();

                for (final Database database : allDatabases) {
                    if (!database.isPublic()) {
                        validDatabases.add(database);
                    }
                }
            }

            return new ArrayList<>(validDatabases);
        } else {
            final List<Database> activeDatabases = new ArrayList<>();

            for (final ResourceIdentifier index : activeIndexes) {
                activeDatabases.add(new Database.Builder()
                        .setDomain(index.getDomain())
                        .setName(index.getName())
                        .setIndexFields(indexFieldsService.getParametricFields(index))
                        .build());
            }

            return activeDatabases;
        }
    }
}
