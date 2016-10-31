/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.filters.DateOption;
import com.autonomy.abc.selenium.find.filters.GraphFilterContainer;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.MainNumericWidget;
import com.autonomy.abc.selenium.find.numericWidgets.NumericWidget;
import com.autonomy.abc.selenium.find.numericWidgets.NumericWidgetService;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.find.save.SavedSearchPanel;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.autonomy.abc.selenium.find.save.SearchTabBar;
import com.autonomy.abc.selenium.find.save.SearchType;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

//NB: FIND-399 -> these tests will stop needing to re-assign MainGraph every minute once stops reloading
@Role(UserRole.BIFHI)
public class NumericWidgetITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;
    private NumericWidgetService numericService;

    public NumericWidgetITCase(final TestConfig config) {
        super(config);
    }

    @Override
    public BIIdolFindElementFactory getElementFactory() {
        return (BIIdolFindElementFactory)super.getElementFactory();
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
        numericService = getApplication().numericWidgetService();
    }

    /*##########ANY NUMERIC GRAPH###########*/
    @Test
    @ActiveBug("FIND-417")
    public void testClickingOnFilterPanelGraphOpensMain() {
        findService.search("book");
        filters().waitForParametricFields();

        assertThat("Default: main graph not shown", !findPage.mainGraphDisplayed());

        MainNumericWidget mainGraph;
        for (final GraphFilterContainer container : filters().graphContainers()) {
            final String graphTitle = numericService.selectFilterGraph(container);
            verifyThat("Main graph now shown", findPage.mainGraphDisplayed());

            mainGraph = findPage.mainGraph();
            verifyThat("Correct graph is open", mainGraph.header(), equalToIgnoringCase(graphTitle));
        }

        findPage.mainGraph().closeWidget();
        verifyThat("Main graph now gone", !findPage.mainGraphDisplayed());
    }

    @Test
    @ResolvedBug("FIND-356")
    public void testSelectionRecDoesNotDisappear() {
        //TODO THIS ISN'T NEEDED - do not hardcode
        MainNumericWidget mainGraph = numericService.searchAndSelectNthGraph(0, "politics");
        mainGraph.clickAndDrag(100, mainGraph.graph());

        filters().waitForParametricFields();

        mainGraph.waitUntilWidgetLoaded();

        mainGraph = findPage.mainGraph();
        mainGraph.waitUntilRectangleBack();

        verifyThat("Selection rectangle hasn't disappeared", mainGraph.graphAsWidget().selectionRectangleExists());
    }

    @Test
    public void testSelectionRecFiltersResults() {
        MainNumericWidget mainGraph = numericService.searchAndSelectNamedGraph("autn date", "space");

        final ResultsView results = getElementFactory().getResultsPage();
        results.goToListView();
        final int beforeNumberResults = findPage.totalResultsNum();
        mainGraph.waitUntilWidgetLoaded();
        final String beforeMin = mainGraph.minFieldValue();
        final String beforeMax = mainGraph.maxFieldValue();

        mainGraph.selectHalfTheBars();
        mainGraph = numericService.waitForReload();

        verifyThat("Filter label has appeared", findPage.filterLabelsText(), hasSize(1));
        verifyThat("Fewer results", findPage.totalResultsNum(), lessThan(beforeNumberResults));

        verifyThat("Min field text value changed", mainGraph.minFieldValue(), not(is(beforeMin)));
        verifyThat("Max field text value changed", mainGraph.maxFieldValue(), not(is(beforeMax)));

        NumericWidget sidePanelChart = filters().getNamedGraph("autn date").getChart();
        verifyThat("Side panel chart has selection rectangle", sidePanelChart.selectionRectangleExists());

        mainGraph.reset();
        mainGraph = numericService.waitForReload();

        verifyThat("Selection rectangle gone from centre", !mainGraph.graphAsWidget().selectionRectangleExists());
        verifyThat("Min bound returned to original", mainGraph.minFieldValue(), is(beforeMin));
        verifyThat("Max bound returned to original", mainGraph.maxFieldValue(), is(beforeMax));

        sidePanelChart = filters().getNamedGraph("autn date").getChart();
        verifyThat("Selection rectangle gone from side panel", !sidePanelChart.selectionRectangleExists());
    }

    @Test
    @ActiveBug("FIND-392")
    public void testWidgetsReflectCurrentSearch() {
        final MainNumericWidget mainGraph = numericService.searchAndSelectNthGraph(2, "face");
        mainGraph.selectFractionOfBars(3, 4);
        numericService.waitForReload();
        final ResultsView results = getElementFactory().getResultsPage();
        results.goToListView();
        verifyThat("There are results present", findPage.totalResultsNum(), greaterThan(0));
    }

    @Test
    @ResolvedBug("FIND-366")
    public void testFilterLabelsUpdate() {
        findService.search("dance");
        filters().waitForParametricFields();
        numericService.selectFilterGraph(filters().getNthGraph(0));

        final MainNumericWidget mainGraph = findPage.mainGraph();
        mainGraph.clickAndDrag(100, mainGraph.graph());
        final String label = findPage.filterLabelsText().get(0);

        mainGraph.clickAndDrag(-100, mainGraph.graph());
        final String changedLabel = findPage.filterLabelsText().get(0);
        assertThat("The label has changed", changedLabel, not(is(label)));
    }

    @Test
    @ResolvedBug("FIND-282")
    @ActiveBug("FIND-417")
    public void testFilterLabelsHaveTitleOnTooltip() {
        findService.search("ball");
        filters().waitForParametricFields();

        final List<String> graphTitles = filterByAllGraphs();
        final List<WebElement> webElements = findPage.filterLabels();

        verifyThat("All filters have a label", webElements, hasSize(graphTitles.size()));

        final List<String> tooltipsText = webElements.stream()
                .map(labelElement -> labelElement.findElement(By.cssSelector(".filter-display-text")).getAttribute("data-original-title"))
                .collect(Collectors.toList());

        for (int i = 0; i < graphTitles.size(); i++) {
            final String title = graphTitles.get(i);
            verifyThat("Title " + title.toLowerCase() + " is in filter tooltip", tooltipsText.get(i).toLowerCase(), containsString(title.toLowerCase()));
        }
    }

    private List<String> filterByAllGraphs() {
        final List<String> titles = new ArrayList<>();
        MainNumericWidget mainGraph;

        for (final GraphFilterContainer container : filters().graphContainers()) {
            titles.add(numericService.selectFilterGraph(container));
            mainGraph = findPage.mainGraph();
            mainGraph.clickAndDrag(100, mainGraph.graph());
        }

        return titles;
    }

    @Test
    @ResolvedBug("FIND-273")
    public void testRemovingViaFilterLabelRemovesSelection() {
        MainNumericWidget mainGraph = numericService.searchAndSelectFirstNumericGraph("space");
        mainGraph.clickAndDrag(100, mainGraph.graph());

        filters().waitForParametricFields();
        mainGraph = findPage.mainGraph();
        mainGraph.waitUntilRectangleBack();

        final List<WebElement> labels = findPage.filterLabels();
        assertThat("Filter label appeared", labels, hasSize(1));
        findPage.removeFilterLabel(labels.get(0));

        filters().waitForParametricFields();
        final NumericWidget sidePanelChart = filters().getFirstNumericGraph().getChart();
        verifyThat("Side panel chart selection rectangle gone", !sidePanelChart.selectionRectangleExists());
    }

    @Test
    @ResolvedBug({"FIND-270", "FIND-143"})
    public void testFilterLabelPresentInSavedQuery() {
        final String searchName = "meh";
        final MainNumericWidget mainGraph = numericService.searchAndSelectNthGraph(1, "moon");
        mainGraph.clickAndDrag(-50, mainGraph.graph());

        final SavedSearchService saveService = getApplication().savedSearchService();
        final SearchTabBar searchTabs = getElementFactory().getSearchTabBar();

        try {
            saveService.saveCurrentAs(searchName, SearchType.QUERY);
            saveService.openNewTab();
            searchTabs.switchTo(searchName);

            Waits.loadOrFadeWait();

            verifyThat("Filter labels have appeared", findPage.filterLabels(), not(empty()));
        } finally {
            findService.search("back to results");
            saveService.deleteAll();
        }
    }

    /*##########DATE GRAPHS##########*/
    @Test
    @ResolvedBug("FIND-390")
    public void testInteractionWithRegularDateFilters() {
        final MainNumericWidget mainGraph = numericService.searchAndSelectFirstDateGraph("whatever");

        filters().expandDateFilters();
        filters().toggleFilter(DateOption.MONTH);
        filters().waitForParametricFields();
        mainGraph.waitUntilWidgetLoaded();

        final WebElement errorMessage = mainGraph.errorMessage();
        verifyThat("Error message not displayed", !errorMessage.isDisplayed());
        verifyThat("Error message not 'failed to load data'", errorMessage.getText(), not(equalToIgnoringCase("Failed to load data")));
    }

    @Test
    @ResolvedBug("FIND-400")
    @Ignore("Numeric widget reloading currently makes it impossible to Selenium test this.")
    public void testInputDateBoundsAsText() throws Exception {
        MainNumericWidget mainGraph = numericService.searchAndSelectFirstDateGraph("red");
        final String startDate = "1976-10-22 08:46";
        final String endDate = "2012-10-10 21:49";

        LOGGER.info("Currently fails due to reloading of numeric widget");
        mainGraph = setMinAndMax(startDate, endDate, mainGraph);

        mainGraph.waitUntilWidgetLoaded();

        dateRectangleHover(mainGraph, "1976", "2012");
    }

    @Test
    @ResolvedBug("FIND-633")
    @ActiveBug("FIND-690")
    public void testInputDateBoundsWithCalendar() {
        MainNumericWidget mainGraph = numericService.searchAndSelectFirstDateGraph("tragedy");
        final DatePicker startCalendar = mainGraph.openCalendar(mainGraph.startCalendar());

        assertThat("Calendar widget has opened",mainGraph.calendarHasOpened());

        startCalendar.calendarDateSelect(new Date(76, 8, 26));
        final DatePicker endCalendar = mainGraph.openCalendar(mainGraph.endCalendar());
        endCalendar.calendarDateSelect(new Date(120, 3, 22));

        mainGraph = findPage.mainGraph();
        mainGraph.waitUntilWidgetLoaded();
        mainGraph.waitUntilRectangleBack();
        //to close the calendar pop-up
        mainGraph.messageRow().click();
        mainGraph.waitUntilDatePickerGone();

        dateRectangleHover(mainGraph, "1976", "2022");
    }

    private void dateRectangleHover(final MainNumericWidget mainGraph, final String start, final String end) {
        mainGraph.rectangleHoverRight();
        final String rightCorner = mainGraph.hoverMessage().split(" ")[0];
        mainGraph.rectangleHoverLeft();
        final String leftCorner = mainGraph.hoverMessage().split(" ")[0];

        verifyThat("Start bound is correct", leftCorner, containsString(start));
        verifyThat("End bound is not an empty string due to selection rectangle truncation", leftCorner, not(""));
        verifyThat("End bound is correct", rightCorner, containsString(end));
    }

    @Test
    @ResolvedBug({"FIND-389", "FIND-143"})
    public void testSnapshotDateRangesDisplayedCorrectly() {
        final MainNumericWidget mainGraph = numericService.searchAndSelectFirstDateGraph("dire");
        final String filterType = mainGraph.header();
        mainGraph.clickAndDrag(-50, mainGraph.graph());
        findPage.waitForParametricValuesToLoad();

        final SavedSearchService saveService = getApplication().savedSearchService();
        final SearchTabBar searchTabs = getElementFactory().getSearchTabBar();

        try {
            saveService.saveCurrentAs("bad", SearchType.SNAPSHOT);
            searchTabs.switchTo("bad");
            Waits.loadOrFadeWait();
            final String dateRange = new SavedSearchPanel(getDriver()).getFirstSelectedFilterOfType(filterType);
            verifyThat("Date range formatted like date", dateRange, allOf(containsString("/"), containsString(":")));
        } finally {
            searchTabs.switchTo("bad");
            saveService.deleteCurrentSearch();
        }
    }

    /*##########NON-DATE GRAPHS##########*/
    @Test
    @ResolvedBug("FIND-365")
    // Make sure that the named graph used is a numeric and not date widget
    public void testFilterLabelFormatReflectsNumericData() {
        final MainNumericWidget mainGraph = numericService.searchAndSelectNamedGraph("Random Number", "beer");
        assumeThat("Test assumes that there is the numeric field place elevation", mainGraph.header(), equalToIgnoringCase("Random Number"));

        mainGraph.clickAndDrag(200, mainGraph.graph());
        numericService.waitForReload();

        verifyThat("Place elevation filter label doesn't have time format", findPage.filterLabelsText().get(0), not(containsString(":")));
    }

    @Test
    //TODO: improve by getting the range and minValue from the widget at the time
    //NB: the proportion of the graph needs to be commensurate with the data in the graph
    //Set range and minValue to something reasonable for your graph
    public void testInputNumericBoundsAsText() throws Exception {
        final double range = 0.3;
        final String minValue = "0.4";

        final String maxValue = Double.toString(Double.parseDouble(minValue) + range);
        final int rangeMinusDelta = (int) (range * 0.98);
        final int rangePlusDelta = (int) (range * 1.02);

        MainNumericWidget mainGraph = numericService.searchAndSelectFirstNumericGraph("red");
        final int numericUnitsPerChartWidth = mainGraph.getRange() / mainGraph.graphWidth();

        //#1 testing that correct proportion of chart selected
        mainGraph = setMinAndMax(minValue, maxValue, mainGraph);
        Waits.loadOrFadeWait();
        final int newUnitsPerWidthUnit = 250 / findPage.mainGraph().graphAsWidget().selectionRectangleWidth();
        verifyThat("Selection rectangle covering correct fraction of chart", newUnitsPerWidthUnit, is(numericUnitsPerChartWidth));

        //#2 testing correct boundaries of rectangle
        mainGraph.waitUntilWidgetLoaded();
        mainGraph = findPage.mainGraph();

        mainGraph.rectangleHoverRight();
        final String rightCorner = mainGraph.hoverMessage();
        mainGraph.rectangleHoverLeft();
        final String leftCorner = mainGraph.hoverMessage();

        final int rectangleRange = (int) (Double.parseDouble(rightCorner) - Double.parseDouble(leftCorner));
        verifyThat("Edges of rectangle reflect bounds correctly", rectangleRange, allOf(greaterThanOrEqualTo(rangeMinusDelta), lessThanOrEqualTo(rangePlusDelta)));
    }

    @Test
    @ResolvedBug("FIND-393")
    public void testTextMaxBoundCannotBeLessThanMin() throws Exception {
        final String lowNum = "0";
        final String highNum = "600";
        MainNumericWidget mainGraph = numericService.searchAndSelectFirstNumericGraph("red");

        mainGraph = setMinAndMax(highNum, lowNum, mainGraph);
        verifyThat("Min bound re-set to value of max", mainGraph.minFieldValue(), is(lowNum));

        mainGraph.setMaxValueViaText(lowNum);
        Waits.loadOrFadeWait();
        mainGraph = findPage.mainGraph();
        mainGraph.setMinValueViaText(highNum);

        verifyThat("Max bound re-set to value of min", mainGraph.maxFieldValue(), is(highNum));
    }

    private MainNumericWidget setMinAndMax(final String min, final String max, MainNumericWidget mainGraph) throws Exception {
        Waits.loadOrFadeWait();
        Waits.loadOrFadeWait();
        mainGraph.setMinValueViaText(min);
        //bad but soon the graph will not reload so this won't be necessary
        Waits.loadOrFadeWait();
        Waits.loadOrFadeWait();
        mainGraph = findPage.mainGraph();
        mainGraph.setMaxValueViaText(max);
        return findPage.mainGraph();
    }

    //###########BOTH DATE AND NUMERIC NEEDED###########//
    @Test
    @Ignore("Desired behaviour but not implemented and not a bug")
    public void testMinAndMaxReflectCurrentSearch() {
        //currently 0th graph is place elevation (i.e. non-date)
        numericService.searchAndSelectNthGraph(0, "*");
        checkBoundsForPlaceElevationWidget();

        numericService.searchAndSelectNthGraph(1, "*");
        checkBoundsForDateWidget();
    }

    private void checkBoundsForPlaceElevationWidget() {
        findPage.filterBy(new IndexFilter("Cities"));
        MainNumericWidget mainGraph = findPage.mainGraph();
        final int originalRange = mainGraph.getRange();

        findService.search("Tse");
        mainGraph = findPage.mainGraph();
        final int newRange = mainGraph.getRange();

        verifyThat("Bounds are determined by current query for non-date widget", newRange, lessThan(originalRange));
        mainGraph.reset();
    }

    private void checkBoundsForDateWidget() {
        final List<Date> oldD = findPage.mainGraph().getDates();
        findPage.filterBy(IndexFilter.ALL);
        findService.search("George Orwell");
        final List<Date> newD = findPage.mainGraph().getDates();

        if(newD.get(0).after(oldD.get(0))) {
            verifyThat("Bounds are determined by current query for date widget", newD.get(0).after(oldD.get(0)));
        } else {
            verifyThat("Bounds are determined by current query for date widget", newD.get(1).before(oldD.get(1)));
        }
    }

    private IdolFilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}
