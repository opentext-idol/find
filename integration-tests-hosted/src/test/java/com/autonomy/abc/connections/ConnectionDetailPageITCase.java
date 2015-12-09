package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.Platform;

public class ConnectionDetailPageITCase extends HostedTestBase {

    private ConnectionService connectionService;
    private ConnectionsPage connectionsPage;
    private ConnectionsDetailPage connectionsDetailPage;

    public ConnectionDetailPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        // requires a separate account where indexes can safely be added and deleted
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp(){
        connectionService = getApplication().createConnectionService(getElementFactory());
    }

    @After
    public void tearDown(){
        connectionService.deleteAllConnections(true);
        getApplication().createIndexService(getElementFactory()).deleteAllIndexes();
    }
}
