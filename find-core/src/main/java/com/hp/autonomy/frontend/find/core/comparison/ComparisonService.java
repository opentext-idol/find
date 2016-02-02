/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;

import com.hp.autonomy.searchcomponents.core.search.SearchResult;


public interface ComparisonService<R extends SearchResult, E extends Exception> {

    Comparison<R> compareStateTokens(final String firstStateToken,
                                     final String secondStateToken,
                                     final int resultsStart,
                                     final int maxResults,
                                     final String summary,
                                     final String sort,
                                     final boolean highlight) throws E;

    Comparison<R> compareStateTokens(final String firstStateToken,
                                     final String secondStateToken,
                                     final String docsInFirstStateToken,
                                     final String docsInSecondStateToken,
                                     final int resultsStart,
                                     final int maxResults,
                                     final String summary,
                                     final String sort,
                                     final boolean highlight) throws E;
}
