package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.Service;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.core.Is.is;

//CSA-1566
public class PromotionsToFindITCase extends ABCTestBase {

    public PromotionsToFindITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    List<String> browserHandles;
    FindPage find;
    Service service;
    private final String domain = "ce9f1f3d-a780-4793-8a6a-a74b12b7d1ae";
    private CreateNewPromotionsPage createNewPromotionsPage;

    @Before
    public void setUp(){
        PromotionsPage promotions = getElementFactory().getPromotionsPage();
        browserHandles = promotions.createAndListWindowHandles();
        getDriver().switchTo().window(browserHandles.get(1));
        getDriver().get("https://find.dev.idolondemand.com/");
        getDriver().manage().window().maximize();
        find = ((HSOElementFactory) getElementFactory()).getFindPage();
        service = find.getService();
        getDriver().switchTo().window(browserHandles.get(0));
    }

    @Test
    public void testPromotionsToFind(){
        body.getTopNavBar().search("Promotions");
        String searchTrigger = "search";
        String secondaryTrigger = "secondary";

        SearchPage searchPage = getElementFactory().getSearchPage();
        List<String> promotionTitles = searchPage.createAMultiDocumentPromotion(5);
        pinToPosition(searchTrigger,1);
        getElementFactory().getSearchPage();

        getDriver().switchTo().window(browserHandles.get(1));
        find.search(searchTrigger);

        verifyPinToPosition(promotionTitles,1,5);

        getDriver().switchTo().window(browserHandles.get(0));

        getElementFactory().getSearchPage();
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

        PromotionsPage promotionsPage = getElementFactory().getPromotionsPage();
        promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();
        PromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        promotionsDetailPage.findElement(By.cssSelector(".promotion-position-edit .fa-pencil")).click();

        for(int i = 0;i < 5; i++){
            promotionsDetailPage.findElement(By.className("plus")).click();
        }

        promotionsDetailPage.findElement(By.cssSelector(".promotion-position-edit .fa-check")).click();

        new WebDriverWait(getDriver(),60).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".promotion-position-edit .fa-spin")));

        getDriver().switchTo().window(browserHandles.get(1));

        getDriver().navigate().refresh();
        find = ((HSOElementFactory) getElementFactory()).getFindPage();
        service = find.getService();

        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);

        verifyPinToPosition(promotionTitles, 6, 10);

        getDriver().switchTo().window(browserHandles.get(0));

        promotionsDetailPage.triggerAddBox().setAndSubmit(secondaryTrigger);
        final WebElement trigger = promotionsDetailPage.findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + secondaryTrigger + "']"));

        new WebDriverWait(getDriver(),30).until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".promotion-view-match-terms [data-id='" + secondaryTrigger + "'] .fa-spin")));

        getDriver().switchTo().window(browserHandles.get(1));
        find.search(secondaryTrigger);

        verifyPinToPosition(promotionTitles, 6, 10);

        service.filterByIndex(domain, "reddit");

        verifyPinToPosition(promotionTitles, 6, 10);

        service.filterByIndex(domain, "reddit");

        service.filterByParametric("Source Connector", "SIMPSONSARCHIVE");

        verifyPinToPosition(promotionTitles, 6, 10);

        getDriver().switchTo().window(browserHandles.get(0));

        body.getTopNavBar().search("Tertiary");

        searchPage = getElementFactory().getSearchPage();
        List<String> spotlightPromotionTitles = searchPage.createAMultiDocumentPromotion(2);
        createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        createNewPromotionsPage.addSpotlightPromotion("", searchTrigger);

        final SearchPage finalSearchPage = getElementFactory().getSearchPage();
        new WebDriverWait(getDriver(),30).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return finalSearchPage.getPromotedResults().size() != 0;
            }
        });

        getDriver().switchTo().window(browserHandles.get(1));
        find.search(searchTrigger);

        verifyPinToPosition(promotionTitles, 6, 10);
        verifySpotlight(spotlightPromotionTitles);

        getDriver().switchTo().window(browserHandles.get(0));

        body.getTopNavBar().search("187");
        searchPage = getElementFactory().getSearchPage();
        String singlePTPPromotion = searchPage.createAPromotion();
         pinToPosition(searchTrigger, 6);
        getElementFactory().getSearchPage();

        getDriver().switchTo().window(browserHandles.get(1));
        getDriver().navigate().refresh();
        find = ((HSOElementFactory) getElementFactory()).getFindPage();
        service = find.getService();

        verifyThat(singlePTPPromotion, isIn(service.getResultTitles(6, 11)));

        List<String> allPromotions = promotionTitles;
        allPromotions.add(singlePTPPromotion);

        verifyPinToPosition(allPromotions, 6, 11);
        verifySpotlight(spotlightPromotionTitles);

        getDriver().switchTo().window(browserHandles.get(0));
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionsPage.deletePromotion("Spotlight for: " + searchTrigger);

        getDriver().switchTo().window(browserHandles.get(1));
        find.search("Other");
        find.search(searchTrigger);

        verifyThat(service.getPromotionsTitles().isEmpty(), is(Boolean.TRUE));

        LoggerFactory.getLogger(PromotionsToFindITCase.class).info("TEST FINISHED");
    }

    private void pinToPosition(String searchTrigger, int position) {
        createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        createNewPromotionsPage.pinToPosition().click();
        createNewPromotionsPage.loadOrFadeWait();
        createNewPromotionsPage.continueButton().click();
        createNewPromotionsPage.loadOrFadeWait();

        for(int i = 1; i < position; i++){
            createNewPromotionsPage.findElement(By.className("plus")).click();
        }

        createNewPromotionsPage.loadOrFadeWait();
        createNewPromotionsPage.continueButton().click();
        createNewPromotionsPage.loadOrFadeWait();
        createNewPromotionsPage.addSearchTrigger(searchTrigger);
        createNewPromotionsPage.finishButton().click();
    }

    private void verifySpotlight(List<String> promotionTitles) {
        verify(service.getPromotionsTitles(),promotionTitles);
    }

    private void verify(List<String> search, List<String> promotion){
        verifyThat(search, containsInAnyOrder(promotion.toArray()));
    }

    private void verifyPinToPosition(List<String> promotionTitles, int start, int end) {
        verify(service.getResultTitles(start, end), promotionTitles);
    }


    @After
    public void tearDown(){
        getDriver().switchTo().window(browserHandles.get(0));
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        getElementFactory().getPromotionsPage().deleteAllPromotions();
    }
}
