package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.Service;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isIn;

//CSA-1566
public class PromotionsToFindITCase extends ABCTestBase {

    public PromotionsToFindITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    List<String> browserHandles;
    FindPage find;
    Service service;
    private PromotionService promotionService;
    private SearchActionFactory searchActionFactory;

    @Override
    public HSOElementFactory getElementFactory() {
        return (HSOElementFactory) super.getElementFactory();
    }

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());

        PromotionsPage promotions = getElementFactory().getPromotionsPage();
        browserHandles = promotions.createAndListWindowHandles();
        switchToFind();
        getDriver().get(config.getFindUrl());
        getDriver().manage().window().maximize();
        find = getElementFactory().getFindPage();
        service = find.getService();
        switchToSearch();
    }

    private void switchToSearch() {
        getDriver().switchTo().window(browserHandles.get(0));
    }

    private void switchToFind() {
        getDriver().switchTo().window(browserHandles.get(1));
    }

    @Test
    public void testPromotionsToFind(){
        String searchTrigger = "search";
        String secondaryTrigger = "secondary";
        Promotion pinPromotion = new PinToPositionPromotion(1, searchTrigger);
        Promotion spotlightPromotion = new SpotlightPromotion(searchTrigger);
        Promotion secondPinPromotion = new PinToPositionPromotion(6, searchTrigger);

        List<String> promotionTitles = setUpPromotion(pinPromotion, "Promotions", 5);

        switchToFind();
        find.search(searchTrigger);
        verifyPinToPosition(promotionTitles, 1, 5);

        switchToSearch();
        PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails(pinPromotion);
        promotionsDetailPage.pinPosition().setValueAndWait("6");

        switchToFind();
        refreshFind();
        verifyPinToPosition(promotionTitles, 6, 10);

        switchToSearch();
        promotionsDetailPage.triggerAddBox().setAndSubmit(secondaryTrigger);
        promotionsDetailPage.waitForTriggerRefresh();

        switchToFind();
        find.search(secondaryTrigger);
        verifyPinToPosition(promotionTitles, 6, 10);

        service.filterByIndex("default_index");
        verifyPinToPosition(promotionTitles, 6, 10);

        service.filterByIndex("default_index");
        service.filterByParametric("Source Connector", "SIMPSONSARCHIVE");
        verifyPinToPosition(promotionTitles, 6, 10);

        switchToSearch();
        List<String> spotlightPromotionTitles = setUpPromotion(spotlightPromotion, "Tertiary", 2);

        switchToFind();
        find.search(searchTrigger);

        verifyPinToPosition(promotionTitles, 6, 10);
        verifySpotlight(spotlightPromotionTitles);

        switchToSearch();
        String singlePromoted = setUpPromotion(secondPinPromotion, "187", 1).get(0);

        switchToFind();
        refreshFind();

        verifyThat(singlePromoted, isIn(service.getResultTitles(6, 11)));

        List<String> allPromotions = new ArrayList<>(promotionTitles);
        allPromotions.add(singlePromoted);
        verifyPinToPosition(allPromotions, 6, 11);
        verifySpotlight(spotlightPromotionTitles);

        switchToSearch();
        promotionService.delete(spotlightPromotion);

        switchToFind();
        find.search("Other");
        find.search(searchTrigger);

        verifyThat(service.getPromotionsTitles(), empty());
    }

    private void refreshFind() {
        getDriver().navigate().refresh();
        find = getElementFactory().getFindPage();
        service = find.getService();
        service.waitForSearchLoadIndicatorToDisappear(Service.Container.MIDDLE);
    }

    private List<String> setUpPromotion(Promotion promotion, String searchTerm, int numberOfDocs) {
        return promotionService.setUpPromotion(promotion,
                searchActionFactory.makeSearch(searchTerm), numberOfDocs);
    }

    private void verifySpotlight(List<String> promotionTitles) {
        verifyResultsContainPromoted(service.getPromotionsTitles(), promotionTitles);
    }

    private void verifyResultsContainPromoted(List<String> search, List<String> promotion){
        verifyThat(search, containsInAnyOrder(promotion.toArray()));
    }

    private void verifyPinToPosition(List<String> promotionTitles, int start, int end) {
        verifyResultsContainPromoted(service.getResultTitles(start, end), promotionTitles);
    }

    @After
    public void tearDown(){
        switchToSearch();
        promotionService.deleteAll();
    }
}
