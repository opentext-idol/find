/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.web.AbstractRequestMapper;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HodRequestMapper extends AbstractRequestMapper<ResourceIdentifier> {
    @Override
    protected void addCustomMixins(final ObjectMapper objectMapper) {
        objectMapper.addMixIn(HodQueryRestrictions.Builder.class, HodQueryRestrictionsMixins.class);
    }

    @Override
    protected Class<ResourceIdentifier> getDatabaseType() {
        return ResourceIdentifier.class;
    }

    @Override
    protected Class<? extends SearchRequestMixins> getSearchRequestMixinType() {
        return HodSearchRequestMixins.class;
    }

    @SuppressWarnings("unused")
    private abstract static class HodQueryRestrictionsMixins {
        @JsonProperty(value = "text", required = true)
        private String queryText;
        @JsonProperty("field_text")
        private String fieldText;
        @JsonProperty(value = "indexes", required = true)
        private List<ResourceIdentifier> databases;
        @JsonProperty("min_date")
        private DateTime minDate;
        @JsonProperty("max_date")
        private DateTime maxDate;
        @JsonProperty("min_score")
        private Integer minScore;
    }

    @SuppressWarnings("unused")
    private static class HodSearchRequestMixins extends SearchRequestMixins {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "queryRestrictions", defaultImpl = HodQueryRestrictions.class)
        private QueryRestrictions<String> queryRestrictions;
    }
}
