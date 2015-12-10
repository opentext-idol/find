package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.http.HttpClient;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

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

    @Test
    //CSA-1470
    public void testCancellingConnectorScheduling(){
        connector = new WebConnector("http://www.google.co.uk","google").withDuration(60);

        connectionService.setUpConnection(connector);
        connectionsDetailPage = connectionService.cancelConnectionScheduling(connector);

        verifyThat(connectionsDetailPage.getScheduleString(), is("The connector is not scheduled to run."));
    }

    @Test
    //CSA-1469
    public void testEditConnectorWithNoIndex(){
        IndexService indexService = getApplication().createIndexService(getElementFactory());
        Index indexOne = new Index("one");
        Index indexTwo = new Index("two");

        indexService.setUpIndex(indexOne);
        indexService.setUpIndex(indexTwo);

        connector = new WebConnector("http://www.bbc.co.uk", "bbc", indexOne).withDuration(60);

        connectionService.setUpConnection(connector);
        connectionService.goToDetails(connector);

        verifyIndexNameForConnector();

        indexService.deleteIndexViaAPICalls(indexOne);

        indexService.goToIndexes();

        getDriver().navigate().refresh();
        body = getBody();

        verifyThat(getElementFactory().getIndexesPage().getIndexNames(), not(hasItem(indexOne.getName())));

        connector = connectionService.changeIndex(connector, indexTwo);

        verifyIndexNameForConnector();
    }

    private void verifyIndexNameForConnector() {
        verifyThat(getElementFactory().getConnectionsDetailPage().getIndexName(), is(connector.getIndex().getName()));
    }

    @After
    public void tearDown(){
        connectionService.deleteAllConnections(true);
        getApplication().createIndexService(getElementFactory()).deleteAllIndexes();
    }
}
