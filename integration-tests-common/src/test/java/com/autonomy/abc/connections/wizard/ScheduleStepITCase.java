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
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

/**
 * Created by avidan on 02-11-15.
 */
public class ScheduleStepITCase extends ABCTestBase {
    public ScheduleStepITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;

    @Before
    public void setUp() {
        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        HSOElementFactory elementFactory = (HSOElementFactory) getElementFactory();
        connectionsPage = elementFactory.getConnectionsPage();
        newConnectionPage = elementFactory.getNewConnectionPage();

        connectionsPage.newConnectionButton().click();

        AppElement webConnectorTypeBtn = newConnectionPage.webConnectorType(elementFactory.getDriver());
        FormInput connectorUrl = newConnectionPage.connectorUrl();
        FormInput connectorName = newConnectionPage.connectorName();

        connectorUrl.setValue("foo.com");
        newConnectionPage.nextButton().click();
    }

    /**
     *
     */
    @Test
    public void validateScheduling(){

    }
}
