package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by avidan on 10-11-15.
 */
public class FileSystemConnectorTypeITCase extends ConnectorTypeStepBase {
    public FileSystemConnectorTypeITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    private FormInput connectorPath;
    private FormInput connectorName;

    @Before
    public void selectStep() {
        selectFSConnectorType();
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

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            connectorPath.setValue((String) pair.getValue());
            connectorName.getElement().click();
            assertThat("The url input field isn't valid ", !AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorPath.getElement()))));

            assertThat("The name was extracted from the URL", connectorName.getElement().getAttribute("value"), equalTo((String) pair.getKey()));
            assertThat("The next button in the wizard is enabled", newConnectionPage.nextButton().isEnabled());
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
            assertThat("The url input field isn't valid ", AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorPath.getElement()))));

            newConnectionPage.nextButton().click();
            assertThat("The step should be set as error when value is " + pair.getKey(), !isStepValid(newConnectionPage.connectorTypeStepTab()));
            assertThat("The step should be set as current", isStepCurrent(newConnectionPage.connectorTypeStepTab()));

            connectorPath.clear();
            connectorName.clear();
        }
    }
}
