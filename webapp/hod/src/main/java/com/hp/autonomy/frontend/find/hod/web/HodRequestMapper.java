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

package com.hp.autonomy.frontend.find.hod.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.web.AbstractRequestMapper;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.hod.requests.HodQueryRequestMixin;
import com.hp.autonomy.searchcomponents.hod.requests.HodQueryRestrictionsMixin;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
class HodRequestMapper extends AbstractRequestMapper<HodQueryRequest> {
    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public HodRequestMapper(final HodQueryRestrictionsBuilder queryRestrictionsBuilder,
                            final HodQueryRequestBuilder queryRequestBuilder) {
        super(queryRestrictionsBuilder, queryRequestBuilder);
    }

    @Override
    protected void addCustomMixins(final ObjectMapper objectMapper, final QueryRestrictionsBuilder<?, ?, ?> queryRestrictionsBuilder, final QueryRequestBuilder<?, ?, ?> queryRequestBuilder) {
        objectMapper.addMixIn(HodQueryRequest.class, HodQueryRequestMixin.class);
        objectMapper.addMixIn(queryRequestBuilder.getClass(), HodQueryRequestBuilderMixins.class);
        objectMapper.addMixIn(HodQueryRestrictions.class, HodQueryRestrictionsMixin.class);
        objectMapper.addMixIn(queryRestrictionsBuilder.getClass(), HodQueryRestrictionsBuilderMixins.class);
    }

    @Override
    protected Class<HodQueryRequest> getType() {
        return HodQueryRequest.class;
    }

    @SuppressWarnings("unused")
    private interface HodQueryRequestBuilderMixins {
        @JsonProperty(value = "max_results", required = true)
        HodQueryRequestBuilder maxResults(int maxResults);

        @JsonProperty(required = true)
        HodQueryRequestBuilder summary(String summary);

        @JsonProperty("auto_correct")
        HodQueryRequestBuilder autoCorrect(boolean autoCorrect);
    }

    @SuppressWarnings("unused")
    private abstract static class HodQueryRestrictionsBuilderMixins {
        @JsonProperty(value = "text", required = true)
        private String queryText;
        @JsonProperty("field_text")
        private String fieldText;
        @JsonProperty("indexes")
        private List<ResourceIdentifier> databases;
        @JsonProperty("min_date")
        private ZonedDateTime minDate;
        @JsonProperty("max_date")
        private ZonedDateTime maxDate;
        @JsonProperty("min_score")
        private Integer minScore;
    }
}
