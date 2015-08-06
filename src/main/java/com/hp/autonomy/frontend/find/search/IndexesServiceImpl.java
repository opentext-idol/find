/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.configuration.FindConfig;
import com.hp.autonomy.hod.client.api.resource.ListResourcesRequestBuilder;
import com.hp.autonomy.hod.client.api.resource.Resource;
import com.hp.autonomy.hod.client.api.resource.ResourceFlavour;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.resource.ResourceType;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.api.resource.ResourcesService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.client.token.TokenProxyService;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class IndexesServiceImpl implements IndexesService {

    private static final Set<ResourceFlavour> FLAVOURS_TO_REMOVE = EnumSet.of(ResourceFlavour.querymanipulation, ResourceFlavour.categorization);

    @Autowired
    private ConfigService<FindConfig> configService;

    @Autowired
    private ResourcesService resourcesService;

    @Autowired
    private TokenProxyService tokenProxyService;

    @Override
    public Resources listIndexes() throws HodErrorException {
        return listIndexes(tokenProxyService.getTokenProxy());
    }

    @Override
    public Resources listIndexes(final TokenProxy tokenProxy) throws HodErrorException {
        final Set<ResourceType> types = new HashSet<>();
        types.add(ResourceType.content);

        final ListResourcesRequestBuilder params = new ListResourcesRequestBuilder()
                .setTypes(types);

        final Resources indexes = resourcesService.list(tokenProxy, params);

        final Iterator<Resource> iterator = indexes.getResources().iterator();

        while (iterator.hasNext()) {
            final Resource i = iterator.next();

            if (FLAVOURS_TO_REMOVE.contains(i.getFlavour())) {
                iterator.remove();
            }
        }

        return indexes;
    }

    @Override
    public List<ResourceIdentifier> listActiveIndexes() {
        return configService.getConfig().getIod().getActiveIndexes();
    }

    @Override
    public List<ResourceIdentifier> listVisibleIndexes() throws HodErrorException {
        final List<ResourceIdentifier> activeIndexes = configService.getConfig().getIod().getActiveIndexes();

        if(activeIndexes.isEmpty()) {
            final HodAuthentication auth = (HodAuthentication) SecurityContextHolder.getContext().getAuthentication();

            final Resources resources = listIndexes();
            final String domain = auth.getDomain();

            final List<ResourceIdentifier> resourceIdentifiers = new ArrayList<>();

            if (configService.getConfig().getIod().getPublicIndexesEnabled()) {
                for (final Resource resource : resources.getPublicResources()) {
                    resourceIdentifiers.add(new ResourceIdentifier(ResourceIdentifier.PUBLIC_INDEXES_DOMAIN, resource.getResource()));
                }
            }

            for (final Resource resource : resources.getResources()) {
                resourceIdentifiers.add(new ResourceIdentifier(domain, resource.getResource()));
            }

            return resourceIdentifiers;
        }
        else {
            return activeIndexes;
        }
    }
}
