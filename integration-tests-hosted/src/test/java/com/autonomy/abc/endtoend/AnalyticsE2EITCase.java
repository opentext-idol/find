package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Removable;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.analytics.Term;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

//CSA-1572
public class AnalyticsE2EITCase extends ABCTestBase {

    private AnalyticsPage analyticsPage;
    private PromotionActionFactory promotionActionFactory;
    private SearchActionFactory searchActionFactory;
    private final Matcher<String> noDocs = containsString("No results found");

    public AnalyticsE2EITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        getElementFactory().getKeywordsPage().deleteKeywords();

        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
        promotionActionFactory = new PromotionActionFactory(getApplication(), getElementFactory());

        String trigger = "trigger";

        promotionActionFactory.makeCreatePromotion(
            new SpotlightPromotion(trigger + "1"),
            new SearchActionFactory(getApplication(), getElementFactory()).makeSearch("One"),
            3)
            .apply();

        promotionActionFactory.makeCreatePromotion(
            new SpotlightPromotion(trigger + "2"),
            new SearchActionFactory(getApplication(), getElementFactory()).makeSearch("Two"),
            3)
            .apply();

        promotionActionFactory.makeCreatePromotion(
            new SpotlightPromotion(trigger + "3"),
            new SearchActionFactory(getApplication(), getElementFactory()).makeSearch("Three"),
            3)
            .apply();

        searchActionFactory.makeSearch(trigger + "1").apply();
        searchActionFactory.makeSearch(trigger + "2").apply();
        searchActionFactory.makeSearch(trigger + "1").apply();
        searchActionFactory.makeSearch(trigger + "2").apply();
        searchActionFactory.makeSearch(trigger + "1").apply();
        searchActionFactory.makeSearch(trigger + "3").apply();

        body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        analyticsPage = ((HSOElementFactory) getElementFactory()).getAnalyticsPage();
    }

    @Test
    public void testAnalytics() throws InterruptedException {
        Pair<Term,Term> synonymTuple = new ImmutablePair<>(analyticsPage.getMostPopularNonZeroSearchTerm(),analyticsPage.getMostPopularZeroSearchTerm());

        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
        body = getBody();
        keywordsPage.createNewKeywordsButton().click();
        getElementFactory().getCreateNewKeywordsPage().createSynonymGroup(synonymTuple.getLeft().getTerm() + " " + synonymTuple.getRight().getTerm(), "English");

        searchActionFactory.makeSearch(synonymTuple.getLeft().getTerm());
        SearchPage searchPage = getElementFactory().getSearchPage();
        assertThat(searchPage.synonymInGroup(synonymTuple.getRight().getTerm()).getText(), is(synonymTuple.getRight().getTerm().toLowerCase()));
        assertThat(searchPage.getText(), not(noDocs));

        searchActionFactory.makeSearch(synonymTuple.getRight().getTerm());
        searchPage = getElementFactory().getSearchPage();
        assertThat(searchPage.synonymInGroup(synonymTuple.getLeft().getTerm()).getText(), is(synonymTuple.getLeft().getTerm().toLowerCase()));
        assertThat(searchPage.getText(), not(noDocs));

        body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
        analyticsPage = ((HSOElementFactory) getElementFactory()).getAnalyticsPage();
        body = getBody();

        analyticsPage.reversePromotionSort();
        analyticsPage.getMostPopularPromotion().findElement(By.tagName("a")).click();    //TODO rename

        if(!getDriver().getCurrentUrl().contains("detail")){
            getElementFactory().getPromotionsPage().promotionsList().get(0).click();
        }

        PromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();

        body = getBody();

        List<String> newTriggers = Arrays.asList("Happy","Sad");
        List<String> existingTriggers = promotionsDetailPage.getTriggerList();
        List<String> promotedDocuments = promotionsDetailPage.getPromotedTitles();

        for(String trigger : newTriggers){
            promotionsDetailPage.addTrigger(trigger);
        }

        for(Removable trigger : promotionsDetailPage.triggers()){
            if(!newTriggers.contains(trigger.getText())) {
                trigger.removeAndWait();
            }
        }

        for(String trigger : newTriggers){
            body.getTopNavBar().search(trigger);
            searchPage = getElementFactory().getSearchPage();
            searchPage.waitForPromotionsLoadIndicatorToDisappear();
            assertThat(searchPage.getPromotedDocumentTitles(), hasItems(promotedDocuments.toArray(new String[promotedDocuments.size()])));
        }

        for(String trigger : existingTriggers){
            body.getTopNavBar().search(trigger);
            searchPage = getElementFactory().getSearchPage();
            searchPage.waitForSearchLoadIndicatorToDisappear();
            assertThat(searchPage.getPromotedDocumentTitles(), not(hasItems(promotedDocuments.toArray(new String[promotedDocuments.size()]))));
        }

    }

    @After
    public void tearDown(){
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        getElementFactory().getKeywordsPage().deleteKeywords();
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        getElementFactory().getPromotionsPage().deleteAllPromotions();
    }
}
