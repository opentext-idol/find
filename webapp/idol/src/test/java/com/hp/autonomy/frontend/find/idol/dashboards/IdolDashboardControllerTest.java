/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards;

import com.hp.autonomy.frontend.configuration.ConfigResponse;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolDashboardControllerTest {
    private static final String url = "http://abc.xyz";
    @Mock
    private ConfigResponse<IdolDashboardConfig> configResponse;
    @Mock
    private ControllerUtils controllerUtils;
    @Mock
    private IdolDashboardConfigService dashConfig;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    private IdolDashboardController controller;

    @Before
    public void setUp() throws Exception {
        when(request.getHeader(HttpHeaders.REFERER)).thenReturn(url);
        Mockito.doNothing().when(dashConfig).init();

        // Required for audit logging call
        Mockito.doReturn(configResponse).when(dashConfig).getConfigResponse();
        Mockito.doReturn("abc.json").when(configResponse).getConfigPath();

        controller = new IdolDashboardController(dashConfig, controllerUtils);
    }

    @Test
    public void testReloadConfigRedirectsToOriginalUrl() throws Exception {
        controller.reloadConfig(request, response);
        verify(dashConfig, times(1)).init();
        verify(response, times(1)).sendRedirect(url);
    }
}
