/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.bi;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.filters.FilterContainer;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TrendingService {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

    private final FindElementFactory elementFactory;

    public TrendingService(final FindApplication<?> find) {
        elementFactory = find.elementFactory();
    }

    public Float yAxisLabelRange(final TrendingView trendingView) {
        final List<WebElement> yAxisTicks = trendingView.yAxisTicks();
        final List<Float> valueArray = yAxisTicks
                .stream()
                .map(label -> label.getText().isEmpty() ? 0f : parseFormattedDecimal(label).floatValue())
                .collect(Collectors.toList());
        return yAxisTicks.isEmpty() ? 0f : Collections.max(valueArray) - Collections.min(valueArray);
    }

    private Number parseFormattedDecimal(final WebElement label) {
        try {
            return DECIMAL_FORMAT.parse(label.getText());
        } catch (final ParseException e) {
            throw new IllegalStateException("Axis number in unexpected format", e);
        }
    }

    public String finalXAxisLabel(final TrendingView trendingView) {
        final List<WebElement> xAxisTicks = trendingView.xAxisTicks();
        return xAxisTicks.isEmpty() ? "" : xAxisTicks.get(xAxisTicks.size() - 1).getText();
    }

    public List<String> fieldSelectorFields(final TrendingView trendingView) {
        return trendingView.fieldsList()
                .stream()
                .map(fieldAndCount -> removeCountFromFieldName(fieldAndCount).toUpperCase())
                .collect(Collectors.toList());
    }

    public String removeCountFromFieldName(final String fieldAndCount) {
        return fieldAndCount.split("\\(")[0].trim();
    }

    public List<String> filterFields() {
        return elementFactory.getFilterPanel().allFilterContainers()
                .stream()
                .map(FilterContainer::filterCategoryName)
                .collect(Collectors.toList());
    }

    public void dragRight(final TrendingView trendingView, final WebDriver driver) {
        final String firstValue = trendingView.chartValueGroups().get(0).getAttribute("data-name");
        final List<WebElement> points = trendingView.pointsForNamedValue(firstValue);
        new Actions(driver).dragAndDrop(points.get(1), points.get(3)).perform();
    }

    public void dragLeft(final TrendingView trendingView, final WebDriver driver) {
        final String firstValue = trendingView.chartValueGroups().get(0).getAttribute("data-name");
        final List<WebElement> points = trendingView.pointsForNamedValue(firstValue);
        new Actions(driver).dragAndDrop(points.get(points.size() - 1), points.get(points.size() - 3)).perform();
    }

    public void changeSelectedField(final int index, final TrendingView trendingView) {
        trendingView.fields().get(index).click();
    }

    public void selectNonZeroField(final TrendingView trendingView) {
        if (trendingView.getSelectedFieldCount(trendingView.chosenField()) == 0) {
            trendingView.fields().stream()
                    .filter(field -> trendingView.getSelectedFieldCount(field) > 0)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No parametric fields with any values for the current query"))
                    .click();
            trendingView.waitForChartToLoad();
        }
    }

    public void selectLastValueListedOfDisplayedField(final String selectedField) {
        final List<WebElement> filters = elementFactory.getFilterPanel().parametricContainer(selectedField).filters();
        filters.get(filters.size() - 1).click();
    }
}
