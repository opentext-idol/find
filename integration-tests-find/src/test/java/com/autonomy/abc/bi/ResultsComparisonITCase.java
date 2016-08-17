package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.comparison.ResultsComparisonView;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.junit.Assert.fail;

//The result comparisons for non-list view
public class ResultsComparisonITCase extends IdolFindTestBase{

    private FindService findService;
    private SavedSearchService savedSearchService;
    private IdolFindElementFactory elementFactory;

    private ResultsComparisonView resultsComparison;
    private IdolFindPage findPage;

    public ResultsComparisonITCase(final TestConfig config) {
        super(config);
    }


    @Before
    public void setUp() {
        findService = getApplication().findService();
        savedSearchService = getApplication().savedSearchService();
        elementFactory = getApplication().elementFactory();
        findPage = getElementFactory().getFindPage();
        findService.search("careful now");

        try {
            findPage.waitUntilSearchTabsLoaded();
            savedSearchService.deleteAll();
        }
        catch (TimeoutException e) {
            //no-op
        }
    }

    @After
    public void tearDown() {
        findService.search("back to results");
        savedSearchService.deleteAll();
    }


    @Test
    @ResolvedBug("FIND-370")
    public void testMapSliderDoesThingsInComparison() {
        final String firstSearch = "mellow";
        search("yellow",firstSearch,SearchType.QUERY);
        savedSearchService.openNewTab();
        search("red","unmellow",SearchType.QUERY);

        savedSearchService.compareCurrentWith(firstSearch);

        Waits.loadOrFadeWait();

        TopicMapView mapView = getElementFactory().getTopicMap();
        mapView.waitForMapLoaded();
        WebElement mapEntity = mapView.mapEntities().get(0);
        mapView.speedVsAccuracySlider().dragBy(100);
        mapView.waitForMapLoaded();
        try{
            mapEntity.click();
            fail("Map should have reloaded but did not");
        }
        catch (Exception e) {
            verifyThat("Map reloaded after using slider",mapView.topicMapVisible());
        }
    }

    private void search(final String query, final String saveAs, final SearchType saveType) {
        findService.search(query);
        getElementFactory().getTopicMap().waitForMapLoaded();
        savedSearchService.saveCurrentAs(saveAs, saveType);
    }
}
