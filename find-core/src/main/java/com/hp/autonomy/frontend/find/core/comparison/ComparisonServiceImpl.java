/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
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
        return new Documents<>(Collections.<R>emptyList(), 0, "", null, null);
    }

    private String generateDifferenceStateToken(final String firstQueryStateToken, final String secondQueryStateToken) throws E {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder.build("*", "", Collections.<S>emptyList(), null, null, Collections.singletonList(firstQueryStateToken), Collections.singletonList(secondQueryStateToken));
        return documentsService.getStateToken(queryRestrictions, ComparisonController.STATE_TOKEN_MAX_RESULTS);
    }

    private Documents<R> getResultsForStateToken(final String stateToken, final int resultsStart, final int maxResults, final String summary, final String sort, final boolean highlight) throws E {
        if(stateToken == null) {
            return getEmptyResults();
        }

        return getResults(Collections.singletonList(stateToken), Collections.<String>emptyList(), resultsStart, maxResults, summary, sort, highlight);
    }

    @Override
    public Documents<R> getResults(final List<String> stateMatchIds, final List<String> stateDontMatchIds, final int resultsStart, final int maxResults, final String summary, final String sort, final boolean highlight) throws E {
        final QueryRestrictions<S> queryRestrictions = queryRestrictionsBuilder.build("*", "", Collections.<S>emptyList(), null, null, stateMatchIds, stateDontMatchIds);
        final SearchRequest<S> searchRequest = new SearchRequest<>(queryRestrictions, resultsStart, maxResults, summary, sort, highlight, false, SearchRequest.QueryType.RAW);
        return documentsService.queryTextIndex(searchRequest);
    }

    @Override
    public Comparison<R> compareStateTokens(final String firstStateToken,
                                            final String secondStateToken,
                                            final int resultsStart,
                                            final int maxResults,
                                            final String summary,
                                            final String sort,
                                            final boolean highlight) throws E {
        // First generate the relative complement state tokens, i.e. A \ B and B \ A
        // - where A is the set of documents in our "firstStateToken" and B is the set of
        // documents in our "secondStateToken".
        final String docsInFirstStateToken = generateDifferenceStateToken(firstStateToken, secondStateToken);
        final String docsInSecondStateToken = generateDifferenceStateToken(secondStateToken, firstStateToken);

        return compareStateTokens(firstStateToken, secondStateToken, docsInFirstStateToken, docsInSecondStateToken, resultsStart, maxResults, summary, sort, highlight);
    }

    @Override
    public Comparison<R> compareStateTokens(final String firstStateToken,
                                            final String secondStateToken,
                                            final String docsInFirstStateToken,
                                            final String docsInSecondStateToken,
                                            final int resultsStart,
                                            final int maxResults,
                                            final String summary,
                                            final String sort,
                                            final boolean highlight) throws E {
        final Comparison<R> comparison = new Comparison<>();
        comparison.setFirstQueryStateToken(firstStateToken);
        comparison.setSecondQueryStateToken(secondStateToken);
        comparison.setDocumentsInFirstStateToken(docsInFirstStateToken);
        comparison.setDocumentsInSecondStateToken(docsInSecondStateToken);

        // Generate the set of results for the documents in our first query without those from the second
        // Use the state token previously generated as it's marginally easier than using the original state tokens
        final Documents<R> docsInFirstQueryOnly = getResultsForStateToken(docsInFirstStateToken, resultsStart, maxResults, summary, sort, highlight);
        comparison.setDocumentsInFirst(docsInFirstQueryOnly);

        // Generate the set of results for the documents in our second query without those from the first
        final Documents<R> docsInSecondQueryOnly = getResultsForStateToken(docsInSecondStateToken, resultsStart, maxResults, summary, sort, highlight);
        comparison.setDocumentsInSecond(docsInSecondQueryOnly);

        // State tokens to match all documents, if either of these is null then the intersection is empty so return early
        if(firstStateToken == null || secondStateToken == null) {
            comparison.setDocumentsInBoth(getEmptyResults());
            return comparison;
        }

        final List<String> stateMatchIdAllDocs = new ArrayList<>();
        stateMatchIdAllDocs.add(firstStateToken);
        stateMatchIdAllDocs.add(secondStateToken);

        // State tokens that are exclusive to one query
        final List<String> stateMatchIdExclusiveDocs = new ArrayList<>();
        if(docsInFirstStateToken != null) stateMatchIdExclusiveDocs.add(docsInFirstStateToken);
        if(docsInSecondStateToken != null) stateMatchIdExclusiveDocs.add(docsInSecondStateToken);

        // Finally generate the intersection of our 2 sets of results (no need to get the state token for this)
        final QueryRestrictions<S> intersectionQueryRestrictions = queryRestrictionsBuilder.build("*", "", Collections.<S>emptyList(), null, null, stateMatchIdAllDocs, stateMatchIdExclusiveDocs);
        final SearchRequest<S> intersectionSearchRequest = new SearchRequest<>(intersectionQueryRestrictions, resultsStart, maxResults, summary, sort, highlight, false, SearchRequest.QueryType.RAW);
        comparison.setDocumentsInBoth(documentsService.queryTextIndex(intersectionSearchRequest));

        return comparison;
    }

}
