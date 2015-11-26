/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.Identifier;
import com.hp.autonomy.types.requests.idol.actions.query.QuerySummaryElement;

import java.util.List;

public interface RelatedConceptsService<Q extends QuerySummaryElement, I extends Identifier, E extends Exception> {

    List<Q> findRelatedConcepts(String text, List<I> indexes, String fieldText) throws E;

}
