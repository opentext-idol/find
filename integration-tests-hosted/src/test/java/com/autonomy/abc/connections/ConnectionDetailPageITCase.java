package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

public class ConnectionDetailPageITCase extends HostedTestBase {

    private ConnectionService connectionService;
    private ConnectionsPage connectionsPage;
    private ConnectionsDetailPage connectionsDetailPage;
    private Connector connector;

    public ConnectionDetailPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        // requires a separate account where indexes can safely be added and deleted
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp(){
        connectionService = getApplication().createConnectionService(getElementFactory());
    }

    @Test
    //CSA-1736
    public void testWebConnectorURLOpensInNewTab(){
        String connectorURL = "https://www.google.co.uk";
        connector = new WebConnector(connectorURL,"google").withDepth(1).withDuration(60);
        connectionService.setUpConnection(connector);
        connectionsDetailPage = connectionService.goToDetails(connector);

        verifyThat(getDriver().getWindowHandles().size(), is(1));

        List<String> windowHandles = null;

        try {
            connectionsDetailPage.webConnectorURL().click();

            windowHandles = new ArrayList<>(getDriver().getWindowHandles());

            verifyThat(windowHandles.size(), is(2));
            getDriver().switchTo().window(windowHandles.get(1));
            verifyThat(getDriver().getCurrentUrl(), containsString(connectorURL));
        } finally {
            if(windowHandles != null && windowHandles.size() > 1) {
                getDriver().close();
                getDriver().switchTo().window(windowHandles.get(0));
            }
        }
    }

    @After
    public void tearDown(){
        connectionService.deleteAllConnections(true);
        getApplication().createIndexService(getElementFactory()).deleteAllIndexes();
    }
}
