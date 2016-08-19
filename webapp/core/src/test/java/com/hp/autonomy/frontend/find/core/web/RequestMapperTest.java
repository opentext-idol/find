/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class RequestMapperTest<S extends Serializable> {
    private RequestMapper<S> requestMapper;

    protected abstract RequestMapper<S> constructRequestMapper();

    protected abstract String completeJsonObject() throws IOException;

    protected abstract String minimalJsonObject() throws IOException;

    protected abstract void validateDatabases(final List<S> databases);

    @Before
    public void setUp() {
        requestMapper = constructRequestMapper();
    }

    @Test
    public void jsonToSearchRequest() throws IOException {
        final String json = completeJsonObject();
        final SearchRequest<S> searchRequest = requestMapper.parseSearchRequest(json);

        assertNotNull(searchRequest.getQueryRestrictions());
        assertThat(searchRequest.getQueryRestrictions().getQueryText(), is("Homer"));
        assertThat(searchRequest.getQueryRestrictions().getFieldText(), is("MATCH{Iliad}:WORK"));
        validateDatabases(searchRequest.getQueryRestrictions().getDatabases());
        assertNull(searchRequest.getQueryRestrictions().getMinDate());
        assertNotNull(searchRequest.getQueryRestrictions().getMaxDate());
        assertThat(searchRequest.getQueryRestrictions().getMinScore(), is(5));

        assertThat(searchRequest.getStart(), is(10));
        assertThat(searchRequest.getMaxResults(), is(30));
        assertThat(searchRequest.getSummary(), is("off"));
        assertThat(searchRequest.getSort(), is("DocumentCount"));
        assertThat(searchRequest.isHighlight(), is(true));
        assertThat(searchRequest.isAutoCorrect(), is(true));
        assertThat(searchRequest.getQueryType(), is(SearchRequest.QueryType.RAW));
    }

    @Test
    public void minimalJsonToSearchRequest() throws IOException {
        final String json = minimalJsonObject();
        final SearchRequest<S> searchRequest = requestMapper.parseSearchRequest(json);

        assertNotNull(searchRequest.getQueryRestrictions());
        assertThat(searchRequest.getQueryRestrictions().getQueryText(), is("Homer"));
        assertNull(searchRequest.getQueryRestrictions().getFieldText());
        validateDatabases(searchRequest.getQueryRestrictions().getDatabases());
        assertNull(searchRequest.getQueryRestrictions().getMinDate());
        assertNull(searchRequest.getQueryRestrictions().getMaxDate());
        assertNull(searchRequest.getQueryRestrictions().getMinScore());

        assertThat(searchRequest.getStart(), is(1));
        assertThat(searchRequest.getMaxResults(), is(30));
        assertThat(searchRequest.getSummary(), is("off"));
        assertNull(searchRequest.getSort());
        assertThat(searchRequest.isHighlight(), is(false));
        assertThat(searchRequest.isAutoCorrect(), is(false));
        assertThat(searchRequest.getQueryType(), is(SearchRequest.QueryType.MODIFIED));
    }
}
