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
public class WebConnectorTypeITCase extends ConnectorTypeStepBase {
    public WebConnectorTypeITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    private FormInput connectorUrl ;
    private FormInput connectorName ;

    @Before
    public void selectStep() {
        selectWebConnectorType();
        connectorUrl = connectorTypeStepTab.connectorUrl();
        connectorName = connectorTypeStepTab.connectorName();
    }

    @Test
    public void testConnectorTypeSelection() {
        selectWebConnectorType();
        selectFSConnectorType();
        selectSharepointConnectorType();
        selectDBConnectorType();
    }

    @Test
    public void testWebConnectorAutoPrefixed() {
        connectorUrl.getElement().click();
        assertThat("The 'http://' prefix was added to the web connector url field", connectorUrl.getElement().getAttribute("value"), equalTo("http://"));
    }

    /**
     * Test for name extraction from valid URLs without the auto http prefix
     */
    @Test
    public void testWebConnectorValidatorsPositive() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "http://www.foo.com");
        map.put("koo", "http://koo.co.uk");
        map.put("koo2", "https://koo2.co.uk");
        map.put("koo3", "http://koo3.co.uk");
        map.put("koo4", "http://koo4.com/a?a=vv");

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            connectorUrl.setValue((String) pair.getValue());
            connectorName.getElement().click();
            assertThat("The url input field is valid ", !connectorUrl.getElement().hasClass(INVALID_INPUT_CLASS));
            assertThat("The name was extracted from the URL", connectorName.getElement().getAttribute("value"), equalTo((String) pair.getKey()));
            assertThat("The next button in the wizard is enabled", newConnectionPage.nextButton().isEnabled());
            connectorUrl.clear();
            connectorName.clear();
        }
    }

    /**
     * Test for name extraction from invalid URLs
     */
    @Test
    public void testWebConnectorValidatorsNegative() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "foo,com");
        map.put("koo", "koo.co.uk");
        map.put("koo2", "ftp://koo.co.uk");
        map.put("koo3", "smb://koo.co.uk");
        map.put("koo4", "nfs://koo.co.uk");
        map.put("koo4", "nfs://koo.co.uk?a=1");

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            connectorUrl.setValue((String) pair.getValue());
            connectorName.getElement().click();
            assertThat("The url input field isn't valid ", AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorUrl.getElement()))));

            newConnectionPage.nextButton().click();
            assertThat("The step should be set as error ", !isStepValid(newConnectionPage.connectorTypeStepTab()));
            assertThat("The step should be set as current", isStepCurrent(newConnectionPage.connectorTypeStepTab()));

            connectorUrl.clear();
            connectorName.clear();
        }
    }

    /**
     * Test that the connector URL and name are required fields
     */
    @Test
    public void testWebConnectorRequiredFields() {
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorUrl.getElement()))));
        assertThat("The url input field isn't valid ", AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorName.getElement()))));

        connectorUrl.setValue("http://foo.com");
        connectorName.clear();
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", !AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorUrl.getElement()))));
        assertThat("The url input field isn't valid ", AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorName.getElement()))));

        connectorUrl.clear();
        connectorName.clear();
        connectorName.setValue("foo");
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorUrl.getElement()))));
        assertThat("The url input field isn't valid ", !AppElement.hasClass(INVALID_INPUT_CLASS, AppElement.getParent(AppElement.getParent(connectorName.getElement()))));
    }
}
