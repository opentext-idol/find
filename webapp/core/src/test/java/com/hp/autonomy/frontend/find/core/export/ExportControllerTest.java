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

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportService;
import com.hp.autonomy.frontend.find.core.export.service.VisualDataExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
public abstract class ExportControllerTest<R extends QueryRequest<?>, E extends Exception> {
    @MockBean
    protected RequestMapper<R> requestMapper;
    @MockBean
    private ControllerUtils controllerUtils;
    @MockBean
    private ExportServiceFactory<R, E> exportServiceFactory;
    @Mock
    private PlatformDataExportService<R, E> platformDataExportService;
    @Mock
    private VisualDataExportService visualDataExportService;
    @Autowired
    private ExportController<R, E> controller;

    protected abstract void mockRequestObjects() throws IOException;

    protected abstract void mockNumberOfResults(int numberOfResults) throws E;

    @Before
    public void setUp() throws IOException {
        when(exportServiceFactory.getPlatformDataExportService(any())).thenReturn(Optional.of(platformDataExportService));
        when(exportServiceFactory.getVisualDataExportService(any())).thenReturn(Optional.of(visualDataExportService));

        mockRequestObjects();
    }

    @Test
    public void exportQueryResults() throws IOException, E {
        mockNumberOfResults(PlatformDataExportService.PAGINATION_SIZE);
        controller.exportQueryResults("{}", Collections.emptyList());
        verify(platformDataExportService).exportQueryResults(any(OutputStream.class), any(), any(), eq(Collections.emptyList()), anyInt());
    }

    @Test
    public void exportQueryResultsNoResults() throws IOException, E {
        mockNumberOfResults(0);
        controller.exportQueryResults("{}", Collections.emptyList());
        verify(platformDataExportService, times(1)).exportQueryResults(any(), any(), any(), any(), anyInt());
    }

    @Test
    public void exportQueryResultsMultipleResults() throws IOException, E {
        mockNumberOfResults(2 * PlatformDataExportService.PAGINATION_SIZE + 1);
        controller.exportQueryResults("{}", Collections.emptyList());
        verify(platformDataExportService, times(1)).exportQueryResults(any(OutputStream.class), any(), any(), eq(Collections.emptyList()), anyInt());
    }

    @Test
    public void handleException() {
        controller.handleException(new IOException(""), new MockHttpServletRequest(), new MockHttpServletResponse());
        verify(controllerUtils).buildErrorModelAndView(any(ErrorModelAndViewInfo.class));
    }

    @Test
    public void topicMap() throws Exception {
        controller.topicMap("{}");
        verify(visualDataExportService).topicMap(any(), any());
    }

    @Test
    public void sunburst() throws Exception {
        controller.sunburst("{}");
        verify(visualDataExportService).sunburst(any(), any());
    }

    @Test
    public void table() throws Exception {
        controller.table("{}", "");
        verify(visualDataExportService).table(any(), any(), anyString());
    }

    @Test
    public void map() throws Exception {
        controller.map("{}", "");
        verify(visualDataExportService).map(any(), any(), anyString());
    }

    @Test
    public void list() throws Exception {
        controller.list("{}", "", "");
        verify(visualDataExportService).list(any(), any(), anyString(), anyString());
    }

    @Test
    public void dateGraph() throws Exception {
        controller.dateGraph("{}");
        verify(visualDataExportService).dateGraph(any(), any());
    }

    @Test
    public void report() throws Exception {
        controller.report("{}", false);
        verify(visualDataExportService).report(any(), any(), anyBoolean());
    }
}
