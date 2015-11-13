package com.autonomy.abc.indexes;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.HSOApplication;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static junit.framework.TestCase.fail;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexesPageITCase extends ABCTestBase {
    IndexesPage indexesPage;
    HSOElementFactory hsoElementFactory;
    HSOApplication hsoApplication;

    public IndexesPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    @Override
    public void baseSetUp() throws InterruptedException {
        regularSetUp();
        hostedLogIn("yahoo");
        getElementFactory().getPromotionsPage();

        hsoElementFactory = (HSOElementFactory) getElementFactory();
        hsoApplication = (HSOApplication) getApplication();

        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        indexesPage = hsoElementFactory.getIndexesPage();

        body = getBody();
    }

    @Test
    //CSA1720
    public void testDefaultIndexIsNoDeletedWhenDeletingTheSoleConnectorAssociatedWithIt(){
        ConnectionService cs = hsoApplication.createConnectionService(getElementFactory());
        WebConnector connector = new WebConnector("www.bbc.co.uk","bbc",new Index("default_index"));

        cs.setUpConnection(connector);

        try {
            cs.deleteConnection(connector, true);

            fail("Deleting connection succeeded in deleting index");
        } catch (ElementNotFoundException e) {}

        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);

        assertThat(hsoElementFactory.getIndexesPage().getIndexNames(),hasItem("default_index"));
    }

    @Test
    //Potentially should be in ConnectionsPageITCase
    //CSA1710
    public void testDeletingConnectionWhileItIsProcessingDoesNotDeleteAssociatedIndex(){
        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        ConnectionsPage connectionsPage = hsoElementFactory.getConnectionsPage();
        ConnectionService connectionService = hsoApplication.createConnectionService(getElementFactory());

        WebConnector connector = new WebConnector("www.bbc.co.uk","bbc");
        Index index = connector.getIndex();

        connectionsPage.newConnectionButton().click();
        NewConnectionPage newConnectionPage = hsoElementFactory.getNewConnectionPage();
        connector.makeWizard(newConnectionPage).apply();

        connectionService.deleteConnection(connector, true);

        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        IndexesPage indexesPage = hsoElementFactory.getIndexesPage();

        assertThat(indexesPage.getIndexNames(),hasItem(index.getName()));
    }

    @Test
    //CSA1626
    public void testDeletingIndexDoesNotInvalidatePromotions(){}

}
