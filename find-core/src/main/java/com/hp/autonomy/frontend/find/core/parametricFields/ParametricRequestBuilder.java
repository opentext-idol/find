/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.core.parametricvalues.ParametricRequest;

import java.io.Serializable;
import java.util.Set;

public interface ParametricRequestBuilder<R extends ParametricRequest<S>, S extends Serializable> {
    R buildRequest(final Set<S> databases, final Set<String> fieldNames, final String queryText, final String fieldText);
}
