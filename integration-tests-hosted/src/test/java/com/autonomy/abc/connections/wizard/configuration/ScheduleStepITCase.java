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
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static  com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static com.autonomy.abc.matchers.ElementMatchers.hasAttribute;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
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
    //CSA1717
    public void testConnectionSchedulingValidation() {
        List<WebElement> buttons = connectorConfigStep.getAllButtons();

        checkPage(buttons, 1, "Run the connector every 1 day");

        connectorConfigStep.hoursButton().click();
        checkPage(buttons, 0, "Connector can be scheduled to run every 6 hours at minimum");

        connectorConfigStep.timeIntervalInput().setAndSubmit("6");
        checkPage(buttons, 0, "Run the connector every 6 hours");

        connectorConfigStep.weeksButton().click();
        checkPage(buttons, 2, "Run the connector every 6 weeks");

        connectorConfigStep.timeIntervalInput().setAndSubmit("3");
        checkPage(buttons, 2, "Run the connector every 3 weeks");

        connectorConfigStep.hoursButton().click();
        checkPage(buttons, 0, "Connector can be scheduled to run every 6 hours at minimum");

        connectorConfigStep.daysButton().click();
        checkPage(buttons, 1, "Run the connector every 3 days");

        connectorConfigStep.timeIntervalInput().setAndSubmit("0.25");
        checkPage(buttons, 1, "Run the connector every 0.25 days");

        connectorConfigStep.timeIntervalInput().setAndSubmit("0.24");
        checkPage(buttons, 1, "Connector can be scheduled to run every 6 hours at minimum");
    }

    private void checkButton(WebElement currentButton, List<WebElement> allButtons){
        for(WebElement button : allButtons){
            if(button.getText().equals(currentButton.getText())){
                assertThat(button,hasClass("active"));
            } else {
                assertThat(button,not(hasClass("active")));
            }
        }
    }

    private void checkPage(List<WebElement> allButtons, int button, String schedulerString){
        checkButton(allButtons.get(button),allButtons);
        assertThat(connectorConfigStep.scheduleForm().isEnabled(), is(true));
        assertThat(connectorConfigStep.unlimitedOccurrencesCheckBox().isEnabled(), is(true));
        assertThat(connectorConfigStep.scheduleString(), is(schedulerString));
    }
}
