/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;

import java.io.Serializable;
import java.util.List;

public interface RelatedConceptsService<Q extends QuerySummaryElement, S extends Serializable, E extends Exception> {

    List<Q> findRelatedConcepts(String text, List<S> indexes, String fieldText) throws E;

}
