package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.comparison.ResultsComparisonView;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchOptionsBar;
import com.autonomy.abc.selenium.find.save.SearchTabBar;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;

//The result comparisons for non-list view
@Role(UserRole.BIFHI)
public class ResultsComparisonITCase extends IdolFindTestBase {
    private FindService findService;
    private SavedSearchService savedSearchService;
    private BIIdolFindElementFactory elementFactory;

    private ResultsComparisonView resultsComparison;
    private IdolFindPage findPage;

    public ResultsComparisonITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        savedSearchService = getApplication().savedSearchService();
        elementFactory = (BIIdolFindElementFactory) getElementFactory();
        findPage = getElementFactory().getFindPage();
        findService.search("careful now");

        try {
            findPage.waitUntilSearchTabsLoaded();
            savedSearchService.deleteAll();
            elementFactory.getConceptsPanel().removeAllConcepts();
        } catch (final TimeoutException ignored) {
            //no-op
        }
    }

    @After
    public void tearDown() {
        findService.search("back to results");
        savedSearchService.deleteAll();
    }


    private void saveTwoSearches(final String searchName1, final String searchName2) {
        search("yellow",searchName1,SearchType.QUERY);
        savedSearchService.openNewTab();
        search("red",searchName2,SearchType.QUERY);
    }

    @Test
    public void testDeletingATab() {
        saveTwoSearches("mellow","unmellow");

        SearchTabBar bar = elementFactory.getSearchTabBar();
        final String title = bar.currentTab().getTitle();

        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.delete();

        verifyThat("Deleted search is gone",bar.tabs(),hasSize(1));
        bar.waitUntilTabGone(title);
    }


    @Test
    @ResolvedBug("FIND-370")
    public void testMapSliderDoesThingsInComparison() {
        final String firstSearch = "mellow";

        saveTwoSearches(firstSearch,"unmellow");
        savedSearchService.compareCurrentWith(firstSearch);

        Waits.loadOrFadeWait();

        TopicMapView mapView = elementFactory.getTopicMap();
        mapView.waitForMapLoaded();
        WebElement mapEntity = mapView.mapEntities().get(0);
        mapView.speedVsAccuracySlider().dragBy(100);
        mapView.waitForMapLoaded();
        try {
            mapEntity.click();
            fail("Map should have reloaded but did not");
        } catch (Exception e) {
            verifyThat("Map reloaded after using slider", mapView.topicMapVisible());
        }
    }

    private void search(final String query, final String saveAs, final SearchType saveType) {
        findService.search(query);
        new WebDriverWait(getDriver(), 30L).withMessage("Buttons should become active").until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".service-view-container:not(.hide) .save-button:not(.disabled)")));
        savedSearchService.saveCurrentAs(saveAs, saveType);
    }
}
