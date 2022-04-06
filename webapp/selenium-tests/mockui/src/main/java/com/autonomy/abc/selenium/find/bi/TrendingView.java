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

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.element.RangeInput;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class TrendingView {
    private static final int TRENDING_VIEW_LOAD_TIMEOUT = 30;
    private static final int TRENDING_VIEW_FIELD_DROPDOWN_TIMEOUT = 30;
    private static final Pattern FIELD_SELECTOR_TEXT = Pattern.compile("^.* \\((?<count>\\d+)\\)");

    private final WebDriver driver;
    private final WebElement container;

    public TrendingView(final WebDriver driver) {
        this.driver = driver;
        container = ElementUtil.ancestor(Container.currentTabContents(driver).findElement(By.className("trending-chart")), 2);
    }

    public boolean trendingChartVisible() {
        return !findElements(cssSelector(".trending-chart:not(.hide)")).isEmpty();
    }

    public boolean isLoading() {
        return !container.findElements(cssSelector(".trending-loading:not(.hide)")).isEmpty();
    }

    public void waitForChartToLoad() {
        new WebDriverWait(driver, TRENDING_VIEW_LOAD_TIMEOUT).withMessage("Trending never stopped loading")
            .until(ExpectedConditions.presenceOfElementLocated(cssSelector(".trending-loading.hide")));
    }

    public void waitForNumberOfPointsToChange(final int targetNumber) {
        final String firstValue = findElement(cssSelector("svg > g[data-name]")).getAttribute("data-name");
        new WebDriverWait(driver, TRENDING_VIEW_LOAD_TIMEOUT).withMessage("Target number of buckets not found")
            .until(ExpectedConditions.numberOfElementsToBe(cssSelector("[data-name='" + firstValue + "'] circle"), targetNumber));
    }

    private void waitForDropdownToOpen() {
        new WebDriverWait(driver, TRENDING_VIEW_FIELD_DROPDOWN_TIMEOUT).withMessage("Field selector never opened")
            .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(cssSelector(".active-result")));
    }

    public WebElement trendingChart() {
        return findElement(cssSelector(".trending-chart svg"));
    }

    public WebElement chosenField() {
        return findElement(cssSelector(".chosen-single span"));
    }

    List<WebElement> fields() {
        final WebElement dropdown = findElement(cssSelector(".chosen-single"));
        dropdown.click();
        waitForDropdownToOpen();
        return findElements(cssSelector(".active-result"));
    }

    List<String> fieldsList() {
        return fields()
            .stream()
            .map(WebElement::getText)
            .collect(Collectors.toList());
    }

    public RangeInput slider() {
        return new RangeInput(findElement(className("range-input-slider")), driver, 10);
    }

    WebElement graphArea() {
        return findElement(className("graph-area"));
    }

    public List<WebElement> chartValueGroups() {
        return findElements(cssSelector("svg > g[data-name]"));
    }

    public List<WebElement> pointsForNamedValue(final String valueName) {
        return findElements(cssSelector("[data-name='" + valueName + "'] circle"));
    }

    List<WebElement> yAxisTicks() {
        return findElements(cssSelector(".y-axis .tick text"));
    }

    List<WebElement> xAxisTicks() {
        return findElements(cssSelector(".x-axis .tick p"));
    }

    public List<WebElement> legendValueGroups() {
        return findElements(cssSelector(".legend > g"));
    }

    int getSelectedFieldCount(final WebElement fieldElement) {
        final String selectorText = fieldElement.getText();
        final Matcher matcher = FIELD_SELECTOR_TEXT.matcher(selectorText);

        return matcher.find() ? NumberUtils.toInt(matcher.group("count")) : 0;
    }

    private WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }
}
