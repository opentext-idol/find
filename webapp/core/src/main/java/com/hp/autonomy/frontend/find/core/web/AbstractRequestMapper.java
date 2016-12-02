/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;

import java.io.IOException;

public abstract class AbstractRequestMapper<R extends QueryRequest<?>> implements RequestMapper<R> {
    private final ObjectMapper objectMapper;

    protected AbstractRequestMapper(final QueryRestrictionsBuilder<?, ?, ?> queryRestrictionsBuilder,
                                    final QueryRequestBuilder<?, ?, ?> queryRequestBuilder) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        addCustomMixins(objectMapper, queryRestrictionsBuilder, queryRequestBuilder);
    }

    protected abstract void addCustomMixins(final ObjectMapper objectMapper, final QueryRestrictionsBuilder<?, ?, ?> queryRestrictionsBuilder, final QueryRequestBuilder<?, ?, ?> queryRequestBuilder);

    protected abstract Class<R> getType();

    @Override
    public R parseQueryRequest(final String json) throws IOException {
        return objectMapper.readValue(json, getType());
    }
}
