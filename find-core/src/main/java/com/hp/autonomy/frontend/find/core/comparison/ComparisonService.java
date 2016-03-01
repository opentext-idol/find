/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;

import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;

import java.util.List;


public interface ComparisonService<R extends SearchResult, E extends Exception> {

    ComparisonStateTokens getCompareStateTokens(final String firstStateToken, final String secondStateToken) throws E;

    Documents<R> getResults(final List<String> stateMatchIds,
                            final List<String> stateDontMatchIds,
                            final String text,
                            final int resultsStart,
                            final int maxResults,
                            final String summary,
                            final String sort,
                            final boolean highlight) throws E;
}
