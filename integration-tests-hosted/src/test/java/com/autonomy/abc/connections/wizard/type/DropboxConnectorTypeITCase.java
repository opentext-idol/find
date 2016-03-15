package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.ConnectorType;
import com.autonomy.abc.selenium.connections.DropboxConnector;
import com.autonomy.abc.selenium.connections.DropboxCredentialsConfigurations;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Test;

import java.util.Arrays;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

public class DropboxConnectorTypeITCase extends ConnectorTypeStepBase {
    public DropboxConnectorTypeITCase(TestConfig config) {
        super(config);
    }

    @Test
    public void testDBConnectorValidators(){
        FormInput connectorName = connectorTypeStepTab.connectorName();

        selectConnectorType(ConnectorType.DROPBOX);

        newConnectionPage.nextButton().click();
        Waits.loadOrFadeWait();

        assertThat("The step is invalid without connector name", newConnectionPage.connectorTypeStepTab(), not(stepIsValid()));
        assertThat(ElementUtil.ancestor(connectorName.getElement(), 2), hasClass(INVALID_INPUT_CLASS));


        connectorName.setValue("name");
        Waits.loadOrFadeWait();
        assertThat(ElementUtil.ancestor(connectorName.getElement(), 2), not(hasClass(INVALID_INPUT_CLASS)));
    }

    @Test
    public void testRequiredFields(){
        Connector connector = new DropboxConnector("abc", true, "123", "456", "needstobe@anemail.com");
        Wizard wizard = connector.makeWizard(newConnectionPage);
        wizard.getCurrentStep().apply();
        wizard.next();
        newConnectionPage.nextButton().click();

        DropboxCredentialsConfigurations cc = newConnectionPage.getConnectorConfigStep().getDropboxCredentialsConfigurations();
        FormInput emailInput = cc.notificationEmailInput();

        for (FormInput input : Arrays.asList(cc.applicationKeyInput(), cc.accessTokenInput(), emailInput)) {
            verifyThat(input.getElement(), hasClass("ng-invalid-required"));
        }

        emailInput.setValue("abc123");
        verifyThat(emailInput.getElement(), hasClass("ng-invalid-email"));

        wizard.getCurrentStep().apply();
        wizard.next();

        verifyThat(newConnectionPage.getIndexStep().selectIndexButton(), displayed());
    }
}
