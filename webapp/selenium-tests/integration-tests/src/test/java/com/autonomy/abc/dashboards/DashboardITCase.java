/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.dashboards;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.DashboardPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

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
}
