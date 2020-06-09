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

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.caching.CachingConfiguration;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorCode;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesService;
import com.hp.autonomy.searchcomponents.hod.search.*;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.Warnings;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

@Primary
@Service
class FindHodDocumentService implements HodDocumentsService {
    private final HodDatabasesService databasesService;
    private final HodDocumentsService documentsService;
    private final ObjectFactory<HodDatabasesRequestBuilder> databasesRequestBuilderFactory;
    private final ConfigService<HodFindConfig> findConfigService;

    @Autowired
    public FindHodDocumentService(
            final HodDatabasesService databasesService,
            @Qualifier(DOCUMENTS_SERVICE_BEAN_NAME)
            final HodDocumentsService documentsService,
            final ObjectFactory<HodDatabasesRequestBuilder> databasesRequestBuilderFactory,
            final ConfigService<HodFindConfig> configService
    ) {
        this.databasesService = databasesService;
        this.documentsService = documentsService;
        this.databasesRequestBuilderFactory = databasesRequestBuilderFactory;
        findConfigService = configService;
    }

    @Override
    @Cacheable(value = FindCacheNames.DOCUMENTS, cacheResolver = CachingConfiguration.PER_USER_CACHE_RESOLVER_NAME)
    public Documents<HodSearchResult> queryTextIndex(final HodQueryRequest queryRequest) throws HodErrorException {
        try {
            return documentsService.queryTextIndex(queryRequest);
        } catch (final HodErrorException e) {
            if (e.getErrorCode() == HodErrorCode.INDEX_NAME_INVALID) {
                final Boolean publicIndexesEnabled = findConfigService.getConfig().getHod().getPublicIndexesEnabled();
                final HodDatabasesRequest databasesRequest = databasesRequestBuilderFactory.getObject().publicIndexesEnabled(publicIndexesEnabled).build();

                final Set<Database> updatedDatabases = databasesService.getDatabases(databasesRequest);

                final QueryRestrictions<ResourceName> queryRestrictions = queryRequest.getQueryRestrictions();
                final Set<ResourceName> badIndexes = new HashSet<>(queryRestrictions.getDatabases());

                for (final Database database : updatedDatabases) {
                    final ResourceName resourceIdentifier = new ResourceName(database.getDomain(), database.getName());
                    badIndexes.remove(resourceIdentifier);
                }

                final Collection<ResourceName> goodIndexes = new ArrayList<>(queryRestrictions.getDatabases());
                goodIndexes.removeAll(badIndexes);

                final HodQueryRequest queryRequestWithoutBadIndexes = queryRequest.toBuilder()
                        .queryRestrictions(queryRequest.getQueryRestrictions()
                                .toBuilder()
                                .databases(goodIndexes)
                                .build())
                        .build();
                final Documents<HodSearchResult> resultDocuments = documentsService.queryTextIndex(queryRequestWithoutBadIndexes);
                final Warnings warnings = new Warnings(badIndexes);
                return new Documents<>(
                        resultDocuments.getDocuments(),
                        resultDocuments.getTotalResults(),
                        resultDocuments.getExpandedQuery(),
                        resultDocuments.getSuggestion(),
                        resultDocuments.getAutoCorrection(),
                        warnings);
            } else {
                throw e;
            }
        }
    }

    @Override
    @Cacheable(value = FindCacheNames.SIMILAR_DOCUMENTS, cacheResolver = CachingConfiguration.PER_USER_CACHE_RESOLVER_NAME)
    public Documents<HodSearchResult> findSimilar(final HodSuggestRequest suggestRequest) throws HodErrorException {
        return documentsService.findSimilar(suggestRequest);
    }

    @Override
    public List<HodSearchResult> getDocumentContent(final HodGetContentRequest getContentRequest) throws HodErrorException {
        return documentsService.getDocumentContent(getContentRequest);
    }

    @Override
    public String getStateToken(final HodQueryRestrictions queryRestrictions, final int maxResults, final boolean promotions) throws HodErrorException {
        return documentsService.getStateToken(queryRestrictions, maxResults, promotions);
    }

    @Override
    public StateTokenAndResultCount getStateTokenAndResultCount(final HodQueryRestrictions queryRestrictions, final int maxResults, final boolean promotions) throws HodErrorException {
        return documentsService.getStateTokenAndResultCount(queryRestrictions, maxResults, promotions);
    }
}
