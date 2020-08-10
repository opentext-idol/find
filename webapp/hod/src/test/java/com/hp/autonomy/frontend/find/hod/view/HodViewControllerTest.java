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

package com.hp.autonomy.frontend.find.hod.view;

import com.hp.autonomy.frontend.find.core.view.AbstractViewControllerTest;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodError;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.view.HodViewRequest;
import com.hp.autonomy.searchcomponents.hod.view.HodViewRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.view.HodViewServerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HodViewControllerTest extends AbstractViewControllerTest<HodViewController, HodViewRequest, ResourceName, HodErrorException> {
    @Mock
    private HodViewServerService hodViewServerService;
    @Mock
    private ObjectFactory<HodViewRequestBuilder> viewRequestBuilderFactory;
    @Mock
    private HodViewRequestBuilder viewRequestBuilder;

    @Override
    @Before
    public void setUp() {
        when(viewRequestBuilderFactory.getObject()).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.documentReference(any())).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.database(any())).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.highlightExpression(any())).thenReturn(viewRequestBuilder);
        when(viewRequestBuilder.original(anyBoolean())).thenReturn(viewRequestBuilder);

        viewController = new HodViewController(hodViewServerService, viewRequestBuilderFactory, controllerUtils);
        viewServerService = hodViewServerService;
        response = new MockHttpServletResponse();
        super.setUp();
    }

    @Override
    protected ResourceName getSampleDatabase() {
        return ResourceName.WIKI_ENG;
    }

    @Test
    public void viewStaticContentPromotion() throws IOException, HodErrorException {
        final String reference = "SomeReference";
        viewController.viewStaticContentPromotion(reference, new MockHttpServletResponse());
        verify(viewServerService).viewStaticContentPromotion(eq(reference), any(OutputStream.class));
    }

    @Test
    public void handleKnownHodErrorException() {
        when(controllerUtils.getMessage(anyString(), any(Object[].class))).thenReturn("Some known error message");
        assertNotNull(viewController.handleHodErrorException(new HodErrorException(new HodError.Builder().build(), 400), new MockHttpServletRequest(), new MockHttpServletResponse()));
        verify(controllerUtils).buildErrorModelAndView(any(ErrorModelAndViewInfo.class));
    }

    @Test
    public void handleUnknownHodErrorException() {
        when(controllerUtils.getMessage(anyString(), any(Object[].class))).thenThrow(new NoSuchMessageException("")).thenReturn(null);
        assertNotNull(viewController.handleHodErrorException(new HodErrorException(new HodError.Builder().build(), 400), new MockHttpServletRequest(), new MockHttpServletResponse()));
        verify(controllerUtils).buildErrorModelAndView(any(ErrorModelAndViewInfo.class));
    }

    @Test
    public void hodAuthenticationFailedException() {
        assertNotNull(viewController.hodAuthenticationFailedException(mock(HodAuthenticationFailedException.class), new MockHttpServletRequest(), new MockHttpServletResponse()));
        verify(controllerUtils).buildErrorModelAndView(any(ErrorModelAndViewInfo.class));
    }

    @Test
    public void handleGeneralException() {
        assertNotNull(viewController.handleGeneralException(new Exception(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        verify(controllerUtils).buildErrorModelAndView(any(ErrorModelAndViewInfo.class));
    }
}
