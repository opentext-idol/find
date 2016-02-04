/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;


import com.hp.autonomy.searchcomponents.core.search.SearchResult;
import com.hp.autonomy.types.requests.Documents;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Comparison<R extends SearchResult> {

    private Documents<R> documentsInBoth;
    private Documents<R> documentsOnlyInFirst;
    private Documents<R> documentsOnlyInSecond;

    private String firstQueryStateToken;
    private String secondQueryStateToken;
    private String documentsOnlyInFirstStateToken;
    private String documentsOnlyInSecondStateToken;
}
