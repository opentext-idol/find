package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
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

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

//The result comparisons for non-list view
@Role(UserRole.BIFHI)
public class ResultsComparisonITCase extends IdolFindTestBase {
    private FindService findService;
    private SavedSearchService savedSearchService;
    private BIIdolFindElementFactory elementFactory;

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
            findPage = getElementFactory().getFindPage();
            findPage.waitUntilSearchTabsLoaded();
            savedSearchService.deleteAll();

            elementFactory.getConceptsPanel().removeAllConcepts();
        } catch (final TimeoutException ignored) {
            //no-op
        }
    }

    @After
    public void tearDown() {
        findPage = getElementFactory().getFindPage();
        findPage.goBackToSearch();
        savedSearchService.deleteAll();
    }

    private TopicMapView compareAndGetTopicMap(final String firstSearch, final String secondSearch) {
        saveTwoSearches(firstSearch,secondSearch);
        savedSearchService.compareCurrentWith(firstSearch);

        Waits.loadOrFadeWait();

        final TopicMapView mapView = elementFactory.getTopicMap();
        mapView.waitForMapLoaded();
        return mapView;
    }

    @Test
    @ResolvedBug("FIND-370")
    public void testMapSliderDoesThingsInComparison() {
        final String firstSearch = "mellow";

        final TopicMapView mapView = compareAndGetTopicMap(firstSearch, "unmellow");

        final WebElement mapEntity = mapView.mapEntities().get(0);
        mapView.speedVsAccuracySlider().dragBy(100);
        mapView.waitForMapLoaded();
        try {
            mapEntity.click();
            fail("Map should have reloaded but did not");
        } catch (Exception e) {
            verifyThat("Map reloaded after using slider", mapView.topicMapVisible());
        }
    }

    private void saveTwoSearches(final String searchName1, final String searchName2) {
        search("yellow", searchName1, SearchType.QUERY);
        savedSearchService.openNewTab();
        search("red", searchName2, SearchType.QUERY);
    }

    private void search(final String query, final String saveAs, final SearchType saveType) {
        Waits.loadOrFadeWait();
        findService.search(query);
        findPage.waitUntilSaveButtonsActive();
        savedSearchService.saveCurrentAs(saveAs, saveType);
    }

    @Test
    @ResolvedBug("FIND-402")
    public void testCannotCompareUnsavedSearchWithSelf() {
        saveTwoSearches("meep","eep");

        savedSearchService.openNewTab();
        final ComparisonModal modal = findPage.openCompareModal();

        final List<String> possibleComparees = modal.getItems();
        verifyThat("2 items to compare with", possibleComparees, hasSize(2));
        verifyThat("Does not contain itself",possibleComparees, not(contains("New Search")));
    }

    @Test
    @ResolvedBug("FIND-631")
    public void testClickingTopicMapClusterHeaderAddsConcept() {
        final TopicMapView mapView = compareAndGetTopicMap("woo", "boo");

        final String clickedCluster = mapView.clickNthClusterHeading(1);
        verifyThat("Clicking has revealed child concepts",
                mapView.conceptClusterNames(),
                not(hasItem(clickedCluster.toLowerCase())));
    }
}
