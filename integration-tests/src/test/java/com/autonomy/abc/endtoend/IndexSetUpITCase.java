package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOAppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.core.Is.is;

public class IndexSetUpITCase extends ABCTestBase {
    public IndexSetUpITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    private IndexesPage indexes;
    private String indexName;

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        body = getBody();
        indexes = ((HSOElementFactory) getElementFactory()).getIndexesPage();

        indexName = UUID.randomUUID().toString().replace('-','a');
        LoggerFactory.getLogger(IndexSetUpITCase.class).info("Index Name: "+indexName);
    }

    @Test
    public void testIndexSetUp(){
        indexes.newIndexButton().click();
        CreateNewIndexPage createNewIndexPage = ((HSOElementFactory) getElementFactory()).getCreateNewIndexPage();
        createNewIndexPage.inputIndexName(indexName);
        createNewIndexPage.nextButton().click();
        createNewIndexPage.loadOrFadeWait();
//        createNewIndexPage.inputIndexFields(Arrays.asList("1", "2", "3"));
//        createNewIndexPage.inputParametricFields(Arrays.asList("4", "5", "6"));
        createNewIndexPage.nextButton().click();
        createNewIndexPage.loadOrFadeWait();
        createNewIndexPage.finishButton().click();

        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Created a new index: " + indexName));

        indexes = ((HSOElementFactory) getElementFactory()).getIndexesPage();
        indexes.findIndex(indexName).click();

        IndexesDetailPage index = ((HSOElementFactory) getElementFactory()).getIndexesDetailPage();
        verifyThat(index.getIndexHeader(), is(indexName));
        verifyThat(index.getIndexTitle(), is(indexName));

        verifyThat(index.getCreatedDate(), is(new SimpleDateFormat("EEEE, dd MMMM yyyy").format(new Date())));

        String site = "www.bbc.co.uk";
        index.addSiteToIndex(site);

        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Document \"http://" + site + "\" was uploaded successfully"));

        body.getTopNavBar().search("*");

        body = getBody();

        SearchPage searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        searchPage.selectAllIndexes();
        searchPage.findElement(By.xpath(".//label[text()[contains(., 'All')]]/div/ins")).click();
        searchPage.findElement(By.cssSelector("[data-name='"+indexName+"'] label")).click();
        searchPage.loadOrFadeWait();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        String promotedTitle = searchPage.createAPromotion();

        String trigger = "trigger";

        CreateNewPromotionsPage createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        createNewPromotionsPage.addSpotlightPromotion("",trigger);

        searchPage = getElementFactory().getSearchPage();

        searchPage.waitForPromotionsLoadIndicatorToDisappear();

        verifyThat(searchPage.getTopPromotedLinkTitle(), is(promotedTitle));
        verifyThat(searchPage.getPromotionBucketElementByTitle(promotedTitle).findElement(By.className("index")).getText(),is("Index: "+indexName));
    }

    @After
    public void tearDown(){
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        getElementFactory().getPromotionsPage().deleteAllPromotions();
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);
        indexes = ((HSOElementFactory) getElementFactory()).getIndexesPage();
        indexes.loadOrFadeWait();
        indexes.findIndex(indexName).click();
        IndexesDetailPage index = ((HSOElementFactory) getElementFactory()).getIndexesDetailPage();
        indexes.loadOrFadeWait();
        index.deleteButton().click();
        indexes.loadOrFadeWait();
        index.confirmDeleteButton().click();
        indexes.loadOrFadeWait();
    }
}
