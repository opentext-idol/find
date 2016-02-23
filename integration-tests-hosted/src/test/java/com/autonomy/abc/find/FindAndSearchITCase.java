package com.autonomy.abc.find;

import com.autonomy.abc.config.FindTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.*;
import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasTextThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.Is.is;
import static org.openqa.selenium.lift.Matchers.displayed;

public class FindAndSearchITCase extends FindTestBase {
    private final Matcher<String> noDocs = containsString(Errors.Search.NO_RESULTS);

    private FindPage findPage;
    private FindResultsPage findResultsPage;
    private SearchPage searchPage;

    private HSODPromotionService promotionService;
    private KeywordService keywordService;
    private Window searchWindow;
    private Window findWindow;
    private HSODApplication searchApp = new HSODApplication();

    public FindAndSearchITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findWindow = getWindow();
        findPage = getElementFactory().getFindPage();
        findResultsPage = findPage.getResultsPage();

        searchWindow = launchInNewWindow(searchApp);
        promotionService = searchApp.promotionService();
        keywordService = searchApp.keywordService();
    }

    private void adminSearch(String term) {
        searchWindow.activate();
        searchPage = searchApp.searchService().search(term);
    }

    private void findSearch(String term) {
        findWindow.activate();
        findPage.search(term);
    }

    @Test
    public void testSortByRelevance() {
        adminSearch("stars bbc");
        searchPage.sortBy(SearchBase.Sort.RELEVANCE);
        List<String> searchTitles = searchPage.getSearchResultTitles(30);

        findSearch("stars bbc");
        assertThat(findResultsPage.getResultTitles(), is(searchTitles));
    }

    @Test
    public void testSortByDate(){
        adminSearch("stars bbc");
        searchPage.sortBy(SearchBase.Sort.DATE);
        List<String> searchTitles = searchPage.getSearchResultTitles(30);

        findSearch("stars bbc");
        findPage.sortBy(SearchBase.Sort.DATE);

        assertThat(findResultsPage.getResultTitles(), is(searchTitles));
    }

    @Test
    public void testPinToPosition(){
        String search = "red";
        String trigger = "mate";
        PinToPositionPromotion promotion = new PinToPositionPromotion(1, trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try {
            String documentTitle = promotionService.setUpPromotion(promotion, search, 1).get(0);

            findSearch(trigger);
            assertThat(findResultsPage.searchResult(1).getTitleString(), is(documentTitle));
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    public void testPinToPositionThree(){
        String search = "red";
        String trigger = "mate";
        PinToPositionPromotion promotion = new PinToPositionPromotion(3, trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try {
            String documentTitle = promotionService.setUpPromotion(promotion, search, 1).get(0);

            findSearch(trigger);
            assertThat(findResultsPage.searchResult(3).getTitleString(), is(documentTitle));
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    @KnownBug("CSA-2098")
    public void testSpotlightPromotions(){
        String search = "Proper";
        String trigger = "Prim";
        SpotlightPromotion spotlight = new SpotlightPromotion(trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try {
            List<String> createdPromotions = promotionService.setUpPromotion(spotlight, search, 3);

            findSearch(trigger);

            List<String> findPromotions = findResultsPage.getPromotionsTitles();

            assertThat(findPromotions, not(empty()));
            assertThat(createdPromotions, everyItem(isIn(findPromotions)));

            promotionShownCorrectly(findResultsPage.promotions());
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    @KnownBug("CSA-2098")
    public void testStaticPromotions(){
        String title = "TITLE";
        String content = "CONTENT";
        String trigger = "LOVE";
        StaticPromotion promotion = new StaticPromotion(title, content, trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try {
            ((HSODPromotionService) promotionService).setUpStaticPromotion(promotion);

            findSearch(trigger);
            List<FindSearchResult> promotions = findResultsPage.promotions();

            assertThat(promotions.size(), is(1));
            FindSearchResult staticPromotion = promotions.get(0);
            assertThat(staticPromotion.getTitleString(), is(title));
            assertThat(staticPromotion.getDescription(), containsString(content));
            promotionShownCorrectly(staticPromotion);
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    @KnownBug({"CSA-2058 - titles on Search Optimizer are blank this ruins the test trying to check Find against them","CSA-2067 - 'Rugby', for some reason, is hated by Find"})
    public void testDynamicPromotions(){
        int resultsToPromote = 13;
        String search = "kittens";
        String trigger = "Rugby";
        DynamicPromotion dynamicPromotion = new DynamicPromotion(resultsToPromote, trigger);

        searchWindow.activate();
        promotionService.deleteAll();

        try{
            List<String> promotedDocumentTitles = promotionService.setUpPromotion(dynamicPromotion, search, resultsToPromote);

            findSearch(trigger);

            verifyThat(promotedDocumentTitles, everyItem(isIn(findResultsPage.getPromotionsTitles())));

            promotionShownCorrectly(findResultsPage.promotions());
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    private void promotionShownCorrectly (FindSearchResult promotion){
        verifyThat(promotion.isPromoted(), is(true));
        verifyThat(promotion.star(), displayed());
    }

    private void promotionShownCorrectly (List<FindSearchResult> promotions){
        for(FindSearchResult promotion : promotions){
            promotionShownCorrectly(promotion);
        }
    }

    @Test
    public void testSynonyms() throws InterruptedException {
        String nonsense = "iuhdsafsaubfdja";
        searchWindow.activate();
        keywordService.deleteAll(KeywordFilter.ALL);

        findSearch(nonsense);
        assertThat(findResultsPage.getText(), noDocs);

        findSearch("Cat");
        assertThat(findResultsPage.getText(), not(noDocs));

        searchWindow.activate();
        keywordService.addSynonymGroup(Language.ENGLISH, "cat", nonsense);

        /* need a separate session due to caching */
        Session secondSession = launchInNewSession(new HSODFind());
        try {
            FindPage otherFindPage = initialiseSession(secondSession);
            otherFindPage.search("Cat");
            FindResultsPage otherResults = otherFindPage.getResultsPage();
            String firstTitle = otherResults.searchResult(1).getTitleString();

            otherFindPage.search(nonsense);
            assertThat(otherResults.getText(), not(noDocs));
            verifyThat(otherResults.searchResult(1).getTitleString(), is(firstTitle));

        } finally {
            getSessionRegistry().endSession(secondSession);
        }
    }

    @Test
    public void testBlacklist() throws InterruptedException {
        searchWindow.activate();
        keywordService.deleteAll(KeywordFilter.ALL);

        findSearch("Cat");
        assertThat(findResultsPage.getText(), not(noDocs));

        findSearch("Holder");

        searchWindow.activate();
        keywordService.addBlacklistTerms(Language.ENGLISH, "cat");

        /* need a separate session due to caching */
        Session secondSession = launchInNewSession(new HSODFind());
        try {
            FindPage otherFindPage = initialiseSession(secondSession);
            otherFindPage.search("Cat");

            assertThat(otherFindPage.getResultsPage(), hasTextThat(noDocs));
        } finally {
            getSessionRegistry().endSession(secondSession);
        }
    }

    // TODO: this does not belong here
    private FindPage initialiseSession(Session session) {
        HSODFindElementFactory otherElementFactory = new HSODFind(session.getActiveWindow()).elementFactory();
        otherElementFactory.getLoginPage().loginWith(getConfig().getDefaultUser().getAuthProvider());
        return otherElementFactory.getFindPage();
    }

    @Test   @Ignore("Not implemented")
    public void testOverlappingSynonyms(){}

    @Test
    @KnownBug("CSA-1630")
    public void testAllPromotedDocumentsHaveTitles(){
        try {
            searchWindow.activate();
            promotionService.setUpPromotion(new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Tiger"), "scg-2", 10);

            findSearch("Tiger");
            for(String title : findResultsPage.getPromotionsTitles()){
                assertThat(title, is(not("")));
            }

        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }

    @Test
    @KnownBug("CSA-2076")
    public void testLongTitleDoesNotOverflowFind(){
        try {
            String trigger = "trigger";

            searchWindow.activate();
            promotionService.setUpStaticPromotion(new StaticPromotion("loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooonnnnggggggggggggggggggggggg", "content", trigger));

            findWindow.activate();
            findPage.search(trigger);

            WebElement toggleRelatedConcepts = findPage.rightContainerToggleButton();

            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            Point p = toggleRelatedConcepts.getLocation();

            verifyThat("Collapse Related Concepts Icon Within Screen", p.getX() < d.getWidth() && p.getY() < d.getHeight());
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
    }
}
