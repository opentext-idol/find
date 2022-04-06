/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;
import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ControllerUtilsTest<C extends FindConfig<?, ?>> {
    @Mock
    private MessageSource messageSource;

    @Mock
    private ConfigService<C> configService;

    @Mock
    private UiCustomization uiCustomization;

    @Mock
    private C config;

    private ControllerUtils controllerUtils;

    @Before
    public void setUp() {
        controllerUtils = new ControllerUtilsImpl(new ObjectMapper(), messageSource, "dev", configService);

        when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class))).thenReturn("Some Message");

        when(configService.getConfig()).thenReturn(config);
        when(config.getUiCustomization()).thenReturn(uiCustomization);
    }

    @Test
    public void convertToJson() throws JsonProcessingException {
        final String json = controllerUtils.convertToJson(new Object() {
            private final String field = "</close_tag>";

            @SuppressWarnings("unused")
            public String getField() {
                return field;
            }
        });
        assertFalse(json.contains("</"));
    }

    @Test
    public void buildErrorModelAndView() {
        final ErrorModelAndViewInfo errorModelAndViewInfo = new ErrorModelAndViewInfo.Builder()
            .setRequest(new MockHttpServletRequest())
            .setMainMessageCode("some.code")
            .setSubMessageCode("some.code")
            .setSubMessageArguments(new Object[]{})
            .setStatusCode(HttpStatus.SC_FAILED_DEPENDENCY)
            .setContactSupport(true)
            .setButtonHref(URI.create("http://some-address"))
            .build();
        final ModelAndView modelAndView = controllerUtils.buildErrorModelAndView(errorModelAndViewInfo);
        assertNotNull(modelAndView.getModel().get(ErrorAttributes.MAIN_MESSAGE.value()));
        assertNotNull(modelAndView.getModel().get(ErrorAttributes.CONTACT_SUPPORT.value()));
        assertNotNull(modelAndView.getModel().get(ErrorAttributes.BUTTON_HREF.value()));
    }
}
