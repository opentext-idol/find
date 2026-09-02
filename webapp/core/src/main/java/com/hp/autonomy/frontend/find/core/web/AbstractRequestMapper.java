/*
 * Copyright 2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

public abstract class AbstractRequestMapper<R extends QueryRequest<?>> implements RequestMapper<R> {
    private final ObjectMapper objectMapper;

    protected AbstractRequestMapper(final QueryRestrictionsBuilder<?, ?, ?> queryRestrictionsBuilder,
                                    final QueryRequestBuilder<?, ?, ?> queryRequestBuilder) {
        objectMapper = withCustomMixins(JsonMapper.builder(), queryRestrictionsBuilder, queryRequestBuilder).build();
    }

    protected abstract JsonMapper.Builder withCustomMixins(final JsonMapper.Builder builder, final QueryRestrictionsBuilder<?, ?, ?> queryRestrictionsBuilder, final QueryRequestBuilder<?, ?, ?> queryRequestBuilder);

    protected abstract Class<R> getType();

    @Override
    public R parseQueryRequest(final String json) throws IOException {
        return objectMapper.readValue(json, getType());
    }
}
