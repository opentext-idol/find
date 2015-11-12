package com.autonomy.abc.indexes;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import org.junit.Before;
import org.openqa.selenium.Platform;

public class IndexesPageITCase extends ABCTestBase {

    ConnectionsPage connectionsPage;
    HSOElementFactory hsoElementFactory;

    public IndexesPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    @Override
    public void baseSetUp() throws InterruptedException {
        regularSetUp();
        hostedLogIn("yahoo");
        getElementFactory().getPromotionsPage();

        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);

        hsoElementFactory = (HSOElementFactory) getElementFactory();
        connectionsPage = hsoElementFactory.getConnectionsPage();
    }

}
