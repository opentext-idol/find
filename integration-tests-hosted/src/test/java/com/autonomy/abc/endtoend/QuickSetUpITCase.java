package com.autonomy.abc.endtoend;

import com.autonomy.abc.base.IsoTearDown;
import com.autonomy.abc.base.HostedTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.icma.GettingStartedPage;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

@RelatedTo("CSA-1563")
public class QuickSetUpITCase extends HostedTestBase {

    public QuickSetUpITCase(TestConfig config) {
        super(config);
    }

    private GettingStartedPage gettingStarted;
    private PromotionService<?> promotionService;

    @Before
    public void setUp(){
        promotionService = getApplication().promotionService();
        promotionService.deleteAll();

        gettingStarted = getApplication().switchTo(GettingStartedPage.class);
    }

    @After
    public void tearDown(){
        IsoTearDown.PROMOTIONS.tearDown(this);
    }

    @Test
    public void testQuickSetUp(){
        String site = "http://www.cnet.com";
        gettingStarted.addSiteToIndex(site);

        SearchService searchService = getApplication().searchService();
        //Can't search for forward slash, so take those out
        Query query = new Query(site.split("//")[1]).withFilter(new IndexFilter(Index.DEFAULT));

        SearchPage searchPage = searchService.search(query);

        //Check promoting the correct document
        searchPage.getSearchResult(1).title().click();
        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        assumeThat(docViewer.getReference(), is(site));
        docViewer.close();

        String trigger = "trigger";
        try {
            List<String> promotionTitles = promotionService.setUpPromotion(new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, trigger), query, 1);

            searchPage = getElementFactory().getSearchPage();
            searchPage.waitForPromotionsLoadIndicatorToDisappear();
            assertThat(searchPage.promotedDocumentTitle(1).getText(), is(promotionTitles.get(0)));
        } finally {
            //Delete Promotion
            PromotionsPage promotionsPage = promotionService.delete(trigger);
            new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Removed a spotlight promotion"));
            assertThat(promotionsPage.promotionsList().size(), is(0));
        }
    }

}
