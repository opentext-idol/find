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

package com.autonomy.abc.dashboards;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.DashboardPage;
import com.autonomy.abc.selenium.find.OnPremNavBarSettings;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

@Role(UserRole.BIFHI)
public class RoleBasedDashboardITCase extends IdolFindTestBase {
    private OnPremNavBarSettings onPremNavBarSettings;
    private static final String ADMIN_ONLY_DASHBOARD = "Admins Only";
    private static final String CUSTOM_ROLE_DASHBOARD = "Custom Role";


    public RoleBasedDashboardITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        onPremNavBarSettings = getElementFactory().getTopNavBar();
    }

    @Test
    public void testAdminDashboardRole() {
        final List<String> availableDashboards = onPremNavBarSettings.getAvailableDashboards();
        assertThat("Admin only dashboard is not visible", availableDashboards.contains(ADMIN_ONLY_DASHBOARD));
        assertThat("Custom role dashboard is not visible", availableDashboards.contains(CUSTOM_ROLE_DASHBOARD));
    }

    @Test
    public void testDashboardRole() {
        final LoginService loginService = getApplication().loginService();
        loginService.logout();
        loginService.login(getConfig().getUser("dashboard")); // dashboard user has the custom role
        onPremNavBarSettings = getElementFactory().getTopNavBar();
        final List<String> availableDashboards = onPremNavBarSettings.getAvailableDashboards();
        assertThat("Admin only dashboard is visible", !availableDashboards.contains(ADMIN_ONLY_DASHBOARD));
        assertThat("Custom role dashboard is not visible", availableDashboards.contains(CUSTOM_ROLE_DASHBOARD));
    }

    @Test
    public void testNoDashboardRole() {
        final LoginService loginService = getApplication().loginService();
        loginService.logout();
        loginService.login(getConfig().getUser("dashboard2")); // dashboard2 user has no extra roles
        onPremNavBarSettings = getElementFactory().getTopNavBar();
        final List<String> availableDashboards = onPremNavBarSettings.getAvailableDashboards();
        assertThat("Admin only dashboard is visible", !availableDashboards.contains(ADMIN_ONLY_DASHBOARD));
        assertThat("Custom role dashboard is visible", !availableDashboards.contains(CUSTOM_ROLE_DASHBOARD));
    }
}
