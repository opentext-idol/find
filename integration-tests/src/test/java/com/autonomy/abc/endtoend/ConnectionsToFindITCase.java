package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionActionFactory;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.element.Checkbox;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.apache.commons.lang3.StringUtils;
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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

public class ConnectionsToFindITCase extends ABCTestBase {
    public ConnectionsToFindITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }


    private ConnectionActionFactory connectionActionFactory;
    private SearchActionFactory searchActionFactory;
    private PromotionActionFactory promotionActionFactory;

    private final WebConnector connector = new WebConnector("http://www.fifa.com", "fifa");
    private final String indexName = "fifa";
    private final String searchTerm = "football";
    private final String trigger = "corruption";

    private List<String> synonyms = Arrays.asList(searchTerm,"evil","malfoy","slytherin","greed");
    private SearchPage searchPage;
    private List<String> promotedTitles;

    @Before
    public void setUp(){
        connectionActionFactory = new ConnectionActionFactory(getApplication(), getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
        promotionActionFactory = new PromotionActionFactory(getApplication(),getElementFactory());
    }

    @Test
    public void testConnectionsToFind() throws InterruptedException {
        connectionActionFactory.makeSetUpConnection(connector).apply();
        Search search = searchActionFactory.makeSearch(searchTerm);
        search.applyFilter(new IndexFilter(indexName));
        search.apply();

        searchPage = getElementFactory().getSearchPage();
        verifyThat("index shows up on search page", searchPage.getSelectedDatabases(), hasItem(indexName));
        verifyThat("index has search results", searchPage.countSearchResults(), greaterThan(0));

        promotedTitles = searchPage.createAMultiDocumentPromotion(3);
        getElementFactory().getCreateNewPromotionsPage().addSpotlightPromotion("", trigger);

        assertThat(searchPage.getPromotedDocumentTitles(), containsInAnyOrder(promotedTitles.toArray()));

        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
        keywordsPage.createNewKeywordsButton().click();
        getElementFactory().getCreateNewKeywordsPage().createSynonymGroup(StringUtils.join(synonyms, " "), "English");

        searchPage = getElementFactory().getSearchPage();
        assertThat(searchPage.getPromotedDocumentTitles(), containsInAnyOrder(promotedTitles.toArray()));

        assertPromotedItemsForEverySynonym();

        connectionActionFactory.makeDeleteConnection(connector).apply();

        assertPromotedItemsForEverySynonym();

        promotionActionFactory.makeDelete(trigger).apply();

        for(String synonym : synonyms){
            searchActionFactory.makeSearch(synonym).apply();
            assertThat(searchPage.getPromotedResults().size(),is(0));
        }

        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);

        IndexesPage indexesPage = ((HSOElementFactory) getElementFactory()).getIndexesPage();
        indexesPage.findIndex(indexName).findElement(By.className("fa-trash-o")).click();
        indexesPage.loadOrFadeWait();
        getDriver().findElement(By.className("btn-alert")).click();

        searchActionFactory.makeSearch(searchTerm).apply();

        for(Checkbox checkbox : searchPage.indexList()){
            assertThat(checkbox.getName(),not(indexName));
        }
    }

    private void assertPromotedItemsForEverySynonym() {
        for(String synonym : synonyms){
            searchActionFactory.makeSearch(synonym).apply();
            assertThat(searchPage.getPromotedDocumentTitles(), containsInAnyOrder(promotedTitles.toArray()));
        }
    }

    @After
    public void tearDown(){}
}
