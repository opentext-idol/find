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
import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

@Role(UserRole.BIFHI)
public abstract class DashboardITCase extends IdolFindTestBase {
    private final int numberOfWidgets;
    private final String dashboardName;
    DashboardPage page;

    DashboardITCase(final TestConfig config, final int numberOfWidgets, final String dashboardName) {
        super(config);
        this.numberOfWidgets = numberOfWidgets;
        this.dashboardName = dashboardName;
    }

    @Before
    public void setUp() {
        getElementFactory().getTopNavBar().goToDashboard(dashboardName);
        page = getElementFactory().getDashboard();
    }

    @Test
    public void testDashboardLoaded() {
        assertThat("widgets exist", !page.getWidgets().isEmpty());
    }

    @Test
    public void testWidgetsLoaded() {
        assertThat("Incorrect number of widgets", page.getWidgets().size() == numberOfWidgets);
    }

    @Test
    public void testFullscreen() {
        assertThat("dropdown toggle is not present", page.getDropdownToggle().isDisplayed());
        page.enterFullscreen();
        assertThat("page is not fullscreen", page.isFullscreen());
    }
}
