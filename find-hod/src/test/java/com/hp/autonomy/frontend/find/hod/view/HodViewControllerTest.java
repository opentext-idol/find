/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.view;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.view.AbstractViewControllerTest;
import com.hp.autonomy.frontend.find.core.web.AuthenticationInformationRetriever;
import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodError;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.hod.sso.HodAuthenticationPrincipal;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationCapable;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import com.hp.autonomy.searchcomponents.hod.view.HodViewService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.NoSuchMessageException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HodViewControllerTest extends AbstractViewControllerTest<HodViewController, HodViewService, ResourceIdentifier, HodErrorException> {
    @Mock
    private HodViewService hodViewService;

    @Mock
    private ConfigService<? extends QueryManipulationCapable> configService;

    @Mock
    private AuthenticationInformationRetriever<HodAuthentication> authenticationInformationRetriever;

    @Mock
    private HodAuthentication hodAuthentication;

    @Mock
    private HodAuthenticationPrincipal hodAuthenticationPrincipal;

    @Mock
    private QueryManipulationCapable config;

    @Override
    @Before
    public void setUp() {
        viewServerService = hodViewService;
        viewController = new HodViewController(viewServerService, configService, authenticationInformationRetriever, controllerUtils);
        response = new MockHttpServletResponse();
        super.setUp();

        when(config.getQueryManipulation()).thenReturn(new QueryManipulationConfig("SomeProfile", "SomeIndex"));
        when(configService.getConfig()).thenReturn(config);

        when(hodAuthenticationPrincipal.getApplication()).thenReturn(new ResourceIdentifier("SomeDomain", "SomeIndex"));
        when(hodAuthentication.getPrincipal()).thenReturn(hodAuthenticationPrincipal);
        when(authenticationInformationRetriever.getAuthentication()).thenReturn(hodAuthentication);
    }

    @Override
    protected ResourceIdentifier getSampleDatabase() {
        return ResourceIdentifier.WIKI_ENG;
    }

    @Test
    public void viewStaticContentPromotion() throws IOException, HodErrorException {
        final String reference = "SomeReference";
        viewController.viewStaticContentPromotion(reference, new MockHttpServletResponse());
        verify(viewServerService).viewStaticContentPromotion(eq(reference), any(ResourceIdentifier.class), any(OutputStream.class));
    }

    @Test
    public void handleKnownHodErrorException() {
        when(controllerUtils.getMessage(anyString(), any(Object[].class))).thenReturn("Some known error message");
        assertNotNull(viewController.handleHodErrorException(new HodErrorException(new HodError.Builder().build(), 400), new MockHttpServletRequest(), new MockHttpServletResponse()));
        verify(controllerUtils).buildErrorModelAndView(any(HttpServletRequest.class), eq(HodViewController.HOD_ERROR_MESSAGE_CODE_MAIN), eq(HodViewController.HOD_ERROR_MESSAGE_CODE_SUB), any(Object[].class), any(Integer.class), eq(true));
    }

    @Test
    public void handleUnknownHodErrorException() {
        when(controllerUtils.getMessage(anyString(), any(Object[].class))).thenThrow(new NoSuchMessageException("")).thenReturn(null);
        assertNotNull(viewController.handleHodErrorException(new HodErrorException(new HodError.Builder().build(), 400), new MockHttpServletRequest(), new MockHttpServletResponse()));
        verify(controllerUtils).buildErrorModelAndView(any(HttpServletRequest.class), eq(HodViewController.HOD_ERROR_MESSAGE_CODE_MAIN), eq(HodViewController.HOD_ERROR_MESSAGE_CODE_SUB_NULL), any(Object[].class), any(Integer.class), eq(true));
    }

    @Test
    public void hodAuthenticationFailedException() {
        assertNotNull(viewController.hodAuthenticationFailedException(mock(HodAuthenticationFailedException.class), new MockHttpServletRequest(), new MockHttpServletResponse()));
        verify(controllerUtils).buildErrorModelAndView(any(HttpServletRequest.class), eq(HodViewController.HOD_ERROR_MESSAGE_CODE_MAIN), eq(HodViewController.HOD_ERROR_MESSAGE_CODE_TOKEN_EXPIRED), any(Object[].class), eq(HttpServletResponse.SC_FORBIDDEN), eq(false));
    }

    @Test
    public void handleGeneralException() {
        assertNotNull(viewController.handleGeneralException(new Exception(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        verify(controllerUtils).buildErrorModelAndView(any(HttpServletRequest.class), eq(HodViewController.HOD_ERROR_MESSAGE_CODE_INTERNAL_MAIN), eq(HodViewController.HOD_ERROR_MESSAGE_CODE_INTERNAL_SUB), any(Object[].class), eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), eq(true));
    }
}
