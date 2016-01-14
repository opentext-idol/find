package com.autonomy.abc.indexes;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.util.DriverUtil;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.PageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexesPageITCase extends HostedTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexesPageITCase.class);
    private IndexService indexService;
    private IndexesPage indexesPage;

    public IndexesPageITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        // requires a separate account where indexes can safely be added and deleted
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp() {
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        indexesPage = getElementFactory().getIndexesPage();
        body = getBody();
        indexService = getApplication().createIndexService(getElementFactory());
    }

    @Test
    //CSA-1450
    public void testDeletingIndex(){
        Index index = new Index("index");
        indexesPage = indexService.setUpIndex(index);

        verifyThat(indexesPage.getIndexDisplayNames(), hasItem(index.getName()));

        indexService.deleteIndex(index);

        verifyThat(indexesPage.getIndexDisplayNames(), not(hasItem(index.getName())));
    }

    @Test
    //CSA-1720
    public void testDefaultIndexIsNotDeletedWhenDeletingTheSoleConnectorAssociatedWithIt(){
        ConnectionService cs = getApplication().createConnectionService(getElementFactory());
        WebConnector connector = new WebConnector("http://www.bbc.co.uk","bbc", Index.DEFAULT).withDepth(2);

        //Create connection
        cs.setUpConnection(connector);

        try {
            //Try to delete the connection, (and the default index)
            cs.deleteConnection(connector, true);
        } catch (ElementNotVisibleException | NoSuchElementException e) {
            //If there's an error it is likely because the index couldn't be deleted - which is expected
            //Need to exit the deletion modal that will still be open
            getDriver().findElement(By.cssSelector(".modal-footer [type=button]")).click();
        }

        //Navigate to indexes
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        IndexesPage indexesPage = getElementFactory().getIndexesPage();

        //Make sure default index is still there
        assertThat(indexesPage.getIndexDisplayNames(), hasItem(Index.DEFAULT.getDisplayName()));
    }

    @Test
    //Potentially should be in ConnectionsPageITCase
    //CSA1710
    public void testAttemptingToDeleteConnectionWhileItIsProcessingDoesNotDeleteAssociatedIndex(){
        body.getSideNavBar().switchPage(NavBarTabId.CONNECTIONS);
        ConnectionsPage connectionsPage = getElementFactory().getConnectionsPage();
        ConnectionService connectionService = getApplication().createConnectionService(getElementFactory());

        //Create connector; index will be automatically set to 'bbc'
        WebConnector connector = new WebConnector("http://www.bbc.co.uk","bbc").withDepth(2);
        Index index = connector.getIndex();

        //Create new connector - NO WAIT
        connectionsPage.newConnectionButton().click();
        NewConnectionPage newConnectionPage = getElementFactory().getNewConnectionPage();
        connector.makeWizard(newConnectionPage).apply();

        //Try deleting the index straight away, while it is still processing
        //TODO change the Gritter Notice it's expecting
        try {
            connectionService.deleteConnection(connector, true);
        } catch (Exception e) {
            LOGGER.warn("Error deleting index");
        }

        //Navigate to Indexes
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        IndexesPage indexesPage = getElementFactory().getIndexesPage();

        //Ensure the index wasn't deleted
        assertThat(indexesPage.getIndexDisplayNames(), hasItem(index.getName()));
    }

    @Test
    //CSA1626
    public void testDeletingIndexDoesNotInvalidatePromotions(){
        //Create connection - attached to the same index (we need it to have data for a promotion)
        ConnectionService connectionService = getApplication().createConnectionService(getElementFactory());
        WebConnector connector = new WebConnector("http://www.bbc.co.uk","bbc").withDepth(2);

        connectionService.setUpConnection(connector);

        //Create a promotion (using the index created)
        PromotionService promotionService = getApplication().createPromotionService(getElementFactory());
        PinToPositionPromotion ptpPromotion = new PinToPositionPromotion(1,"trigger");
        SearchQuery search = new SearchQuery("bbc").withFilter(new IndexFilter(connector.getIndex()));

        try {
            int numberOfDocs = 1;
            promotionService.setUpPromotion(ptpPromotion, search, numberOfDocs);

            //Now delete the index
            connectionService.deleteConnection(connector, true);

            //Navigate to the promotion - this will time out if it can't get to the Promotions Detail Page
            PromotionsDetailPage pdp = promotionService.goToDetails(ptpPromotion);

            //Get the promoted documents, there should still be one
            //TODO this is a workaround as getting promoted documents 'properly' errors if they are 'Unknown Document's
            List<WebElement> promotedDocuments = getDriver().findElements(By.cssSelector(".promoted-documents-list h3"));

            assertThat(promotedDocuments.size(), is(numberOfDocs));

            //All documents should know be 'unknown documents'
            for(WebElement promotedDocument : promotedDocuments){
                assertThat(promotedDocument.getText(), is("Unknown Document"));

            }
        } finally {
            promotionService.deleteAll();
        }
    }

    @Test
    //CSA1544
    public void testNoInvalidIndexNameNotifications(){
        ConnectionService connectionService = getApplication().createConnectionService(getElementFactory());

        Connector hassleRecords = new WebConnector("http://www.hasslerecords.com","hassle records").withDepth(1);
        String errorMessage = "Index name invalid";

        connectionService.setUpConnection(hassleRecords);

        try {
            new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining(errorMessage));

            fail("Index name should be valid - likely failed due to double encoding of requests");
        } catch (TimeoutException e){
            LOGGER.info("Timeout exception");
        }

        body.getTopNavBar().notificationsDropdown();
        for(String message : body.getTopNavBar().getNotifications().getAllNotificationMessages()){
            assertThat(message,not(errorMessage));
        }
    }

    @Test
    //CSA-1689
    public void testNewlyCreatedIndexSize (){
        IndexService indexService = getApplication().createIndexService(getElementFactory());
        indexService.deleteAllIndexes();

        Index index = new Index("yellow cat red cat");

        indexService.setUpIndex(index);
        indexService.goToDetails(index);

        IndexesDetailPage indexesDetailPage = getElementFactory().getIndexesDetailPage();

        verifyThat(indexesDetailPage.sizeString(), allOf(containsString("128 B"), containsString("(0 items)")));
    }

    @Test
    //CSA-1735
    public void testNavigatingToNonExistingIndexByURL(){
        getDriver().get("https://search.dev.idolondemand.com/search/#/index/doesntexistmate");
        verifyThat(PageUtil.getWrapperContent(getDriver()), containsText(Errors.Index.INVALID_INDEX));
    }

    @Test
    //CSA-1886
    public void testDeletingDefaultIndex(){
        IndexService indexService = getApplication().createIndexService(getElementFactory());

        indexService.deleteIndexViaAPICalls(Index.DEFAULT, getCurrentUser(), config.getApiUrl());

        getDriver().navigate().refresh();

        indexesPage = getElementFactory().getIndexesPage();
        body = getBody();

        verifyThat(indexesPage.getIndexDisplayNames(), hasItem(Index.DEFAULT.getName()));
    }

    @Test
    //CCUK-3450
    public void testFindNoParametricFields(){
        Index index = new Index("index");
        indexService.setUpIndex(index);

        List<String> browserHandles = DriverUtil.createAndListWindowHandles(getDriver());

        try {
            getDriver().switchTo().window(browserHandles.get(1));
            getDriver().get(config.getFindUrl());
            getDriver().manage().window().maximize();
            Find find = getElementFactory().getFindPage();

            find.search("search");
            find.filterBy(new IndexFilter(index));

            verifyThat(find.getResultsPage().resultsDiv().getText(), is("No results found"));
        } finally {
            getDriver().switchTo().window(browserHandles.get(1));
            getDriver().close();
            getDriver().switchTo().window(browserHandles.get(0));
        }
    }

    @After
    public void tearDown(){
        try {
            getApplication().createConnectionService(getElementFactory()).deleteAllConnections(false);
            getApplication().createIndexService(getElementFactory()).deleteAllIndexes();
        } catch (Exception e) {
            LOGGER.warn("Failed to tear down");
        }
    }
}
