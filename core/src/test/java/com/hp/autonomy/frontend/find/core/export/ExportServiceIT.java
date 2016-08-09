/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.core.test.TestUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
public abstract class ExportServiceIT<S extends Serializable, E extends Exception> {
    @Autowired
    protected ExportService<S, E> exportService;

    @Autowired
    protected TestUtils<S> testUtils;

    @Test
    public void exportToCsv() throws E {
        final SearchRequest<S> searchRequest = new SearchRequest.Builder<S>()
                .setQueryRestrictions(testUtils.buildQueryRestrictions())
                .setQueryType(SearchRequest.QueryType.MODIFIED)
                .build();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.export(outputStream, searchRequest, ExportFormat.CSV);
        assertNotNull(outputStream.toString());
    }
}
