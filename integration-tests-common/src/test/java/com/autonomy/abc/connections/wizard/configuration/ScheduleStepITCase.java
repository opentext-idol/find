package com.autonomy.abc.connections.wizard.configuration;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorTypeStepTab;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static  com.autonomy.abc.framework.ABCAssert.assertThat;

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
        selectDBConnectorType();

        connectorName.setValue("name");
        newConnectionPage.loadOrFadeWait();

        newConnectionPage.nextButton().click();
        newConnectionPage.loadOrFadeWait();

        connectorConfigStep = newConnectionPage.getConnectorConfigStep();

    }

    @Test
    public void validateSchedulingFormInputs() {
        assertThat("The scheduling form is displayed by default",connectorConfigStep.schedualeForm().isDisplayed());
        assertThat("The 'Run indefinitely' checkbox is checked by default", AppElement.getParent(connectorConfigStep.unlimitedOccurrencesCheckBox()).getAttribute("aria-checked").equalsIgnoreCase("true"));
        assertThat("The 'Run definitely' checkbox isn't checked by default", AppElement.getParent(connectorConfigStep.limitedOccurrencesCheckBox()).getAttribute("aria-checked").equalsIgnoreCase("false"));
        assertThat("The repeating input is disabled default ",!connectorConfigStep.occurrencesInput().getElement().isEnabled());
    }
}
