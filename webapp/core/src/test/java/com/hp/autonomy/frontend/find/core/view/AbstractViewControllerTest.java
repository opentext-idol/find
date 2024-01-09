/*
 * Copyright 2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.view;

import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.searchcomponents.core.view.ViewRequest;
import com.hp.autonomy.searchcomponents.core.view.ViewServerService;
import com.hp.autonomy.searchcomponents.core.view.ViewingPart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractViewControllerTest<C extends ViewController<R, S, E>, R extends ViewRequest<S>, S extends Serializable, E extends Exception> {
    @Mock
    protected ControllerUtils controllerUtils;

    protected C viewController;
    protected ViewServerService<R, S, E> viewServerService;
    protected MockHttpServletResponse response;

    @Before
    public void setUp() {
        response = new MockHttpServletResponse();
        when(controllerUtils.buildErrorModelAndView(any(ErrorModelAndViewInfo.class))).thenReturn(mock(ModelAndView.class));
    }

    protected abstract S getSampleDatabase();

    @Test
    public void viewDocument() throws E, IOException {
        final String reference = "SomeReference";
        final S sampleDatabase = getSampleDatabase();
        viewController.viewDocument(reference, sampleDatabase, null, ViewingPart.DOCUMENT, null, null, response);

        verify(viewServerService).viewDocument(any(), any(OutputStream.class));
        Assert.assertEquals("text/html", response.getContentType());
    }

    @Test
    public void viewDocument_original() throws E, IOException {
        viewController.viewDocument("SomeReference", getSampleDatabase(), null, ViewingPart.ORIGINAL, null, null, response);
        Assert.assertEquals("application/octet-stream", response.getContentType());
    }

}
