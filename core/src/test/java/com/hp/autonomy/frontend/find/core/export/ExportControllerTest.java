/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class ExportControllerTest<S extends Serializable, E extends Exception> {
    @Mock
    protected ExportService<S, E> exportService;
    @Mock
    protected RequestMapper<S> requestMapper;
    @Mock
    protected ControllerUtils controllerUtils;

    private ExportController<S, E> controller;

    protected abstract ExportController<S, E> constructController();

    @Before
    public void setUp() {
        controller = constructController();
    }

    @Test
    public void exportToCsv() throws IOException, E {
        controller.exportToCsv("{}");
        verify(exportService).export(any(OutputStream.class), Matchers.<SearchRequest<S>>any(), eq(ExportFormat.CSV));
    }

    @Test
    public void handleException() {
        controller.handleException(new IOException(""), new MockHttpServletRequest(), new MockHttpServletResponse());
        verify(controllerUtils).buildErrorModelAndView(any(ErrorModelAndViewInfo.class));
    }
}
