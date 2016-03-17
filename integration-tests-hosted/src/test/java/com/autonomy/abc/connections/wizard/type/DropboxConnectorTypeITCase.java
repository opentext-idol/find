package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.ConnectorType;
import com.autonomy.abc.selenium.connections.DropboxConnector;
import com.autonomy.abc.selenium.connections.DropboxCredentialsConfigurations;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
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
            verifyThat(input.getErrorMessage(), is("Required"));
        }

        emailInput.setValue("abc123");
        verifyThat(emailInput.getErrorMessage(), is("Invalid Value"));

        wizard.getCurrentStep().apply();
        wizard.next();

        verifyThat(newConnectionPage.getIndexStep().selectIndexButton(), displayed());
    }

    @Test
    public void testCreating(){
        try {
            Connector connector = new DropboxConnector("abc", false, "abcdef", "abcdef", "email@email.com");
            connector.makeWizard(newConnectionPage).apply();

            new WebDriverWait(getDriver(), 15).until(GritterNotice.notificationContaining("Created a new connection"));

            verifyThat(getElementFactory().getConnectionsPage().getConnectionNames(), hasItem(connector.getName()));

            new WebDriverWait(getDriver(), 300).until(GritterNotice.notificationContaining("finished running"));
        } finally {
            getApplication().connectionService().deleteAllConnections(true);
        }
    }
}
