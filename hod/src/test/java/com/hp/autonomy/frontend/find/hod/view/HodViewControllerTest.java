/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.view;

import com.hp.autonomy.frontend.find.core.view.AbstractViewControllerTest;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodError;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.view.ViewServerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
public class HodViewControllerTest extends AbstractViewControllerTest<HodViewController, ResourceIdentifier, HodErrorException> {
    @Mock
    private ViewServerService<ResourceIdentifier, HodErrorException> hodViewService;

    @Override
    @Before
    public void setUp() {
        viewServerService = hodViewService;
        viewController = new HodViewController(viewServerService, controllerUtils);
        response = new MockHttpServletResponse();
        super.setUp();
    }

    @Override
    protected ResourceIdentifier getSampleDatabase() {
        return ResourceIdentifier.WIKI_ENG;
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
