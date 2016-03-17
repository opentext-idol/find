package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.ConnectorType;
import com.autonomy.abc.selenium.connections.FileSystemConnector;
import com.autonomy.abc.selenium.connections.SharepointCompleteStepTab;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.autonomy.abc.framework.TestStateAssert.assertThat;
import static com.autonomy.abc.framework.TestStateAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class FileSystemConnectorTypeITCase extends ConnectorTypeStepBase {
    public FileSystemConnectorTypeITCase(TestConfig config) {
        super(config);
    }

    private FormInput connectorPath;
    private FormInput connectorName;

    @Before
    public void selectStep() {
        selectConnectorType(ConnectorType.FILESYSTEM);
        connectorPath = connectorTypeStepTab.connectorPath();
        connectorName = connectorTypeStepTab.connectorName();
    }

    /**
     * Test for name extraction from valid URLs without the auto http prefix
     */
    @Test
    public void testFSConnectorValidatorsPositive() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "c:\\foo");
        map.put("koo", "c:\\foo\\koo");
        map.put("aa", "\\\\d\\aa");

        for (String key : map.keySet()) {
            connectorPath.setValue(map.get(key));
            connectorName.getElement().click();
            assertThat("The url input field isn't valid ", !ElementUtil.hasClass(INVALID_INPUT_CLASS, ElementUtil.ancestor(connectorPath.getElement(), 2)));

            assertThat("The name was extracted from the URL", connectorName.getElement(), hasAttribute("value", equalTo(key)));
            assertThat("The next button in the wizard is enabled", newConnectionPage.nextButton(), not(disabled()));
            connectorPath.clear();
            connectorName.clear();
        }
    }

    /**
     * Test for name extraction from invalid URLs
     */
    @Test
    public void testFSConnectorValidatorNegative() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "http://foo");
        map.put("koo", "c://foo\\koo");
        map.put("koo2", "c:\\foo//koo");
        map.put("foo2", "\\\\\\foo\\aa");

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            connectorPath.setValue((String) pair.getValue());
            connectorName.getElement().click();
            assertThat("The url input field isn't valid ", ElementUtil.ancestor(connectorPath.getElement(), 2), hasClass(INVALID_INPUT_CLASS));

            newConnectionPage.nextButton().click();
            assertThat("The step has an error when value is " + pair.getKey(), newConnectionPage.connectorTypeStepTab(), not(stepIsValid()));
            assertThat(newConnectionPage.connectorTypeStepTab(), stepIsCurrent());

            connectorPath.clear();
            connectorName.clear();
        }
    }

    @Test
    public void testCreating(){
        try {
            Connector connector = new FileSystemConnector("/c/", "c");
            connector.makeWizard(newConnectionPage).apply();

            new WebDriverWait(getDriver(), 15).until(GritterNotice.notificationContaining("Created a new connection"));

            verifyThat(getElementFactory().getConnectionsPage().getConnectionNames(), hasItem(connector.getName()));
        } finally {
            getApplication().connectionService().deleteAllConnections(true);
        }
    }

    @Test
    @KnownBug("CSA-2096")
    public void testDocumentationLink(){
        goToLastStep(new FileSystemConnector("/c/","163"));

        SharepointCompleteStepTab completeStep = new SharepointCompleteStepTab(getDriver());

        completeStep.downloadAgentButton().click();
        Waits.loadOrFadeWait();
        completeStep.agentInformationButton().click();
        Waits.loadOrFadeWait();

        getMainSession().switchWindow(1);

        verifyThat(getDriver().getCurrentUrl(), not(containsString(".int.")));
        verifyThat(getDriver().getPageSource(), not(containsString("404")));

        getWindow().close();
        getMainSession().switchWindow(0);

        completeStep.modalCancel().click();
    }

    @Test
    public void testAPIKeyGen(){
        goToLastStep(new FileSystemConnector("/c/","42152"));

        SharepointCompleteStepTab completeStep = new SharepointCompleteStepTab(getDriver());

        completeStep.downloadAgentButton().click();
        Waits.loadOrFadeWait();

        verifyThat(completeStep.apiKey().getAttribute("value"), is(""));

        completeStep.generateAPIKeyButton().click();
        Waits.loadOrFadeWait();

        verifyThat(completeStep.apiKey().getAttribute("value"), not(""));

        completeStep.modalCancel().click();
    }

    private void goToLastStep(FileSystemConnector connector){
        Wizard wizard = connector.makeWizard(newConnectionPage);

        for(int i = 0; i < 3; i++){
            wizard.getCurrentStep().apply();
            wizard.next();
        }
    }
}
