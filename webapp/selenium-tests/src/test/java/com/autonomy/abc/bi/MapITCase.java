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
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

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


    private int checkMarkersPresent(MapView mapView) {
        assertThat("Test requires there to be location results for the query", !mapView.noResults());

        int numberMarkers = mapView.markers().size() + mapView.markerClusters().size();
        assertThat("Markers present on map",numberMarkers,greaterThan(0));
        return numberMarkers;
    }

    @Test
    @ResolvedBug("FIND-394")
    public void testMapSummariesHaveNoPlaceholder() {
        mapView = search("saint");
        checkMarkersPresent(mapView);

        Waits.loadOrFadeWait();
        clickClustersUntilMarker();

        WebElement popover = mapView.popover();
        verifyThat(popover.getText(), not(containsString("QueryText-Placeholder")));
    }

    private void clickClustersUntilMarker() {
        List<WebElement> markers = mapView.markers();
        if(!markers.isEmpty()) {
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
        assumeThat("There are no location results for this search",mapView.noResults());

        findPage.goToListView();

        InlinePreview documentViewer = getElementFactory().getResultsPage().searchResult(1).openDocumentPreview();
        final DetailedPreviewPage detailedPreviewPage = documentViewer.openPreview();
        verifyThat("There is no location tab",!detailedPreviewPage.locationTabExists());
    }

    @Test
    @ResolvedBug("FIND-328")
    @ActiveBug("FIND-649")
    public void testOnlyLocationDataInMapComparison() {
        final String firstSearch = "Dr Jekyll";
        final String secondSearch = "Mr Hyde";
        mapView = search("saint");
        mapView.waitForMarkers();
        int firstResults = mapView.numberResults();

        try {
            savedSearchService.saveCurrentAs(firstSearch, SearchType.QUERY);

            savedSearchService.openNewTab();
            Waits.loadOrFadeWait();
            mapView = search("bear");
            mapView.waitForMarkers();

            int secondResults = mapView.numberResults();

            savedSearchService.saveCurrentAs(secondSearch, SearchType.QUERY);
            savedSearchService.compareCurrentWith(firstSearch);

            mapView = getElementFactory().getResultsComparison().goToMapView();

            mapView.waitForMarkers();
            //map often adjusts zoom and moves markers
            Waits.loadOrFadeWait();

            final int common = mapView.countCommonLocations();
            final int comparee = mapView.countLocationsForComparee() + common;
            final int comparer = mapView.countLocationsForComparer() + common;

            verifyThat("First search has same number results",comparee,is(firstResults));
            verifyThat("Second search has same number results",comparer,is(secondResults));
        }
        finally {
            findPage.goBackToSearch();
            savedSearchService.deleteAll();
        }
    }

    private MapView search(String searchTerm) {
        findService.search(searchTerm);
        findPage.goToMap();
        MapView map = getElementFactory().getMap();
        map.waitForMarkers();
        return map;
    }

}
