/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.content.GetContentService;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarService;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SuggestRequest;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationCapable;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FindHodDocumentService extends HodDocumentsService {
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
        return super.findSimilar(suggestRequest);
    }
}
