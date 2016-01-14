package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.util.DriverUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isIn;

//CSA-1566
public class PromotionsToFindITCase extends HostedTestBase {
    private List<String> browserHandles;
    private Find find;
    private FindResultsPage service;
    private PromotionService promotionService;
    private final static Logger LOGGER = LoggerFactory.getLogger(PromotionsToFindITCase.class);

    public PromotionsToFindITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().createPromotionService(getElementFactory());

        promotionService.deleteAll();
        browserHandles = DriverUtil.createAndListWindowHandles(getDriver());
        switchToFind();
        getDriver().get(config.getFindUrl());
        getDriver().manage().window().maximize();
        find = getElementFactory().getFindPage();
        service = find.getResultsPage();
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

        List<String> promotionTitles = promotionService.setUpPromotion(pinPromotion, "Promotions", 5);
        LOGGER.info("set up pin to position");

        switchToFind();
        find.search(searchTrigger);
        verifyPinToPosition(promotionTitles, 1, 5);

        switchToSearch();
        PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails(pinPromotion);
        promotionsDetailPage.pinPosition().setValueAndWait("6");
        LOGGER.info("updated pin position");

        boolean addedQuickly = true;
        try {
            promotionsDetailPage.getTriggerForm().addTrigger(secondaryTrigger);
        } catch (TimeoutException e) {
            addedQuickly = false;
            promotionsDetailPage.getTriggerForm().waitForTriggerRefresh();
        } finally {
            verifyThat("added trigger within reasonable time", addedQuickly);
        }
        LOGGER.info("added secondary trigger");

        switchToFind();
        find.search(secondaryTrigger);
        verifyPinToPosition(promotionTitles, 6, 10);

        find.filterBy(new IndexFilter(Index.DEFAULT));
        verifyPinToPosition(promotionTitles, 6, 10);

        find.filterBy(IndexFilter.PRIVATE);
        service.filterByParametric("Source Connector", "SIMPSONSARCHIVE");
        verifyPinToPosition(promotionTitles, 6, 10);

        switchToSearch();
        List<String> spotlightPromotionTitles = promotionService.setUpPromotion(spotlightPromotion, "another", 2);
        LOGGER.info("set up spotlight promotion");

        switchToFind();
        find.search(searchTrigger);

        verifyPinToPosition(promotionTitles, 6, 10);
        verifySpotlight(spotlightPromotionTitles);

        switchToSearch();
        String singlePromoted = promotionService.setUpPromotion(secondPinPromotion, "187", 1).get(0);
        LOGGER.info("set up second pin to position");

        switchToFind();
        refreshFind();

        verifyThat(singlePromoted, isIn(service.getResultTitles(6, 11)));

        List<String> allPromotions = new ArrayList<>(promotionTitles);
        allPromotions.add(singlePromoted);
        verifySpotlight(spotlightPromotionTitles);

        switchToSearch();
        promotionService.delete("Spotlight for: " + spotlightPromotion.getTrigger());
        LOGGER.info("deleted spotlight promotion");

        switchToFind();
        find.search("Other");
        find.search(searchTrigger);
    }

    private void refreshFind() {
        getDriver().navigate().refresh();
        find = getElementFactory().getFindPage();
        service = find.getResultsPage();
        service.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
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
        try {
            switchToSearch();
            promotionService.deleteAll();
        } catch (NullPointerException e) {
            LOGGER.warn("skipping tear down");
        }
    }
}
