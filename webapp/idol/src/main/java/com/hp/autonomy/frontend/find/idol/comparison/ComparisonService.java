/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.comparison;

import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;

import java.util.List;

public interface ComparisonService<R extends SearchResult, E extends Exception> {

    ComparisonStateTokens getCompareStateTokens(String firstStateToken, String secondStateToken) throws E;

    int getStateTokenMaxResults();

    Documents<R> getResults(
            List<String> stateMatchIds,
            List<String> stateDontMatchIds,
            String text,
            String fieldText,
            int resultsStart,
            int maxResults,
            String summary,
            String sort,
            boolean highlight
    ) throws E;

}
