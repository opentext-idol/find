package com.autonomy.abc.connections.wizard;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;


/**
 * Created by avidan on 29-10-15.
 */
public class ConnectorStepITCase extends ABCTestBase {
    public ConnectorStepITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    final String INVALID_INPUT_CLASS = "invalid";


    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;

    private AppElement webConnectorTypeBtn;
    private AppElement filesystemConnectorTypeBtn;
    private AppElement sharepointConnectorTypeBtn;
    private AppElement dropboxConnectorTypeBtn;


    @Before
    public void setUp() {
        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        HSOElementFactory elementFactory = (HSOElementFactory) getElementFactory();
        connectionsPage = elementFactory.getConnectionsPage();
        newConnectionPage = elementFactory.getNewConnectionPage();

        connectionsPage.newConnectionButton().click();

        webConnectorTypeBtn = newConnectionPage.webConnectorType(elementFactory.getDriver());
        filesystemConnectorTypeBtn = newConnectionPage.filesystemConnectorType(elementFactory.getDriver());
        sharepointConnectorTypeBtn = newConnectionPage.sharepointConnectorType(elementFactory.getDriver());
        dropboxConnectorTypeBtn = newConnectionPage.dropboxConnectorType(elementFactory.getDriver());

    }

    @After
    public void cleanUp() {

    }

    @Test
    public void testConnectorTypeSelection() {
        selectWebConnectorType();

        selectFSConnectorType();

        selectSharepointConnectorType();

        selectDBConnectorType();
    }

    /**
     * Test for name extraction from valid URLs
     */
    @Test
    public void testWebConnectorValidatorsPositive() {
        selectWebConnectorType();
        FormInput connectorUrl = newConnectionPage.connectorUrl();
        FormInput connectorName = newConnectionPage.connectorName();

        Map<String, String> map = new HashMap<>();
        map.put("foo", "http://www.foo.com");
        map.put("foo", "www.foo.com");
        map.put("koo", "http://koo.co.uk");
        map.put("koo", "https://koo.co.uk");
        map.put("koo", "koo.co.uk");
        map.put("koo", "koo.co.uk?a=1&b=2");

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            connectorUrl.setValue((String) pair.getValue());
            assertThat("The url input field is valid ", !connectorUrl.getElement().hasClass(INVALID_INPUT_CLASS));
            assertThat("The name was extracted from the URL", connectorName.getValue(), equalTo((String) pair.getKey()));
            assertThat("The next button in the wizard is enabled", newConnectionPage.nextButton().isEnabled());
        }
    }

    /**
     * Test for name extraction from invalid URLs
     */
    @Test
    public void testWebConnectorValidatorsNegative() {
        selectWebConnectorType();
        FormInput connectorUrl = newConnectionPage.connectorUrl();
        FormInput connectorName = newConnectionPage.connectorName();

        Map<String, String> map = new HashMap<>();
        map.put("foo", "foo,com");
        map.put("koo", "koo.co.uk");

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            connectorUrl.setValue((String) pair.getValue());
            assertThat("The url input field is valid ", connectorUrl.getElement().hasClass(INVALID_INPUT_CLASS));
            assertThat("The next button in the wizard is enabled", !newConnectionPage.nextButton().isEnabled());
        }
    }

    /**
     * Test that the connector URL and name are required fields
     */
    @Test
    public void testWebConnectorRequiredFields() {
        selectWebConnectorType();
        FormInput connectorUrl = newConnectionPage.connectorUrl();
        FormInput connectorName = newConnectionPage.connectorName();

        newConnectionPage.nextButton().click();
        assertThat("The input URL field is marked as invalid when clicking next without submitting any value", connectorUrl.getElement().hasClass(INVALID_INPUT_CLASS));
        assertThat("The input name field is marked as invalid when clicking next without submitting any value", connectorName.getElement().hasClass(INVALID_INPUT_CLASS));

        connectorUrl.setValue("foo.com");
        connectorName.clear();
        newConnectionPage.nextButton().click();
        assertThat("The input URL field is marked as invalid when clicking next without submitting any value", !connectorUrl.getElement().hasClass(INVALID_INPUT_CLASS));
        assertThat("The input name field is marked as invalid when clicking next without submitting any value", connectorName.getElement().hasClass(INVALID_INPUT_CLASS));

        connectorUrl.clear();
        connectorName.clear();
        connectorName.setValue("foo");
        newConnectionPage.nextButton().click();
        assertThat("The input URL field is marked as invalid when clicking next without submitting any value", !connectorUrl.getElement().hasClass(INVALID_INPUT_CLASS));
        assertThat("The input name field is marked as invalid when clicking next without submitting any value", connectorName.getElement().hasClass(INVALID_INPUT_CLASS));
    }

    private void selectWebConnectorType() {
        webConnectorTypeBtn.click();
        assertThat("The web connector is selected", webConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The FS connector isn't selected", !filesystemConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The SharePoint connector isn't selected", !sharepointConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The DropBox connector isn't selected", !dropboxConnectorTypeBtn.hasClass("connector-icon-selected"));
    }

    private void selectDBConnectorType() {
        dropboxConnectorTypeBtn.click();
        assertThat("The web connector isn't selected", !webConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The FS connector isn't selected", !filesystemConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The SharePoint connector isn't selected", !sharepointConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The DropBox connector is selected", dropboxConnectorTypeBtn.hasClass("connector-icon-selected"));
    }

    private void selectSharepointConnectorType() {
        sharepointConnectorTypeBtn.click();
        assertThat("The web connector isn't selected", !webConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The FS connector isn't selected", !filesystemConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The SharePoint connector is selected", sharepointConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The DropBox connector isn't selected", !dropboxConnectorTypeBtn.hasClass("connector-icon-selected"));
    }

    private void selectFSConnectorType() {
        filesystemConnectorTypeBtn.click();
        assertThat("The web connector isn't selected", !webConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The FS connector is selected", filesystemConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The SharePoint connector isn't selected", !sharepointConnectorTypeBtn.hasClass("connector-icon-selected"));
        assertThat("The DropBox connector isn't selected", !dropboxConnectorTypeBtn.hasClass("connector-icon-selected"));
    }
}
