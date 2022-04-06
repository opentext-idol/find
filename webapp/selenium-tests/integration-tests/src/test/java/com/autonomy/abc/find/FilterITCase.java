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

package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.filters.AppliedFiltersPanel;
import com.autonomy.abc.selenium.find.filters.DateOption;
import com.autonomy.abc.selenium.find.filters.FilterContainer;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.FindParametricFilter;
import com.autonomy.abc.selenium.find.filters.ParametricFieldContainer;
import com.autonomy.abc.selenium.find.filters.ParametricFilterModal;
import com.autonomy.abc.selenium.find.results.DocumentViewer;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.StringDateFilter;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.text.WordUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class FilterITCase extends FindTestBase {
    private FindPage findPage;
    private FindService findService;

    public FilterITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
        findPage.goToListView();
    }

    private ListView searchAndWait(final String searchTerm) {
        final ListView results = findService.search(searchTerm);
        findPage.waitForParametricValuesToLoad();
        results.waitForResultsToLoad();
        return results;
    }

    @Test
    public void testAllFiltersDefaultCollapsed() {
        searchAndWait("knee");

        for (final FilterContainer container : filters().allFilterContainers()) {
            verifyThat("Container is collapsed", container.isCollapsed());
        }
    }

    @Test
    @ResolvedBug("FIND-671")
    public void testDatabasesDisplayed() {
        searchAndWait("dark");

        assertThat("Databases are not hidden", filters().indexesTreeContainer().getContainer(), displayed());
    }

    @Test
    @ResolvedBug("FIND-638")
    public void testParametricFiltersResults() {
        final ListView results = searchAndWait("cheese");
        final FilterPanel filterPanel = filters();

        final List<ParametricFieldContainer> containers = filterPanel.parametricFieldContainers();
        for (final ParametricFieldContainer container : containers) {
            final int numberFields = container.getFilters().size();
            verifyThat("Field values: " + numberFields + " - less than or equal to 5", numberFields, lessThanOrEqualTo(5));
        }

        final ParametricFieldContainer firstContainer = filterPanel.parametricField(filterPanel.nonZeroParamFieldContainer(0));

        final FindParametricFilter firstField = firstContainer.getFilters().get(0);
        final String filterName = firstField.getName();
        final int expectedResults = firstField.getResultsCount();

        final int originalNumberOfResults = results.getTotalResultsNum();
        assumeThat("Fewer results predicted w/ this filter", expectedResults, lessThan(originalNumberOfResults));

        firstField.check();
        results.waitForResultsToLoad();

        verifyThat("Expected number of results (according to panel) equals actual number of results",
                results.getTotalResultsNum(), is(expectedResults));

        Waits.loadOrFadeWait();
        final ParametricFieldContainer container = filterPanel.parametricContainerOfFilter(filterName);
        final int filterNumber = container.getFilterCount();
        final String filterCategory = container.filterCategoryName();

        container.seeAll();
        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        verifyThat("Filter category title shows the number of filters chosen from total", filterNumber, is(filterModal.filtersWithResultsForCurrentSearch()));

        filterPanel.checkboxForParametricValue(WordUtils.capitalize(filterCategory.toLowerCase()), filterName).uncheck();
        findPage.waitForParametricValuesToLoad();
        final int totalParametricFields = filterPanel.parametricFieldContainers().size();

        searchAndWait("shouldhavenoresultsprobably");
        findPage.ensureTermNotAutoCorrected();

        findPage.waitForParametricValuesToLoad();
        final int noResultsParametricFields = filterPanel.parametricFieldContainers().size();

        verifyThat("Filters changed: no results -> parametric fields remain", noResultsParametricFields, is(totalParametricFields));
    }

    @Test
    public void testModalOnlyContainsParametricFields() {
        searchAndWait("cheese");
        final FilterPanel filterPanel = filters();
        final List<String> parametricFields = filterPanel.parametricFieldContainers().stream()
                .map(ParametricFieldContainer::filterCategoryName)
                .collect(Collectors.toList());

        final ParametricFieldContainer firstField = filterPanel.parametricField(0);
        firstField.expand();
        firstField.seeAll();

        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        filterModal.waitForLoad();
        final List<String> modalFields = filterModal.tabNames()
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        modalFields.forEach(tabName -> verifyThat("Modal field with name " + tabName + " is parametric", parametricFields, hasItem(tabName)));

        verifyThat("Parametric filter and modal field ordering identical", modalFields, equalTo(parametricFields));

        filterModal.cancel();
    }

    @Test
    public void testFilterPanelAndModalLinked() {
        searchAndWait("cats");
        final FilterPanel filterPanel = filters();

        final ParametricFieldContainer container = filterPanel.parametricField(1);
        final String filterCategory = container.filterCategoryName();

        final FindParametricFilter checkbox = filterPanel.checkboxForParametricValue(1, 1);
        final List<String> selectedFilter = Collections.singletonList(checkbox.getName());
        checkbox.check();

        findPage.waitForParametricValuesToLoad();

        final ParametricFieldContainer refreshedContainer = filterPanel.parametricContainer(WordUtils.capitalize(filterCategory.toLowerCase()));
        refreshedContainer.expand();
        refreshedContainer.seeAll();

        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        filterModal.waitForLoad();

        verifyThat("Modal not loading forever", !filterModal.isCurrentTabLoading());
        verifyThat("Correct tab is active", filterModal.activeTabName(), equalToIgnoringCase(filterCategory));
        verifyThat("Same fields selected in modal as panel", filterModal.checkedFiltersAllPanes(), is(selectedFilter));

        final String filterType = filterModal.activeTabName();
        final String checkedFilterName = filterModal.checkCheckBoxInActivePane(0);
        filterModal.apply();

        final FindParametricFilter panelBox = filterPanel.checkboxForParametricValue(filterType, checkedFilterName);
        verifyThat("Filter: " + checkedFilterName + " is now checked on panel", panelBox.isChecked());
    }

    //TODO this test makes many unreasonable assumptions
    @Test
    @ResolvedBug({"FIND-242", "FIND-406"})
    public void testModalShowsALLFiltersRegardlessOfQuery() {
        searchAndWait("cheese");
        final FilterPanel filterPanel = filters();

        findPage.waitForParametricValuesToLoad();
        final Collection<String> allFilterCategories = new ArrayList<>();
        for (final ParametricFieldContainer container : filterPanel.parametricFieldContainers()) {
            allFilterCategories.add(container.filterCategoryName());
        }

        filterPanel.parametricField(0).seeAll();
        final ParametricFilterModal filterModal = ParametricFilterModal.getParametricModal(getDriver());
        final int totalNumberFilters = filterModal.allFilters().size();

        filterModal.goToTab(1);
        if (!isHosted()) {
            LOGGER.info("Test works on basis that Category should have has >10 filters");
            assertThat(">10 filters shown in modal", filterModal.activePaneFilterList(), hasSize(greaterThan(10)));
        }
        filterModal.cancel();

        filterPanel.checkboxForParametricValue(0, 1).check();

        filterPanel.waitForParametricFields();
        filterPanel.parametricField(0).seeAll();
        final ParametricFilterModal refreshedFilterModal = ParametricFilterModal.getParametricModal(getDriver());

        assertThat("Modal shows all filter categories", refreshedFilterModal.tabs(), hasSize(allFilterCategories.size()));
        verifyThat("Shows all filters for restricted search (some filters may have 0 docs)"
                , refreshedFilterModal.allFilters()
                , hasSize(totalNumberFilters));

        refreshedFilterModal.cancel();
    }

    @Test
    @ResolvedBug("FIND-231")
    public void testDeselectingFiltersNoFloatingTooltips() {
        searchAndWait("home");

        final List<FindParametricFilter> boxes = checkAllVisibleFiltersInFirstParametrics();
        for (final FindParametricFilter checkbox : boxes) {
            checkbox.name().click();
        }

        verifyThat("Tooltips aren't floating everywhere", filters().toolTips(), hasSize(lessThan(boxes.size())));
    }

    private List<FindParametricFilter> checkAllVisibleFiltersInFirstParametrics() {
        final List<FindParametricFilter> boxes = filters().checkBoxesForParametricFieldContainer(0);
        for (final FindParametricFilter checkBox : boxes) {
            checkBox.check();
        }
        return boxes;
    }

    @Test
    @ResolvedBug("FIND-247")
    public void testSelectDifferentCategoryFiltersAndResultsLoad() throws InterruptedException {
        LOGGER.info("Because filter categories all collapse after selecting 1, must be quick or throws NoSuchElement");
        final ListView results = findService.search("face");
        final FilterPanel filterPanel = filters();
        final FindParametricFilter filter1 = filterPanel.checkBoxesForParametricFieldContainer(0).get(0);
        final FindParametricFilter filter2 = filterPanel.checkBoxesForParametricFieldContainer(1).get(0);

        filter1.check();
        filter2.check();

        results.waitForResultsToLoad();
        verifyThat("Loading indicator not present", !results.loadingIndicatorPresent());
    }

    @Test
    public void testUnselectingContentTypeQuicklyDoesNotLeadToError() {
        final ListView results = findService.search("wolf");

        final FindParametricFilter filter = filters().checkBoxesForParametricFieldContainer(0).get(0);
        filter.check();
        filter.uncheck();

        results.waitForResultsToLoad();
        assertThat("No error message", !results.errorContainerShown());
    }

    @Test
    public void testFilterByIndex() {
        final ListView results = findService.search("face");
        final FindResult queryResult = results.searchResult(1);
        final String titleString = queryResult.getTitleString();
        final DocumentViewer docPreview = queryResult.openDocumentPreview();

        final String index = docPreview.getIndexName();
        docPreview.close();

        filters().indexesTreeContainer().expand();
        findPage.filterBy(new IndexFilter(index));
        assertThat(results.searchResult(1).getTitleString(), is(titleString));
    }

    @Test
    public void testFilterByMultipleIndexes() {
        findService.search("unbelievable");
        final FilterPanel filterPanel = filters();
        filterPanel.indexesTreeContainer().expand();
        final int numberOfIndexes = filterPanel.indexesTree().allIndexes().getIndexNodes().size();
        assertThat("Need more than one database to perform test", numberOfIndexes, greaterThanOrEqualTo(2));
        findPage.filterBy(IndexFilter.ALL);

        final IndexFilter filter = new IndexFilter(filterPanel.getIndex(0));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        final ListView results = getElementFactory().getListView();
        final int firstFilterResults = results.getTotalResultsNum();

        filter.add(filterPanel.getIndex(1));
        findPage.filterBy(filter);
        Waits.loadOrFadeWait();
        final int bothFilterResults = results.getTotalResultsNum();

        findPage.filterBy(new IndexFilter(filterPanel.getIndex(1)));
        final int secondFilterResults = results.getTotalResultsNum();

        assertThat("Both filter indexes thus both results", firstFilterResults + secondFilterResults, is(bothFilterResults));
    }

    @Test
    public void testFilteredByIndexOnlyHasFilesFromIndex() {
        final ListView results = findService.search("Better");

        final DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();
        final String chosenIndex = docPreview.getIndexName();
        docPreview.close();

        filters().indexesTreeContainer().expand();
        findPage.filterBy(new IndexFilter(chosenIndex));
        for (int i = 1; i < 6; i++) {
            final DocumentViewer docViewer = results.searchResult(1).openDocumentPreview();
            assertThat(docPreview.getIndexName(), is(chosenIndex));
            docViewer.close();
        }
    }

    @Test
    public void testQuickDoubleClickOnDateFilterNotCauseError() {
        final ListView results = findService.search("wookie");

        toggleDateSelection(DateOption.MONTH);
        toggleDateSelection(DateOption.MONTH);

        results.waitForResultsToLoad();
        assertThat(results.resultsDiv().getText().toLowerCase(), not(containsString("an error")));

    }

    @Test
    public void testPreDefinedWeekHasSameResultsAsCustomWeek() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.WEEK);
    }

    @Test
    public void testPreDefinedMonthHasSameResultsAsCustomMonth() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.MONTH);
    }

    @Test
    public void testPreDefinedYearHasSameResultsAsCustomYear() {
        preDefinedDateFiltersVersusCustomDateFilters(DateOption.YEAR);
    }

    private void preDefinedDateFiltersVersusCustomDateFilters(final DateOption period) {
        final ListView results = findService.search("cheese");

        toggleDateSelection(period);
        final List<String> preDefinedResults = results.getResultTitles();
        findPage.filterBy(new StringDateFilter().from(getDate(period)).until(new Date()));
        final List<String> customResults = results.getResultTitles();

        assertThat(preDefinedResults, is(customResults));
    }

    private Date getDate(final DateOption period) {
        final Calendar cal = Calendar.getInstance();

        if (period != null) {
            switch (period) {
                case WEEK:
                    cal.add(Calendar.DATE, -7);
                    break;
                case MONTH:
                    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
                    break;
                case YEAR:
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                    break;
            }
        }
        return cal.getTime();
    }

    @Test
    @ActiveBug("FIND-152")
    public void testDateRemainsWhenClosingAndReopeningDateFilters() {
        final Date start = getDate(DateOption.MONTH);
        final Date end = getDate(DateOption.WEEK);

        findService.search(new Query("Corbyn"));
        findPage.filterBy(new StringDateFilter().from(start).until(end));

        Waits.loadOrFadeWait();
        for (int unused = 0; unused < 3; unused++) {
            toggleDateSelection(DateOption.CUSTOM);
            Waits.loadOrFadeWait();
        }
        assertThat(findPage.fromDateInput().getValue(), is(findPage.formatInputDate(start)));
        assertThat(findPage.untilDateInput().getValue(), is(findPage.formatInputDate(end)));
    }

    @Test
    @ResolvedBug("CSA-1577")
    public void testClickingCustomDateFilterDoesNotRefreshResults() {
        final ListView results = findService.search("O Captain! My Captain!");
        // may not happen the first time
        for (int unused = 0; unused < 5; unused++) {
            toggleDateSelection(DateOption.CUSTOM);
            assertThat(results.resultsDiv().getText(), not(containsString("Loading")));
        }
    }

    private void toggleDateSelection(final DateOption date) {
        filters().toggleFilter(date);
        getElementFactory().getListView().waitForResultsToLoad();
    }

    // Filters Applied section
    @Test
    public void testFiltersAppearInFiltersAppliedSection() {
        final ListView results = findService.search("truth");

        final int initialLabelsSize = getFilterLabels().size();

        final FilterPanel filterPanel = filters();
        filterPanel.toggleFilter(DateOption.WEEK);
        results.waitForResultsToLoad();

        assertThat("Filter label is in the Applied Filters Panel", getFilterLabels(), hasSize(greaterThan(initialLabelsSize)));
    }

    private List<WebElement> getFilterLabels() {
        return appliedFilters()
                .getPanel()
                .findElements(By.cssSelector(".filter-label"));
    }

    @Test
    public void testAppliedFiltersCounterDefaultsToZero() {
        assumeThat("Should only default to 0 filters on prem", !isHosted());

        findService.search("bear");

        final AppliedFiltersPanel appliedFiltersPanel = appliedFilters();

        final List<WebElement> appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("By default there are no filters present", appliedFilters, is(empty()));

        final String headerText = appliedFiltersPanel.getHeader().getText();
        assertThat("The header reports zero filters", headerText, endsWith("(" + appliedFilters.size() + ')'));
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void testApplyingFiltersIncrementsTheHeaderCounter() {
        final ListView results = findService.search("truth");

        final FilterPanel filterPanel = filters();
        final AppliedFiltersPanel appliedFiltersPanel = appliedFilters();

        List<WebElement> appliedFilters = appliedFiltersPanel.getAppliedFilters();
        final int initialLabelsSize = appliedFilters.size();

        if (!isHosted()) {
            assertThat("By default there are no filters present", initialLabelsSize, is(0));
        }

        filterPanel.toggleFilter(DateOption.WEEK);
        results.waitForResultsToLoad();

        appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("A filter label appears", appliedFilters, hasSize(initialLabelsSize + 1));

        String headerText = appliedFiltersPanel.appliedFilterCounter().getText();
        assertThat("The header reports the number of filter labels", headerText, containsString("(" + appliedFilters.size() + ')'));

        filterPanel.toggleFilter(DateOption.WEEK);
        results.waitForResultsToLoad();

        appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("The filter label disappears", appliedFilters, hasSize(initialLabelsSize));

        headerText = appliedFiltersPanel.appliedFilterCounter().getText();
        assertThat("The header again reports number of filters", headerText, containsString("(" + appliedFilters.size() + ')'));
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void testTheRemoveAllFiltersButtonIsHiddenWhenThereAreNoFilters() {
        final ListView results = findService.search("truth");

        final WebElement removeAllButton = appliedFilters().getRemoveAllFiltersButton();

        if (!isHosted()) {
            assertThat("Button is hidden at startup", !removeAllButton.isDisplayed());
        }

        final FilterPanel filterPanel = filters();

        filterPanel.toggleFilter(DateOption.WEEK);

        results.waitForResultsToLoad();
        assertThat("Button is displayed when filter is present", removeAllButton.isDisplayed());

        removeAllButton.click();
        results.waitForResultsToLoad();
        assertThat("Button disappears after removing filter", !removeAllButton.isDisplayed());
    }

    @SuppressWarnings("FeatureEnvy")
    @Test
    public void testClickingTheRemoveAllButtonRemovesAllFilters() {
        final ListView results = findService.search("truth");
        results.waitForResultsToLoad();

        final AppliedFiltersPanel appliedFiltersPanel = appliedFilters();

        List<WebElement> appliedFilters = appliedFiltersPanel.getAppliedFilters();
        final int initialLabelsSize = appliedFilters.size();

        final FilterPanel filterPanel = filters();
        final int index = filterPanel.nonZeroParamFieldContainer(0);
        assumeThat("There are non-zero parametric filters", index > -1);

        final FindParametricFilter parametricFilter = filterPanel.parametricField(index).getFilters().get(0);

        parametricFilter.check();
        results.waitForResultsToLoad();

        filterPanel.toggleFilter(DateOption.YEAR);
        results.waitForResultsToLoad();

        appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("Two more filters are applied", appliedFilters, hasSize(initialLabelsSize + 2));

        appliedFiltersPanel.getRemoveAllFiltersButton().click();
        appliedFilters = appliedFiltersPanel.getAppliedFilters();
        assertThat("All filters are removed", appliedFilters, is(empty()));
    }

    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }

    private AppliedFiltersPanel appliedFilters() {
        return getElementFactory().getAppliedFiltersPanel();
    }
}
