/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.Identifier;
import com.hp.autonomy.types.requests.Documents;

import java.util.List;
import java.util.Set;

public interface DocumentsService<I extends Identifier, D extends FindDocument, E extends Exception> {

    Documents<D> queryTextIndex(final FindQueryParams<I> findQueryParams) throws E;

    Documents<D> queryTextIndexForPromotions(final FindQueryParams<I> findQueryParams) throws E;

    List<D> findSimilar(Set<I> indexes, String reference) throws E;

}
