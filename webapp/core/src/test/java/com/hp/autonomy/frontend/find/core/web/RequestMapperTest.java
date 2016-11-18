/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
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
    public void jsonToQueryRequest() throws IOException {
        final String json = completeJsonObject();
        final QueryRequest<S> queryRequest = requestMapper.parseQueryRequest(json);

        assertNotNull(queryRequest.getQueryRestrictions());
        assertThat(queryRequest.getQueryRestrictions().getQueryText(), is("Homer"));
        assertThat(queryRequest.getQueryRestrictions().getFieldText(), is("MATCH{Iliad}:WORK"));
        validateDatabases(queryRequest.getQueryRestrictions().getDatabases());
        assertNull(queryRequest.getQueryRestrictions().getMinDate());
        assertNotNull(queryRequest.getQueryRestrictions().getMaxDate());
        assertThat(queryRequest.getQueryRestrictions().getMinScore(), is(5));

        assertThat(queryRequest.getStart(), is(10));
        assertThat(queryRequest.getMaxResults(), is(30));
        assertThat(queryRequest.getSummary(), is("off"));
        assertThat(queryRequest.getSort(), is("DocumentCount"));
        assertThat(queryRequest.isHighlight(), is(true));
        assertThat(queryRequest.isAutoCorrect(), is(true));
        assertThat(queryRequest.getQueryType(), is(QueryRequest.QueryType.RAW));
    }

    @Test
    public void minimalJsonToQueryRequest() throws IOException {
        final String json = minimalJsonObject();
        final QueryRequest<S> queryRequest = requestMapper.parseQueryRequest(json);

        assertNotNull(queryRequest.getQueryRestrictions());
        assertThat(queryRequest.getQueryRestrictions().getQueryText(), is("Homer"));
        assertNull(queryRequest.getQueryRestrictions().getFieldText());
        validateDatabases(queryRequest.getQueryRestrictions().getDatabases());
        assertNull(queryRequest.getQueryRestrictions().getMinDate());
        assertNull(queryRequest.getQueryRestrictions().getMaxDate());
        assertNull(queryRequest.getQueryRestrictions().getMinScore());

        assertThat(queryRequest.getStart(), is(1));
        assertThat(queryRequest.getMaxResults(), is(30));
        assertThat(queryRequest.getSummary(), is("off"));
        assertNull(queryRequest.getSort());
        assertThat(queryRequest.isHighlight(), is(false));
        assertThat(queryRequest.isAutoCorrect(), is(false));
        assertThat(queryRequest.getQueryType(), is(QueryRequest.QueryType.MODIFIED));
    }
}
