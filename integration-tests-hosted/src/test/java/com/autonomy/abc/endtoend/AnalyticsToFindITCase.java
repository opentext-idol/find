package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.application.HSODFind;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.FindSearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("CSA-1590")
public class AnalyticsToFindITCase extends HostedTestBase {
    private FindPage findPage;
    private FindResultsPage service;
    private Window searchWindow;
    private Window findWindow;
    
    private PromotionService<?> promotionService;

    private KeywordService keywordService;

    public AnalyticsToFindITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().promotionService();
        keywordService = getApplication().keywordService();

        searchWindow = getMainSession().getActiveWindow();
        findWindow = getMainSession().openWindow(config.getFindUrl());
        findPage = new HSODFind(findWindow).elementFactory().getFindPage();
        service = findPage.getResultsPage();
        searchWindow.activate();
    }

    @Test
    public void testPromotionToFind() throws InterruptedException {
        AnalyticsPage analyticsPage = getApplication().switchTo(AnalyticsPage.class);

        String searchTerm = analyticsPage.getMostPopularNonZeroSearchTerm();
        String trigger = "Trigger";
        String synonym = analyticsPage.getZeroHitSearch(0);

        promotionService.deleteAll();

        List<String> createdPromotions = promotionService.setUpPromotion(new SpotlightPromotion(trigger), searchTerm, 3);

        keywordService.addSynonymGroup(Language.ENGLISH, trigger, synonym);

        findWindow.activate();
        findPage.search(trigger);

        List<String> triggerResults = service.getResultTitles();

        findPage.search(synonym);

        List<String> findPromotions = service.getPromotionsTitles();

        verifyThat(findPromotions.size(), not(0));
        verifyThat(findPromotions, containsInAnyOrder(createdPromotions.toArray()));
        verifyThat(service.getResultTitles(), contains(triggerResults.toArray()));

        for(FindSearchResult promotion : service.promotions()){
            promotionShownCorrectly(promotion);
        }
    }

    private void promotionShownCorrectly (FindSearchResult promotion){
        verifyThat(promotion.isPromoted(), is(true));
        verifyThat(promotion.star(), displayed());
    }

    @After
    public void tearDown(){
        searchWindow.activate();

        promotionService.deleteAll();

        keywordService.deleteAll(KeywordFilter.ALL);
    }

}