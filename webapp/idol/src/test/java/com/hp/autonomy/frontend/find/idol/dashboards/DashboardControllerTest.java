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
import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {
    private static final String URL = "http://abc.xyz";
    private static final String ROOT_URL = "/";
    private static final String URL_VALID_DASHBOARD = "http://abc.xyz/public/dashboards/yup";
    private static final String URL_INVALID_DASHBOARD = "http://abc.xyz/public/dashboards/nope";
    private static final String URL_ENCODED_DASHBOARD = "http://abc.xyz/public/dashboards/why%20not";
    @Mock
    private ControllerUtils controllerUtils;
    @Mock
    private ConfigResponse<DashboardConfig> configResponse;
    @Mock
    private DashboardConfig config;
    @Mock
    private DashboardConfigService configService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    private DashboardController controller;

    @Before
    public void setUp() throws Exception {
        when(request.getHeader(HttpHeaders.REFERER)).thenReturn(URL);

        final Dashboard dashboard = Dashboard.builder().dashboardName("yup").build();
        Mockito.doReturn(Collections.singletonList(dashboard))
            .when(config).getDashboards();

        Mockito.doNothing().when(configService).init();
        Mockito.doReturn(configResponse).when(configService).getConfigResponse();

        Mockito.doReturn(config).when(configResponse).getConfig();
        Mockito.doReturn("abc.json").when(configResponse).getConfigPath();

        controller = new DashboardController(configService, controllerUtils);
    }

    @Test
    public void testReloadConfigRedirectsToOriginalUrl() throws Exception {
        controller.reloadConfig(request, response);
        verify(configService, times(1)).init();
        verify(response, times(1)).sendRedirect(URL);
    }

    @Test
    public void testReloadConfigHandlesNonExistentDashboard() throws Exception {
        when(request.getHeader(HttpHeaders.REFERER)).thenReturn(URL_INVALID_DASHBOARD);

        controller.reloadConfig(request, response);
        verify(configService, times(1)).init();
        verify(response, times(1)).sendRedirect(ROOT_URL);
    }

    @Test
    public void testReloadConfigHandlesExistingDashboard() throws Exception {
        when(request.getHeader(HttpHeaders.REFERER)).thenReturn(URL_VALID_DASHBOARD);

        controller.reloadConfig(request, response);
        verify(configService, times(1)).init();
        verify(response, times(1)).sendRedirect(URL_VALID_DASHBOARD);
    }

    @Test
    public void testReloadConfigHandlesPercentEncodedDashboardNames() throws Exception {
        when(request.getHeader(HttpHeaders.REFERER)).thenReturn(URL_ENCODED_DASHBOARD);

        final Dashboard dashboard = Dashboard.builder().dashboardName("why not").build();
        Mockito.doReturn(Collections.singletonList(dashboard))
            .when(config).getDashboards();

        controller.reloadConfig(request, response);
        verify(configService, times(1)).init();
        verify(response, times(1)).sendRedirect(URL_ENCODED_DASHBOARD);
    }
}
