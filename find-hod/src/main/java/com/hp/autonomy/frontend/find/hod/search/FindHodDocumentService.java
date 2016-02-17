/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.google.common.collect.ImmutableSet;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.content.GetContentService;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarService;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SuggestRequest;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationCapable;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
public class FindHodDocumentService extends HodDocumentsService {
    private static final ImmutableSet<String> PUBLIC_INDEX_NAMES = ImmutableSet.of(
            ResourceIdentifier.WIKI_CHI.getName(),
            ResourceIdentifier.WIKI_ENG.getName(),
            ResourceIdentifier.WIKI_FRA.getName(),
            ResourceIdentifier.WIKI_GER.getName(),
            ResourceIdentifier.WIKI_ITA.getName(),
            ResourceIdentifier.WIKI_SPA.getName(),
            ResourceIdentifier.WORLD_FACTBOOK.getName(),
            ResourceIdentifier.NEWS_ENG.getName(),
            ResourceIdentifier.NEWS_FRA.getName(),
            ResourceIdentifier.NEWS_GER.getName(),
            ResourceIdentifier.NEWS_ITA.getName(),
            ResourceIdentifier.ARXIV.getName(),
            ResourceIdentifier.PATENTS.getName()
    );

    @Autowired
    public FindHodDocumentService(final FindSimilarService<HodSearchResult> findSimilarService, final ConfigService<? extends QueryManipulationCapable> configService, final QueryTextIndexService<HodSearchResult> queryTextIndexService, final GetContentService<HodSearchResult> getContentService) {
        super(findSimilarService, configService, queryTextIndexService, getContentService);
    }

    @Override
    @Cacheable(FindCacheNames.DOCUMENTS)
    public Documents<HodSearchResult> queryTextIndex(final SearchRequest<ResourceIdentifier> findQueryParams) throws HodErrorException {
        return super.queryTextIndex(findQueryParams);
    }

    @Override
    @Cacheable(FindCacheNames.PROMOTED_DOCUMENTS)
    public Documents<HodSearchResult> queryTextIndexForPromotions(final SearchRequest<ResourceIdentifier> findQueryParams) throws HodErrorException {
        return super.queryTextIndexForPromotions(findQueryParams);
    }

    @Override
    @Cacheable(FindCacheNames.SIMILAR_DOCUMENTS)
    public Documents<HodSearchResult> findSimilar(final SuggestRequest<ResourceIdentifier> suggestRequest) throws HodErrorException {
        final Documents<HodSearchResult> results = super.findSimilar(suggestRequest);
        final List<HodSearchResult> resultsWithDomain = new LinkedList<>();
        addDomainToSearchResults(resultsWithDomain, suggestRequest.getQueryRestrictions().getDatabases(), results.getDocuments());
        return new Documents<>(resultsWithDomain, results.getTotalResults(), results.getExpandedQuery(), results.getSuggestion(), results.getAutoCorrection());
    }

    @Override
    public List<HodSearchResult> getDocumentContent(final GetContentRequest<ResourceIdentifier> request) throws HodErrorException {
        final List<HodSearchResult> results = super.getDocumentContent(request);
        final List<HodSearchResult> resultsWithDomain = new LinkedList<>();
        addDomainToSearchResults(resultsWithDomain, Collections.singleton(request.getIndexesAndReferences().iterator().next().getIndex()), results);
        return resultsWithDomain;
    }

    private void addDomainToSearchResults(final Collection<HodSearchResult> resultsWithDomain, final Iterable<ResourceIdentifier> indexIdentifiers, final Iterable<HodSearchResult> documents) {
        for (final HodSearchResult hodSearchResult : documents) {
            resultsWithDomain.add(addDomain(indexIdentifiers, hodSearchResult));
        }
    }

    // Add a domain to a FindDocument, given the collection of indexes which were queried against to return it from HOD
    private HodSearchResult addDomain(final Iterable<ResourceIdentifier> indexIdentifiers, final HodSearchResult document) {
        // HOD does not return the domain for documents yet, but it does return the index
        final String index = document.getIndex();
        String domain = null;

        // It's most likely that the returned documents will be in one of the indexes we are querying (hopefully the
        // names are unique between the domains...)
        for (final ResourceIdentifier indexIdentifier : indexIdentifiers) {
            if (index.equals(indexIdentifier.getName())) {
                domain = indexIdentifier.getDomain();
                break;
            }
        }

        if (domain == null) {
            // If not, it might be a public index
            domain = PUBLIC_INDEX_NAMES.contains(index) ? ResourceIdentifier.PUBLIC_INDEXES_DOMAIN : getDomain();
        }

        return new HodSearchResult.Builder(document)
                .setDomain(domain)
                .build();
    }

    private String getDomain() {
        return ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();
    }
}
