/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;

import java.io.IOException;

public abstract class AbstractRequestMapper<R extends QueryRequest<?>> implements RequestMapper<R> {
    private final ObjectMapper objectMapper;

    protected AbstractRequestMapper(final QueryRestrictionsBuilder<?, ?, ?> queryRestrictionsBuilder,
                                    final QueryRequestBuilder<?, ?, ?> queryRequestBuilder) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        addCustomMixins(objectMapper, queryRestrictionsBuilder, queryRequestBuilder);
    }

    protected abstract void addCustomMixins(final ObjectMapper objectMapper, final QueryRestrictionsBuilder<?, ?, ?> queryRestrictionsBuilder, final QueryRequestBuilder<?, ?, ?> queryRequestBuilder);

    protected abstract Class<R> getType();

    @Override
    public R parseQueryRequest(final String json) throws IOException {
        return objectMapper.readValue(json, getType());
    }
}
