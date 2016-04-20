/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.comparison;

import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;

import java.util.List;

public interface ComparisonService<R extends SearchResult, E extends Exception> {

    ComparisonStateTokens getCompareStateTokens(final String firstStateToken, final String secondStateToken) throws E;

    Documents<R> getResults(
            List<String> stateMatchIds,
            List<String> stateDontMatchIds,
            String text,
            int resultsStart,
            int maxResults,
            String summary,
            String sort,
            boolean highlight
    ) throws E;

}
