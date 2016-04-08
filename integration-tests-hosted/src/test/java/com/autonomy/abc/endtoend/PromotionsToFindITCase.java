package com.autonomy.abc.endtoend;

import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.fixtures.PromotionTearDownStrategy;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.autonomy.abc.selenium.find.*;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.ParametricFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isIn;

@RelatedTo("CSA-1566")
public class PromotionsToFindITCase extends HostedTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(PromotionsToFindITCase.class);

    private Window searchWindow;
    private Window findWindow;
    private HsodFindElementFactory findFactory;

    private PromotionService<?> promotionService;
    private FindService findService;

    private FindPage findPage;
    private FindResultsPage resultsPage;

    public PromotionsToFindITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        promotionService = getApplication().promotionService();
        promotionService.deleteAll();
        searchWindow = getWindow();

        HsodFind findApp = new HsodFind();
        findWindow = launchInNewWindow(findApp);
        findFactory = findApp.elementFactory();
        findService = findApp.findService();
        findPage = findFactory.getFindPage();
        resultsPage = findPage.getResultsPage();

        searchWindow.activate();
    }

    @After
    public void tearDown(){
        searchWindow.activate();
        new PromotionTearDownStrategy().tearDown(this);
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
        findService.search(searchTrigger);
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
        findService.search(secondaryTrigger);
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
        findService.search(searchTrigger);

        verifyPinToPosition(promotionTitles, 6, 10);
        verifySpotlight(spotlightPromotionTitles);

        searchWindow.activate();
        String singlePromoted = promotionService.setUpPromotion(secondPinPromotion, "187", 1).get(0);
        LOGGER.info("set up second pin to position");

        findWindow.activate();
        refreshFind();

        verifyThat(singlePromoted, isIn(resultsPage.getResultTitles(6, 11)));

        List<String> allPromotions = new ArrayList<>(promotionTitles);
        allPromotions.add(singlePromoted);
        verifySpotlight(spotlightPromotionTitles);

        searchWindow.activate();
        promotionService.delete("Spotlight for: " + spotlightPromotion.getTrigger());
        LOGGER.info("deleted spotlight promotion");

        findWindow.activate();
        findService.search("Other");
        findService.search(searchTrigger);
    }

    private void refreshFind() {
        getWindow().refresh();
        findPage = findFactory.getFindPage();
        resultsPage = findPage.getResultsPage();
        resultsPage.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
    }

    private void verifySpotlight(List<String> promotionTitles) {
        verifyResultsContainPromoted(resultsPage.getPromotionsTitles(), promotionTitles);
    }

    private void verifyResultsContainPromoted(List<String> search, List<String> promotion){
        verifyThat(search, containsInAnyOrder(promotion.toArray()));
    }

    private void verifyPinToPosition(List<String> promotionTitles, int start, int end) {
        verifyResultsContainPromoted(resultsPage.getResultTitles(start, end), promotionTitles);
    }
}
