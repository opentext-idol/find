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
import com.autonomy.abc.selenium.error.Errors.Find;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFind;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.SunburstView;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.NumericWidgetService;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.find.save.SavedSearchPanel;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchOptionsBar;
import com.autonomy.abc.selenium.find.save.SearchTab;
import com.autonomy.abc.selenium.find.save.SearchTabBar;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.autonomy.abc.matchers.ErrorMatchers.isError;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.checked;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Role(UserRole.BIFHI)
public class SavedSearchITCase extends IdolFindTestBase {
    private SearchTabBar searchTabBar;
    private FindService findService;
    private SavedSearchService saveService;
    private BIIdolFindElementFactory elementFactory;

    public SavedSearchITCase(final TestConfig config) {
        super(config);
    }

    private static Matcher<SearchTab> modified() {
        return ModifiedMatcher.INSTANCE;
    }

    @Before
    public void setUp() {
        findService = getApplication().findService();
        saveService = getApplication().savedSearchService();

        elementFactory = (BIIdolFindElementFactory)getElementFactory();
        elementFactory.getFindPage().goToListView();
        searchTabBar = elementFactory.getSearchTabBar();
    }

    @After
    public void tearDown() {
        saveService.waitForSomeTabsAndDelete();
    }

    @Test
    @ResolvedBug("FIND-467")
    public void testCanSaveSearch() {
        findService.search("queen");

        saveService.saveCurrentAs("save me", SearchType.QUERY);

        final SearchTab currentTab = searchTabBar.currentTab();
        assertThat(currentTab.getTitle(), is("save me"));
        assertThat(currentTab.getTitle(), not(containsString("New Search")));
        assertThat(currentTab, not(modified()));
    }

    @Test
    public void testSnapshotSavedInNewTab() {
        findService.search("crocodile");

        saveService.saveCurrentAs("snap", SearchType.SNAPSHOT);

        final List<SearchTab> tabs = searchTabBar.tabs();
        assertThat(tabs, hasSize(2));
        assertThat(tabs.get(0), is(modified()));
        assertThat(tabs.get(0).getType(), is(SearchType.QUERY));
        assertThat(tabs.get(1), not(modified()));
        assertThat(tabs.get(1).getType(), is(SearchType.SNAPSHOT));
    }

    @Test
    public void testOpenSnapshotAsQuery() {
        findService.search("open");
        elementFactory.getListView().waitForResultsToLoad();

        saveService.saveCurrentAs("sesame", SearchType.SNAPSHOT);
        findService.search("no longer open");
        searchTabBar.switchTo("sesame");

        elementFactory.getSearchOptionsBar().openSnapshotAsQuery();

        assertThat(searchTabBar.currentTab().getTitle(), is("New Search"));
        assertThat(searchTabBar.currentTab().getType(), is(SearchType.QUERY));
        assertThat(searchTabBar.tab("sesame").getType(), is(SearchType.SNAPSHOT));

        final List<WebElement> addedConcepts = elementFactory.getConceptsPanel().selectedConcepts();
        assertThat(addedConcepts, hasSize(1));
        assertThat(addedConcepts.get(0), containsText("open"));
    }

    @Test
    public void testDuplicateNamesPrevented() {
        findService.search("useless");
        saveService.saveCurrentAs("duplicate", SearchType.QUERY);
        saveService.openNewTab();
        elementFactory.getFindPage().waitUntilDatabasesLoaded();

        checkSavingDuplicateThrowsError("duplicate", SearchType.QUERY);
        checkSavingDuplicateThrowsError("duplicate", SearchType.SNAPSHOT);
    }

    private void checkSavingDuplicateThrowsError(final String searchName, final SearchType type) {
        Waits.loadOrFadeWait();
        final SearchOptionsBar options = saveService.nameSavedSearch(searchName, type);
        options.saveConfirmButton().click();
        assertThat(options.getSaveErrorMessage(), isError(Find.DUPLICATE_SEARCH));
        options.cancelSave();
    }

    @Test
    public void testSavedSearchVisibleInNewSession() {
        findService.search(new Query("live forever"));
        final ListView results = elementFactory.getListView();
        results.waitForResultsToLoad();

        final FilterPanel filterPanel = elementFactory.getFilterPanel();
        filterPanel.waitForParametricFields();

        final int index = filterPanel.nonZeroParamFieldContainer(0);
        filterPanel.checkboxForParametricValue(index, 0).check();

        results.waitForResultsToLoad();
        saveService.saveCurrentAs("oasis", SearchType.QUERY);

        final BIIdolFind other = new BIIdolFind();
        launchInNewInstance(other);
        other.loginService().login(getInitialUser());
        other.findService().searchAnyView("blur");

        final BIIdolFindElementFactory factory = other.elementFactory();
        factory.getSearchTabBar().switchTo("oasis");
        factory.getFilterPanel().waitForParametricFields();
        assertThat(factory.getFilterPanel().checkboxForParametricValue(index, 0), checked());
    }

    @Test
    @ResolvedBug("FIND-278")
    public void testCannotChangeParametricValuesInSnapshot() {
        findService.search("terrible");
        final String searchName = "horrible";

        saveService.saveCurrentAs(searchName, SearchType.SNAPSHOT);
        searchTabBar.switchTo(searchName);

        final IdolFindPage findPage = elementFactory.getFindPage();
        findPage.goToSunburst();
        Waits.loadOrFadeWait();

        final SavedSearchPanel panel = new SavedSearchPanel(getDriver());
        final int originalCount = panel.resultCount();

        final SunburstView results = elementFactory.getSunburst();

        results.waitForSunburst();
        //TODO: extract coordinates from SVG to ensure click/hover over segments
        results.getIthSunburstSegment(1).click();
        results.waitForSunburst();

        verifyThat("Has not added filter", findPage.filterLabels(), hasSize(0));
        verifyThat("Same number of results", panel.resultCount(), is(originalCount));
    }

    @Test
    @ResolvedBug("FIND-284")
    public void testRenamingSnapshot() {
        findService.search("broken");

        final String originalName = "originalName";
        saveService.saveCurrentAs(originalName, SearchType.SNAPSHOT);
        searchTabBar.switchTo(originalName);

        final String newName = "newName";
        saveService.renameCurrentAs(newName);

        saveService.openNewTab();
        searchTabBar.switchTo(newName);
        verifyThat("Saved search has content", elementFactory.getTopicMap().topicMapVisible());
    }

    @Test
    @ResolvedBug("FIND-269")
    public void testSearchesWithNumericFilters() {
        final NumericWidgetService widgetService = ((BIIdolFind)getApplication()).numericWidgetService();
        DriverUtil.clickAndDrag(100, widgetService.searchAndSelectNthGraph(0, "saint", getDriver()).graph(), getDriver());

        elementFactory.getListView().waitForResultsToLoad();
        saveService.saveCurrentAs("saaaaved", SearchType.QUERY);

        assertThat(searchTabBar.currentTab(), not(modified()));
    }

    // Checks that the saved-ness of the search respects the selected concepts
    @Test
    public void testSearchesWithConcepts() {
        final TopicMapView topicMap = elementFactory.getFindPage().goToTopicMap();
        topicMap.waitForMapLoaded();

        // Select a concept and save the search
        final String selectedConcept = topicMap.clickNthClusterHeading(0);
        elementFactory.getFindPage().waitUntilSavePossible();
        saveService.saveCurrentAs("Conceptual Search", SearchType.QUERY);

        // Remove the selected concept
        final ConceptsPanel conceptsPanel = elementFactory.getConceptsPanel();
        conceptsPanel.removableConceptForHeader(selectedConcept).removeAndWait();

        assertThat(searchTabBar.currentTab(), is(modified()));
        assertThat(conceptsPanel.selectedConceptHeaders(), empty());

        // Reset the search
        saveService.resetCurrentQuery();

        assertThat(searchTabBar.currentTab(), not(modified()));
        final List<String> finalConceptHeaders = conceptsPanel.selectedConceptHeaders();
        assertThat(finalConceptHeaders, hasSize(1));
        assertThat(finalConceptHeaders, hasItem('"' + selectedConcept + '"'));
    }

    @Test
    @ResolvedBug("FIND-167")
    public void testCannotSaveSearchWithWhitespaceAsName() {
        findService.search("yolo");
        final SearchOptionsBar searchOptions = saveService.nameSavedSearch("   ", SearchType.QUERY);

        assertThat("Save button is disabled", !searchOptions.saveConfirmButton().isEnabled());
    }

    @Test
    public void testDeletingATab() {
        saveService.deleteAll();
        saveManySearchesWithSameNameAsSearchText(new String[]{"yellow", "red"}, SearchType.QUERY);

        final SearchTabBar searchTabBar = elementFactory.getSearchTabBar();
        final String title = searchTabBar.currentTab().getTitle();

        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.delete();
        searchTabBar.waitUntilTabGone(title);

        verifyThat("Deleted search is gone", searchTabBar.savedTabTitles(), not(contains(title)));
    }

    @Test
    @ResolvedBug({"FIND-1170", "FIND-1168"})
    public void newSearchParametricsLoadedAfterSavedSearchPageLoad() {
        findService.search("cheese");
        saveService.saveCurrentAs("cheese", SearchType.QUERY);
        final String url = getDriver().getCurrentUrl();
        getDriver().navigate().to(url);
        elementFactory.getFindPage().waitForLoad();
        elementFactory.getFilterPanel().waitForParametricFields();

        assertThat("There is a non-zero parametric filter available on the saved search", elementFactory.getFilterPanel().nonZeroParamFieldContainer(0) >= 0);
        elementFactory.getSearchTabBar().switchTo("New Search");
        elementFactory.getFilterPanel().waitForParametricFields();
        assertThat("There is a non-zero parametric filter available on the new search", elementFactory.getFilterPanel().nonZeroParamFieldContainer(0) >= 0);
    }

    private void saveManySearchesWithSameNameAsSearchText(final String[] searchNames, final SearchType saveType) {
        boolean firstSearch = true;
        for(final String name : searchNames) {
            if(!firstSearch) {
                saveService.openNewTab();
            }
            firstSearch = false;
            findService.searchAnyView(name);
            elementFactory.getFindPage().waitUntilSaveButtonsActive();
            saveService.saveCurrentAs(name, saveType);
        }
    }

    private static class ModifiedMatcher extends TypeSafeMatcher<SearchTab> {
        private static final Matcher<SearchTab> INSTANCE = new ModifiedMatcher();

        @Override
        protected boolean matchesSafely(final SearchTab searchTab) {
            return searchTab.isNew();
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("a modified tab");
        }
    }
}
