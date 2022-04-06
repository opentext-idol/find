/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.customization;

import com.hp.autonomy.frontend.find.core.customization.style.CssGenerationException;
import com.hp.autonomy.frontend.find.core.customization.ReloadableCustomizationComponent;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.FindController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CustomizationReloadControllerTest {
    private CustomizationReloadController controller;
    @Mock
    private ControllerUtils controllerUtils;
    @Mock
    private ReloadableCustomizationComponent reloadable1;
    @Mock
    private ReloadableCustomizationComponent reloadable2;
    @Mock
    private HttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        doNothing().when(response).sendRedirect(FindController.APP_PATH);
        doNothing().when(reloadable1).reload();
        doNothing().when(reloadable2).reload();
        Collection<ReloadableCustomizationComponent> reloadableCustomizationComponents = new ArrayList<>();
        reloadableCustomizationComponents.add(reloadable1);
        reloadableCustomizationComponents.add(reloadable2);
        controller = new CustomizationReloadController("/", reloadableCustomizationComponents, controllerUtils);
    }

    @Test
    public void testReloadConfigCallsThroughToReloadMethods() throws Exception {
        controller.reloadConfig(response);
        verify(reloadable1, times(1)).reload();
        verify(reloadable2, times(1)).reload();
    }

    @Test
    public void testReloadConfigRedirectsToApplicationRoot() throws Exception {
        controller.reloadConfig(response);
        verify(response, times(1)).sendRedirect(FindController.APP_PATH);
    }

    @Test
    public void testReloadConfigHandlesExceptions() throws Exception {
        doThrow(new CssGenerationException("ouch!")).when(reloadable1).reload();
        try {
            controller.reloadConfig(response);
            fail("Expected exception to have been thrown");
        } catch(final CssGenerationException e) {
            assertThat("Propagated exception has the correct message",
                       e.getMessage(),
                       containsString("ouch!"));
        }
    }
}
