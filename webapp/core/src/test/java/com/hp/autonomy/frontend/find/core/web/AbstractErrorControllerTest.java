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

package com.hp.autonomy.frontend.find.core.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.net.MalformedURLException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractErrorControllerTest<T extends CustomErrorController> {
    @Mock
    protected ControllerUtils controllerUtils;

    protected T errorController;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    @Before
    public void setUp() throws MalformedURLException {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        when(controllerUtils.buildErrorModelAndView(any(ErrorModelAndViewInfo.class))).thenReturn(mock(ModelAndView.class));
    }

    @Test
    public void authenticationErrorPage() {
        assertNotNull(errorController.authenticationErrorPage(request, response));
        verify(controllerUtils).buildErrorModelAndView(argThat(new HasPropertyWithValue<>("mainMessageCode", is(CustomErrorController.MESSAGE_CODE_AUTHENTICATION_ERROR_MAIN))));
    }

    @Test
    public void serverErrorPageWithUUID() {
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, mock(Exception.class));

        assertNotNull(errorController.serverErrorPage(request, response));
        verify(controllerUtils).buildErrorModelAndView(argThat(new HasPropertyWithValue<>("mainMessageCode", is(CustomErrorController.MESSAGE_CODE_INTERNAL_SERVER_ERROR_MAIN))));
        verify(controllerUtils).buildErrorModelAndView(argThat(new HasPropertyWithValue<>("subMessageCode", is(CustomErrorController.MESSAGE_CODE_INTERNAL_SERVER_ERROR_SUB))));
    }

    @Test
    public void serverErrorPageWithoutUUID() {
        assertNotNull(errorController.serverErrorPage(request, response));
        verify(controllerUtils).buildErrorModelAndView(argThat(new HasPropertyWithValue<>("mainMessageCode", is(CustomErrorController.MESSAGE_CODE_INTERNAL_SERVER_ERROR_MAIN))));
        verify(controllerUtils).buildErrorModelAndView(argThat(new HasPropertyWithValue<>("subMessageCode", is(CustomErrorController.MESSAGE_CODE_INTERNAL_SERVER_ERROR_SUB))));
    }

    @Test
    public void notFoundError() {
        assertNotNull(errorController.notFoundError(request, response));
        verify(controllerUtils).buildErrorModelAndView(argThat(new HasPropertyWithValue<>("mainMessageCode", is(CustomErrorController.MESSAGE_CODE_NOT_FOUND_MAIN))));
    }
}
