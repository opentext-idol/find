package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ConnectionsPageITCase extends HostedTestBase {
    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;
    private ConnectionService connectionService;

    public ConnectionsPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before @Override
    public void baseSetUp() throws InterruptedException {
        regularSetUp();
        hostedLogIn("yahoo");

        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        connectionsPage = getElementFactory().getConnectionsPage();
        connectionService = getApplication().createConnectionService(getElementFactory());
        body = getBody();
    }

    @Test
    public void testNavigateToConnectorViaURL(){
        WebConnector webConnector = new WebConnector("http://www.bbc.co.uk","bbc").withDepth(2);
        connectionService.setUpConnection(webConnector);
        connectionService.goToDetails(webConnector);

        String url = getDriver().getCurrentUrl();

        body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        getElementFactory().getAnalyticsPage();

        navigateToConnectionViaURL(url);

        //For completeness try from both halves of the application
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        getElementFactory().getKeywordsPage();

        navigateToConnectionViaURL(url);
    }

    private void navigateToConnectionViaURL(String url){
        getDriver().navigate().to(url);
        getElementFactory().getConnectionsDetailPage();
        assertThat(getDriver().getCurrentUrl(), is(url));
    }

    @After
    public void tearDown(){
        connectionService.deleteAllConnections(true);
    }
}
