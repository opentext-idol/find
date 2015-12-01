/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.view;

import com.autonomy.aci.client.services.Processor;
import com.hp.autonomy.frontend.view.idol.ViewServerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IdolViewControllerTest {
    @Mock
    private ViewServerService viewServerService;

    private IdolViewController idolViewController;
    private MockHttpServletResponse response;

    @Before
    public void setUp() {
        idolViewController = new IdolViewController();
        response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(idolViewController, "viewServerService", viewServerService, ViewServerService.class);
    }

    @Test
    public void viewDocument() throws IOException {
        final String reference = "SomeReference";
        idolViewController.viewDocument(reference, "SomeDatabase", response);
        verify(viewServerService).viewDocument(eq(reference), anyListOf(String.class), any(Processor.class));
    }
}
