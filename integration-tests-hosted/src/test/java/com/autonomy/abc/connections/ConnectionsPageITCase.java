package com.autonomy.abc.connections;

import com.autonomy.abc.base.HSODTearDown;
import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.connections.*;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.autonomy.abc.framework.TestStateAssert.assertThat;
import static com.autonomy.abc.framework.TestStateAssert.verifyThat;
import static com.autonomy.abc.matchers.ControlMatchers.url;
import static com.autonomy.abc.matchers.ControlMatchers.urlContains;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;

public class ConnectionsPageITCase extends HostedTestBase {
    private ConnectionsPage connectionsPage;
    private NewConnectionPage newConnectionPage;
    private ConnectionService connectionService;

    public ConnectionsPageITCase(TestConfig config) {
        super(config);
        useIndexTestsUser();
    }

    @Before
    public void setUp() {
        connectionService = getApplication().connectionService();

        connectionsPage = connectionService.goToConnections();
    }

    @After
    public void tearDown(){
        HSODTearDown.INDEXES.tearDown(this);
    }

    @Test
    public void testNavigateToConnectorViaURL(){
        WebConnector webConnector = new WebConnector("http://www.bbc.co.uk","bbc").withDepth(2);
        connectionService.setUpConnection(webConnector);
        connectionService.goToDetails(webConnector);

        String url = getWindow().getUrl();

        getApplication().switchTo(AnalyticsPage.class);

        navigateToConnectionViaURL(url);

        //For completeness try from both halves of the application
        getApplication().switchTo(KeywordsPage.class);

        navigateToConnectionViaURL(url);
    }

    @Test
    @KnownBug("IOD-4785")
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
    @KnownBug("CSA-1795")
    public void testBackButton(){
        WebConnector webConnector = new WebConnector("http://www.bbc.co.uk","bbc").withDuration(60);

        connectionService.setUpConnection(webConnector);
        connectionService.goToDetails(webConnector);

        ConnectionsDetailPage connectionsDetailPage = getElementFactory().getConnectionsDetailPage();

        connectionsDetailPage.backButton().click();

        verifyThat(getWindow(), urlContains("repositories"));

        connectionService.goToDetails(webConnector);

        connectionsDetailPage = getElementFactory().getConnectionsDetailPage();

        connectionsDetailPage.editButton().click();

        //TODO maybe doesn't belong here?
        connectionsDetailPage.cancelButton().click();
        connectionsDetailPage.backButton().click();

        verifyThat(getWindow(), urlContains("repositories"));
    }

    @Test
    @KnownBug("CSA-1798")
    public void testCanSelectLastIndex(){
        IndexService indexService = getApplication().indexService();
        ConnectorIndexStepTab connectorIndexStepTab = null;

        try {
            try {
                indexService.setUpIndex(new Index("index one"));
                indexService.setUpIndex(new Index("index two"));
            } catch (Exception e) { /* couldn't create an index */  }

            connectionsPage = connectionService.goToConnections();
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
                if(connectorIndexStepTab != null) {
                    connectorIndexStepTab.closeDropdown();
                    connectorIndexStepTab.closeModal();
                }
            } catch (Exception e) { /* No visible modal */ }

            indexService.deleteAllIndexes();
        }
    }

    @Test
    @KnownBug({"CSA-1679","CSA-2053"})
    public void testCreateFromIndexAutoSelectsIndex(){
        Index index = new Index("index");
        IndexService indexService = getApplication().indexService();

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

    private void navigateToConnectionViaURL(String connectionUrl){
        getWindow().goTo(connectionUrl);
        getElementFactory().getConnectionsDetailPage();
        assertThat(getWindow(), url(is(connectionUrl)));
    }
}
