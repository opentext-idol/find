/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.BIFindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.comparison.AppearsInTopicMap;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
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

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

//The result comparisons for non-list view
@Role(UserRole.BIFHI)
public class ResultsComparisonITCase extends IdolFindTestBase {
    private BIFindService findService;
    private SavedSearchService savedSearchService;
    private BIIdolFindElementFactory elementFactory;

    private IdolFindPage findPage;

    public ResultsComparisonITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findService = (BIFindService)getApplication().findService();
        savedSearchService = getApplication().savedSearchService();
        elementFactory = (BIIdolFindElementFactory)getElementFactory();
        findPage = elementFactory.getFindPage();
        findService.searchAnyView("careful now");

        try {
            findPage = elementFactory.getFindPage();
            findPage.waitUntilSearchTabsLoaded();
            savedSearchService.deleteAll();

            elementFactory.getConceptsPanel().removeAllConcepts();
        } catch(final TimeoutException ignored) {
            //no-op
        }
        elementFactory.getTopicMap().waitForMapLoaded();
    }

    @After
    public void tearDown() {
        findPage = elementFactory.getFindPage();
        getDriver().get(getConfig().getAppUrl(getApplication()));
        getElementFactory().getFindPage().waitUntilDatabasesLoaded();
        savedSearchService.waitForSomeTabsAndDelete();
    }

    private TopicMapView compareAndGetTopicMap(final String firstSearch, final String secondSearch) {
        saveTwoSearches(firstSearch, secondSearch);
        savedSearchService.compareCurrentWith(firstSearch);

        Waits.loadOrFadeWait();
        final TopicMapView mapView = elementFactory.getResultsComparison().topicMap();

        mapView.waitForMapLoaded();
        return mapView;
    }

    @Test
    @ResolvedBug("FIND-370")
    public void testMapSliderDoesThingsInComparison() {
        final String firstSearch = "mellow";

        final TopicMapView mapView = compareAndGetTopicMap(firstSearch, "unmellow");

        final WebElement mapEntity = mapView.mapEntities().get(0);
        mapView.speedVsAccuracySlider().dragBy(10);
        mapView.waitForMapLoaded();
        try {
            mapEntity.click();
            fail("Map should have reloaded but did not");
        } catch(final Exception e) {
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
        findService.searchAnyView(query);
        findPage.waitUntilSaveButtonsActive();
        savedSearchService.saveCurrentAs(saveAs, saveType);
    }

    @Test
    @ResolvedBug("FIND-402")
    public void testCannotCompareUnsavedSearchWithSelf() {
        saveTwoSearches("meep", "eep");

        savedSearchService.openNewTab();
        final ComparisonModal modal = findPage.openCompareModal();

        final List<String> possibleComparees = modal.getItems();
        verifyThat("2 items to compare with", possibleComparees, hasSize(2));
        verifyThat("Does not contain itself", possibleComparees, not(contains("New Search")));
        modal.close();
    }

    @Test
    @ResolvedBug("FIND-631")
    //TODO: not working because of a wait -> works on debug & not cleaning up
    public void testClickingTopicMapClusterHeaderAddsConcept() {
        final TopicMapView mapView = compareAndGetTopicMap("woo", "boo");

        mapView.waitForMapLoaded();

        final String clickedCluster = mapView.clickNthClusterHeading(1);
        verifyThat("Clicking has revealed child concepts",
                   mapView.conceptClusterNames(),
                   not(hasItem(clickedCluster.toLowerCase())));
    }

    @Test
    @ResolvedBug("FIND-632")
    public void testCommonToBothMap() {
        search("face", "Has results", SearchType.QUERY);
        savedSearchService.openNewTab();

        search("elsijfleisjtgilsejtlisejt", "No results", SearchType.QUERY);
        findPage.ensureTermNotAutoCorrected();

        savedSearchService.compareCurrentWith("Has results");

        final TopicMapView mapView = elementFactory.getTopicMap();
        mapView.waitForMapLoaded();

        final ResultsComparisonView resultsComparison = elementFactory.getResultsComparison();
        final TopicMapView map = resultsComparison.topicMapView(AppearsInTopicMap.BOTH);
        verifyThat("Map not present", !map.topicMapPresent());
    }
}
