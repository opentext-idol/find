/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

public interface QueryRestrictionsBuilder<S extends Serializable> {
    QueryRestrictions<S> build(final String queryText, final String fieldText, final List<S> databases, final DateTime minDate, final DateTime maxDate, final List<String> stateMatchId, final List<String> stateDontMatchId);
}
