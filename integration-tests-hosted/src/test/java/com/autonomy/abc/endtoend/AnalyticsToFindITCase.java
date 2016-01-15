package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.analytics.Term;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.util.DriverUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

//CSA-1590
public class AnalyticsToFindITCase extends HostedTestBase {
    private PromotionService promotionService;
    private KeywordService keywordService;

    public AnalyticsToFindITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());
        keywordService = new KeywordService(getApplication(), getElementFactory());

        browserHandles = DriverUtil.createAndListWindowHandles(getDriver());
        getDriver().switchTo().window(browserHandles.get(1));
        getDriver().get(config.getFindUrl());
        getDriver().manage().window().maximize();
        find = getElementFactory().getFindPage();
        service = find.getResultsPage();
        getDriver().switchTo().window(browserHandles.get(0));
    }

    private Find find;
    private FindResultsPage service;
    private List<String> browserHandles;

    @Test
    public void testPromotionToFind() throws InterruptedException {
        getElementFactory().getSideNavBar().switchPage(NavBarTabId.ANALYTICS);

        AnalyticsPage analyticsPage = getElementFactory().getAnalyticsPage();

        Term zeroSearch = analyticsPage.getMostPopularZeroSearchTerm();
        Term nonZero = analyticsPage.getMostPopularNonZeroSearchTerm();

        if(nonZero == null){
            nonZero = new Term("replacement",0);
        }

        String searchTerm = nonZero.getTerm();
        String trigger = "Trigger";
        String synonym = zeroSearch.getTerm();

        promotionService.deleteAll();

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

        for(WebElement promotion : service.promotions()){
            promotionShownCorrectly(promotion);
        }
    }

    private void promotionShownCorrectly (WebElement promotion) {
        verifyThat(promotion, hasClass("promoted-document"));
        verifyThat(promotion.findElement(By.className("promoted-label")).getText(),containsString("Promoted"));
        verifyThat(promotion.findElement(By.className("fa-star")), displayed());
    }

    @After
    public void tearDown(){
        getDriver().switchTo().window(browserHandles.get(0));

        promotionService.deleteAll();

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
        keywordsPage.deleteKeywords();
    }

}