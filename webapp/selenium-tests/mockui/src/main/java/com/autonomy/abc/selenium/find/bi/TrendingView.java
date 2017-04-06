/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.bi;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.stream.Collectors;

import static org.openqa.selenium.By.cssSelector;

public class TrendingView {
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
        new WebDriverWait(driver, 30).withMessage("Trending never stopped loading")
                .until(ExpectedConditions.presenceOfElementLocated(cssSelector(".trending-loading.hide")));
    }

    private void waitForDropdownToOpen() {
        new WebDriverWait(driver, 30).withMessage("Field selector never opened")
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(cssSelector(".active-result")));
    }

    public WebElement trendingChart() {
        return findElement(cssSelector(".trending-chart svg"));
    }

    public WebElement chosenField() {
        return findElement(cssSelector(".chosen-single span"));
    }

    public List<WebElement> fields() {
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

    public List<WebElement> chartValueGroups() {
        return findElements(cssSelector("g.value"));
    }

    List<WebElement> pointsForNamedValue(final String valueName) {
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


    private WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }
}