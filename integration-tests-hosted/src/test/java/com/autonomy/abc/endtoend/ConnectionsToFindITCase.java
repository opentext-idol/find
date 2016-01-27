package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;

@RelatedTo("CSA-1565")
public class ConnectionsToFindITCase extends HostedTestBase {
    private ConnectionService connectionService;
    private KeywordService keywordService;
    private SearchService searchService;
    private PromotionService<?> promotionService;
    private IndexService indexService;
    private final Index index = new Index("fifa");
    private final WebConnector connector = new WebConnector("http://www.fifa.com", index.getName(), index).withDuration(180);
    private final String searchTerm = "football";
    private final String trigger = "corruption";

    private List<String> synonyms = Arrays.asList(searchTerm,"evil","malfoy","slytherin","greed");
    private SearchPage searchPage;
    private List<String> promotedTitles;

    public ConnectionsToFindITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp(){
        connectionService = getApplication().createConnectionService(getElementFactory());
        searchService = getApplication().createSearchService(getElementFactory());
        promotionService = getApplication().createPromotionService(getElementFactory());
        keywordService = getApplication().createKeywordService(getElementFactory());
        indexService = getApplication().createIndexService(getElementFactory());
    }

    @Test
    //Fails due to Unhandled Exception when attempting to create promotion
    public void testConnectionsToFind() throws InterruptedException {
        connectionService.setUpConnection(connector);

        searchPage = searchService.search(new SearchQuery(searchTerm).withFilter(new IndexFilter(index)));
        verifyThat("index shows up on search page", searchPage.indexesTree().getSelected(), hasItem(index));
        verifyThat("index has search results", searchPage.getHeadingResultsCount(), greaterThan(0));

        Promotion promotion = new SpotlightPromotion(trigger);
        promotedTitles = promotionService.setUpPromotion(promotion, searchTerm, 3);
        assertThat(searchPage.getPromotedDocumentTitles(true), containsInAnyOrder(promotedTitles.toArray()));

        searchPage = keywordService.addSynonymGroup(Language.ENGLISH, synonyms);
        assertThat(searchPage.getPromotedDocumentTitles(true), containsInAnyOrder(promotedTitles.toArray()));
        assertPromotedItemsForEverySynonym();

        connectionService.deleteConnection(connector, true);
        assertPromotedItemsForEverySynonym();

        promotionService.delete(promotion);
        for(String synonym : synonyms){
            searchService.search(synonym);
            assertThat(searchPage.getPromotedDocumentTitles(false), empty());
        }

        indexService.deleteIndex(index);
        searchService.search(searchTerm);
        for (IndexNodeElement node : searchPage.indexesTree()) {
            assertThat(node.getName(), not(index.getName()));
        }
    }

    private void assertPromotedItemsForEverySynonym() {
        for(String synonym : synonyms){
            searchService.search(synonym);
            assertThat(searchPage.getPromotedDocumentTitles(true), containsInAnyOrder(promotedTitles.toArray()));
        }
    }

    @After
    public void tearDown(){
        promotionService.deleteAll();
        connectionService.deleteConnection(connector, true);
    }
}
