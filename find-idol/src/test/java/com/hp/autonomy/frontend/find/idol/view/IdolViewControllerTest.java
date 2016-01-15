/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.view;

import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.aci.client.services.Processor;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.idol.view.ViewDocumentNotFoundException;
import com.hp.autonomy.searchcomponents.idol.view.ViewNoReferenceFieldException;
import com.hp.autonomy.searchcomponents.idol.view.ViewServerErrorException;
import com.hp.autonomy.searchcomponents.idol.view.ViewServerService;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolViewControllerTest {
    @Mock
    private ConfigService<IdolFindConfig> configService;

    @Mock
    private ViewServerService viewServerService;

    @Mock
    private ControllerUtils controllerUtils;

    private IdolViewController idolViewController;
    private MockHttpServletResponse response;

    @Before
    public void setUp() {
        idolViewController = new IdolViewController(configService, viewServerService, controllerUtils);
        response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(idolViewController, "viewServerService", viewServerService, ViewServerService.class);

        when(controllerUtils.buildErrorModelAndView(any(HttpServletRequest.class), anyString(), anyString(), any(Object[].class), any(Integer.class), anyBoolean())).thenReturn(mock(ModelAndView.class));
    }

    @Test
    public void viewDocument() throws IOException, InterruptedException {
        final String reference = "SomeReference";
        idolViewController.viewDocument(reference, "SomeDatabase", response);
        verify(viewServerService).viewDocument(eq(reference), anyListOf(String.class), any(Processor.class));
    }

    @Test
    public void viewDocumentNotFound() {
        assertNotNull(idolViewController.handleViewDocumentNotFoundException(new ViewDocumentNotFoundException("some reference"), new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void noReferenceField() {
        when(configService.getConfig()).thenReturn(new IdolFindConfig.Builder().setView(new ViewConfig.Builder().build()).build());

        assertNotNull(idolViewController.handleViewNoReferenceFieldException(new ViewNoReferenceFieldException("some reference", "some field"), new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void referenceFieldBlank() {
        assertNotNull(idolViewController.handleReferenceFieldBlankException(new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void viewServerError() {
        assertNotNull(idolViewController.handleViewServerErrorException(new ViewServerErrorException("some reference", new AciServiceException("It broke")), new MockHttpServletRequest(), new MockHttpServletResponse()));
    }
}
