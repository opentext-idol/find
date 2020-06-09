/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.MapView;
import com.autonomy.abc.selenium.find.preview.DetailedPreviewPage;
import com.autonomy.abc.selenium.find.preview.InlinePreview;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;

@Role(UserRole.BIFHI)
public class MapITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;
    private MapView mapView;
    private SavedSearchService savedSearchService;

    public MapITCase(final TestConfig config) {
        super(config);
    }

    @Override
    public BIIdolFindElementFactory getElementFactory() {
        return (BIIdolFindElementFactory) super.getElementFactory();
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
        savedSearchService = getApplication().savedSearchService();
    }

    @Test
    @ResolvedBug("FIND-154")
    public void testMapLoads() {
        mapView = search("atrocious");
        verifyThat("Map displayed", mapView.mapPresent());

        mapView.waitForMarkers();
        verifyThat("Map isn't stuck loading forever", !mapView.isLoading());
        getElementFactory().getConceptsPanel().removeAllConcepts();

        search("tse");
        mapView.waitForMarkers();

        checkMarkersPresent(mapView);
    }

    private int checkMarkersPresent(final MapView mapView) {
        assertThat("Test requires there to be location results for the query", !mapView.noResults());

        final int numberMarkers = mapView.markers().size() + mapView.markerClusters().size();
        assertThat("Markers present on map", numberMarkers, greaterThan(0));
        return numberMarkers;
    }

    @Test
    @ResolvedBug("FIND-394")
    public void testMapSummariesHaveNoPlaceholder() {
        LOGGER.info("Test failing on Selenium hub");
        mapView = search("saint");
        checkMarkersPresent(mapView);

        Waits.loadOrFadeWait();
        clickClustersUntilMarker();

        final WebElement popover = mapView.popover();
        verifyThat(popover.getText(), not(containsString("QueryText-Placeholder")));
    }

    private void clickClustersUntilMarker() {
        final List<WebElement> markers = mapView.markers();
        if (!markers.isEmpty()) {
            mapView.clickMarker(markers.get(0));
            return;
        }

        mapView.markerClusters().get(0).click();
        Waits.loadOrFadeWait();
        clickClustersUntilMarker();
    }

    @Test
    @ResolvedBug("FIND-156")
    public void testOnlyDocsWithLocationHaveMetaDataTab() {
        mapView = search("\"car AND house\"");
        assumeThat("There are no location results for this search", mapView.noResults());

        findPage.goToListView();

        final InlinePreview documentViewer = getElementFactory().getListView().searchResult(1).openDocumentPreview();
        final DetailedPreviewPage detailedPreviewPage = documentViewer.openPreview();
        verifyThat("There is no location tab", !detailedPreviewPage.locationTabExists());
    }

    @Test
    @ResolvedBug("FIND-328")
    public void testLocationCountsInMapResultsAndComparison() {
        final MapView firstSearchMapView = search("saint");
        firstSearchMapView.waitForMarkers();
        final int firstResults = firstSearchMapView.countLocations();
        final int firstSearchDisplayedDocumentCount = firstSearchMapView.numberOfDisplayedDocuments();
        verifyThat("First search has expected number of map points", firstResults, greaterThanOrEqualTo(firstSearchDisplayedDocumentCount));

        try {
            final String firstSearch = "Dr Jekyll";
            savedSearchService.saveCurrentAs(firstSearch, SearchType.QUERY);

            savedSearchService.openNewTab();
            Waits.loadOrFadeWait();
            final MapView secondSearchMapView = search("bear");
            secondSearchMapView.waitForMarkers();

            final int secondResults = secondSearchMapView.countLocations();
            final int secondSearchDisplayedDocumentCount = secondSearchMapView.numberOfDisplayedDocuments();
            verifyThat("Second search has expected number of map points", secondResults, greaterThanOrEqualTo(secondSearchDisplayedDocumentCount));

            final String secondSearch = "Mr Hyde";
            savedSearchService.saveCurrentAs(secondSearch, SearchType.QUERY);
            savedSearchService.compareCurrentWith(firstSearch);

            final MapView comparisonMapView = getElementFactory().getResultsComparison().goToMapView();

            comparisonMapView.waitForMarkers();
            //map often adjusts zoom and moves markers
            Waits.loadOrFadeWait();

            final int common = comparisonMapView.countCommonLocations();
            final int comparee = comparisonMapView.countLocationsForComparee();
            final int comparer = comparisonMapView.countLocationsForComparer();
            final int totalMapPoints = common + comparee + comparer;

            verifyThat("Comparison has expected number of map points", totalMapPoints, greaterThanOrEqualTo(Math.max(firstSearchDisplayedDocumentCount, secondSearchDisplayedDocumentCount)));
        } finally {
            findPage.goBackToSearch();
            savedSearchService.waitForSomeTabsAndDelete();
        }
    }

    private MapView search(final String searchTerm) {
        findService.searchAnyView(searchTerm);
        findPage.goToMap();
        final MapView map = getElementFactory().getMap();
        map.waitForMarkers();
        return map;
    }
}
