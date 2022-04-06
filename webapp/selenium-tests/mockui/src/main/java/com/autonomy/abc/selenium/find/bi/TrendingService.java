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

package com.autonomy.abc.selenium.find.bi;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.filters.FilterContainer;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.ButtonReleaseAction;
import org.openqa.selenium.interactions.ClickAndHoldAction;
import org.openqa.selenium.interactions.CompositeAction;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.MoveToOffsetAction;
import org.openqa.selenium.internal.Locatable;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TrendingService {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
    private static final int SAFE_DISTANCE_FROM_POINT_TO_CLICK_FOR_DRAG = 10;
    private static final int DISTANCE_TO_DRAG = 500;

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
        } catch(final ParseException e) {
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
        drag(trendingView, driver, DISTANCE_TO_DRAG);
    }

    public void dragLeft(final TrendingView trendingView, final WebDriver driver) {
        drag(trendingView, driver, -DISTANCE_TO_DRAG);
    }

    public void changeSelectedField(final int index, final TrendingView trendingView) {
        trendingView.fields().get(index).click();
    }

    public void selectNonZeroField(final TrendingView trendingView) {
        if(trendingView.getSelectedFieldCount(trendingView.chosenField()) == 0) {
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

    private void drag(final TrendingView trendingView, final WebDriver driver, final int xOffset) {
        final List<Point> firstPoints = trendingView.chartValueGroups().stream()
            .map(value -> trendingView.pointsForNamedValue(value.getAttribute("data-name")).get(0).getLocation())
            .sorted((x, y) -> y.getY() - x.getY())
            .collect(Collectors.toList());
        final Iterator<Point> iterator = firstPoints.iterator();
        Point point = iterator.next();
        while(iterator.hasNext()) {
            final Point next = iterator.next();
            if(point.getY() - next.getY() > SAFE_DISTANCE_FROM_POINT_TO_CLICK_FOR_DRAG) {
                break;
            }

            point = next;
        }

        final WebElement graphArea = trendingView.graphArea();
        final Point graphAreaLocation = graphArea.getLocation();
        final int yOffsetForInitialClick = point.getY() - graphAreaLocation.getY() - SAFE_DISTANCE_FROM_POINT_TO_CLICK_FOR_DRAG > 0
            ? point.getY() - graphAreaLocation.getY() - SAFE_DISTANCE_FROM_POINT_TO_CLICK_FOR_DRAG : firstPoints.get(0).getY() - graphAreaLocation.getY() + SAFE_DISTANCE_FROM_POINT_TO_CLICK_FOR_DRAG;

        final Mouse mouse = ((HasInputDevices)driver).getMouse();

        final CompositeAction action = new CompositeAction();
        action.addAction(new MoveToOffsetAction(mouse, (Locatable)graphArea, point.getX() - graphAreaLocation.getX() + SAFE_DISTANCE_FROM_POINT_TO_CLICK_FOR_DRAG, yOffsetForInitialClick));
        action.addAction(new ClickAndHoldAction(mouse, null));
        action.addAction(new MoveToOffsetAction(mouse, null, xOffset, 0));
        action.addAction(new ButtonReleaseAction(mouse, null));
        action.perform();
    }
}
