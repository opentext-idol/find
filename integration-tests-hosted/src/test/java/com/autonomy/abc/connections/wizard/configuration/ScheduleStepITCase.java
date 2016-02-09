package com.autonomy.abc.connections.wizard.configuration;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.connections.wizard.ConnectorType;
import com.autonomy.abc.selenium.connections.wizard.ConnectorTypeStepTab;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.Is.is;
import static org.openqa.selenium.lift.Matchers.displayed;

public class ScheduleStepITCase extends ConnectorTypeStepBase {
    public ScheduleStepITCase(TestConfig config) {
        super(config);
    }

    private ConnectorConfigStepTab connectorConfigStep;

    @Before
    public void navigateToStep() {
        ConnectorTypeStepTab connectorTypeStep = newConnectionPage.getConnectorTypeStep();

        FormInput connectorName = connectorTypeStep.connectorName();
        selectConnectorType(ConnectorType.DROPBOX);

        connectorName.setValue("name");
        Waits.loadOrFadeWait();

        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();

        connectorConfigStep = newConnectionPage.getConnectorConfigStep();

    }

    @Test
    public void validateSchedulingFormInputs() {
        assertThat("The scheduling form is displayed by default", connectorConfigStep.scheduleForm(), displayed());
        assertThat("The 'Run indefinitely' checkbox is checked by default", ElementUtil.getParent(connectorConfigStep.unlimitedOccurrencesCheckBox()), hasAttribute("aria-checked", equalToIgnoringCase("true")));
        assertThat("The 'Run definitely' checkbox isn't checked by default", ElementUtil.getParent(connectorConfigStep.limitedOccurrencesCheckBox()), hasAttribute("aria-checked", equalToIgnoringCase("false")));
        assertThat("The repeating input is disabled default ", connectorConfigStep.occurrencesInput().getElement(), disabled());
    }

    @Test
    @KnownBug("CSA-1717")
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
