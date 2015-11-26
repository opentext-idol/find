package com.hp.autonomy.frontend.find.core.parametricFields;

import com.hp.autonomy.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.types.Identifier;

import java.util.Set;

public interface ParametricRequestBuilder<R extends ParametricRequest, I extends Identifier> {
    R buildRequest(final Set<I> databases, final Set<String> fieldNames, final String queryText, final String fieldText);
}
