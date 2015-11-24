/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.google.common.collect.ImmutableSet;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.frontend.find.core.web.CacheNames;
import com.hp.autonomy.frontend.find.hod.beanconfiguration.HodCondition;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarService;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryRequestBuilder;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexService;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
@Conditional(HodCondition.class)
public class HodDocumentsService implements DocumentsService {
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

    private static final int MAX_SIMILAR_DOCUMENTS = 3;

    @Autowired
    private FindSimilarService<FindDocument> findSimilarService;

    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Autowired
    private QueryTextIndexService<FindDocument> queryTextIndexService;

    @Override
    @Cacheable(CacheNames.DOCUMENTS)
    public Documents<FindDocument> queryTextIndex(final QueryParams queryParams) throws HodErrorException {
        return queryTextIndex(queryParams, false);
    }

    @Override
    @Cacheable(CacheNames.PROMOTED_DOCUMENTS)
    public Documents<FindDocument> queryTextIndexForPromotions(final QueryParams queryParams) throws HodErrorException {
        return queryTextIndex(queryParams, true);
    }

    @Override
    @Cacheable(CacheNames.SIMILAR_DOCUMENTS)
    public List<FindDocument> findSimilar(final Set<ResourceIdentifier> indexes, final String reference) throws HodErrorException {
        final QueryRequestBuilder requestBuilder = new QueryRequestBuilder()
                .setIndexes(indexes)
                .setPrint(Print.none)
                .setAbsoluteMaxResults(MAX_SIMILAR_DOCUMENTS)
                .setSummary(Summary.concept);

        final Documents<FindDocument> result = findSimilarService.findSimilarDocumentsToIndexReference(reference, requestBuilder);
        final List<FindDocument> documents = new LinkedList<>();

        for (final FindDocument document : result.getDocuments()) {
            documents.add(addDomain(indexes, document));
        }

        return documents;
    }

    private Documents<FindDocument> queryTextIndex(final QueryParams queryParams, final boolean fetchPromotions) throws HodErrorException {
        final String profileName = configService.getConfig().getQueryManipulation().getProfile();

        final QueryRequestBuilder params = new QueryRequestBuilder()
                .setAbsoluteMaxResults(queryParams.getMaxResults())
                .setSummary(queryParams.getSummary())
                .setIndexes(queryParams.getIndex())
                .setFieldText(queryParams.getFieldText())
                .setQueryProfile(new ResourceIdentifier(getDomain(), profileName))
                .setSort(queryParams.getSort())
                .setMinDate(queryParams.getMinDate())
                .setMaxDate(queryParams.getMaxDate())
                .setPromotions(fetchPromotions)
                .setPrint(Print.fields)
                .setPrintFields(new ArrayList<>(FindDocument.ALL_FIELDS));

        final Documents<FindDocument> hodDocuments = queryTextIndexService.queryTextIndexWithText(queryParams.getText(), params);
        final List<FindDocument> documentList = new LinkedList<>();

        for (final FindDocument hodDocument : hodDocuments.getDocuments()) {
            documentList.add(addDomain(queryParams.getIndex(), hodDocument));
        }

        return new Documents<>(documentList, hodDocuments.getTotalResults(), hodDocuments.getExpandedQuery());
    }

    // Add a domain to a FindDocument, given the collection of indexes which were queried against to return it from HOD
    private FindDocument addDomain(final Iterable<ResourceIdentifier> indexIdentifiers, final FindDocument document) {
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

        return new FindDocument.Builder(document)
                .setDomain(domain)
                .build();
    }

    private String getDomain() {
        return ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();
    }
}
