package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.not;

/**
 * Created by avidan on 10-11-15.
 */
public class DropboxConnectorTypeITCase extends ConnectorTypeStepBase {
    public DropboxConnectorTypeITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Test
    public void testDBConnectorValidators(){
        FormInput connectorName = connectorTypeStepTab.connectorName();

        selectDBConnectorType();

        newConnectionPage.nextButton().click();
        newConnectionPage.loadOrFadeWait();

        assertThat("The step is invalid without connector name", newConnectionPage.connectorTypeStepTab(), not(stepIsValid()));
        assertThat(AppElement.getParent(AppElement.getParent(connectorName.getElement())), hasClass(INVALID_INPUT_CLASS));


        connectorName.setValue("name");
        newConnectionPage.loadOrFadeWait();
        assertThat(AppElement.getParent(AppElement.getParent(connectorName.getElement())), not(hasClass(INVALID_INPUT_CLASS)));
    }
}
