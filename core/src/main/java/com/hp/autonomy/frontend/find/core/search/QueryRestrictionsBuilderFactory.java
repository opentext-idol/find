package com.hp.autonomy.frontend.find.core.search;

import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;

import java.io.Serializable;

/**
 * Creates a QueryRestrictions object initialised with the defaults for the platform.
 */
public interface QueryRestrictionsBuilderFactory<Q extends QueryRestrictions<S>, S extends Serializable> {

    QueryRestrictions.Builder<Q, S> createBuilder();

}
