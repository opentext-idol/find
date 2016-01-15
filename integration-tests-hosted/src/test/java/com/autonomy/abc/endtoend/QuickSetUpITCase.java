package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

//CSA-1563
public class QuickSetUpITCase extends HostedTestBase {

    public QuickSetUpITCase(TestConfig config) {
        super(config);
    }

    private GettingStartedPage gettingStarted;
    private PromotionService promotionService;

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());
        promotionService.deleteAll();

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.GETTING_STARTED);
        gettingStarted = getElementFactory().getGettingStartedPage();
    }

    @Test
    public void testQuickSetUp(){
        String site = "http://www.cnet.com";
        gettingStarted.addSiteToIndex(site);

        SearchService searchService = getApplication().createSearchService(getElementFactory());
        //Can't search for forward slash, so take those out
        SearchQuery searchQuery = new SearchQuery(site.split("//")[1]).withFilter(new IndexFilter(Index.DEFAULT));

        SearchPage searchPage = searchService.search(searchQuery);

        //Check promoting the correct document
        searchPage.searchResult(1).click();
        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        assumeThat(docViewer.getReference(), is(site));
        docViewer.close();

        String trigger = "trigger";
        try {
            List<String> promotionTitles = promotionService.setUpPromotion(new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, trigger), searchQuery, 1);

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

    @After
    public void tearDown(){
        promotionService.deleteAll();
    }

}
