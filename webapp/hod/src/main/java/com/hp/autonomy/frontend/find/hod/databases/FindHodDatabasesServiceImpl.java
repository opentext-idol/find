/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.hod.databases;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

@Primary
@Service
class FindHodDatabasesServiceImpl implements HodDatabasesService {
    private final DatabasesService<Database, HodDatabasesRequest, HodErrorException> databasesService;
    private final ConfigService<HodFindConfig> configService;

    @Autowired
    public FindHodDatabasesServiceImpl(
            @Qualifier(DATABASES_SERVICE_BEAN_NAME) final DatabasesService<Database, HodDatabasesRequest, HodErrorException> databasesService,
            final ConfigService<HodFindConfig> configService
    ) {
        this.databasesService = databasesService;
        this.configService = configService;
    }

    @Override
    public Set<Database> getDatabases(final HodDatabasesRequest request) throws HodErrorException {
        final Collection<ResourceName> activeIndexes = configService.getConfig().getHod().getActiveIndexes();
        return activeIndexes.isEmpty() ? databasesService.getDatabases(request) : listActiveIndexes(activeIndexes);
    }

    private Set<Database> listActiveIndexes(final Iterable<ResourceName> activeIndexes) {
        final Set<Database> activeDatabases = new TreeSet<>();

        for (final ResourceName index : activeIndexes) {
            activeDatabases.add(Database.builder()
                    .domain(index.getDomain())
                    .name(index.getName())
                    .build());
        }

        return activeDatabases;
    }
}
