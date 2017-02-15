/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import static com.hp.autonomy.frontend.find.core.export.ExportController.PAGINATION_SIZE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class ExportControllerTest<R extends QueryRequest<?>, E extends Exception> {
    @Mock
    protected ExportService<R, E> exportService;
    @Mock
    protected RequestMapper<R> requestMapper;
    @Mock
    protected ControllerUtils controllerUtils;
    @Mock
    protected ObjectMapper objectMapper;

    private ExportController<R, E> controller;

    protected abstract ExportController<R, E> constructController() throws IOException;

    protected abstract void mockNumberOfResults(int numberOfResults) throws E;

    @Before
    public void setUp() throws IOException {
        controller = constructController();
    }

    @Test
    public void exportToCsv() throws IOException, E {
        mockNumberOfResults(PAGINATION_SIZE);
        controller.exportToCsv("{}", Collections.emptyList());
        verify(exportService).export(any(OutputStream.class), any(), eq(ExportFormat.CSV), eq(Collections.emptyList()));
    }

    @Test
    public void exportToCsvNoResults() throws IOException, E {
        mockNumberOfResults(0);
        controller.exportToCsv("{}", Collections.emptyList());
        verify(exportService, never()).export(any(), any(), any(), any());
    }

    @Test
    public void exportToCsvMultipleResults() throws IOException, E {
        mockNumberOfResults(2 * PAGINATION_SIZE + 1);
        controller.exportToCsv("{}", Collections.emptyList());
        verify(exportService, times(3)).export(any(OutputStream.class), any(), eq(ExportFormat.CSV), eq(Collections.emptyList()));
    }

    @Test
    public void handleException() {
        controller.handleException(new IOException(""), new MockHttpServletRequest(), new MockHttpServletResponse());
        verify(controllerUtils).buildErrorModelAndView(any(ErrorModelAndViewInfo.class));
    }
}
