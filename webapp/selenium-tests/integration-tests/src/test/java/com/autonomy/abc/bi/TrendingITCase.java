/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TrendingService;
import com.autonomy.abc.selenium.find.bi.TrendingView;
import com.autonomy.abc.selenium.find.results.ListView;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;

@Role(UserRole.BIFHI)
public class TrendingITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;
    private TrendingView trendingView;
    private TrendingService trendingService;

    public TrendingITCase(final TestConfig config) {
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
        trendingService = new TrendingService(getApplication());

        trendingView = findPage.goToTrending();
        trendingView.waitForChartToLoad();
    }

    @Test
    public void testTrendingLoads() {
        assertThat("the trending chart container is present", trendingView.trendingChartVisible());
        assertThat("the trending chart svg has been added", trendingView.trendingChart().isDisplayed());
    }

    @Test
    public void testFieldSelectorPopulated() {
        final List<String> fields = trendingService.fieldSelectorFields(trendingView);

        findPage.waitForParametricValuesToLoad();
        final List<String> fieldsInLeftHandSide = trendingService.filterFields();
        assertThat("The fields in the field selector are all contained in the left hand side list",
                fieldsInLeftHandSide.containsAll(fields));
    }

    @Test
    public void testCorrectValuesRendered() {
        final List<String> values = getDataNames(trendingView.chartValueGroups());
        final List<String> legendValues = getDataNames(trendingView.legendValueGroups());
        assertThat("There are the same value names for the lines and the legend labels", legendValues.equals(values));
    }

    @Test
    public void testAddingFiltersRedrawsChart() {
        final Float startingRange = trendingService.yAxisLabelRange(trendingView);
        final String startingFinalXLabel = trendingService.finalXAxisLabel(trendingView);

        final String selectedField = trendingService.removeCountFromFieldName(trendingView.chosenField().getText());
        getElementFactory().getFilterPanel().parametricContainer(selectedField).filters().get(0).click();

        Waits.loadOrFadeWait();
        trendingView.waitForChartToLoad();

        assertThat("Either the y-axis values or the x-axis values have changed after adding a filter",
                !trendingService.yAxisLabelRange(trendingView).equals(startingRange)
                        || !trendingService.finalXAxisLabel(trendingView).equals(startingFinalXLabel));
    }

    @Test
    public void testAddingConceptsRedrawsChart() {
        final Float startingRange = trendingService.yAxisLabelRange(trendingView);
        final String startingFinalXLabel = trendingService.finalXAxisLabel(trendingView);

        final String searchTerm = "cheese";
        findService.searchAnyView(searchTerm);
        trendingView.waitForChartToLoad();

        assertThat("Either the y-axis values or the x-axis values have changed after adding a filter",
                !trendingService.yAxisLabelRange(trendingView).equals(startingRange)
                        || !trendingService.finalXAxisLabel(trendingView).equals(startingFinalXLabel));
    }

    @Test
    public void testDragUpdatesXAxis() {
        final String startingFinalXLabel = trendingService.finalXAxisLabel(trendingView);

        trendingService.dragRight(trendingView, getDriver());
        final String draggedFinalXLabel = trendingService.finalXAxisLabel(trendingView);
        assertThat("The x-axis range has changed", !draggedFinalXLabel.equals(startingFinalXLabel));

        trendingService.dragLeft(trendingView, getDriver());
        assertThat("The x-axis has changed again", !trendingService.finalXAxisLabel(trendingView).equals(draggedFinalXLabel));
    }

    @Test
    public void testChangingSelectedFieldRedrawsChart() {
        final List<String> values = getDataNames(trendingView.chartValueGroups());
        trendingService.changeSelectedField(3, trendingView);
        trendingView.waitForChartToLoad();
        assertThat("The values have changed", !getDataNames(trendingView.chartValueGroups()).equals(values));
    }

    @Test
    public void testChangeSearchInAnotherTab() {
        final Float startingRange = trendingService.yAxisLabelRange(trendingView);
        final String startingFinalXLabel = trendingService.finalXAxisLabel(trendingView);

        final String selectedField = trendingService.removeCountFromFieldName(trendingView.chosenField().getText());
        final ListView listView = findPage.goToListView();
        getElementFactory().getFilterPanel().parametricContainer(selectedField).filters().get(0).click();
        listView.waitForResultsToLoad();
        trendingView = findPage.goToTrending();
        trendingView.waitForChartToLoad();

        assertThat("Either the y-axis values or the x-axis values have changed after adding a filter",
                !trendingService.yAxisLabelRange(trendingView).equals(startingRange)
                        || !trendingService.finalXAxisLabel(trendingView).equals(startingFinalXLabel));
    }


    private List<String> getDataNames(final List<WebElement> elements) {
        return elements.stream()
                .map(v -> v.getAttribute("data-name"))
                .collect(Collectors.toList());
    }


}
