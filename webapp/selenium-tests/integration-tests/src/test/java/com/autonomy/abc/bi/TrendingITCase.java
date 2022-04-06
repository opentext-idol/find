/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
import com.autonomy.abc.selenium.find.bi.TrendingService;
import com.autonomy.abc.selenium.find.bi.TrendingView;
import com.autonomy.abc.selenium.find.results.ListView;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.RangeInput;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.*;

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
        return (BIIdolFindElementFactory) super.getElementFactory();
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
        trendingService = new TrendingService(getApplication());

        findPage.waitForParametricValuesToLoad();
        trendingView = findPage.goToTrending();
        trendingView.waitForChartToLoad();
        trendingService.selectNonZeroField(trendingView);
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
        final Set<String> values = getDataNames(trendingView.chartValueGroups());
        final Set<String> legendValues = getDataNames(trendingView.legendValueGroups());
        assertThat("There are the same value names for the lines and the legend labels", values, is(equalTo(legendValues)));
    }

    @Test
    public void testAddingFiltersRedrawsChart() {
        final Float startingRange = trendingService.yAxisLabelRange(trendingView);
        final String startingFinalXLabel = trendingService.finalXAxisLabel(trendingView);

        final String selectedField = trendingService.removeCountFromFieldName(trendingView.chosenField().getText());
        trendingService.selectLastValueListedOfDisplayedField(selectedField);

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
        final Set<String> values = getDataNames(trendingView.chartValueGroups());
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
        trendingService.selectLastValueListedOfDisplayedField(selectedField);
        listView.waitForResultsToLoad();
        trendingView = findPage.goToTrending();
        trendingView.waitForChartToLoad();

        assertThat("Either the y-axis values or the x-axis values have changed after adding a filter",
                !trendingService.yAxisLabelRange(trendingView).equals(startingRange)
                        || !trendingService.finalXAxisLabel(trendingView).equals(startingFinalXLabel));
    }

    @Test
    public void sliderTooltip() {
        final RangeInput slider = trendingView.slider();
        final int firstNumber = sliderToolTipValue(slider);

        slider.dragBy(10);
        slider.hover();

        assertThat("Tooltip reappears after dragging", slider.tooltip().isDisplayed());
        verifyThat("New tooltip value bigger than old", sliderToolTipValue(slider), greaterThanOrEqualTo(firstNumber));
    }

    @Test
    public void draggingSliderUpdatesGraph() {
        final String firstValue = trendingView.chartValueGroups().get(0).getAttribute("data-name");
        final int originalPointCount = trendingView.pointsForNamedValue(firstValue).size();

        final RangeInput slider = trendingView.slider();

        slider.dragBy(50);
        final int updatedSliderValue = slider.getValue();
        trendingView.waitForNumberOfPointsToChange(updatedSliderValue);
        final int updatedPointCount = trendingView.pointsForNamedValue(firstValue).size();
        assertThat("Changing the slider has added more data points to the graph", updatedPointCount, greaterThan(originalPointCount));

        slider.dragBy(-50);
        final int finalSliderValue = slider.getValue();
        trendingView.waitForNumberOfPointsToChange(finalSliderValue);
        final int finalPointCount = trendingView.pointsForNamedValue(firstValue).size();
        assertThat("Changing the slider has added fewer data points to the graph", finalPointCount, lessThan(updatedPointCount));
    }

    private Set<String> getDataNames(final Collection<WebElement> elements) {
        return elements.stream()
                .map(v -> v.getAttribute("data-name"))
                .collect(Collectors.toSet());
    }

    private int sliderToolTipValue(final RangeInput slider) {
        slider.hover();
        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(slider.tooltip()));
        verifyThat("Tooltip appears on hover", slider.tooltip().isDisplayed());
        return slider.getTooltipValue();
    }
}
