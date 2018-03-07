/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.find.core.export.ExportControllerTest;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

abstract class IdolExportControllerTest extends ExportControllerTest<IdolQueryRequest, AciErrorException> {
    @MockBean
    private IdolDocumentsService documentsService;
    @Mock
    private IdolQueryRequest queryRequest;
    @Mock
    private IdolQueryRequestBuilder queryRequestBuilder;
    @Mock
    private IdolQueryRestrictions queryRestrictions;
    @Mock
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;
    @MockBean
    private ConfigFileService configFileService;

    @Override
    protected void mockRequestObjects() throws IOException {
        when(requestMapper.parseQueryRequest(any())).thenReturn(queryRequest);

        when(queryRequest.toBuilder()).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryRestrictions(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.start(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.maxResults(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.build()).thenReturn(queryRequest);

        when(queryRequest.getQueryRestrictions()).thenReturn(queryRestrictions);
        when(queryRestrictions.toBuilder()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateMatchId(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.build()).thenReturn(queryRestrictions);
    }

    @Override
    protected void mockNumberOfResults(final int numberOfResults) throws AciErrorException {
        when(documentsService.getStateTokenAndResultCount(any(), anyInt(), anyBoolean())).thenReturn(new StateTokenAndResultCount(new TypedStateToken(), numberOfResults));
    }
}
