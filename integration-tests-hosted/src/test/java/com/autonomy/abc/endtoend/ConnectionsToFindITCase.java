package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.element.Checkbox;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;

import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

//CSA-1565
public class ConnectionsToFindITCase extends HostedTestBase {
    private ConnectionService connectionService;
    private SearchActionFactory searchActionFactory;
    private PromotionActionFactory promotionActionFactory;
    private KeywordService keywordService;
    private final WebConnector connector = new WebConnector("http://www.fifa.com", "fifa");
    private final String indexName = "fifa";
    private final String searchTerm = "football";
    private final String trigger = "corruption";

    private List<String> synonyms = Arrays.asList(searchTerm,"evil","malfoy","slytherin","greed");
    private SearchPage searchPage;
    private List<String> promotedTitles;

    public ConnectionsToFindITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp(){
        connectionService = new ConnectionService(getApplication(), getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
        promotionActionFactory = new PromotionActionFactory(getApplication(),getElementFactory());
        keywordService = new KeywordService(getApplication(), getElementFactory());
    }

    @Test
    public void testConnectionsToFind() throws InterruptedException {
        connectionService.setUpConnection(connector);
        Search search = searchActionFactory.makeSearch(searchTerm);
        search.applyFilter(new IndexFilter(indexName));
        searchPage = search.apply();

        verifyThat("index shows up on search page", searchPage.getSelectedDatabases(), hasItem(indexName));
        verifyThat("index has search results", searchPage.getHeadingResultsCount(), greaterThan(0));

        promotedTitles = searchPage.createAMultiDocumentPromotion(3);
        getElementFactory().getCreateNewPromotionsPage().addSpotlightPromotion("", trigger);

        assertThat(searchPage.promotionsSummaryList(true), containsInAnyOrder(promotedTitles.toArray()));

        searchPage = keywordService.addSynonymGroup(Language.ENGLISH, synonyms);

        assertThat(searchPage.promotionsSummaryList(true), containsInAnyOrder(promotedTitles.toArray()));

        assertPromotedItemsForEverySynonym();

        connectionService.deleteConnection(connector, true);

        assertPromotedItemsForEverySynonym();

        promotionActionFactory.makeDelete(trigger).apply();

        for(String synonym : synonyms){
            searchActionFactory.makeSearch(synonym).apply();
            assertThat(searchPage.promotionsSummaryList(false), empty());
        }

        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);

        IndexesPage indexesPage = getElementFactory().getIndexesPage();
        indexesPage.findIndex(indexName).findElement(By.className("fa-trash-o")).click();
        Waits.loadOrFadeWait();
        getDriver().findElement(By.className("btn-alert")).click();

        searchActionFactory.makeSearch(searchTerm).apply();

        for(Checkbox checkbox : searchPage.indexList()){
            assertThat(checkbox.getName(),not(indexName));
        }
    }

    private void assertPromotedItemsForEverySynonym() {
        for(String synonym : synonyms){
            searchActionFactory.makeSearch(synonym).apply();
            assertThat(searchPage.promotionsSummaryList(true), containsInAnyOrder(promotedTitles.toArray()));
        }
    }

    @After
    public void tearDown(){
        connectionService.deleteConnection(connector, true);
    }
}
