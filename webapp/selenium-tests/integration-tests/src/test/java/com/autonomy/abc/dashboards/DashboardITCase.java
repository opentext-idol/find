package com.autonomy.abc.dashboards;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.DashboardPage;
import com.autonomy.abc.selenium.find.FindPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;

public abstract class DashboardITCase extends IdolFindTestBase {

    DashboardPage page;
    private final int numberOfWidgets;
    private final String dashboardName;

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
