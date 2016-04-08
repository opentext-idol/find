package com.autonomy.abc.endtoend;

import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.fixtures.KeywordTearDownStrategy;
import com.autonomy.abc.fixtures.PromotionTearDownStrategy;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindSearchResult;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.HsodFind;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("CSA-1590")
public class AnalyticsToFindITCase extends HostedTestBase {
    private FindService findService;
    private FindResultsPage resultsPage;
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

        searchWindow = getWindow();
        HsodFind findApp = new HsodFind();
        findWindow = launchInNewWindow(findApp);

        findService = findApp.findService();
        resultsPage = findApp.elementFactory().getResultsPage();

        searchWindow.activate();
    }

    @After
    public void promotionTearDown() {
        searchWindow.activate();
        new PromotionTearDownStrategy().tearDown(this);
    }

    @After
    public void keywordTearDown() {
        searchWindow.activate();
        new KeywordTearDownStrategy().tearDown(this);
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
        findService.search(trigger);

        List<String> triggerResults = resultsPage.getResultTitles();

        findService.search(synonym);

        List<String> findPromotions = resultsPage.getPromotionsTitles();

        verifyThat(findPromotions.size(), not(0));
        verifyThat(findPromotions, containsInAnyOrder(createdPromotions.toArray()));
        verifyThat(resultsPage.getResultTitles(), contains(triggerResults.toArray()));

        for(FindSearchResult promotion : resultsPage.promotions()){
            promotionShownCorrectly(promotion);
        }
    }

    private void promotionShownCorrectly (FindSearchResult promotion){
        verifyThat(promotion.isPromoted(), is(true));
        verifyThat(promotion.star(), displayed());
    }

}