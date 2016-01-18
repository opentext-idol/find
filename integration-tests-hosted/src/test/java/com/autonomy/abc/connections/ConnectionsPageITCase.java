package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.ConnectionStatistics;
import com.autonomy.abc.selenium.connections.Credentials;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorIndexStepTab;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorTypeStepTab;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;

public class ConnectionsPageITCase extends HostedTestBase {
    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;
    private ConnectionService connectionService;

    public ConnectionsPageITCase(TestConfig config) {
        super(config);
        // requires a separate account where indexes can safely be added and deleted
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp() {
        connectionService = getApplication().createConnectionService(getElementFactory());

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        connectionsPage = getElementFactory().getConnectionsPage();
    }

    @Test
    public void testNavigateToConnectorViaURL(){
        WebConnector webConnector = new WebConnector("http://www.bbc.co.uk","bbc").withDepth(2);
        connectionService.setUpConnection(webConnector);
        connectionService.goToDetails(webConnector);

        String url = getDriver().getCurrentUrl();

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        getElementFactory().getAnalyticsPage();

        navigateToConnectionViaURL(url);

        //For completeness try from both halves of the application
        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        getElementFactory().getKeywordsPage();

        navigateToConnectionViaURL(url);
    }

    @Test
    //IOD-4785
    public void testSecureWebConnector(){
        String email = "matthew.williamson@hpe.com";

        WebConnector webConnector = new WebConnector("http://www.facebook.com/settings","facebooksecure", new Credentials(email,"vdPAuTGU",email)).withDuration(255);

        connectionService.setUpConnection(webConnector);
        connectionService.goToDetails(webConnector);

        connectionService.updateLastRun(webConnector);
        ConnectionStatistics connectionStatistics = webConnector.getStatistics();

        verifyThat("detected some documents", connectionStatistics.getDetected(), not(0));
        verifyThat("ingested some documents", connectionStatistics.getIngested(), not(0));
    }

    @Test
    //CSA-1795
    public void testBackButton(){
        WebConnector webConnector = new WebConnector("http://www.bbc.co.uk","bbc").withDuration(60);

        connectionService.setUpConnection(webConnector);
        connectionService.goToDetails(webConnector);

        ConnectionsDetailPage connectionsDetailPage = getElementFactory().getConnectionsDetailPage();

        connectionsDetailPage.backButton().click();

        verifyThat(getDriver().getCurrentUrl(), containsString("repositories"));

        connectionService.goToDetails(webConnector);

        connectionsDetailPage = getElementFactory().getConnectionsDetailPage();

        connectionsDetailPage.editButton().click();

        //TODO maybe doesn't belong here?
        connectionsDetailPage.cancelButton().click();
        connectionsDetailPage.backButton().click();

        verifyThat(getDriver().getCurrentUrl(), containsString("repositories"));
    }

    @Test
    //CSA-1798
    public void testCanSelectLastIndex(){
        IndexService indexService = getApplication().createIndexService(getElementFactory());
        ConnectorIndexStepTab connectorIndexStepTab = null;

        try {
            try {
                indexService.setUpIndex(new Index("index one"));
                indexService.setUpIndex(new Index("index two"));
            } catch (Exception e) { /* couldn't create an index */  }

            getElementFactory().getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);

            connectionsPage = getElementFactory().getConnectionsPage();
            connectionsPage.newConnectionButton().click();

            newConnectionPage = getElementFactory().getNewConnectionPage();

            ConnectorTypeStepTab connectorTypeStepTab = newConnectionPage.getConnectorTypeStep();
            connectorTypeStepTab.connectorUrl().setValue("http://w.ww");
            connectorTypeStepTab.connectorName().setValue("w");
            newConnectionPage.nextButton().click();

            newConnectionPage.nextButton().click();

            connectorIndexStepTab = newConnectionPage.getIndexStep();
            connectorIndexStepTab.selectIndexButton().click();
            connectorIndexStepTab.selectFirstIndex();
            Waits.loadOrFadeWait();

            Index firstIndex = connectorIndexStepTab.getChosenIndexInModal();

            connectorIndexStepTab.selectLastIndex();
            Waits.loadOrFadeWait();

            Index lastIndex = connectorIndexStepTab.getChosenIndexInModal();

            assertThat(firstIndex, not(lastIndex));
        } finally {
            try {
                connectorIndexStepTab.closeDropdown();
                connectorIndexStepTab.closeModal();
            } catch (Exception e) { /* No visible modal */ }

            indexService.deleteAllIndexes();
        }
    }

    @Test
    //CSA-1679
    public void testCreateFromIndexAutoSelectsIndex(){
        Index index = new Index("index");
        IndexService indexService = getApplication().createIndexService(getElementFactory());

        try {
            indexService.setUpIndex(index);
            IndexesDetailPage indexDetailPage = indexService.goToDetails(index);

            indexDetailPage.newConnectionButton().click();

            newConnectionPage = getElementFactory().getNewConnectionPage();

            ConnectorTypeStepTab connectorTypeStep = newConnectionPage.getConnectorTypeStep();
            connectorTypeStep.connectorUrl().setValue("http://waitinginthefor.est");
            connectorTypeStep.connectorName().setValue("i am lost");
            newConnectionPage.nextButton().click();
            Waits.loadOrFadeWait();
            newConnectionPage.nextButton().click();

            ConnectorIndexStepTab connectorIndexStep = newConnectionPage.getIndexStep();

            verifyThat(connectorIndexStep.getChosenIndexOnPage(), is(index));
        } finally {
            indexService.deleteAllIndexes();
        }
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
