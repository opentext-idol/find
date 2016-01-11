package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.PromotionsDetailTriggerForm;
import com.autonomy.abc.selenium.element.Removable;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.analytics.Term;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.Waits;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;

//CSA-1572
public class AnalyticsE2EITCase extends HostedTestBase {

    private AnalyticsPage analyticsPage;
    private SearchPage searchPage;
    private PromotionsDetailPage promotionsDetailPage;

    private PromotionService promotionService;
    private SearchService searchService;
    private final static Matcher<? super WebElement> NO_RESULTS = containsText("No results found");
    private final static Logger LOGGER = LoggerFactory.getLogger(AnalyticsE2EITCase.class);

    public AnalyticsE2EITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        List<String> searchTerms = Arrays.asList("one", "two", "three");
        List<String> triggers = Arrays.asList("trigger1", "trigger2", "trigger3");
        List<Integer> searchOrder = Arrays.asList(0, 1, 0, 1, 0, 2);

        searchService = getApplication().createSearchService(getElementFactory());
        promotionService = getApplication().createPromotionService(getElementFactory());

        deleteAllKeywords();
        for (int i=0; i < searchTerms.size(); i++) {
            setUpPromotion(searchTerms.get(i), triggers.get(i));
        }
        for (Integer termIndex : searchOrder) {
            search(triggers.get(termIndex));
        }
        goToAnalytics();
    }

    @Test
    public void testAnalytics() throws InterruptedException {
        List<String> newTriggers = Arrays.asList("happy", "sad");

        Term nonZeroTerm;
        try {
            nonZeroTerm = analyticsPage.getMostPopularNonZeroSearchTerm();
        } catch (NoSuchElementException e) {
            // CSA-1752
            LOGGER.warn("all popular search terms are zero hit terms");
            nonZeroTerm = analyticsPage.getPopularSearchTerm(2);
        }
        Term zeroTerm = analyticsPage.getMostPopularZeroSearchTerm();
        addSynonymGroup(nonZeroTerm, zeroTerm);

        verifyTermSearch(zeroTerm);
        verifyTermSearch(nonZeroTerm);

        goToAnalytics();
        tryGoToLeastPopularPromotion();
        if (promotionsDetailPage == null) {
            // least popular promotion no longer exists
            goToFirstPromotion();
        }

        PromotionsDetailTriggerForm triggerForm = promotionsDetailPage.getTriggerForm();

        List<String> existingTriggers = triggerForm.getTriggersAsStrings();
        List<String> promotedDocuments = promotionsDetailPage.getPromotedTitles();

        for(String trigger : newTriggers){
            triggerForm.addTrigger(trigger);
        }

        for(Removable trigger : triggerForm.getTriggers()){
            if(!newTriggers.contains(trigger.getText())) {
                trigger.removeAndWait();
            }
        }

        for(String trigger : newTriggers){
            verifyTriggerPromotes(promotedDocuments, trigger, true);
        }

        for(String trigger : existingTriggers){
            verifyTriggerPromotes(promotedDocuments, trigger, false);
        }
    }

    @After
    public void tearDownKeywords() {
        deleteAllKeywords();
    }

    @After
    public void tearDownPromotions() {
        promotionService.deleteAll();
    }

    private void deleteAllKeywords() {
        getBody().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        getElementFactory().getKeywordsPage().deleteKeywords();
    }

    private void setUpPromotion(String searchTerm, String trigger) {
        Promotion promotion = new SpotlightPromotion(trigger);
        promotionService.setUpPromotion(promotion, searchTerm, 3);
        LOGGER.info("set up promotion for trigger " + trigger);
    }

    private void search(String searchTerm) {
        searchPage = searchService.search(searchTerm);
    }

    private void goToAnalytics() {
        getBody().getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        analyticsPage = getElementFactory().getAnalyticsPage();
    }

    private void addSynonymGroup(Term... terms) throws InterruptedException {
        List<String> synonyms = new ArrayList<>();
        for (Term term : terms) {
            synonyms.add(term.getTerm());
        }
        new KeywordService(getApplication(), getElementFactory()).addSynonymGroup(synonyms);
        LOGGER.info("added synonym group: " + synonyms);
    }

    private void verifyTermSearch(Term term) {
        search(term.getTerm());
        LOGGER.warn("[CSA-1724] skipping query analysis test");
//        verifyThat(searchPage.getSynonymGroupSynonyms(term.getTerm()), hasItem(equalToIgnoringCase(term.getTerm())));
        verifyThat(searchPage, not(NO_RESULTS));
    }

    private void tryGoToLeastPopularPromotion() {
        analyticsPage.reversePromotionSort();
        analyticsPage.getMostPopularPromotion().click();
        if (getDriver().getCurrentUrl().contains("detail")) {
            promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
            LOGGER.info("gone to least popular promotion");
        } else {
            promotionsDetailPage = null;
        }
    }

    private void goToFirstPromotion() {
        promotionService.goToPromotions().promotionsList().get(0).click();
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        LOGGER.warn("gone to first promotion in list");
    }

    private void verifyTriggerPromotes(List<String> promotedDocuments, String trigger, boolean promotes) {
        search(trigger);
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
        Waits.loadOrFadeWait();
        if (promotes) {
            verifyThat(searchPage.getPromotedDocumentTitles(true), everyItem(isIn(promotedDocuments)));
        } else {
            verifyThat(searchPage.getPromotedDocumentTitles(true), everyItem(not(isIn(promotedDocuments))));
        }
    }
}
