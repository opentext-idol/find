/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.Identifier;
import com.hp.autonomy.types.query.Documents;

import java.util.List;
import java.util.Set;

public interface DocumentsService<I extends Identifier, E extends Exception> {

    Documents<FindDocument> queryTextIndex(final QueryParams<I> queryParams) throws E;

    Documents<FindDocument> queryTextIndexForPromotions(final QueryParams<I> queryParams) throws E;

    List<FindDocument> findSimilar(Set<I> indexes, String reference) throws E;

}
