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

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class RequestMapperTest<R extends QueryRequest<Q>, Q extends QueryRestrictions<S>, S extends Serializable> {
    private RequestMapper<R> requestMapper;

    protected abstract RequestMapper<R> constructRequestMapper();

    protected abstract String completeJsonObject() throws IOException;

    protected abstract String minimalJsonObject() throws IOException;

    protected abstract void validateDatabases(final List<S> databases);

    protected abstract void validate(final R queryRequest);

    protected abstract void validateMinimal(final R queryRequest);

    @Before
    public void setUp() {
        requestMapper = constructRequestMapper();
    }

    @Test
    public void jsonToQueryRequest() throws IOException {
        final String json = completeJsonObject();
        final R queryRequest = requestMapper.parseQueryRequest(json);

        assertNotNull(queryRequest.getQueryRestrictions());
        assertThat(queryRequest.getQueryRestrictions().getQueryText(), is("Homer"));
        assertThat(queryRequest.getQueryRestrictions().getFieldText(), is("MATCH{Iliad}:WORK"));
        validateDatabases(queryRequest.getQueryRestrictions().getDatabases());
        assertNull(queryRequest.getQueryRestrictions().getMinDate());
        assertNotNull(queryRequest.getQueryRestrictions().getMaxDate());
        assertThat(queryRequest.getQueryRestrictions().getMinScore(), is(5));

        assertThat(queryRequest.getStart(), is(10));
        assertThat(queryRequest.getMaxResults(), is(30));
        assertThat(queryRequest.isHighlight(), is(true));
        assertThat(queryRequest.isAutoCorrect(), is(true));
        assertThat(queryRequest.getQueryType(), is(QueryRequest.QueryType.RAW));

        validate(queryRequest);
    }

    @Test
    public void minimalJsonToQueryRequest() throws IOException {
        final String json = minimalJsonObject();
        final R queryRequest = requestMapper.parseQueryRequest(json);

        assertNotNull(queryRequest.getQueryRestrictions());
        assertThat(queryRequest.getQueryRestrictions().getQueryText(), is("Homer"));
        assertNull(queryRequest.getQueryRestrictions().getFieldText());
        validateDatabases(queryRequest.getQueryRestrictions().getDatabases());
        assertNull(queryRequest.getQueryRestrictions().getMinDate());
        assertNull(queryRequest.getQueryRestrictions().getMaxDate());
        assertNull(queryRequest.getQueryRestrictions().getMinScore());

        assertThat(queryRequest.getStart(), is(1));
        assertThat(queryRequest.getMaxResults(), is(30));
        assertThat(queryRequest.isHighlight(), is(false));
        assertThat(queryRequest.isAutoCorrect(), is(false));
        assertThat(queryRequest.getQueryType(), is(QueryRequest.QueryType.MODIFIED));

        validateMinimal(queryRequest);
    }
}
