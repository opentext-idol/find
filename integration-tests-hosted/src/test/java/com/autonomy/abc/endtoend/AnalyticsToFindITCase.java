package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResults;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.analytics.Term;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

//CSA-1590
public class AnalyticsToFindITCase extends HostedTestBase {
    private PromotionService promotionService;
    private KeywordService keywordService;

    public AnalyticsToFindITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());
        keywordService = new KeywordService(getApplication(), getElementFactory());

        PromotionsPage promotions = getElementFactory().getPromotionsPage();
        browserHandles = promotions.createAndListWindowHandles();
        getDriver().switchTo().window(browserHandles.get(1));
        getDriver().get(config.getFindUrl());
        getDriver().manage().window().maximize();
        find = getElementFactory().getFindPage();
        service = find.getService();
        getDriver().switchTo().window(browserHandles.get(0));
    }

    private FindPage find;
    private FindResults service;
    private List<String> browserHandles;

    @Test
    public void testPromotionToFind() throws InterruptedException {
        body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);

        AnalyticsPage analyticsPage = getElementFactory().getAnalyticsPage();
        body = getBody();

        Term zeroSearch = analyticsPage.getMostPopularZeroSearchTerm();
        Term nonZero = analyticsPage.getMostPopularNonZeroSearchTerm();

        if(nonZero == null){
            nonZero = new Term("replacement",0);
        }

        String searchTerm = nonZero.getTerm();
        String trigger = "Trigger";
        String synonym = zeroSearch.getTerm();

        promotionService.deleteAll();

        body = getBody();

        List<String> createdPromotions = promotionService.setUpPromotion(new SpotlightPromotion(trigger), searchTerm, 3);

        keywordService.addSynonymGroup(Language.ENGLISH, trigger, synonym);

        getDriver().switchTo().window(browserHandles.get(1));
        find.search(trigger);

        List<String> triggerResults = service.getResultTitles();

        find.search(synonym);

        List<String> findPromotions = service.getPromotionsTitles();

        verifyThat(findPromotions.size(), not(0));
        verifyThat(findPromotions, containsInAnyOrder(createdPromotions.toArray()));
        verifyThat(service.getResultTitles(), contains(triggerResults.toArray()));

        for(WebElement promotion : service.getPromotions()){
            promotionShownCorrectly(promotion);
        }
    }

    private void promotionShownCorrectly (WebElement promotion) {
        assertThat(promotion, hasClass("promoted-document"));
        assertThat(promotion.findElement(By.className("promoted-label")).getText(),containsString("Promoted"));
        assertThat(promotion.findElement(By.className("icon-star")), displayed());
    }

    @After
    public void tearDown(){
        getDriver().switchTo().window(browserHandles.get(0));

        promotionService.deleteAll();

        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
        keywordsPage.deleteKeywords();
    }

}