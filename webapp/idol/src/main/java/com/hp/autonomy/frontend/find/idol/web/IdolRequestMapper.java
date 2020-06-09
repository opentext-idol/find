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

package com.hp.autonomy.frontend.find.idol.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.web.AbstractRequestMapper;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.requests.IdolQueryRequestMixin;
import com.hp.autonomy.searchcomponents.idol.requests.IdolQueryRestrictionsMixin;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static com.hp.autonomy.searchcomponents.core.search.QueryRestrictions.QUERY_RESTRICTIONS_BUILDER_BEAN_NAME;

@Component
class IdolRequestMapper extends AbstractRequestMapper<IdolQueryRequest> {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolRequestMapper(@Qualifier(QUERY_RESTRICTIONS_BUILDER_BEAN_NAME)
                             final IdolQueryRestrictionsBuilder queryRestrictionsBuilder,
                             final IdolQueryRequestBuilder queryRequestBuilder) {
        super(queryRestrictionsBuilder, queryRequestBuilder);
    }

    @Override
    protected void addCustomMixins(final ObjectMapper objectMapper, final QueryRestrictionsBuilder<?, ?, ?> queryRestrictionsBuilder, final QueryRequestBuilder<?, ?, ?> queryRequestBuilder) {
        objectMapper.addMixIn(IdolQueryRequest.class, IdolQueryRequestMixin.class);
        objectMapper.addMixIn(queryRequestBuilder.getClass(), IdolQueryRequestBuilderMixins.class);
        objectMapper.addMixIn(IdolQueryRestrictions.class, IdolQueryRestrictionsMixin.class);
        objectMapper.addMixIn(queryRestrictionsBuilder.getClass(), IdolQueryRestrictionsBuilderMixins.class);
    }

    @Override
    protected Class<IdolQueryRequest> getType() {
        return IdolQueryRequest.class;
    }

    @Override
    public IdolQueryRequest parseQueryRequest(final String json) throws IOException {
        final IdolQueryRequest queryRequest = super.parseQueryRequest(json);
        return queryRequest
                .toBuilder()
                .queryRestrictions(queryRequest.getQueryRestrictions()
                        .toBuilder()
                        .anyLanguage(true)
                        .build())
                .build();
    }

    @SuppressWarnings("unused")
    private interface IdolQueryRequestBuilderMixins {
        @JsonProperty(value = "max_results", required = true)
        IdolQueryRequestBuilder maxResults(int maxResults);

        @JsonProperty(required = true)
        IdolQueryRequestBuilder summary(String summary);

        @JsonProperty("auto_correct")
        IdolQueryRequestBuilder autoCorrect(boolean autoCorrect);
    }

    @SuppressWarnings("unused")
    private abstract static class IdolQueryRestrictionsBuilderMixins {
        @JsonProperty(value = "text", required = true)
        private String queryText;
        @JsonProperty("field_text")
        private String fieldText;
        @JsonProperty("indexes")
        private List<String> databases;
        @JsonProperty("min_date")
        private ZonedDateTime minDate;
        @JsonProperty("max_date")
        private ZonedDateTime maxDate;
        @JsonProperty("min_score")
        private Integer minScore;
    }
}
