package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.core.Is.is;

//CSA-1563
public class QuickSetUpITCase extends HostedTestBase {

    public QuickSetUpITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    private GettingStartedPage gettingStarted;
    private PromotionService promotionService;

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());
        promotionService.deleteAll();

        body.getSideNavBar().switchPage(NavBarTabId.GETTING_STARTED);
        gettingStarted = getElementFactory().getGettingStartedPage();
        body = getBody();
    }

    @Test
    public void testQuickSetUp(){
        String site = "http://www.cnet.com";
        gettingStarted.addSiteToIndex(site);

        SearchService searchService = getApplication().createSearchService(getElementFactory());
        //Can't search for forward slash, so take those out
        SearchQuery searchQuery = new SearchQuery(site.split("//")[1]).withFilter(new IndexFilter(Index.DEFAULT));

        SearchPage searchPage = searchService.search(searchQuery);
        body = getBody();

        //Check promoting the correct document
        searchPage.getSearchResult(1).click();
        DocumentViewer docViewer = DocumentViewer.make(getDriver());
        assertThat(docViewer.getReference(), is(site));
        docViewer.close();

        String promotionTitle = searchPage.createAPromotion();

        CreateNewPromotionsPage createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();

        String trigger = "trigger";

        createNewPromotionsPage.addSpotlightPromotion("", trigger);

        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
        assertThat(searchPage.promotedDocumentTitle(1).getText(), is(promotionTitle));

        //Delete Promotion
        PromotionsPage promotionsPage = promotionService.delete(trigger);

        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Removed a spotlight promotion"));

        assertThat(promotionsPage.promotionsList().size(),is(0));
    }

    @After
    public void tearDown(){
        promotionService.deleteAll();
    }

}
