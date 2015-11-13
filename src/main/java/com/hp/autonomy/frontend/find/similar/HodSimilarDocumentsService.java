/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.similar;

import com.hp.autonomy.frontend.find.beanconfiguration.HodCondition;
import com.hp.autonomy.frontend.find.web.CacheNames;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Document;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.FindSimilarService;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryRequestBuilder;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Conditional(HodCondition.class)
public class HodSimilarDocumentsService implements SimilarDocumentsService {
    private static final int MAX_RESULTS = 3;

    @Autowired
    private FindSimilarService<Document> findSimilarService;

    @Override
    @Cacheable(CacheNames.SIMILAR_DOCUMENTS)
    public List<Document> findSimilar(final Set<ResourceIdentifier> indexes, final String reference) throws HodErrorException {
        final QueryRequestBuilder requestBuilder = new QueryRequestBuilder()
                .setIndexes(indexes)
                .setPrint(Print.none)
                .setAbsoluteMaxResults(MAX_RESULTS)
                .setSummary(Summary.concept);

        final Documents<Document> result = findSimilarService.findSimilarDocumentsToIndexReference(reference, requestBuilder);
        return result.getDocuments();
    }
}
