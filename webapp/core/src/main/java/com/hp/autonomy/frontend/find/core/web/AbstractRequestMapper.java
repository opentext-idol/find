/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;

import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractRequestMapper<S extends Serializable> implements RequestMapper<S> {
    private final ObjectMapper objectMapper;

    protected AbstractRequestMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.addMixIn(SearchRequest.class, getSearchRequestMixinType());
        addCustomMixins(objectMapper);
    }

    protected abstract void addCustomMixins(final ObjectMapper objectMapper);

    protected abstract Class<S> getDatabaseType();

    protected abstract Class<? extends SearchRequestMixins> getSearchRequestMixinType();

    @Override
    public SearchRequest<S> parseSearchRequest(final String json) throws IOException {
        final JavaType type = objectMapper.getTypeFactory().constructParametrizedType(SearchRequest.class, SearchRequest.class, getDatabaseType());
        return objectMapper.readValue(json, type);
    }

    @SuppressWarnings("unused")
    public static class SearchRequestMixins {
        @JsonProperty(value = "max_results", required = true)
        private Integer maxResults;
        @JsonProperty(required = true)
        private String summary;
        @JsonProperty("auto_correct")
        private Boolean autoCorrect;
    }
}
