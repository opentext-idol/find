package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorType;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.not;

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
}
