package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.HSODFind;
import com.autonomy.abc.selenium.find.HSODFindElementFactory;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.ParametricFilter;
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

@RelatedTo("CSA-1566")
public class PromotionsToFindITCase extends HostedTestBase {
    private Window searchWindow;
    private Window findWindow;

    private HSODFindElementFactory findFactory;
    private FindPage findPage;
    private FindResultsPage service;
    private PromotionService<?> promotionService;
    private final static Logger LOGGER = LoggerFactory.getLogger(PromotionsToFindITCase.class);

    public PromotionsToFindITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().promotionService();

        promotionService.deleteAll();
        searchWindow = getWindow();
        HSODFind findApp = new HSODFind();
        findWindow = launchInNewWindow(findApp);
        findFactory = findApp.elementFactory();
        findPage = findFactory.getFindPage();
        service = findPage.getResultsPage();
        searchWindow.activate();
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

        findWindow.activate();
        findPage.search(searchTrigger);
        verifyPinToPosition(promotionTitles, 1, 5);

        searchWindow.activate();
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

        findWindow.activate();
        findPage.search(secondaryTrigger);
        verifyPinToPosition(promotionTitles, 6, 10);

        findPage.filterBy(new IndexFilter(Index.DEFAULT));
        verifyPinToPosition(promotionTitles, 6, 10);

        findPage.filterBy(IndexFilter.PRIVATE);
        findPage.filterBy(new ParametricFilter("Source Connector", "SIMPSONSARCHIVE"));
        verifyPinToPosition(promotionTitles, 6, 10);

        searchWindow.activate();
        List<String> spotlightPromotionTitles = promotionService.setUpPromotion(spotlightPromotion, "another", 2);
        LOGGER.info("set up spotlight promotion");

        findWindow.activate();
        findPage.search(searchTrigger);

        verifyPinToPosition(promotionTitles, 6, 10);
        verifySpotlight(spotlightPromotionTitles);

        searchWindow.activate();
        String singlePromoted = promotionService.setUpPromotion(secondPinPromotion, "187", 1).get(0);
        LOGGER.info("set up second pin to position");

        findWindow.activate();
        refreshFind();

        verifyThat(singlePromoted, isIn(service.getResultTitles(6, 11)));

        List<String> allPromotions = new ArrayList<>(promotionTitles);
        allPromotions.add(singlePromoted);
        verifySpotlight(spotlightPromotionTitles);

        searchWindow.activate();
        promotionService.delete("Spotlight for: " + spotlightPromotion.getTrigger());
        LOGGER.info("deleted spotlight promotion");

        findWindow.activate();
        findPage.search("Other");
        findPage.search(searchTrigger);
    }

    private void refreshFind() {
        getDriver().navigate().refresh();
        findPage = findFactory.getFindPage();
        service = findPage.getResultsPage();
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
            searchWindow.activate();
            promotionService.deleteAll();
        } catch (NullPointerException e) {
            LOGGER.warn("skipping tear down");
        }
    }
}
