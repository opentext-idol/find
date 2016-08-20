/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.view;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciServiceException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.view.AbstractViewControllerTest;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.view.ViewServerService;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import com.hp.autonomy.searchcomponents.idol.view.ViewDocumentNotFoundException;
import com.hp.autonomy.searchcomponents.idol.view.ViewNoReferenceFieldException;
import com.hp.autonomy.searchcomponents.idol.view.ViewServerErrorException;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolViewControllerTest extends AbstractViewControllerTest<IdolViewController, String, AciErrorException> {
    @Mock
    private ConfigService<? extends IdolSearchCapable> configService;

    @Mock
    private ViewServerService<String, AciErrorException> idolViewServerService;

    @Override
    @Before
    public void setUp() {
        viewServerService = idolViewServerService;
        viewController = new IdolViewController(viewServerService, configService, controllerUtils);
        response = new MockHttpServletResponse();
        super.setUp();
    }

    @Override
    protected String getSampleDatabase() {
        return "SomeDatabase";
    }

    @Test
    public void viewDocumentNotFound() {
        assertNotNull(viewController.handleViewDocumentNotFoundException(new ViewDocumentNotFoundException("some reference"), new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void noReferenceField() {
        when(configService.getConfig()).thenReturn(new IdolFindConfig.Builder().setView(new ViewConfig.Builder().build()).build());

        assertNotNull(viewController.handleViewNoReferenceFieldException(new ViewNoReferenceFieldException("some reference", "some field"), new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void referenceFieldBlank() {
        assertNotNull(viewController.handleReferenceFieldBlankException(new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void viewServerError() {
        assertNotNull(viewController.handleViewServerErrorException(new ViewServerErrorException("some reference", new AciServiceException("It broke")), new MockHttpServletRequest(), new MockHttpServletResponse()));
    }
}
