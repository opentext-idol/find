package com.autonomy.abc.indexes;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexesPageITCase extends HostedTestBase {
    IndexesPage indexesPage;

    public IndexesPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    @Override
    public void baseSetUp() throws InterruptedException {
        regularSetUp();
        hostedLogIn("yahoo");
        getElementFactory().getPromotionsPage();


        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        indexesPage = getElementFactory().getIndexesPage();

        body = getBody();
    }

    @Test
    //CSA1720
    public void testDefaultIndexIsNotDeletedWhenDeletingTheSoleConnectorAssociatedWithIt(){
        ConnectionService cs = getApplication().createConnectionService(getElementFactory());
        Index default_index = new Index("default_index");
        WebConnector connector = new WebConnector("www.bbc.co.uk","bbc",default_index);

        //Create connection
        cs.setUpConnection(connector);

        try {
            //Try to delete the connection, (and the default index)
            cs.deleteConnection(connector, true);
        } catch (ElementNotFoundException e) {
            //If there's an error it is likely because the index couldn't be deleted - which is expected
        }

        //Navigate to indexes
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        IndexesPage indexesPage = getElementFactory().getIndexesPage();

        //Make sure default index is still there
        assertThat(indexesPage.getIndexNames(),hasItem(default_index.getName()));
    }

    @Test
    //Potentially should be in ConnectionsPageITCase
    //CSA1710
    public void testDeletingConnectionWhileItIsProcessingDoesNotDeleteAssociatedIndex(){
        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        ConnectionsPage connectionsPage = getElementFactory().getConnectionsPage();
        ConnectionService connectionService = getApplication().createConnectionService(getElementFactory());

        //Create connector; index will be automatically set to 'bbc'
        WebConnector connector = new WebConnector("www.bbc.co.uk","bbc");
        Index index = connector.getIndex();

        //Create new connector - NO WAIT
        connectionsPage.newConnectionButton().click();
        NewConnectionPage newConnectionPage = getElementFactory().getNewConnectionPage();
        connector.makeWizard(newConnectionPage).apply();

        //Try deleting the index straight away, while it is still processing
        //TODO change the Gritter Notice it's expecting
        connectionService.deleteConnection(connector, true);

        //Navigate to Indexes
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        IndexesPage indexesPage = getElementFactory().getIndexesPage();

        //Ensure the index wasn't deleted
        assertThat(indexesPage.getIndexNames(),hasItem(index.getName()));
    }

    @Test
    //CSA1626
    public void testDeletingIndexDoesNotInvalidatePromotions(){
        Index index = new Index("bbc");

        //Create index
        IndexService indexService = getApplication().createIndexService(getElementFactory());
        indexService.setUpIndex(index);

        //Create connection - attached to the same index (we need it to have data for a promotion)
        ConnectionService connectionService = getApplication().createConnectionService(getElementFactory());
        WebConnector connector = new WebConnector("www.bbc.co.uk","bbc",index);

        connectionService.setUpConnection(connector);

        //Create a promotion (using the index created)
        PromotionService promotionService = getApplication().createPromotionService(getElementFactory());
        PinToPositionPromotion ptpPromotion = new PinToPositionPromotion(1,"trigger");
        Search search = new Search(getApplication(),getElementFactory(),"search").applyFilter(new IndexFilter(index));

        promotionService.setUpPromotion(ptpPromotion, search, 3);

        //Now delete the index
        indexService.deleteIndex(index);

        //Navigate to the promotion - this will time out if it can't get to the Promotions Detail Page
        PromotionsDetailPage pdp = promotionService.goToDetails(ptpPromotion);

        //Get the promoted documents, there should still be three
        List<String> promotionTitles = pdp.getPromotedTitles();
        assertThat(promotionTitles.size(),is(3));

        //All documents should know be 'unknown documents'
        for(String promotionTitle : promotionTitles){
            assertThat(promotionTitle,is("Unknown Document"));
        }
    }

}
