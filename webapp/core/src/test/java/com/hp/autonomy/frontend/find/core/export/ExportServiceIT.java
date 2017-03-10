/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.test.TestUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
public abstract class ExportServiceIT<R extends QueryRequest<Q>, Q extends QueryRestrictions<?>, E extends Exception> {
    @Autowired
    protected ExportService<R, E> exportService;

    @Autowired
    protected TestUtils<Q> testUtils;

    @Autowired
    protected ObjectFactory<QueryRequestBuilder<R, Q, ?>> queryRequestBuilderFactory;

    @Test
    public void exportToCsv() throws E, IOException {
        final R queryRequest = queryRequestBuilderFactory.getObject()
                .queryRestrictions(testUtils.buildQueryRestrictions())
                .queryType(QueryRequest.QueryType.MODIFIED)
                .build();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.export(outputStream, queryRequest, ExportFormat.CSV, Collections.emptyList(), 1001L);
        assertNotNull(outputStream.toString());
    }
}
