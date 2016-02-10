/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.comparison;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ComparisonStateTokens {
    private String firstQueryStateToken;
    private String secondQueryStateToken;
    private String documentsOnlyInFirstStateToken;
    private String documentsOnlyInSecondStateToken;
}
