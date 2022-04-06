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

package com.hp.autonomy.frontend.find.idol.export.service;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportStrategy;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.configuration.AciServiceRetriever;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolPlatformDataExportServiceTest {
    @Mock
    private PlatformDataExportStrategy exportStrategy;
    @Mock
    private HavenSearchAciParameterHandler parameterHandler;
    @Mock
    private AciServiceRetriever aciServiceRetriever;
    @Mock
    private AciService aciService;
    @Mock
    private OutputStream outputStream;
    @Mock
    private IdolQueryRequest queryRequest;
    @Mock
    private IdolQueryRequestBuilder idolQueryRequestBuilder;

    private IdolPlatformDataExportService idolExportService;

    @Before
    public void setUp() {
        when(exportStrategy.getExportFormat()).thenReturn(ExportFormat.CSV);
        when(aciServiceRetriever.getAciService(any(QueryRequest.QueryType.class))).thenReturn(aciService);
        when(queryRequest.toBuilder()).thenReturn(idolQueryRequestBuilder);
        when(idolQueryRequestBuilder.start(anyInt())).thenReturn(idolQueryRequestBuilder);
        when(idolQueryRequestBuilder.maxResults(anyInt())).thenReturn(idolQueryRequestBuilder);
        when(idolQueryRequestBuilder.build()).thenReturn(queryRequest);

        idolExportService = new IdolPlatformDataExportService(parameterHandler, aciServiceRetriever, new PlatformDataExportStrategy[]{exportStrategy});
    }

    @Test
    public void export() throws IOException {
        idolExportService.exportQueryResults(outputStream, queryRequest, ExportFormat.CSV, Collections.emptyList(), 10L);
        verify(aciService).executeAction(anySetOf(AciParameter.class), any());
    }
}
