/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.CacheNames;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarService;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.search.HavenQueryParams;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationCapable;
import com.hp.autonomy.searchcomponents.hod.search.HodDocument;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.types.requests.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class FindHodDocumentService extends HodDocumentsService {
    @Autowired
    public FindHodDocumentService(final FindSimilarService<HodDocument> findSimilarService, final ConfigService<? extends QueryManipulationCapable> configService, final QueryTextIndexService<HodDocument> queryTextIndexService) {
        super(findSimilarService, configService, queryTextIndexService);
    }

    @Override
    @Cacheable(CacheNames.DOCUMENTS)
    public Documents<HodDocument> queryTextIndex(final HavenQueryParams<ResourceIdentifier> findQueryParams) throws HodErrorException {
        return super.queryTextIndex(findQueryParams);
    }

    @Override
    @Cacheable(CacheNames.PROMOTED_DOCUMENTS)
    public Documents<HodDocument> queryTextIndexForPromotions(final HavenQueryParams<ResourceIdentifier> findQueryParams) throws HodErrorException {
        return super.queryTextIndexForPromotions(findQueryParams);
    }

    @Override
    @Cacheable(CacheNames.SIMILAR_DOCUMENTS)
    public List<HodDocument> findSimilar(final Set<ResourceIdentifier> indexes, final String reference) throws HodErrorException {
        return super.findSimilar(indexes, reference);
    }
}
