/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.types.requests.Documents;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface DocumentsService<S extends Serializable, D extends FindDocument, E extends Exception> {

    Documents<D> queryTextIndex(final FindQueryParams<S> findQueryParams) throws E;

    Documents<D> queryTextIndexForPromotions(final FindQueryParams<S> findQueryParams) throws E;

    List<D> findSimilar(Set<S> indexes, String reference) throws E;

}
