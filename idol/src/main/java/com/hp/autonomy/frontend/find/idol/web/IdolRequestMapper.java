/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.web.AbstractRequestMapper;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IdolRequestMapper extends AbstractRequestMapper<String> {
    @Override
    protected void addCustomMixins(final ObjectMapper objectMapper) {
        objectMapper.addMixIn(IdolQueryRestrictions.Builder.class, IdolQueryRestrictionsMixins.class);
    }

    @Override
    protected Class<String> getDatabaseType() {
        return String.class;
    }

    @Override
    protected Class<? extends SearchRequestMixins> getSearchRequestMixinType() {
        return IdolSearchRequestMixins.class;
    }

    @SuppressWarnings("unused")
    private abstract static class IdolQueryRestrictionsMixins {
        @JsonProperty(value = "text", required = true)
        private String queryText;
        @JsonProperty("field_text")
        private String fieldText;
        @JsonProperty("indexes")
        private List<String> databases;
        @JsonProperty("min_date")
        private DateTime minDate;
        @JsonProperty("max_date")
        private DateTime maxDate;
        @JsonProperty("min_score")
        private Integer minScore;
    }

    @SuppressWarnings("unused")
    private static class IdolSearchRequestMixins extends SearchRequestMixins {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "queryRestrictions", defaultImpl = IdolQueryRestrictions.class)
        private QueryRestrictions<String> queryRestrictions;
    }
}
