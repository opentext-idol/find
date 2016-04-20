/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IdolQueryRestrictionsBuilder implements QueryRestrictionsBuilder<String> {
    @Override
    public QueryRestrictions<String> build(final String queryText, final String fieldText, final List<String> databases, final DateTime minDate, final DateTime maxDate, final List<String> stateMatchId, final List<String> stateDontMatchId) {
        return new IdolQueryRestrictions.Builder()
                .setQueryText(queryText)
                .setFieldText(fieldText)
                .setDatabases(databases)
                .setMinDate(minDate)
                .setMaxDate(maxDate)
                .setAnyLanguage(true)
                .setStateMatchId(stateMatchId)
                .setStateDontMatchId(stateDontMatchId)
                .build();
    }
}
