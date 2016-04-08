package com.autonomy.abc.connections;

import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.base.IndexTearDownStrategy;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.connections.*;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.hp.autonomy.frontend.selenium.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTextThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class ConnectionDetailPageITCase extends HostedTestBase {

    private ConnectionService connectionService;
    private ConnectionsDetailPage connectionsDetailPage;
    private Connector connector;
    private User testUser;

    public ConnectionDetailPageITCase(TestConfig config) {
        super(config);
        useIndexTestsUser();
        testUser = getInitialUser();
    }

    @Before
    public void setUp(){
        connectionService = getApplication().connectionService();
    }

    @After
    public void tearDown() {
        new IndexTearDownStrategy().tearDown(this);
    }

    @Test
    @KnownBug("CSA-1736")
    public void testWebConnectorURLOpensInNewTab() throws InterruptedException {
        String connectorURL = "https://www.google.co.uk";
        connector = new WebConnector(connectorURL,"google").withDepth(1).withDuration(60);
        connectionService.setUpConnection(connector);
        connectionsDetailPage = connectionService.goToDetails(connector);

        verifyThat(getMainSession().countWindows(), is(1));

        Window secondWindow = null;
        try {
            connectionsDetailPage.webConnectorURL().click();
            new WebDriverWait(getDriver(), 5)
                    .until(getMainSession().windowCountIs(2));

            verifyThat(getMainSession().countWindows(), is(2));
            secondWindow = getMainSession().switchWindow(1);

            verifyThat(secondWindow, urlContains(connectorURL));
        } finally {
            if(secondWindow != null) {
                secondWindow.close();
                getMainSession().switchWindow(0);
            }
        }
    }

    @Test
    @KnownBug({"CSA-1470", "CSA-2036", "CSA-2053"})
    public void testCancellingConnectorScheduling(){
        connector = new WebConnector("http://www.google.co.uk","google").withDuration(60);

        connectionService.setUpConnection(connector);
        connectionsDetailPage = connectionService.cancelConnectionScheduling(connector);

        verifyThat(connectionsDetailPage.getScheduleString(), is("The connector is not scheduled to run."));
    }

    @Test
    @KnownBug("CSA-2036")
    public void testChangeWebConnectorURL() {
        Index index = new Index("not_displayed", "connector display name");
        getApplication().indexService().setUpIndex(index);

        WebConnector connector = new WebConnector("http://www.bbc.co.uk", "bbc", index).withDepth(0).withDuration(60);
        connectionService.setUpConnection(connector);

        connectionsDetailPage = connectionService.goToDetails(connector);
        connectionsDetailPage.editButton().click();

        final String updateUrl = "http://www.itv.com";
        connector.setUrl(updateUrl);
        NewConnectionPage editConnectionPage = getElementFactory().getNewConnectionPage();
        Wizard editWizard = connector.makeEditWizard(editConnectionPage);

        editWizard.getCurrentStep().apply();
        verifyThat(editConnectionPage.getConnectorTypeStep().connectorUrl().getValue(), is(updateUrl));
        editWizard.next();

        editWizard.next();

        verifyThat(editConnectionPage.getIndexStep().getChosenIndexOnPage().getDisplayName(), is(index.getDisplayName()));
        editWizard.next();

        connectionsDetailPage = connectionService.goToDetails(connector);
        verifyThat(connectionsDetailPage.webConnectorURL(), hasTextThat(is(updateUrl)));
    }

    @Test
    @KnownBug({"CSA-1469", "CSA-2036"})
    public void testEditConnectorWithNoIndex(){
        IndexService indexService = getApplication().indexService();
        Index indexOne = new Index("one");
        Index indexTwo = new Index("two");

        indexService.setUpIndex(indexOne);
        indexService.setUpIndex(indexTwo);

        connector = new WebConnector("http://www.bbc.co.uk", "bbc", indexOne).withDuration(60);

        connectionService.setUpConnection(connector);
        connectionService.goToDetails(connector);

        verifyIndexNameForConnector();

        indexService.deleteIndexViaAPICalls(indexOne, testUser, getConfig().getAppUrl("api"));

        indexService.goToIndexes();

        getWindow().refresh();

        verifyThat(getElementFactory().getIndexesPage().getIndexDisplayNames(), not(hasItem(indexOne.getName())));

        connector = connectionService.changeIndex(connector, indexTwo);

        verifyIndexNameForConnector();
    }

    private void verifyIndexNameForConnector() {
        verifyThat(getElementFactory().getConnectionsDetailPage().getIndexName(), is(connector.getIndex().getName()));
    }
}
