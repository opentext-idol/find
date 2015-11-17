package com.autonomy.abc.connections.wizard.configuration;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorType;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorTypeStepTab;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static  com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static com.autonomy.abc.matchers.ElementMatchers.hasAttribute;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.Is.is;
import static org.openqa.selenium.lift.Matchers.displayed;

/**
 * Created by avidan on 02-11-15.
 */
public class ScheduleStepITCase extends ConnectorTypeStepBase {
    public ScheduleStepITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    private ConnectorConfigStepTab connectorConfigStep;

    @Before
    public void navigateToStep() {
        ConnectorTypeStepTab connectorTypeStep = newConnectionPage.getConnectorTypeStep();

        FormInput connectorName = connectorTypeStep.connectorName();
        selectConnectorType(ConnectorType.DROPBOX);

        connectorName.setValue("name");
        newConnectionPage.loadOrFadeWait();

        newConnectionPage.nextButton().click();
        newConnectionPage.loadOrFadeWait();

        connectorConfigStep = newConnectionPage.getConnectorConfigStep();

    }

    @Test
    public void validateSchedulingFormInputs() {
        assertThat("The scheduling form is displayed by default", connectorConfigStep.scheduleForm(), displayed());
        assertThat("The 'Run indefinitely' checkbox is checked by default", AppElement.getParent(connectorConfigStep.unlimitedOccurrencesCheckBox()), hasAttribute("aria-checked", equalToIgnoringCase("true")));
        assertThat("The 'Run definitely' checkbox isn't checked by default", AppElement.getParent(connectorConfigStep.limitedOccurrencesCheckBox()), hasAttribute("aria-checked", equalToIgnoringCase("false")));
        assertThat("The repeating input is disabled default ", connectorConfigStep.occurrencesInput().getElement(), disabled());
    }

    @Test
    //CSA-1694
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
}
