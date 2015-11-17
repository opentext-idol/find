package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorTypeStepTab;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;

public class ConnectionsPageITCase extends HostedTestBase {
    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;

    public ConnectionsPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        connectionsPage = getElementFactory().getConnectionsPage();
        body = getBody();
    }

    @Test
    public void testConnectionSchedulingValidation() {
        connectionsPage.newConnectionButton().click();
        newConnectionPage = getElementFactory().getNewConnectionPage();

        ConnectorTypeStepTab connectorTypeStepTab = newConnectionPage.getConnectorTypeStep();
        connectorTypeStepTab.connectorUrl().setAndSubmit("http://www.bbc.co.uk");
        connectorTypeStepTab.connectorName().setAndSubmit("bbc");

        newConnectionPage.nextButton().click();

        ConnectorConfigStepTab connectorConfigStepTab = newConnectionPage.getConnectorConfigStep();

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), not(containsString("active")));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"), containsString("active"));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Run the connector every 1 day"));

        connectorConfigStepTab.hoursButton().click();

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), containsString("active"));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Connector can be scheduled to run every 6 hours at minimum"));

        connectorConfigStepTab.timeIntervalInput().setAndSubmit("6");

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), containsString("active"));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Run the connector every 6 hours"));

        connectorConfigStepTab.weeksButton().click();

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), not(containsString("active")));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),containsString("active"));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Run the connector every 6 weeks"));

        connectorConfigStepTab.timeIntervalInput().setAndSubmit("3");

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), not(containsString("active")));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),containsString("active"));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Run the connector every 3 weeks"));

        connectorConfigStepTab.hoursButton().click();

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), containsString("active"));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Connector can be scheduled to run every 6 hours at minimum"));

        connectorConfigStepTab.daysButton().click();

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), not(containsString("active")));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"), containsString("active"));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Run the connector every 3 days"));

        connectorConfigStepTab.timeIntervalInput().setAndSubmit("0.25");

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), not(containsString("active")));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"), containsString("active"));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Run the connector every 0.25 days"));

        connectorConfigStepTab.timeIntervalInput().setAndSubmit("0.24");

        assertThat(connectorConfigStepTab.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.hoursButton().getAttribute("class"), not(containsString("active")));
        assertThat(connectorConfigStepTab.daysButton().getAttribute("class"), containsString("active"));
        assertThat(connectorConfigStepTab.weeksButton().getAttribute("class"),not(containsString("active")));
        assertThat(connectorConfigStepTab.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStepTab.scheduleString(), is("Connector can be scheduled to run every 6 hours at minimum"));
    }

    @After
    public void tearDown(){}
}
