package com.autonomy.abc.indexes;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexDetailsPageITCase extends HostedTestBase {

    private IndexService indexService;
    private IndexesPage indexesPage;
    private IndexesDetailPage indexesDetailPage;

    public IndexDetailsPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        // requires a separate account where indexes can safely be added and deleted
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp(){
        indexService = getApplication().createIndexService(getElementFactory());
    }

    @Test
    //CSA-1643
    public void testAssociatedConnections(){
        ConnectionService connectionService = getApplication().createConnectionService(getElementFactory());
        Index indexOne = new Index("one");
        Index indexTwo = new Index("two");
        Connector connector = new WebConnector("http://www.bbc.co.uk","connector",indexOne).withDuration(60);

        indexService.setUpIndex(indexOne);
        indexService.setUpIndex(indexTwo);
        try {
            connectionService.setUpConnection(connector);

            indexesPage = indexService.goToIndexes();
            verifyThat(indexesPage.getNumberOfConnections(indexOne), is(1));
            verifyThat(indexesPage.getNumberOfConnections(indexTwo), is(0));
            
            indexesDetailPage = indexService.goToDetails(indexOne);
            List<String> associatedConnections = indexesDetailPage.getAssociatedConnectors();
            verifyThat(associatedConnections.size(), is(1));
            verifyThat(associatedConnections, hasItem(connector.getName()));

            connectionService.changeIndex(connector, indexTwo);

            verifyThat(getElementFactory().getConnectionsDetailPage().indexName(), is(indexTwo.getName()));

            indexesPage = indexService.goToIndexes();
            verifyThat(indexesPage.getNumberOfConnections(indexOne), is(0));
            verifyThat(indexesPage.getNumberOfConnections(indexTwo), is(1));

            indexesDetailPage = indexService.goToDetails(indexOne);
            verifyThat(indexesDetailPage.getAssociatedConnectors().size(), is(0));

            indexesDetailPage = indexService.goToDetails(indexTwo);
            associatedConnections = indexesDetailPage.getAssociatedConnectors();
            verifyThat(associatedConnections.size(), is(1));
            verifyThat(associatedConnections, hasItem(connector.getName()));
        } finally {
            connectionService.deleteAllConnections(true);
        }
    }

    @After
    public void tearDown(){
        getApplication().createConnectionService(getElementFactory()).deleteAllConnections(true);
        indexService.deleteAllIndexes();
    }
}
