/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.find.core.export.ExportControllerTest;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.types.requests.Documents;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

abstract class HodExportControllerTest extends ExportControllerTest<HodQueryRequest, HodErrorException> {
    @MockBean
    private HodDocumentsService documentsService;
    @Mock
    private HodQueryRequest queryRequest;
    @Mock
    private HodQueryRequestBuilder queryRequestBuilder;

    @Override
    protected void mockRequestObjects() throws IOException {
        when(requestMapper.parseQueryRequest(any())).thenReturn(queryRequest);
        when(queryRequest.toBuilder()).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.start(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.maxResults(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.print(anyString())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.build()).thenReturn(queryRequest);

        when(queryRequest.getMaxResults()).thenReturn(Integer.MAX_VALUE);
    }

    @Override
    protected void mockNumberOfResults(final int numberOfResults) throws HodErrorException {
        when(documentsService.queryTextIndex(any())).thenReturn(new Documents<>(Collections.emptyList(), numberOfResults, null, null, null, null));
    }
}
