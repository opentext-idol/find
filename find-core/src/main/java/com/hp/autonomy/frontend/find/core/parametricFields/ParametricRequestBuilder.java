package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.core.parametricvalues.ParametricRequest;

import java.io.Serializable;
import java.util.Set;

public interface ParametricRequestBuilder<R extends ParametricRequest<S>, S extends Serializable> {
    R buildRequest(final Set<S> databases, final Set<String> fieldNames, final String queryText, final String fieldText);
}
