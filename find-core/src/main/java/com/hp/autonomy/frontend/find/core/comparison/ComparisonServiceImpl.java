/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;

import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Service
public class ComparisonServiceImpl<S extends Serializable, R extends SearchResult, E extends Exception> implements ComparisonService<R, E> {
    private final DocumentsService<S, R, E> documentsService;
    private final QueryRestrictionsBuilder<S> queryRestrictionsBuilder;

    @Autowired
    public ComparisonServiceImpl(final DocumentsService<S, R, E> documentsService, final QueryRestrictionsBuilder<S> queryRestrictionsBuilder) {
        this.documentsService = documentsService;
        this.queryRestrictionsBuilder = queryRestrictionsBuilder;
    }

    private Documents<R> getEmptyResults() {
        return new Documents<>(Collections.<R>emptyList(), 0, "", null, null, null);
    }

    private String generateDifferenceStateToken(final String firstQueryStateToken, final String secondQueryStateToken) throws E {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder.build("*", "", Collections.<S>emptyList(), null, null, Collections.singletonList(firstQueryStateToken), Collections.singletonList(secondQueryStateToken));
        return documentsService.getStateToken(queryRestrictions, ComparisonController.STATE_TOKEN_MAX_RESULTS);
    }

    @Override
    public Documents<R> getResults(final List<String> stateMatchIds, final List<String> stateDontMatchIds, final String text, final int resultsStart, final int maxResults, final String summary, final String sort, final boolean highlight) throws E {
        if(stateMatchIds.isEmpty()) {
            return getEmptyResults();
        }

        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder.build(text, "", Collections.<S>emptyList(), null, null, stateMatchIds, stateDontMatchIds);
        final SearchRequest<S> searchRequest = new SearchRequest<>(queryRestrictions, resultsStart, maxResults, summary, DocumentsController.MAX_SUMMARY_CHARACTERS, sort, highlight, false, SearchRequest.QueryType.RAW);
        return documentsService.queryTextIndex(searchRequest);
    }

    @Override
    public ComparisonStateTokens getCompareStateTokens(final String firstStateToken, final String secondStateToken) throws E {
        // First generate the relative complement state tokens, i.e. A \ B and B \ A
        // - where A is the set of documents in our "firstStateToken" and B is the set of
        // documents in our "secondStateToken".
        final String docsInFirstStateToken = generateDifferenceStateToken(firstStateToken, secondStateToken);
        final String docsInSecondStateToken = generateDifferenceStateToken(secondStateToken, firstStateToken);

        final ComparisonStateTokens stateTokens = new ComparisonStateTokens();
        stateTokens.setFirstQueryStateToken(firstStateToken);
        stateTokens.setSecondQueryStateToken(secondStateToken);
        stateTokens.setDocumentsOnlyInFirstStateToken(docsInFirstStateToken);
        stateTokens.setDocumentsOnlyInSecondStateToken(docsInSecondStateToken);

        return stateTokens;
    }
}
