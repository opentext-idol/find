package com.autonomy.abc.connections.wizard;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorTypeStepTab;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.not;


/**
 * Created by avidan on 29-10-15.
 */
public class ConnectorTypeStepBase extends ABCTestBase {
    public ConnectorTypeStepBase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    protected final String INVALID_INPUT_CLASS = "has-error";


    protected ConnectionsPage connectionsPage;
    protected NewConnectionPage newConnectionPage;
    protected ConnectorTypeStepTab connectorTypeStepTab;

    protected AppElement webConnectorTypeBtn;
    protected AppElement filesystemConnectorTypeBtn;
    protected AppElement sharepointConnectorTypeBtn;
    protected AppElement dropboxConnectorTypeBtn;
    protected HSOElementFactory elementFactory;

    @Before
    public void setUp() {
        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);

        elementFactory = (HSOElementFactory) getElementFactory();
        connectionsPage = elementFactory.getConnectionsPage();
        connectionsPage.newConnectionButton().click();

        newConnectionPage = elementFactory.getNewConnectionPage();
        connectorTypeStepTab = newConnectionPage.getConnectorTypeStep();

        webConnectorTypeBtn = connectorTypeStepTab.webConnectorType();
        filesystemConnectorTypeBtn = connectorTypeStepTab.filesystemConnectorType();
        sharepointConnectorTypeBtn = connectorTypeStepTab.sharepointConnectorType();
        dropboxConnectorTypeBtn = connectorTypeStepTab.dropboxConnectorType();
    }

    @After
    public void cleanUp() {

    }

    protected void selectWebConnectorType() {
        webConnectorTypeBtn.click();
        newConnectionPage.loadOrFadeWait();
        assertThat("The web connector is selected", webConnectorTypeBtn, hasClass("connector-icon-selected"));
        assertThat("The FS connector isn't selected", filesystemConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The SharePoint connector isn't selected", sharepointConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The DropBox connector isn't selected", dropboxConnectorTypeBtn, not(hasClass("connector-icon-selected")));
    }

    protected void selectDBConnectorType() {
        dropboxConnectorTypeBtn.click();
        newConnectionPage.loadOrFadeWait();
        assertThat("The web connector isn't selected", webConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The FS connector isn't selected", filesystemConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The SharePoint connector isn't selected", sharepointConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The DropBox connector is selected", dropboxConnectorTypeBtn, hasClass("connector-icon-selected"));
    }

    protected void selectSharepointConnectorType() {
        sharepointConnectorTypeBtn.click();
        newConnectionPage.loadOrFadeWait();
        assertThat("The web connector isn't selected", webConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The FS connector isn't selected", filesystemConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The SharePoint connector is selected", sharepointConnectorTypeBtn, hasClass("connector-icon-selected"));
        assertThat("The DropBox connector isn't selected", dropboxConnectorTypeBtn, not(hasClass("connector-icon-selected")));
    }

    protected void selectFSConnectorType() {
        filesystemConnectorTypeBtn.click();
        newConnectionPage.loadOrFadeWait();
        assertThat("The web connector isn't selected", webConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The FS connector is selected", filesystemConnectorTypeBtn, hasClass("connector-icon-selected"));
        assertThat("The SharePoint connector isn't selected", sharepointConnectorTypeBtn, not(hasClass("connector-icon-selected")));
        assertThat("The DropBox connector isn't selected", dropboxConnectorTypeBtn, not(hasClass("connector-icon-selected")));
    }

    protected static Matcher<? super WebElement> stepIsCurrent() {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement item) {
                return AppElement.hasClass("current", item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is current step");
            }
        };
    }

    protected static Matcher<? super WebElement> stepIsValid() {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement item) {
                return !AppElement.hasClass("error", item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is valid step");
            }
        };
    }
}
