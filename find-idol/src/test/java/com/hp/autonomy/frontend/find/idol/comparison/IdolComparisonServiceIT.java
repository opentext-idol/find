/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.comparison;


import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.IdolFindApplication;
import com.hp.autonomy.frontend.find.core.comparison.AbstractComparisonServiceIT;
import com.hp.autonomy.frontend.find.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.springframework.boot.test.SpringApplicationConfiguration;

import java.util.Collections;

@SpringApplicationConfiguration(classes = IdolFindApplication.class)
public class IdolComparisonServiceIT extends AbstractComparisonServiceIT<String, IdolSearchResult, AciErrorException> {

    @Override
    public QueryRestrictions<String> buildQueryRestrictions() {
        return new IdolQueryRestrictionsBuilder().build("*", "", Collections.<String>emptyList(), null, null, Collections.<String>emptyList(), Collections.<String>emptyList());
    }
}
