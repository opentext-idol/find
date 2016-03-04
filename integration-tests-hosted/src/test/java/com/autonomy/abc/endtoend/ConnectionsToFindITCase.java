package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTearDown;
import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.HSODFind;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;

@RelatedTo("CSA-1565")
public class ConnectionsToFindITCase extends HostedTestBase {
    private ConnectionService connectionService;
    private SearchService searchService;
    private PromotionService<?> promotionService;
    private IndexService indexService;
    private final Index index = new Index("fifa");
    private final WebConnector connector = new WebConnector("http://www.fifa.com", index.getName(), index).withDuration(150);

    public ConnectionsToFindITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp(){
        connectionService = getApplication().connectionService();
        searchService = getApplication().searchService();
        promotionService = getApplication().promotionService();
        indexService = getApplication().indexService();
    }

    @After
    public void tearDown() {
        ABCTearDown.PROMOTIONS.tearDown(this);
    }

    @Test
    //Fails due to Unhandled Exception when attempting to create promotion
    public void testConnectionsToFind() throws InterruptedException {
        String searchTerm = "football";
        String trigger = "corruption";

        connectionService.setUpConnection(connector);

        SearchPage searchPage = searchService.search(new SearchQuery(searchTerm).withFilter(new IndexFilter(index)));
        verifyThat("index shows up on search page", searchPage.indexesTree().getSelected(), hasItem(index));
        verifyThat("index has search results", searchPage.getHeadingResultsCount(), greaterThan(0));

        Promotion promotion = new SpotlightPromotion(trigger);
        List<String> promotedTitles = promotionService.setUpPromotion(promotion, searchTerm, 3);
        assertThat(searchPage.getPromotedDocumentTitles(true), containsInAnyOrder(promotedTitles.toArray()));

        Window searchOptimizerWindow = getWindow();
        HSODFind findApp = new HSODFind();
        Window findWindow = launchInNewWindow(findApp);
        findWindow.activate();

        FindPage find = findApp.elementFactory().getFindPage();
        findApp.findService().search(trigger);

        verifyThat("Promoted documents show in Find", find.getResultsPage().getPromotionsTitles(), containsInAnyOrder(promotedTitles.toArray()));

        boolean hasIndex = false;
        for (IndexNodeElement indexNode : find.indexesTree().privateIndexes()){
            if(indexNode.getName().equals(index.getDisplayName())){
                hasIndex = true;
                break;
            }
        }
        verifyThat("Find has the created index", hasIndex);

        searchOptimizerWindow.activate();

        connectionService.deleteConnection(connector, false);

        promotionService.delete(promotion);
        searchPage = searchService.search(searchTerm);

        //Don't bother checking find due to caching
        assertThat(searchPage.getPromotedDocumentTitles(false), empty());

        indexService.deleteIndex(index);
        searchPage = searchService.search(searchTerm);
        for (IndexNodeElement node : searchPage.indexesTree()) {
            assertThat(node.getName(), not(index.getName()));
        }
    }

    @After
    public void tearDown(){
        connectionService.deleteAllConnections(true);
        indexService.deleteAllIndexes();
    }
}
